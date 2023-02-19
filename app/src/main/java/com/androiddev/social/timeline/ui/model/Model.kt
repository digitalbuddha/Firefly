package com.androiddev.social.timeline.ui.model

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.*
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.core.text.parseAsHtml
import com.androiddev.social.theme.PaddingSize2_5
import com.androiddev.social.timeline.data.Emoji
import com.androiddev.social.timeline.data.Mention
import com.androiddev.social.timeline.data.Status
import com.androiddev.social.timeline.data.Tag
import com.androiddev.social.timeline.ui.AvatarImage

data class UI(
    val imageUrl: String? = null,
    val displayName: String = "FriendlyMike",
    val userName: String = "FriendlyMike@androiddev.social",
    val content: String = "",
    val replyCount: Int = 0,
    val boostCount: Int = 0,
    val favoriteCount: Int = 3,
    val timePosted: String = "3m",
    val boostedBy: String? = null,
    val directMessage: Boolean = false,
    val self: Status? = null,
    val avatar: String? = null,
    val mentions: List<Mention>,
    val tags: List<Tag>,
    val emojis: List<Emoji>?,
    val boostedAvatar: String?
)


fun String.parseAsMastodonHtml(): Spanned {
    return this.replace("<br> ", "<br>&nbsp;")
        .replace("<br /> ", "<br />&nbsp;")
        .replace("<br/> ", "<br/>&nbsp;")
        .replace("  ", "&nbsp;&nbsp;")
        .parseAsHtml()
        /* Html.fromHtml returns trailing whitespace if the html ends in a </p> tag, which
         * most status contents do, so it should be trimmed. */
        .trimTrailingWhitespace()

}

fun Spanned.trimTrailingWhitespace(): Spanned {
    var i = length
    do {
        i--
    } while (i >= 0 && get(i).isWhitespace())
    return subSequence(0, i + 1) as Spanned
}

fun Spanned.toAnnotatedString(
    primaryColor: Color,
    emojis: List<Emoji>? = listOf(),
    mutableMapOf: MutableMap<String, InlineTextContent>
): AnnotatedString {
    val builder = AnnotatedString.Builder()
    val split = split(":").filter { group -> group != "" }
    split.forEach { token ->
        val emoji = emojis?.firstOrNull { it.shortcode == token }
        if (emoji != null) {
            builder.appendInlineContent(
                emoji.shortcode,
                token
            )
            mutableMapOf[token] = InlineTextContent(
                Placeholder(
                    20.sp, 20.sp, PlaceholderVerticalAlign.TextCenter
                ), children = {
                    AvatarImage(PaddingSize2_5, url = emoji.url)
                })
        } else {
            builder.append(token)
        }
    }
    val copierContext = CopierContext(primaryColor)
    SpanCopier.values().forEach { copier ->
        val spans = getSpans(0, length, copier.spanClass)
        spans.forEach { span ->
            copier.copySpan(span, getSpanStart(span), getSpanEnd(span), builder, copierContext)
        }
    }

    return builder.toAnnotatedString()
}

private data class CopierContext(
    val primaryColor: Color,
)

private enum class SpanCopier {
    URL {
        override val spanClass = URLSpan::class.java
        override fun copySpan(
            span: Any,
            start: Int,
            end: Int,
            destination: AnnotatedString.Builder,
            context: CopierContext
        ) {
            val urlSpan = span as URLSpan
            destination.addStringAnnotation(
                tag = name,
                annotation = urlSpan.url,
                start = start,
                end = end,
            )
            destination.addStyle(
                style = SpanStyle(
                    color = context.primaryColor,
                    textDecoration = TextDecoration.Underline
                ),
                start = start,
                end = end,
            )

        }
    },
    FOREGROUND_COLOR {
        override val spanClass = ForegroundColorSpan::class.java
        override fun copySpan(
            span: Any,
            start: Int,
            end: Int,
            destination: AnnotatedString.Builder,
            context: CopierContext
        ) {
            val colorSpan = span as ForegroundColorSpan
            destination.addStyle(
                style = SpanStyle(color = Color(colorSpan.foregroundColor)),
                start = start,
                end = end,
            )
        }
    },
    UNDERLINE {
        override val spanClass = UnderlineSpan::class.java
        override fun copySpan(
            span: Any,
            start: Int,
            end: Int,
            destination: AnnotatedString.Builder,
            context: CopierContext
        ) {
            destination.addStyle(
                style = SpanStyle(textDecoration = TextDecoration.Underline),
                start = start,
                end = end,
            )
        }
    },
    STYLE {
        override val spanClass = StyleSpan::class.java
        override fun copySpan(
            span: Any,
            start: Int,
            end: Int,
            destination: AnnotatedString.Builder,
            context: CopierContext
        ) {
            val styleSpan = span as StyleSpan

            destination.addStyle(
                style = when (styleSpan.style) {
                    Typeface.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
                    Typeface.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
                    Typeface.BOLD_ITALIC -> SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic
                    )

                    else -> SpanStyle()
                },
                start = start,
                end = end,
            )
        }
    };

    abstract val spanClass: Class<out CharacterStyle>
    abstract fun copySpan(
        span: Any,
        start: Int,
        end: Int,
        destination: AnnotatedString.Builder,
        context: CopierContext
    )
}