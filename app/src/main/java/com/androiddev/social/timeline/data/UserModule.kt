package com.androiddev.social.timeline.data

import android.app.Application
import androidx.room.Room
import com.androiddev.social.SingleIn
import com.androiddev.social.UserScope
import com.androiddev.social.auth.data.AccessTokenRequest
import com.androiddev.social.shared.UserApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

@ContributesTo(UserScope::class)
@Module
class UserModule {

    @Provides
    @SingleIn(UserScope::class)
    fun providesRetrofit(
        httpClient: OkHttpClient,
        accessTokenRequest: AccessTokenRequest
    ): UserApi {
        val json = Json { ignoreUnknownKeys = true }
        val contentType = "application/json".toMediaType()
        return Retrofit
            .Builder()
            .baseUrl("https://${accessTokenRequest.domain}")
            .client(httpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build().create(UserApi::class.java)
    }

    @Provides
    @SingleIn(UserScope::class)
    fun provideDB(applicationContext: Application): AppDatabase =
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        )
            .fallbackToDestructiveMigration()
            .build()


    @Provides
    @SingleIn(UserScope::class)
    fun provideStatusDao(appDatabase: AppDatabase): StatusDao = appDatabase.statusDao()
}