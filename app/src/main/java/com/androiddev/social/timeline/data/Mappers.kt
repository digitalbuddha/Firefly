package com.androiddev.social.timeline.data

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.preference.PreferenceManager
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.util.Log
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.text.AnnotatedString
import androidx.core.net.toUri
import com.androiddev.social.timeline.ui.model.PollHashUI
import com.androiddev.social.timeline.ui.model.PollUI
import com.androiddev.social.timeline.ui.model.UI
import com.androiddev.social.ui.util.emojiText
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.until
import kotlinx.datetime.yearsUntil


fun Status.toStatusDb(feedType: FeedType = FeedType.Home): StatusDB {
    val status = reblog ?: this
    createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).time

    val timestamp: Long = createdAt.toEpochMilliseconds()
    return StatusDB(
        type = feedType.type,
        remoteId = status.id,
        originalId = id,
        uri = status.uri,
        createdAt = timestamp,
        content = status.content,
        accountId = status.account?.id,
        visibility = status.visibility.name,
        spoilerText = status.spoilerText,
        applicationName = status.application?.name ?: "",
        repliesCount = status.repliesCount ?: 0,
        reblogsCount = status.reblogsCount ?: 0,
        favouritesCount = status.favouritesCount ?: 0,
        isDirectMessage = status.visibility == Privacy.direct,
        avatarUrl = status.account?.avatar ?: "",
        imageUrl = status.mediaAttachments?.firstOrNull()?.url,
        accountAddress = status.account?.acct ?: "",
        userName = status.account?.acct ?: " ",
        displayName = status.account?.displayName ?: " ",
        emoji = status.emojis ?: emptyList(),
        accountEmojis = status.account?.emojis ?: emptyList(),
        mentions = status.mentions ?: emptyList(),
        tags = status.tags ?: emptyList(),
        boostedBy = if (reblog != null) account?.displayName else null,
        boostedById = if (reblog != null) account?.id else null,
        boostedAvatar = account?.avatar,
        boostedEmojis = account?.emojis ?: emptyList(),
        favorited = status.favourited ?: false,
        boosted = status.reblogged ?: false,
        inReplyTo = status.inReplyToId,
        bookmarked = status.bookmarked ?: false,
        attachments = status.mediaAttachments ?: emptyList(),
        poll = status.poll,
    )
}

fun StatusDB.mapStatus(colorScheme: ColorScheme): UI {
    val status = this

    val createdAt = Instant.fromEpochMilliseconds(status.createdAt)
    val now = Clock.System.now()
    val years = createdAt.yearsUntil(now, TimeZone.UTC)
    val months = createdAt.monthsUntil(now, TimeZone.UTC)
    val days = createdAt.until(now, DateTimeUnit.DAY, TimeZone.UTC).toInt()
    val hours = createdAt.until(now, DateTimeUnit.HOUR, TimeZone.UTC).toInt()
    val minutes = createdAt.until(now, DateTimeUnit.MINUTE, TimeZone.UTC).toInt()
    val createdString =
        when {
            years == 1 -> {
                "1 year ago"
            }

            years > 1 -> {
                "$years year ago"
            }

            months == 1 -> {
                "1 month ago"
            }

            months > 1 -> {
                "$months months ago"
            }

            days == 1 -> {
                "1 day ago"
            }

            days > 1 -> {
                "$days days ago"
            }

            hours == 1 -> {
                "1 hour ago"
            }

            hours > 1 -> {
                "$hours hours ago"
            }

            minutes == 1 -> {
                "1 min ago"
            }

            minutes > 1 -> {
                "$minutes min ago"
            }

            else -> {
                "now"
            }
        }
    return UI(
        imageUrl = status.imageUrl,
        displayName = status.displayName,
        userName = status.userName,
        content = status.content,
        replyCount = status.repliesCount ?: 0,
        boostCount = status.reblogsCount ?: 0,
        favoriteCount = status.favouritesCount ?: 0,
        timePosted = createdString,
        boostedBy = status.boostedBy,
        boostedById = status.boostedById,
        boostedAvatar = status.boostedAvatar,
        directMessage = status.isDirectMessage,
        avatar = status.avatarUrl,
        mentions = status.mentions,
        tags = status.tags,
        contentEmojis = status.emoji,
        accountEmojis = status.accountEmojis,
        remoteId = status.remoteId,
        boostedEmojis = status.boostedEmojis,
        type = FeedType.valueOf(if (status.type.startsWith("Hashtag")) "Hashtag" else status.type),
        favorited = status.favorited,
        boosted = status.boosted,
        inReplyTo = status.inReplyTo,
        accountId = status.accountId,
        originalId = status.originalId,
        bookmarked = status.bookmarked,
        contentEmojiText = emojiText(
            status.content,
            status.mentions,
            status.tags,
            status.emoji,
            colorScheme
        ),
        accountEmojiText = emojiText(
            status.displayName,
            emptyList(),
            emptyList(),
            status.accountEmojis,
            colorScheme
        ),
        boostedEmojiText = status.boostedBy?.let {
            emojiText(
                it,
                emptyList(),
                emptyList(),
                status.boostedEmojis,
                colorScheme
            )
        },
        attachments = status.attachments,
        poll = status.poll?.mapPoll()
    )
}

fun Poll.mapPoll(): PollUI = PollUI(
    remoteId = id,
    expiresAt = expiresAt,
    expired = expired,
    multiple = multiple,
    votesCount = votesCount,
    votersCount = votersCount,
    voted = voted,
    ownVotes = ownVotes,
    emojis = emojis,
    content = votersCount?.let { v -> "total vote count: $v" },
    options = options?.let { o ->
        o.map { it.mapPollHash(votesCount ?: 0) }
    }
)

fun PollHash.mapPollHash(
    totalVotesCount: Int
): PollHashUI = PollHashUI(
    voteContent = AnnotatedString(title),
    fullContent = if (totalVotesCount != 0) {
        AnnotatedString(
            title + ". " +
                    ("%.2f".format(votesCount.toFloat() / totalVotesCount)) +
                    "% \u21C4 " + votesCount
        )
    } else AnnotatedString(title),
)


/**
 * Finds links, mentions, and hashtags in a piece of text and makes them clickable, associating
 * them with callbacks to notify when they're clicked.
 *
 * @param view the returned text will be put in
 * @param content containing text with mentions, links, or hashtags
 * @param mentions any '@' mentions which are known to be in the content
 * @param listener to notify about particular spans that are clicked
 */
fun setClickableText(
    content: CharSequence, mentions: List<Mention>, tags: List<Tag>?, listener: LinkListener,
): SpannableStringBuilder {

    val spannableContent: SpannableStringBuilder = SpannableStringBuilder(content)

    return spannableContent.apply {
        getSpans(0, content.length, URLSpan::class.java).forEach {
            setClickableText(
                it, this, mentions, tags, listener,
            )
        }
    }
}

open class NoUnderlineURLSpan(
    url: String
) : URLSpan(url) {

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.isUnderlineText = false
    }

    override fun onClick(view: View) {
        view.context.openLink(url)
    }
}

interface LinkListener {
    fun onViewTag(tag: String)
    fun onViewAccount(id: String)
    fun onViewUrl(url: String)
}

@VisibleForTesting
fun setClickableText(
    span: URLSpan,
    builder: SpannableStringBuilder,
    mentions: List<Mention>,
    tags: List<Tag>?,
    listener: LinkListener,
) = builder.apply {
    val start = getSpanStart(span)
    val end = getSpanEnd(span)
    val flags = getSpanFlags(span)
    val text = subSequence(start, end)
    val customSpan = when (text[0]) {
        '#' -> getCustomSpanForTag(text, tags, span, listener)
        '@' -> getCustomSpanForMention(mentions, span, listener)
        else -> null
    } ?: object : NoUnderlineURLSpan(span.url) {
        override fun onClick(view: View) = listener.onViewUrl(url)
    }

    removeSpan(span)
    setSpan(customSpan, start, end, flags)

    if (end >= length || subSequence(end, end + 1).toString() == "\n") {
        insert(end, "\u200B")
    }
}

val unicodeToASCIIMap =
    "ÀÁÂÃÄÅàáâãäåĀāĂăĄąÇçĆćĈĉĊċČčÐðĎďĐđÈÉÊËèéêëĒēĔĕĖėĘęĚěĜĝĞğĠġĢģĤĥĦħÌÍÎÏìíîïĨĩĪīĬĭĮįİıĴĵĶķĸĹĺĻļĽľĿŀŁłÑñŃńŅņŇňŉŊŋÒÓÔÕÖØòóôõöøŌōŎŏŐőŔŕŖŗŘřŚśŜŝŞşŠšſŢţŤťŦŧÙÚÛÜùúûüŨũŪūŬŭŮůŰűŲųŴŵÝýÿŶŷŸŹźŻżŽž".toList()
        .zip(
            "AAAAAAaaaaaaAaAaAaCcCcCcCcCcDdDdDdEEEEeeeeEeEeEeEeEeGgGgGgGgHhHhIIIIiiiiIiIiIiIiIiJjKkkLlLlLlLlLlNnNnNnNnnNnOOOOOOooooooOoOoOoRrRrRrSsSsSsSssTtTtTtUUUUuuuuUuUuUuUuUuUuWwYyyYyYZzZzZz".toList()
        ).toMap()

fun normalizeToASCII(text: CharSequence): CharSequence {
    return String(text.map { unicodeToASCIIMap[it] ?: it }.toCharArray())
}

@VisibleForTesting
fun getTagName(text: CharSequence, tags: List<Tag>?): String? {
    val scrapedName = normalizeToASCII(text.subSequence(1, text.length)).toString()
    return when (tags) {
        null -> scrapedName
        else -> tags.firstOrNull { it.name.equals(scrapedName, true) }?.name
    }
}

private fun getCustomSpanForTag(
    text: CharSequence, tags: List<Tag>?, span: URLSpan, listener: LinkListener
): ClickableSpan? {
    return getTagName(text, tags)?.let {
        object : NoUnderlineURLSpan("###TAG$it") {
            override fun onClick(view: View) = listener.onViewTag(it)
        }
    }
}

private fun getCustomSpanForMention(
    mentions: List<Mention>, span: URLSpan, listener: LinkListener
): ClickableSpan? {
    return mentions.firstOrNull { it.url == span.url }?.let {
        getCustomSpanForMentionUrl(it.id, it.id, listener)
    }
}

private fun getCustomSpanForMentionUrl(
    url: String, mentionId: String, listener: LinkListener
): ClickableSpan {
    return object : NoUnderlineURLSpan(url) {
        override fun onClick(view: View) = listener.onViewAccount(mentionId)
    }
}


/**
 * Opens a link, depending on the settings, either in the browser or in a custom tab
 *
 * @receiver the Context to open the link from
 * @param url a string containing the url to open
 */
fun Context.openLink(url: String) {
    val uri = url.toUri().normalizeScheme()
    val useCustomTabs =
        PreferenceManager.getDefaultSharedPreferences(this).getBoolean("customTabs", false)

//    if (useCustomTabs) {
//        openLinkInCustomTab(uri, this)
//    } else {
    openLinkInBrowser(uri, this)
//    }
}

/**
 * opens a link in the browser via Intent.ACTION_VIEW
 *
 * @param uri the uri to open
 * @param context context
 */
private fun openLinkInBrowser(uri: Uri?, context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, uri)
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Log.w("LinkOpener", "Activity was not found for intent, $intent")
    }
}
