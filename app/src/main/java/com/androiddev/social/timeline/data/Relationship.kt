package com.androiddev.social.timeline.data

import kotlinx.serialization.Serializable


@Serializable
data class Relationship(
    val id: String,
    val following: Boolean,
    val followed_by: Boolean,
    val blocking: Boolean,
    val blocked_by: Boolean,
    val muting: Boolean,
    val muting_notifications: Boolean,
    val requested: Boolean,
    val domain_blocking: Boolean,
    val endorsed: Boolean,
    val note: String
)
