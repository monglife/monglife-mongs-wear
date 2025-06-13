package com.monglife.mongs.core.domain.error

interface ErrorCode {

    fun getMessage() : String

    fun isMessageShow() : Boolean
}