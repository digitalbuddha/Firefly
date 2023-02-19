package com.androiddev.social.timeline.data

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.preference.PreferenceManager
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.core.net.toUri
import com.androiddev.social.timeline.ui.model.UI
import java.net.URI
import java.net.URISyntaxException
import java.text.SimpleDateFormat
import java.util.*

fun List<Status>.mapStatus(): List<UI> {
    val result= map { item ->
        val status = item.reblog ?: item
        val date: Date =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(status.createdAt)
        val timestamp: Long = date.time

        val mediaAttachments = status.mediaAttachments
        UI(
            imageUrl = mediaAttachments?.firstOrNull()?.url,
            displayName = status.account?.displayName ?: "FriendlyMike",
            userName = status.account?.username ?: "FriendlyMike",
            content = status.content,
            replyCount = status.repliesCount ?: 0,
            boostCount = status.reblogsCount ?: 0,
            favoriteCount = status.favouritesCount ?: 0,
            timePosted = TimeUtils.getRelativeTime(timestamp).toString(),
            boostedBy = if (item.reblog != null) item.account?.displayName else null,
            boostedAvatar = item.account?.avatar,
            directMessage = status.visibility == Privacy.direct,
            avatar = status.account?.avatar,
            mentions = status.mentions ?: emptyList(),
            tags = status.tags ?: emptyList(),
            contentEmojis = status.emojis,
            accountEmojis = status.account?.emojis
        )
    }
    return result
}


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
//    waitForDebugger()

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

    /* Add zero-width space after links in end of line to fix its too large hitbox.
     * See also : https://github.com/tuskyapp/Tusky/issues/846
     *            https://github.com/tuskyapp/Tusky/pull/916 */
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
    text: CharSequence,
    tags: List<Tag>?,
    span: URLSpan,
    listener: LinkListener
): ClickableSpan? {
    return getTagName(text, tags)?.let {
        object : NoUnderlineURLSpan(span.url) {
            override fun onClick(view: View) = listener.onViewTag(it)
        }
    }
}

private fun getCustomSpanForMention(
    mentions: List<Mention>,
    span: URLSpan,
    listener: LinkListener
): ClickableSpan? {
    // https://github.com/tuskyapp/Tusky/pull/2339
    return mentions.firstOrNull { it.url == span.url }?.let {
        getCustomSpanForMentionUrl(span.url, it.id, listener)
    }
}

private fun getCustomSpanForMentionUrl(
    url: String,
    mentionId: String,
    listener: LinkListener
): ClickableSpan {
    return object : NoUnderlineURLSpan(url) {
        override fun onClick(view: View) = listener.onViewAccount(mentionId)
    }
}

/**
 * Put mentions in a piece of text and makes them clickable, associating them with callbacks to
 * notify when they're clicked.
 *
 * @param view the returned text will be put in
 * @param mentions any '@' mentions which are known to be in the content
 * @param listener to notify about particular spans that are clicked
 */
fun setClickableMentions(view: TextView, mentions: List<Mention>?, listener: LinkListener) {
    if (mentions?.isEmpty() != false) {
        view.text = null
        return
    }

    view.text = SpannableStringBuilder().apply {
        var start = 0
        var end = 0
        var flags: Int
        var firstMention = true

        for (mention in mentions) {
            val customSpan = getCustomSpanForMentionUrl(mention.url, mention.id, listener)
            end += 1 + mention.username.length // length of @ + username
            flags = getSpanFlags(customSpan)
            if (firstMention) {
                firstMention = false
            } else {
                append(" ")
                start += 1
                end += 1
            }

            append("@")
            append(mention.username)
            setSpan(customSpan, start, end, flags)
            append("\u200B") // same reasoning as in setClickableText
            end += 1 // shift position to take the previous character into account
            start = end
        }
    }
    view.movementMethod = LinkMovementMethod.getInstance()
}

fun createClickableText(text: String, link: String): CharSequence {
    return SpannableStringBuilder(text).apply {
        setSpan(NoUnderlineURLSpan(link), 0, text.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
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

/**
 * tries to open a link in a custom tab
 * falls back to browser if not possible
 *
 * @param uri the uri to open
 * @param context context
 */
//private fun openLinkInCustomTab(uri: Uri, context: Context) {
//
//    val customTabsIntent = CustomTabsIntent.Builder()
//        .setShowTitle(true)
//        .build()
//
//    try {
//        customTabsIntent.launchUrl(context, uri)
//    } catch (e: ActivityNotFoundException) {
//        Log.w(TAG, "Activity was not found for intent $customTabsIntent")
//        openLinkInBrowser(uri, context)
//    }
//}

// https://mastodon.foo.bar/@User
// https://mastodon.foo.bar/@User/43456787654678
// https://pleroma.foo.bar/users/User
// https://pleroma.foo.bar/users/9qTHT2ANWUdXzENqC0
// https://pleroma.foo.bar/notice/9sBHWIlwwGZi5QGlHc
// https://pleroma.foo.bar/objects/d4643c42-3ae0-4b73-b8b0-c725f5819207
// https://friendica.foo.bar/profile/user
// https://friendica.foo.bar/display/d4643c42-3ae0-4b73-b8b0-c725f5819207
// https://misskey.foo.bar/notes/83w6r388br (always lowercase)
// https://pixelfed.social/p/connyduck/391263492998670833
// https://pixelfed.social/connyduck
// https://gts.foo.bar/@goblin/statuses/01GH9XANCJ0TA8Y95VE9H3Y0Q2
// https://gts.foo.bar/@goblin
// https://foo.microblog.pub/o/5b64045effd24f48a27d7059f6cb38f5
fun looksLikeMastodonUrl(urlString: String): Boolean {
    val uri: URI
    try {
        uri = URI(urlString)
    } catch (e: URISyntaxException) {
        return false
    }

    if (uri.query != null ||
        uri.fragment != null ||
        uri.path == null
    ) {
        return false
    }

    return uri.path.let {
        it.matches("^/@[^/]+$".toRegex()) ||
                it.matches("^/@[^/]+/\\d+$".toRegex()) ||
                it.matches("^/users/\\w+$".toRegex()) ||
                it.matches("^/notice/[a-zA-Z0-9]+$".toRegex()) ||
                it.matches("^/objects/[-a-f0-9]+$".toRegex()) ||
                it.matches("^/notes/[a-z0-9]+$".toRegex()) ||
                it.matches("^/display/[-a-f0-9]+$".toRegex()) ||
                it.matches("^/profile/\\w+$".toRegex()) ||
                it.matches("^/p/\\w+/\\d+$".toRegex()) ||
                it.matches("^/\\w+$".toRegex()) ||
                it.matches("^/@[^/]+/statuses/[a-zA-Z0-9]+$".toRegex()) ||
                it.matches("^/o/[a-f0-9]+$".toRegex())
    }


}

fun getDomain(urlString: String?): String {
    val host = urlString?.toUri()?.host
    return when {
        host == null -> ""
        host.startsWith("www.") -> host.substring(4)
        else -> host
    }
}