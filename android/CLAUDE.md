# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Mongs is a Wear OS + Android app for a "growing a pet with your step count" service (다마고치-style). This
repo (`monglife-mongs-wear`) contains two installable apps that share almost all business logic:

- `app/wear-app` — the original Wear OS app (published on Play Store).

Backend lives in a separate repo: https://github.com/MongLife/monglife-mongs

## Build & Common Commands

Standard Gradle wrapper. Run from the repo root.

Every Android module has a `profile` flavor dimension with three flavors — `dev`, `stg`, `prd` — so
build tasks carry the environment in their name (`assembleDevDebug`, not `assembleDebug`).

```bash
./gradlew :app:wear-app:assembleDevDebug      # build the wear app against dev
./gradlew :app:wear-app:installDevDebug       # install wear app on connected device/emulator
./gradlew :app:wear-app:assemblePrdRelease    # signed, R8-minified production build
./gradlew testDevDebugUnitTest                # run all unit tests (plain `test` runs all 3 flavors)
./gradlew :application:mong-application:test  # run tests for one module (pure JVM, no flavors)
./gradlew :application:mong-application:test --tests "*.GetCurrentMongUseCaseTest"  # single test
./gradlew lintDevDebug                        # Android lint across modules
```

`assembleDebug` still exists but builds all three environments at once — prefer the explicit
`assemble<Flavor>Debug` form. In Android Studio, switch environments with the Build Variants panel.

There is no CI config in this repo (no `.github/workflows`) and no top-level lint/format script beyond
Gradle's built-in `lint` task.

### Environment/config files (`configs` submodule)

`.gitmodules` points `configs/` at a private submodule (`monglife-mongs-wear-sub`). **Nothing is copied
out of it** — the build reads it in place, so `git submodule update --init` is the only setup step.

```
configs/core/data-core/{dev,stg,prd}/values/config.xml   per-environment string resources (13 keys)
configs/app/<module>/res/values/strings.xml              app_name (environment-independent)
configs/app/<module>/keystore.properties                 release signing
configs/app/<module>/google-services.json                FCM (environment-independent)
configs/keyStore/monglife                                the keystore itself
```

How each one is wired:

- **Environment strings** — `core/data-core/build.gradle` points each flavor's `res.srcDir` at
  `configs/core/data-core/<flavor>`. This is the *only* place environments differ; everything else in
  `configs/` is shared. The three `config.xml` files must declare an identical key set, since the `R`
  class is generated per variant and a key missing from one flavor is a compile error in that flavor.
- **Flavor declaration** — the root `build.gradle` declares the dimension for *every* Android module via
  `plugins.withId("com.android.base")`. It must be every module, not just `core:data-core`: a module that
  depends on a flavored one without declaring the dimension fails variant resolution.
- **Signing / app_name** — the two `app/*/build.gradle` files reference `configs/app/<module>/` directly.
- **`google-services.json`** — the plugin ignores source sets and offers no path setting, but
  `GoogleServicesTask.googleServicesJsonFiles` is a public input property, so the app modules override it
  inside `afterEvaluate` (earlier than that and the plugin's own default overwrites it). Re-verify this
  wiring when bumping `[versions] gms` in `gradle/libs.versions.toml`.
- **`applicationIdSuffix` must stay off.** Each `google-services.json` holds a single client
  (`com.mongs.wear`) matched exactly by package name; a suffix fails the build.
  Use `versionNameSuffix` if you need to tell builds apart.
- **Cleartext HTTP** — `app/*/src/{dev,stg}/res/xml/network_security_config.xml` permit it, `src/main`
  blocks it. Split by flavor rather than build type, because the environment is what decides it.

## Architecture

This is a multi-module Gradle project following **Clean/Hexagonal Architecture**, layered top-to-bottom as
`app → presentation → application → domain`, with `data` implementing the ports that `application` declares.
Each business area ("vertical slice") has its own module in every layer: `auth`, `battle`, `device`,
`member`, `mong`.

```
app/wear-app                          (Android app, Hilt entry point)
        │
presentation/wear-view-presentation   (Compose UI, wear-specific widgets)
        │  both depend on:
presentation/viewmodel-presentation   (ViewModels shared by both apps — one module, no per-app duplication)
        │
application/{auth,battle,device,member,mong}-application   (use cases, ports, VOs — pure Kotlin/JVM)
        │
domain/{auth,battle,device,member,mong}-domain             (entities, enums — pure Kotlin/JVM, zero deps)

data/{auth,battle,device,member,mong}-data   (Android libs: implements application's ports via
                                               Retrofit/Room/MQTT/DataStore adapters, wired with Hilt)

core/common-core        pure Kotlin: ErrorCode/ErrorException, Base*UseCase "skell" templates
core/application-core   pure Kotlin: BaseParamUseCase / BaseNoParamUseCase, Page/PageResponse
core/presentation-core  Android lib: BaseViewModel (shared exception handling + toast events)
core/data-core          Android lib: shared Retrofit/OkHttp, DataStore, MQTT client setup
core/billing-core       Android lib: Google Play Billing wrapper
```

Key wiring facts (see `settings.gradle` and per-module `build.gradle` for the full graph):

- `domain/*` modules have **no dependencies at all** — just models/enums.
- `application/*` modules depend only on `core:application-core` (`api`) — no Android APIs, testable as
  plain JUnit.
- `data/*` modules depend on the matching `application/*` module plus `core:data-core`; they are Android
  library modules (Room, Retrofit, MQTT, Hilt `@Binds`/`@Provides` live here).
- `presentation:viewmodel-presentation` is the one place that wires everything together: it `api`-exposes
  every `application/*` module (so ViewModels can call use cases) and `implementation`-depends on every
  `data/*` module (so Hilt has adapters to bind at runtime), plus `core:billing-core`.
- `app/wear-app` only depends on `core:data-core` + its
  `presentation:*-view-presentation` module — they contain almost no business logic themselves (just
  `MainActivity`, `MainApplication`, notification service/module).

### Per-slice package pattern (example: `mong`)

For a given vertical slice, follow this pattern when adding a feature — it is consistent across `auth`,
`battle`, `device`, `member`, `mong`:

- `domain/mong-domain/.../model/` — plain domain models (e.g. `Mong.kt`, `Food.kt`) and `enums/`.
- `application/mong-application/.../port/{persistence,web}/` — interfaces the use cases depend on
  (`ManagementPersistencePort`, `ManagementWebPort`, etc.), plus `port/web/response/` DTOs for expected
  responses.
- `application/mong-application/.../usecase/<feature>/` — one class per use case, extending
  `BaseParamUseCase<Command, Result>` or `BaseNoParamUseCase<Result>` from `core:application-core`, injected
  via `@Inject constructor` with the ports it needs. Executed through `invoke()`.
- `application/mong-application/.../vo/` — view objects (`MongVo`, etc.) returned by use cases to the
  presentation layer, built via a `.of(...)` factory from the domain model.
- `application/mong-application/.../error/` + `exception/` — module-specific `ErrorCode`/`Exception`
  subclasses.
- `data/mong-data/.../persistence/adapter/` and `.../web/adapter/` — `@Singleton class XAdapter @Inject
  constructor(...) : XPort` implementations using Room DAOs (`persistence/dao`, `persistence/entity`,
  `persistence/db`) and Retrofit clients (`web/client`, `web/client/request`, `web/client/response`).
- `data/mong-data/.../Module.kt` — Hilt `@Module` binding each adapter to its port (`AdapterModule`) and
  providing Retrofit-backed web clients (`WebClientModule`).
- `presentation/viewmodel-presentation/.../pages/<feature>/` — `XViewModel : BaseViewModel()` classes that
  call use cases and expose `StateFlow`/VO state to Compose screens.

`core/common-core/.../skells/BaseParamUseCaseSkell.kt` and `ViewModelSkell.kt` are (commented-out) copy-paste
templates for scaffolding a new use case / view model in this style — check them when adding a new one.

### Base classes worth knowing

- `core/application-core` `BaseParamUseCase<P, R>` / `BaseNoParamUseCase<R>` — all use cases extend one of
  these; call sites use `useCase(command)` (operator `invoke`), not `.execute()` directly.
- `core/presentation-core` `BaseViewModel` — provides `viewModelScopeWithHandler` (coroutine scope with a
  shared `CoroutineExceptionHandler` that logs, and if the thrown exception is an `ErrorException` whose
  `ErrorCode.isMessageShow()` is true, emits it on the shared `errorEvent` toast flow after
  `NAVIGATE_DELAY`), plus `observeForever`/`observeStop` helpers for subscribing a `Flow` into a
  `MutableStateFlow` for the life of the ViewModel.
- Real-time updates use MQTT (`core:data-core`'s `MqttClient`, subscribed to per-entity topics like
  `mong/management/{mongId}`) alongside Room as the local cache — see
  `ManagementPersistenceAdapter.getMongFlow` for the subscribe/shareIn/unsubscribe pattern used whenever a
  persistence adapter needs to reflect server push updates into a local `Flow`.

### Notifications / FCM

Both apps have their own `NotificationModule.kt` / `NotificationService.kt` under
`app/wear-app/.../module` and `.../service` — these are per-app (not shared through
`core:data-core`), since FCM handling differs between the watch and phone.

### Kotlin/Android versions

Versions live in three places, split by kind:

- **Plugin versions — `gradle/libs.versions.toml`** (the default `libs` catalog; `settings.gradle` needs
  no wiring for it). AGP 9.3.1, Kotlin 2.4.10, KSP 2.3.11, Hilt 2.60.1, google-services 4.4.2. The root
  `plugins {}` block applies them as `alias(libs.plugins.<name>) apply false`; every other module just
  does `apply plugin: "<id>"` with no version.
  **Do not move plugin versions back into `ext` interpolation** (`id "..." version "${androidVersion}"`):
  lint's `GroovyGradleVisitor` reads the build file by slicing the raw source text, so it never resolves
  the interpolation, parses the version as the literal `${androidVersion}`, decides it is below 7.1.0, and
  raises `GradlePluginVersion` at ERROR severity — which fails `lint*` and lights up the IDE editor.
- **Library versions — root `build.gradle` `ext` block** (Java 17, Firebase/FCM, Work, Mockito, …), plus
  per-module `buildscript.ext` blocks for module-specific ones (Compose, Retrofit, Room, etc.). Check the
  relevant module's `build.gradle` before assuming a shared version applies.
  `hiltVersion` is the one value both sides need, so the root script seeds it from the catalog
  (`ext.hiltVersion = libs.versions.hilt.get()`) and the ~12 `rootProject.ext.hiltVersion` call sites
  keep working unchanged.
- **SDK levels — root `build.gradle` `ext`**: `androidCompileSdk` (37), `androidMinSdk` (33),
  `androidTargetSdk` (37) are the single source. The root `subprojects` block applies `compileSdk` and
  `defaultConfig.minSdk` to every Android module through the same `plugins.withId("com.android.base")`
  hook that declares the flavors, and a separate `plugins.withId("com.android.application")` hook sets
  `targetSdk` — `targetSdk` is an application-only DSL (AGP 8+ library `defaultConfig` has no such
  property), so it cannot go in the shared hook. Do not put SDK literals back into module build files:
  one module drifting out of sync breaks manifest merging / AAR metadata checks.
  `minSdk 33` is deliberate — it is Wear OS 4, and the Galaxy Watch 4 (the intended support floor) sits
  above it: the test unit (SM-R875N, Watch4 Classic 46mm LTE) is on **Android 16 / API 36** (Wear OS 6,
  One UI 8 Watch), not the API 34 this note used to claim. `compileSdk 37` needs `platforms;android-37`
  installed locally (API 37.0/37.1 are stable; 37.2 is still beta).

## Step collection — what the device taught us (2026-09-20)

Reference device: **Galaxy Watch 4 Classic 46mm LTE (SM-R875N)**, Android 16 / API 36 (Wear OS 6),
Health Services `0.88.25.934069675`. Verified against `stg` over wireless debugging.

### ⚠️ R8 silently disabled Health Services in every release build

The single most expensive thing to rediscover. `androidx.health.services.client.proto.DataProto$*`
extends **unshaded** `com.google.protobuf.GeneratedMessageLite`, and protobuf-lite resolves fields
**by name** from the `dynamicMethod` schema string. R8 renamed `packageName_` to `e`, so every
capability query threw:

```
W/StepCollection: capability 조회 실패
java.lang.RuntimeException: Field packageName_ for b13 not found.
  Known fields are [public java.lang.String b13.e, ...]
```

`health-services-client:1.0.0` ships **no `proguard.txt`**, so nothing protects it. With the query
failing, `resolveSupportedSource()` returns null and the path falls through to `SENSOR` — meaning
`StepPassiveListenerService`, `StepBootReceiver` and the re-registration logic were **dead code in
production**, and nothing pushed steps while the app was dead. `data/device-data/consumer-rules.pro`
now keeps those fields; `android/.gitignore` needs the `!` exception or the file is never committed.

**Debug builds cannot catch this** (`minifyEnabled false`). Any change touching a reflection-based
library must be checked on a *release* build on a real device.

### Release vs debug: you are half blind in release

`proguard/mongs-release.pro` strips `Log.i` via `-assumenosideeffects`, and `run-as` is unavailable.
In a release build the only surviving step signals are three `Log.w` lines — `capability 조회 실패`,
`passive 등록 실패`, `활동 권한 상실`. Their **absence on a path you know executed** is the proof;
there is no success log. Force a known path by revoking and re-granting `ACTIVITY_RECOGNITION`
(`NONE` is the one state `resolveSource` re-resolves). Use a debug build whenever you need numbers.

### Reproducing "app was dead, then I walked"

- `adb shell am kill <pkg>` — **not `force-stop`**. Force-stop puts the package in the stopped state,
  which suppresses broadcasts (including `MY_PACKAGE_REPLACED`) and invalidates the test. `am kill`
  is what the OS reclaim actually does.
- Granting the permission with `adb shell pm grant` is **not equivalent to the user tapping allow**.
  It leaves `USER_SET` off the permission flags and Health Services then refuses delivery with
  `PassiveListenerServiceDispatcher: Notified client of missing permissions`. Grant through the app.
- The process rarely stays dead: `info.mqtt.android.service.MqttService` restarts it within ~1 s.
  With the process alive Health Services binds into it, so no `Start proc … for service …` line
  appears — you lose your only release-visible evidence.

### Measured behaviour

| | |
|---|---|
| Resolved path | `HEALTH_STEPS` (`DataType.STEPS` delta is supported; the daily fallback is unused) |
| Batch cadence, walking, app foreground | 60–80 s |
| Batch cadence, walking stopped or app dead | 3–4 min |
| First batch after `flush()` on app entry | **~250 ms** |
| Cold start, `am start -W`, stg **debug** | 4698 / 5822 / 5930 ms |
| Cold start, `am start -W`, stg **release** | 1021–1704 ms (p50 ≈ 1.1 s) |
| Service bind, release | 360 ms (Health Services allows 5 s, then drops the registration for good) |

The 5 s bind budget is real — `WHS_PassiveListenerServ: Permanent failure after 1 attempts.
Signalling dispatcher removal.` was observed on the **debug** build and cost 104 steps — but release
has a wide margin, so it is a debug-build artifact, not a production risk. Don't re-derive this by
timing debug builds.

`pendingWalkingCount` was confirmed exact: at every batch the wallet lands on the last sensor total
(105 / 167 / 219 / 226), so the display never jumps or double-counts. Health Services and
`TYPE_STEP_COUNTER` independently agreed on 226.

### Two traps in the accounting

- **The resolved path is sticky.** `resolveSource` returns `current` whenever `isCollecting()`, and
  `SENSOR` qualifies. Fixing the R8 bug alone would have left every existing user stuck on `SENSOR`
  forever. `DeviceDataStore.migrateStepSchema()` v1 → v2 resets the path to `UNRESOLVED` once.
  Keep migrations staged: v0 → v1 wipes the balance, v1 → v2 must **not**.
- **Samsung's `TYPE_STEP_COUNTER` is not a since-boot total here.** It read 0 five hours after boot.
  Harmless while `HEALTH_STEPS` is active (the source gate discards it), but a device that genuinely
  falls back to `SENSOR` can lose the interval before each reset.

### Still open

- `prdRelease` was never exercised on device. It shares the R8 config with `stgRelease`, so it should
  behave identically — unverified.
- The step-restore MQTT path (`{prefix}/device/{deviceId}/step/restore`) needs backend cooperation to
  trigger and was not tested.
- `flush()` is wired to `MainViewModel.init`, i.e. ViewModel creation. Returning to a still-live
  Activity does not flush. Rare on Wear OS; revisit with an `ON_START` hook if it shows up.
