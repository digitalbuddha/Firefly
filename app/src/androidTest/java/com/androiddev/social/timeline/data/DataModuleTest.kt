//package com.androiddev.social.timeline.data
//
//import kotlinx.coroutines.test.runTest
//import org.assertj.core.api.Assertions.assertThat
//import org.junit.Test
//
//
//class DataModuleTest {
//    val module = DataModule()
//    val client = module.providesHttpClient()
//    val timelineApi = module.providesRetrofit(client)
//    private val bearer = " Bearer o4i6i5EmNEqmN8PiecyY5EGHHKEQTT7fIZrPovH8S1s"
//
//    @Test
//    fun simpleTest() = runTest {
//
//        val result = timelineApi.getTimeline(bearer)
//
//        assertThat(result).isNotNull()
//    }
//
//    @Test
//    fun testStatusMappingSimple() = runTest {
//        val mapped = timelineApi.getTimeline(bearer).mapStatus()
//        assertThat(mapped).isNotEmpty()
//    }
//}