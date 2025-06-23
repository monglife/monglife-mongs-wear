package com.monglife.mongs.data.core.interceptor

import android.util.Log
import com.google.gson.Gson
import com.monglife.mongs.data.core.constant.HttpConst
import com.monglife.mongs.data.core.dto.response.ResponseDto
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Request
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
    }

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()

        val out = StringBuilder()
            .append("%-4s".format(request.method()))
            .append(" ")
            .append("%-30s".format(request.url()))
            .append("\n")
            .append("  - authorization => ${request.header(HttpConst.AUTHORIZATION_HEADER)?: ""}\n")
            .append("  - request body  => ${getRequestBodyJson(request = request)}\n")

        val response: Response = try {
            chain.proceed(request)
        } catch (e: ConnectException) {
            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpConst.INTERNAL_SERVER_ERROR_HTTP_STATUS_CODE)
                .message(HttpConst.INTERNAL_SERVER_ERROR_HTTP_MESSAGE)
                .body(
                    ResponseBody.create(
                        MediaType.get("application/json"), gson.toJson(
                            ResponseDto<Map<String, Any>>(
                                httpStatus = 500,
                                code = "CLIENT-200",
                                message = "클라이언트에서 서버로 연결이 불가능합니다.",
                                result = mapOf()
                            )
                        )
                    )
                )
                .build()
        }

        response.body()?.let { responseBody ->
            val bodyJson = responseBody.string()

            try {
                gson.fromJson(bodyJson, ResponseDto::class.java).let { responseDto ->
                    out
                        .append("  - http status   => ${response.code()}\n")
                        .append("  - response code => ${responseDto?.code}\n")
                        .append("  - message       => ${responseDto?.message}\n")
                        .append("  - result        => ${responseDto?.result}")
                }
            } catch (_: Exception) {
                out.append("  - http status   => ${HttpConst.INTERNAL_SERVER_ERROR_HTTP_STATUS_CODE}")
            }

            Log.i(TAG, "API >> $out")

            return response.newBuilder()
                .body(RealResponseBody(body = bodyJson, originalBody = responseBody))
                .build()

        } ?: run {
            return response
        }
    }

    private fun getRequestBodyJson(request: Request): String =
        request.body()?.let { requestBody ->
            val buffer = okio.Buffer()
            val charset = requestBody.contentType()?.charset(Charsets.UTF_8) ?: Charsets.UTF_8
            requestBody.writeTo(buffer)
            buffer.readString(charset)
        } ?: ""

    class RealResponseBody(
        private val body: String,
        private val originalBody: ResponseBody,
    ) : ResponseBody() {
        override fun contentType(): MediaType? = originalBody.contentType()
        override fun contentLength(): Long = originalBody.contentLength()
        override fun source(): BufferedSource = body.byteInputStream().source().buffer()
    }
}