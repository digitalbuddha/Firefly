package com.androiddev.social.timeline.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Status(
    // base attributes
    val id: String,
    val uri: String,
    @SerialName("created_at") val createdAt: String,
    val account: Account? = null,
    val content: String,
    val visibility: Privacy,
    val sensitive: Boolean,
    @SerialName("spoiler_text") val spoilerText: String,
    @SerialName("media_attachments") val mediaAttachments: List<Attachment>? = null,
    val application: Application? = null,

    // rendering attributes
    @SerialName("mentions") val mentions: List<Mention>? = null,
    @SerialName("tags") val tags: List<Tag>? = null,
    @SerialName("emojis") val emojis: List<Emoji>? = null,

    // informal attributes
    @SerialName("reblogs_count") val reblogsCount: Int? = null,
    @SerialName("favourites_count") val favouritesCount: Int? = null,
    @SerialName("replies_count") val repliesCount: Int? = null,

    // nullable attributes
    @SerialName("url") val url: String? = null,
    @SerialName("in_reply_to_id") val inReplyToId: String? = null,
    @SerialName("in_reply_to_account_id") val inReplyToAccountId: String? = null,
    @SerialName("reblog") val reblog: Status?,
    @SerialName("poll") val poll: Poll? = null,
    @SerialName("card") val card: Card? = null,
    @SerialName("language") val language: String? = null,
    @SerialName("text") val text: String? = null,

    // Authorized user attributes
    @SerialName("favourited") val favourited: Boolean? = null,
    @SerialName("reblogged") val reblogged: Boolean? = null,
    @SerialName("muted") val muted: Boolean? = null,
    @SerialName("bookmarked") val bookmarked: Boolean? = null,
    @SerialName("pinned") val pinned: Boolean? = null,
)

@Serializable
data class Attachment(
    // required attributes
    val id: String,
    val type: AttachmentType,
    val url: String?,
    @SerialName("preview_url") val previewUrl: String,

    // optional attributes
    @SerialName("remote_url") val remoteUrl: String? = null,
    val meta: Hash? = null,
    val description: String? = null,
    val blurhash: String? = null,

    // deprecated attributes
    @Deprecated("Not used anymore")
    @SerialName("text_url")
    val textUrl: String? = null
)

enum class AttachmentType {
    unknown,
    image,
    gifv,
    video,
    audio,
}

@Serializable
data class Hash(
    val original: Original? = null,
    val small: Small? = null,
    val focus: Focus? = null,
    val length: String? = null,
    val duration: Float? = null,
    val fps: Int? = null,
    val size: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val aspect: Float? = null,
    @SerialName("audio_encode") val audioEncode: String? = null,
    @SerialName("audio_bitrate") val audioBitrate: String? = null,
    @SerialName("audio_channels") val audioChannels: String? = null,
    val description: String? = null,
    val blurhash: String? = null,
)

@Serializable
data class Original(
    val width: Int? = null,
    val height: Int? = null,
    val size: String? = null,
    val aspect: Float? = null,
    val duration: Float? = null,
    val frame_rate: String? = null,
    val bitrate: Int? = null,
)

@Serializable
data class Small(
    val width: Int? = null,
    val height: Int? = null,
    val size: String? = null,
    val aspect: Float? = null,
)

@Serializable
data class Focus(
    val x: Float? = null,
    val y: Float? = null,
)

@Serializable
data class Poll(
    @SerialName("id") val id: String,
    @SerialName("expires_at") val expiresAt: String,
    @SerialName("expired") val expired: Boolean,
    @SerialName("multiple") val multiple: Boolean,
    @SerialName("votes_count") val votesCount: Int? = null,
    @SerialName("voters_count") val votersCount: Int? = null,
    @SerialName("voted") val voted: Boolean? = null,
    @SerialName("own_votes") val ownVotes: List<Int>? = null,
    @SerialName("options") val options: List<PollHash>? = null,
    @SerialName("emojis") val emojis: List<Emoji>? = null,
)

@Serializable
data class PollHash(
    val title: String,
    @SerialName("votes_count") val votesCount: Int,
)

@Serializable
data class Emoji(
    // required attributes
    @SerialName("shortcode") val shortcode: String,
    @SerialName("url") val url: String,
    @SerialName("static_url") val staticUrl: String,
    @SerialName("visible_in_picker") val visibleInPicker: Boolean,

    // optional attributes
    @SerialName("category") val category: String? = null
)

@Serializable
data class Application(
    val name: String,
    @SerialName("vapid_key") val vapidKey: String? = null,

    // optional attributes
    val website: String? = null,
)

@Serializable
data class NewOauthApplication(
    val id: String,
    val name: String,
    @SerialName("vapid_key") val vapidKey: String,

    // client attributes
    @SerialName("client_id") val clientId: String,
    @SerialName("client_secret") val clientSecret: String,

    // optional attributes
    val website: String? = null,
)

@Serializable
data class Mention(
    // required attributes
    @SerialName("id") val id: String,
    @SerialName("username") val username: String,
    @SerialName("acct") val acct: String,
    @SerialName("url") val url: String,
)

@Serializable
data class Card(
    // base attributes
    @SerialName("url") val url: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
    @SerialName("type") val type: CardType,

    // optional attributes
    @SerialName("author_name") val authorName: String? = null,
    @SerialName("author_url") val authorUrl: String? = null,
    @SerialName("provider_name") val providerName: String? = null,
    @SerialName("provider_url") val providerUrl: String? = null,
    @SerialName("html") val html: String? = null,
    @SerialName("width") val width: Int? = null,
    @SerialName("height") val height: Int? = null,
    @SerialName("image") val image: String? = null,
    @SerialName("embed_url") val embedUrl: String? = null,
    @SerialName("blurhash") val blurhash: String? = null,
)

@Serializable
enum class CardType {
    link,
    photo,
    video,
    rich,
}

@Serializable
data class Tag(
    // required attributes
    @SerialName("name") val name: String,
    @SerialName("url") val url: String,

    // optional attributes
    @SerialName("history") val history: List<History>? = emptyList(),
)

@Serializable
data class History(
    // required attributes
    @SerialName("day") val day: String,
    @SerialName("uses") val uses: String,
    @SerialName("accounts") val accounts: String,
)
