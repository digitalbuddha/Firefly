//package com.androiddev.social.shared.data
//
//import com.androiddev.social.auth.data.AccessTokenRequest
//import com.androiddev.social.shared.UserApi
//import com.androiddev.social.timeline.data.DataModule
//import com.androiddev.social.timeline.data.UserModule
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.test.runTest
//import org.assertj.core.api.Assertions.assertThat
//import org.junit.Test
//
//
//@ExperimentalCoroutinesApi
//class UserModuleTest {
//    val module = DataModule()
//    val userModule = UserModule()
//    val client = module.providesHttpClient()
//    val api: UserApi = userModule.providesRetrofit(client, AccessTokenRequest(domain = "androiddev.social", "", "", "", ""))
//    private val bearer = " Bearer EQkWgT1_hzzx4iITmAVXUzat6h5kTyC4hHl3CPA7FZQ"
//
//    @Test
//    fun timeLineTest() = runTest {
//        val result = api.newStatus(bearer, "Hello World from Firefly")
//        assertThat(result.id).isNotNull()
//    }
////
////    @Test
////    fun accountInfoTest() = runTest {
////        val result =
////            api.accountVerifyCredentials(" Bearer o4i6i5EmNEqmN8PiecyY5EGHHKEQTT7fIZrPovH8S1s")
////        assertThat(result).isNotNull()
////    }
////
////    @Test
////    fun appAuthTest() = runTest {
////        val body = ApplicationBody()
////        val result = api.createApplication(
////            url = "https://androiddev.social/api/v1/apps",
////            scopes = body.scopes,
////            client_name = body.clientName,
////            redirect_uris = body.redirectScheme + body.baseUrl
////        )
////        assertThat(result).isNotNull()
////    }
////
////    @Test
////    fun accessTokenTest() = runTest {
////        val body = ApplicationBody()
////        val result = api.createApplication(
////            url = "https://androiddev.social/api/v1/apps",
////            scopes = body.scopes,
////            client_name = body.clientName,
////            redirect_uris = body.redirectScheme + body.baseUrl
////        )
//////        api.createAccessToken(
//////            domain = "https://androiddev.social/api/v1/apps/oauth/token",
//////            clientId =  result.clientId,
//////            clientSecret = result.clientSecret,
//////            redirectUri = result.redirectUri,
//////            grantType = "authorization_code",
//////            code =
//////
//////        )
////    }
////
////    @Test
////    fun testMappingJVM() = runTest {
////        val mapped = api.getTimeline(bearer, since = null).mapStatus().map { it.imageUrl }
////        assertThat(mapped).isNotEmpty
////    }
//}