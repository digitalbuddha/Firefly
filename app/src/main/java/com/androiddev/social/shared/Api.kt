package com.androiddev.social.shared

import com.androiddev.social.timeline.data.NewOauthApplication
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.*


interface Api {
    @POST
    @FormUrlEncoded
    suspend fun createApplication(
        @Url url: String,
        @Field("scopes") scopes: String,
        @Field("client_name") client_name: String,
        @Field("redirect_uris") redirect_uris: String
    ): NewOauthApplication

    @POST
    @FormUrlEncoded
    suspend fun createAccessToken(
        @Url domain: String,
        @Field("client_id") clientId: String,
        @Field("client_secret")  clientSecret: String,
        @Field("redirect_uri")  redirectUri: String,
        @Field("grant_type")  grantType: String,
        @Field("code") code: String,
        @Field("scope") scope: String
    ): Token

}


@Serializable
data class Token(
    val scope: String,
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String,
)

