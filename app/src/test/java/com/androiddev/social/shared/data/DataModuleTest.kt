package com.androiddev.social.shared.data

import com.androiddev.social.auth.data.ApplicationBody
import com.androiddev.social.timeline.data.DataModule
import com.androiddev.social.timeline.data.mapStatus
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class DataModuleTest {
    val module = DataModule()
    val client = module.providesHttpClient()
    val api = module.providesRetrofit(client)
    private val bearer = " Bearer o4i6i5EmNEqmN8PiecyY5EGHHKEQTT7fIZrPovH8S1s"

    @Test
    fun timeLineTest() = runTest {
        val result = api.getTimeline(bearer, since = null)
        assertThat(result).isNotNull()
    }

    @Test
    fun accountInfoTest() = runTest {
        val result =
            api.accountVerifyCredentials(" Bearer o4i6i5EmNEqmN8PiecyY5EGHHKEQTT7fIZrPovH8S1s")
        assertThat(result).isNotNull()
    }

    @Test
    fun appAuthTest() = runTest {
        val body = ApplicationBody()
        val result = api.createApplication(
            url = "https://androiddev.social/api/v1/apps",
            scopes = body.scopes,
            client_name = body.clientName,
            redirect_uris = body.redirectScheme + body.baseUrl
        )
        assertThat(result).isNotNull()
    }

    @Test
    fun accessTokenTest() = runTest {
        val body = ApplicationBody()
        val result = api.createApplication(
            url = "https://androiddev.social/api/v1/apps",
            scopes = body.scopes,
            client_name = body.clientName,
            redirect_uris = body.redirectScheme + body.baseUrl
        )
//        api.createAccessToken(
//            domain = "https://androiddev.social/api/v1/apps/oauth/token",
//            clientId =  result.clientId,
//            clientSecret = result.clientSecret,
//            redirectUri = result.redirectUri,
//            grantType = "authorization_code",
//            code =
//
//        )
    }

    @Test
    fun testMappingJVM() = runTest {
        val mapped = api.getTimeline(bearer, since = null).mapStatus().map { it.imageUrl }
        assertThat(mapped).isNotEmpty
    }
}