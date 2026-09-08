package com.monglife.mongs.data.device.persistence.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.monglife.mongs.application.device.exception.InvalidExchangeWalkingCountException
import com.monglife.mongs.data.device.persistence.entity.DeviceOptionEntity
import com.monglife.mongs.data.device.persistence.entity.StepStateEntity
import com.monglife.mongs.domain.device.model.StepAccumulation
import com.monglife.mongs.domain.device.model.StepCursor
import com.monglife.mongs.domain.device.model.StepRestore
import com.monglife.mongs.domain.device.model.StepSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val Context.store by preferencesDataStore(name = "DEVICE")

    companion object {
        /** 걸음 수 스키마 버전. 0(부재) = 서버가 잔액을 계산하던 구 스키마. */
        private const val STEP_SCHEMA_CURRENT = 1

        private val STEP_SCHEMA_VERSION = intPreferencesKey("stepSchemaVersion")
        private val STEP_BALANCE = intPreferencesKey("stepBalance")
        private val STEP_SOURCE = stringPreferencesKey("stepSource")
        private val STEP_CURSOR_BOOT_MARK = longPreferencesKey("stepCursorBootMark")
        private val STEP_CURSOR_END_DURATION = longPreferencesKey("stepCursorEndDuration")
        private val STEP_CURSOR_DAILY_WINDOW = longPreferencesKey("stepCursorDailyWindow")
        private val STEP_CURSOR_DAILY_VALUE = intPreferencesKey("stepCursorDailyValue")
        private val STEP_CURSOR_SENSOR_TOTAL = intPreferencesKey("stepCursorSensorTotal")

        /**
         * 이미 반영한 복구 알림 식별자. 개수 제한과 중복 판정은 StepRestore 가 한다.
         *
         * Set 이 아니라 순서 있는 문자열로 두는 이유는 Preferences 의 Set 이 순서를 보장하지 않아
         * "오래된 것부터 버리기" 를 할 수 없기 때문이다.
         */
        private val STEP_RESTORE_APPLIED_EVENT_IDS = stringPreferencesKey("stepRestoreAppliedEventIds")

        private const val RESTORE_EVENT_ID_DELIMITER = ","

        /** 구 스키마 잔재. 서버가 내려 주던 값이라 새 지갑으로 환산할 방법이 없어 지우기만 한다. */
        private val LEGACY_WALKING_COUNT = intPreferencesKey("walkingCount")
        private val LEGACY_CONSUME_WALKING_COUNT = intPreferencesKey("consumeWalkingCount")

        private val CURRENT_MONG_ID = longPreferencesKey("currentMongId")
        private val BACKGROUND_MAP_CODE = stringPreferencesKey("backgroundMapCode")
        private val NOTIFICATION_OPTION = booleanPreferencesKey("notificationOption")
        private val SOUND_VOLUME = floatPreferencesKey("soundVolume")
        private val INIT_NOTIFICATION_DIALOG_OPEN = booleanPreferencesKey("initNotificationDialogOpen")
    }

    /**
     * 걸음 수 상태 조회
     */
    suspend fun getStepState(): StepStateEntity = context.store.data.map { it.readStepState() }.first()

    /**
     * 걸음 수 상태 Flow 조회
     */
    fun getStepStateFlow(): Flow<StepStateEntity> =
        context.store.data.map { it.readStepState() }.distinctUntilChanged()

    /**
     * 걸음 수 적립
     *
     * 읽기-계산-쓰기를 전부 edit 블록 안에서 한다. Health Services 서비스와 폴링 워커가 동시에
     * 들어올 수 있는데, 밖에서 조회한 뒤 저장하면 그 사이 들어온 적립이 통째로 사라진다.
     *
     * [expectedSource] 게이트가 여기 있는 것도 같은 이유다. 수집 경로가 바뀌었는데 이전 경로가
     * 아직 살아 있으면(예: 앱 업데이트 전에 등록해 둔 Health Services 리스너) 두 경로가 같은
     * 걸음을 각자 적립해 그대로 두 배가 된다.
     */
    suspend fun creditStep(
        expectedSource: StepSource,
        accumulate: (StepCursor) -> StepAccumulation,
    ): StepStateEntity = context.store.edit { preferences ->
        val current = preferences.readStepState()
        if (current.source != expectedSource) return@edit

        val accumulation = accumulate(current.cursor)
        preferences[STEP_BALANCE] = (current.balance.toLong() + accumulation.credited)
            .coerceIn(0L, Int.MAX_VALUE.toLong())
            .toInt()
        preferences.writeCursor(accumulation.cursor)
    }.readStepState()

    /**
     * 걸음 수 차감 (환전)
     *
     * 잔액이 모자라면 예외를 던져 edit 을 롤백한다.
     */
    suspend fun consumeStep(amount: Int): StepStateEntity = context.store.edit { preferences ->
        val current = preferences.readStepState()
        if (amount <= 0 || current.balance < amount) throw InvalidExchangeWalkingCountException()

        preferences[STEP_BALANCE] = current.balance - amount
    }.readStepState()

    /**
     * 환전 실패분 걸음 수 복구
     *
     * 서버가 걸음 수를 보관하지 않으므로, 환전 후 페이 포인트 지급이 실패하면 서버는
     * "이만큼 되돌려라" 를 알려 줄 수만 있다. 그 지시를 지갑에 반영한다.
     *
     * 적립([creditStep])이 아니라 별도 연산인 이유는, 적립 경로가 수집 커서와 경로 게이트를
     * 타기 때문이다. 복구는 센서가 센 걸음이 아니라 이미 차감했던 걸음을 되돌리는 것이라
     * 그 게이트를 통과할 수 없다.
     *
     * 중복 검사와 잔액 증가는 반드시 같은 edit 안에서 해야 한다. 나누면 같은 알림이 거의 동시에
     * 두 번 도착했을 때 둘 다 통과한다.
     */
    suspend fun restoreStep(restoreWalkingCount: Int, eventId: String): StepStateEntity =
        context.store.edit { preferences ->
            val appliedEventIds = preferences[STEP_RESTORE_APPLIED_EVENT_IDS]
                ?.split(RESTORE_EVENT_ID_DELIMITER)
                ?.filter { it.isNotBlank() }
                ?: emptyList()

            val result = StepRestore.apply(appliedEventIds, restoreWalkingCount, eventId)
            if (result.restoredWalkingCount <= 0) return@edit

            preferences[STEP_BALANCE] =
                (preferences.readStepState().balance.toLong() + result.restoredWalkingCount)
                    .coerceIn(0L, Int.MAX_VALUE.toLong())
                    .toInt()

            preferences[STEP_RESTORE_APPLIED_EVENT_IDS] =
                result.appliedEventIds.joinToString(RESTORE_EVENT_ID_DELIMITER)
        }.readStepState()

    /**
     * 걸음 수 수집 경로 변경
     *
     * 경로가 바뀌면 커서도 함께 비운다. 커서 칸은 경로마다 다른 의미를 갖는데 남겨 두면
     * 전환 직후 첫 배치에서 엉뚱한 차분이 나온다.
     */
    suspend fun setStepSource(source: StepSource): StepStateEntity = context.store.edit { preferences ->
        if (preferences.readStepState().source == source) return@edit

        preferences[STEP_SOURCE] = source.name
        preferences.writeCursor(StepCursor.EMPTY)
    }.readStepState()

    /**
     * 걸음 수 스키마 마이그레이션
     *
     * 구 스키마의 walkingCount/consumeWalkingCount 는 서버가 계산해 내려 주던 값이라
     * 로컬만으로는 잔액을 복원할 수 없다. 잔액 소실을 허용하기로 했으므로 0 에서 시작한다.
     */
    suspend fun migrateStepSchema() {
        context.store.edit { preferences ->
            if ((preferences[STEP_SCHEMA_VERSION] ?: 0) >= STEP_SCHEMA_CURRENT) return@edit

            preferences[STEP_BALANCE] = 0
            preferences[STEP_SOURCE] = StepSource.UNRESOLVED.name
            preferences.writeCursor(StepCursor.EMPTY)
            preferences.remove(STEP_RESTORE_APPLIED_EVENT_IDS)
            preferences.remove(LEGACY_WALKING_COUNT)
            preferences.remove(LEGACY_CONSUME_WALKING_COUNT)
            preferences[STEP_SCHEMA_VERSION] = STEP_SCHEMA_CURRENT
        }
    }

    private fun Preferences.readStepState(): StepStateEntity = StepStateEntity(
        balance = this[STEP_BALANCE] ?: 0,
        source = this[STEP_SOURCE]
            ?.let { name -> StepSource.entries.firstOrNull { it.name == name } }
            ?: StepSource.UNRESOLVED,
        cursor = StepCursor(
            bootMarkMillis = this[STEP_CURSOR_BOOT_MARK] ?: -1L,
            lastEndDurationFromBootMillis = this[STEP_CURSOR_END_DURATION] ?: -1L,
            dailyWindowStartFromBootMillis = this[STEP_CURSOR_DAILY_WINDOW] ?: StepCursor.DAILY_WINDOW_UNSET,
            dailyValue = this[STEP_CURSOR_DAILY_VALUE] ?: 0,
            lastSensorTotal = this[STEP_CURSOR_SENSOR_TOTAL] ?: -1,
        ),
    )

    private fun androidx.datastore.preferences.core.MutablePreferences.writeCursor(cursor: StepCursor) {
        this[STEP_CURSOR_BOOT_MARK] = cursor.bootMarkMillis
        this[STEP_CURSOR_END_DURATION] = cursor.lastEndDurationFromBootMillis
        this[STEP_CURSOR_DAILY_WINDOW] = cursor.dailyWindowStartFromBootMillis
        this[STEP_CURSOR_DAILY_VALUE] = cursor.dailyValue
        this[STEP_CURSOR_SENSOR_TOTAL] = cursor.lastSensorTotal
    }

    /**
     * DeviceOption 조회
     */
    suspend fun getDeviceOption(): DeviceOptionEntity? = context.store.data.map {
        if (
            it.contains(CURRENT_MONG_ID) &&
            it.contains(BACKGROUND_MAP_CODE) &&
            it.contains(NOTIFICATION_OPTION) &&
            it.contains(SOUND_VOLUME) &&
            it.contains(INIT_NOTIFICATION_DIALOG_OPEN)
        ) {
            DeviceOptionEntity(
                currentMongId = if (it[CURRENT_MONG_ID]!! == -1L) null else it[CURRENT_MONG_ID]!!,
                backgroundMapCode = it[BACKGROUND_MAP_CODE]!!.ifBlank { null },
                notificationOption = it[NOTIFICATION_OPTION]!!,
                soundVolume = it[SOUND_VOLUME]!!,
                initNotificationDialogOpen = it[INIT_NOTIFICATION_DIALOG_OPEN]!!,
            )
        } else {
            null
        }
    }.first()

    /**
     * DeviceOption 조회
     */
    fun getDeviceOptionFlow(): Flow<DeviceOptionEntity?> = context.store.data.map {
        if (
            it.contains(CURRENT_MONG_ID) &&
            it.contains(BACKGROUND_MAP_CODE) &&
            it.contains(NOTIFICATION_OPTION) &&
            it.contains(SOUND_VOLUME) &&
            it.contains(INIT_NOTIFICATION_DIALOG_OPEN)
        ) {
            DeviceOptionEntity(
                currentMongId = if (it[CURRENT_MONG_ID]!! == -1L) null else it[CURRENT_MONG_ID]!!,
                backgroundMapCode = it[BACKGROUND_MAP_CODE]!!.ifBlank { null },
                notificationOption = it[NOTIFICATION_OPTION]!!,
                soundVolume = it[SOUND_VOLUME]!!,
                initNotificationDialogOpen = it[INIT_NOTIFICATION_DIALOG_OPEN]!!,
            )
        } else {
            null
        }
    }.distinctUntilChanged()

    /**
     * DeviceOption 저장
     */
    suspend fun saveDeviceOption(deviceOptionEntity: DeviceOptionEntity): DeviceOptionEntity = context.store.let { store ->
        store.edit { preferences ->
            deviceOptionEntity.currentMongId
                ?.let { preferences[CURRENT_MONG_ID] = it }
                ?:run { preferences[CURRENT_MONG_ID] = -1 }
            deviceOptionEntity.backgroundMapCode
                ?.let { preferences[BACKGROUND_MAP_CODE] = it }
                ?:run { preferences[BACKGROUND_MAP_CODE] = "" }
            preferences[NOTIFICATION_OPTION] = deviceOptionEntity.notificationOption
            preferences[SOUND_VOLUME] = deviceOptionEntity.soundVolume
            preferences[INIT_NOTIFICATION_DIALOG_OPEN] = deviceOptionEntity.initNotificationDialogOpen
        }

        store.data.map {
            DeviceOptionEntity(
                currentMongId = if (it[CURRENT_MONG_ID]!! == -1L) null else it[CURRENT_MONG_ID]!!,
                backgroundMapCode = it[BACKGROUND_MAP_CODE]!!.ifBlank { null },
                notificationOption = it[NOTIFICATION_OPTION]!!,
                soundVolume = it[SOUND_VOLUME]!!,
                initNotificationDialogOpen = it[INIT_NOTIFICATION_DIALOG_OPEN]!!,
            )
        }.first()
    }
}
