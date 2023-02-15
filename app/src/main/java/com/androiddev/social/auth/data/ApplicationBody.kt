package com.androiddev.social.auth.data

import kotlinx.serialization.Serializable

@Serializable
data class ApplicationBody(
    val baseUrl: String = "androiddev.social",
    val scopes: String = "read write follow push",
    val clientName: String = "Ebony",
    val redirectScheme: String = "ebonyoauth2redirect://",
) {
    fun redirectUris():String = redirectScheme + baseUrl
}
