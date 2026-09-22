package com.monglife.core.data.web.utils

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.monglife.core.data.global.adapter.GsonLocalDateTimeFormatAdapter
import com.monglife.core.data.global.adapter.GsonLocalTimeAdapter
import com.monglife.core.data.web.dto.response.ResponseDto
import okhttp3.ResponseBody
import retrofit2.Response
import java.time.LocalDateTime
import java.time.LocalTime

object HttpUtil {
    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, GsonLocalDateTimeFormatAdapter())
        .registerTypeAdapter(LocalTime::class.java, GsonLocalTimeAdapter())
        .create()

    /**
     * ErrorResponseDto 파싱
     */
    private fun getErrorResponseDto(errorBody: ResponseBody?) : ResponseDto<Map<String, Any>> {
        return errorBody?.let { body ->
            gson.fromJson(body.string(), ResponseDto::class.java)?.let { responseDto ->
                responseDto.result?.let { result ->
                    ResponseDto(
                        code = responseDto.code,
                        message = responseDto.message,
                        httpStatus = responseDto.httpStatus,
                        result = gson.fromJson(
                            gson.toJson(result),
                            object : TypeToken<Map<String, Any>>() {}.type
                        )
                    )
                } ?: run {
                    ResponseDto(
                        code = responseDto.code,
                        message = responseDto.message,
                        httpStatus = responseDto.httpStatus,
                        result = emptyMap()
                    )
                }
            } ?: run {
                ResponseDto(
                    code = "-",
                    message = "",
                    httpStatus = 500,
                    result = emptyMap()
                )
            }
        } ?: run {
            ResponseDto(
                code = "-",
                message = "",
                httpStatus = 500,
                result = emptyMap()
            )
        }
    }

    fun <T> Response<T>.getErrorResponseDto(): ResponseDto<Map<String, Any>> = getErrorResponseDto(this.errorBody())
}