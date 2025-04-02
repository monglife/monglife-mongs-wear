package com.mongs.wear.data.global.interceptor

import android.util.Log
import com.google.gson.Gson
import com.mongs.wear.core.dto.response.ResponseDto
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody
import okio.BufferedSource
import okio.buffer
import okio.source
import java.net.ConnectException

class HttpLogInterceptor(
    private val gson: Gson
) : Interceptor {

    companion object {
        private const val TAG = "HttpLogInterceptor"

        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val CONNECTION_RESPONSE_CODE = 500
        private const val CONNECTION_RESPONSE_MESSAGE = "서버 연결 실패"
    }

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()

        val out = StringBuilder()
            .append("%-4s".format(request.method()))
            .append(" ")
            .append("%-30s".format(request.url()))
            .append("\n")
            .append("\t- authorization   => ${request.header(AUTHORIZATION_HEADER)?: "X"}\n")

        val response = try {
             chain.proceed(chain.request())
        } catch (_: ConnectException) {

            val errorResponseBody = ResponseDto<Map<String, Any>>(
                httpStatus = 500,
                code = "CLIENT-200",
                message = "클라이언트에서 서버로 연결이 불가능합니다.",
                result = mapOf()
            )

            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(CONNECTION_RESPONSE_CODE)
                .message(CONNECTION_RESPONSE_MESSAGE)
                .body(ResponseBody.create(MediaType.get("application/json"), gson.toJson(errorResponseBody)))
                .build()
        }
        response.body()?.let { responseBody ->
            val bodyJson = responseBody.string()
            val responseDto = gson.fromJson(bodyJson, ResponseDto::class.java)

            out
                .append("\t- httpStatus      => ${response.code()}\n")
                .append("\t- responseCode    => ${responseDto.code}\n")
                .append("\t- responseMessage => ${responseDto.message}\n")
                .append("\t- responseResult  => ${responseDto.result}")

            Log.i(TAG, "${"%-8s".format("API")} >> $out")

            return response.newBuilder()
                .body(RealResponseBody(body = bodyJson, originalBody = responseBody))
                .build()

        } ?: run {
            return response
        }
    }

    class RealResponseBody(
        private val body: String,
        private val originalBody: ResponseBody,
    ) : ResponseBody() {
        override fun contentType(): MediaType? = originalBody.contentType()
        override fun contentLength(): Long = originalBody.contentLength()
        override fun source(): BufferedSource = body.byteInputStream().source().buffer()
    }
}