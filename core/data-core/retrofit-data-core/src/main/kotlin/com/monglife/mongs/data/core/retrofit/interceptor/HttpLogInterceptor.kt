package com.monglife.mongs.data.core.retrofit.interceptor

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.monglife.mongs.data.core.common.dto.response.ResponseDto
import com.monglife.mongs.data.core.retrofit.constant.HttpConst
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
    }

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()

        val out = StringBuilder()
            .append("%-4s".format(request.method()))
            .append(" ")
            .append("%-30s".format(request.url()))
            .append("\n")
            .append("\t- authorization   => ${request.header(HttpConst.AUTHORIZATION_HEADER)?: "X"}\n")

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
                .code(HttpConst.INTERNAL_SERVER_ERROR_HTTP_STATUS_CODE)
                .message(HttpConst.INTERNAL_SERVER_ERROR_HTTP_MESSAGE)
                .body(ResponseBody.create(MediaType.get("application/json"), gson.toJson(errorResponseBody)))
                .build()
        }
        response.body()?.let { responseBody ->
            val bodyJson = responseBody.string()

            try {
                val responseDto = gson.fromJson(bodyJson, ResponseDto::class.java)
                out
                    .append("\t- httpStatus      => ${response.code()}\n")
                    .append("\t- responseCode    => ${responseDto.code}\n")
                    .append("\t- responseMessage => ${responseDto.message}\n")
                    .append("\t- responseResult  => ${responseDto.result}")
            } catch (_: JsonParseException) {
                out.append("\t- httpStatus      => 500")
            }

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