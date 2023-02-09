package com.androiddev.social.ui.model

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.text.parseAsHtml

data class UI(
    val imageUrl: String = "https://placekitten.com/300/300",
    val displayName: String = "FriendlyMike",
    val userName: String = "FriendlyMike@androiddev.social",
    val content: AnnotatedString = "<p><span class=\"h-card\"><a href=\"https://mastodon.online/@UnfriendlyMike\" class=\"u-url mention\">@<span>UnfriendlyMike</span></a></span> I don't see any reason why there cannot be a high quality Mastodon Android App</p><p><a href=\"https://www.youtube.com/watch?v=WjuIcvQy8i4\" target=\"_blank\" rel=\"nofollow noopener noreferrer\"><span class=\"invisible\">https://www.</span><span class=\"ellipsis\">youtube.com/watch?v=WjuIcvQy8i</span><span class=\"invisible\">4</span></a></p>".parseAsMastodonHtml()
        .toAnnotatedString(Color.Blue),
    val replyCount: Int = 1,
    val boostCount: Int = 2,
    val favoriteCount: Int = 3,
    val timePosted: Long? = null
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

fun Spanned.toAnnotatedString(primaryColor: Color): AnnotatedString {
    val builder = AnnotatedString.Builder(this.toString())
    val copierContext = CopierContext(primaryColor)
    SpanCopier.values().forEach { copier ->
        getSpans(0, length, copier.spanClass).forEach { span ->
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