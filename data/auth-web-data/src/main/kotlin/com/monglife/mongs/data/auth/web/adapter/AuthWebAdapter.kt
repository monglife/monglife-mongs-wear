package com.monglife.mongs.data.auth.web.adapter

import com.monglife.mongs.application.auth.port.web.AuthWebPort
import com.monglife.mongs.application.auth.port.web.response.LoginResponse
import com.monglife.mongs.application.auth.port.web.response.VerifyAppVersionResponse
import javax.inject.Inject

class AuthWebAdapter @Inject constructor(

) : AuthWebPort {

    override suspend fun verifyAppVersion(
        appPackageName: String,
        buildVersion: String
    ): VerifyAppVersionResponse {
        TODO("Not yet implemented")
    }

    override suspend fun join(email: String, name: String, googleAccountId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun login(
        deviceId: String,
        email: String,
        googleAccountId: String,
        appPackageName: String,
        deviceName: String,
        buildVersion: String
    ): LoginResponse {
        TODO("Not yet implemented")
    }

    override suspend fun logout() {
        TODO("Not yet implemented")
    }
}