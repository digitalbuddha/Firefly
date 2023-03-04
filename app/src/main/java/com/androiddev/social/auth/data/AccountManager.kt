package com.androiddev.social.auth.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

@Serializable
data class LoggedInAccounts(val servers: Map<String, Server> = emptyMap())

@Serializable
data class Server(val users: Map<String, User> = emptyMap(), val domain: String)

@Serializable
data class User(val accessTokenRequest: AccessTokenRequest, val accessToken: String?)

object LoggedInAccountsSerializer : Serializer<LoggedInAccounts> {

    override val defaultValue = LoggedInAccounts()

    override suspend fun readFrom(input: InputStream): LoggedInAccounts {
        try {
            return Json.decodeFromString(
                LoggedInAccounts.serializer(), input.readBytes().decodeToString()
            )
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read LoggedInAccounts", serialization)
        }
    }

    override suspend fun writeTo(t: LoggedInAccounts, output: OutputStream) {
        output.write(
            Json.encodeToString(LoggedInAccounts.serializer(), t)
                .encodeToByteArray()
        )
    }
}