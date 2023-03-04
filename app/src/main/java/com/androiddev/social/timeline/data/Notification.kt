package com.androiddev.social.timeline.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    val id: String,
    val type: String,
    val status: Status? = null,
    val account: Account,
    val created_at: Instant,
    val realType:Type?=null
)

@Serializable enum class Type(val value: String) {
    mention("mention"),
    status("status"),
    reblog("reblog"),
    follow("follow"),
    follow_request("follow_request"),
    favourite("favourite"),
    poll("poll"),
    update("update"),
    adminsignup("admin.sign_up"),
    adminreport("admin.report"),
}