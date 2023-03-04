package com.androiddev.social.conversation

import com.androiddev.social.timeline.data.Account
import com.androiddev.social.timeline.data.Status
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Conversation(
    val id:String,
    val unread:Boolean,
    val accounts:List<Account>,
    @SerialName("last_status") val lastStatus:Status
)