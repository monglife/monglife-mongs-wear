package com.monglife.core.common.error

interface ErrorCode {

    fun getMessage() : String

    fun isMessageShow() : Boolean
}