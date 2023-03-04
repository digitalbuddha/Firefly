package com.androiddev.social.timeline.data

import com.androiddev.social.AppScope
import com.androiddev.social.SingleIn
import com.androiddev.social.shared.Api
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import social.androiddev.BuildConfig
import java.util.concurrent.TimeUnit

@ContributesTo(AppScope::class)
@Module
class DataModule {
    @Provides
    @SingleIn(AppScope::class)
    fun providesHttpClient(
//        context: Application,
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
//            .cache(Cache(context.cacheDir, 1000))

        return builder
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                }
            }
            .build()
    }

    @Provides
    @SingleIn(AppScope::class)
    fun providesRetrofit(
        httpClient: OkHttpClient
    ): Api {
        val json = Json { ignoreUnknownKeys = true }
        val contentType = "application/json".toMediaType()
        return Retrofit
            .Builder()
            .baseUrl("https://androiddev.social")
            .client(httpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build().create(Api::class.java)
    }


}