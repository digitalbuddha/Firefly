package com.androiddev.social.timeline.data

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query


interface TimelineApi {
    @GET("api/v1/timelines/home")
    suspend fun getTimeline(
        @Header("Authorization") authHeader: String?,
        @Query("limit") limit:String="40",
        ): List<Status>
}