package com.androiddev.social.ui.util

import android.text.Spanned
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.text.AnnotatedString
import com.androiddev.social.timeline.data.Emoji
import com.androiddev.social.timeline.data.Mention
import com.androiddev.social.timeline.data.Tag
import com.androiddev.social.timeline.data.setClickableText
import com.androiddev.social.timeline.ui.empty
import com.androiddev.social.timeline.ui.model.parseAsMastodonHtml
import com.androiddev.social.timeline.ui.model.toAnnotatedString

fun emojiText(
    content: String,
    mentions: List<Mention>,
    tags: List<Tag>,
    emojis: List<Emoji>?,
    colorScheme: ColorScheme
): EmojiText {
    val parseAsMastodonHtml: Spanned = content.parseAsMastodonHtml()
    println(parseAsMastodonHtml)
    val prettyText = setClickableText(
        parseAsMastodonHtml,
        mentions,
        tags,
        empty
    )

    val mapping = mutableMapOf<String, InlineTextContent>()
    val linkColor = colorScheme.primary
    val text= prettyText.toAnnotatedString(
            linkColor,
            emojis,
            mapping
        )
    return EmojiText(mapping, text)
}

data class EmojiText(val mapping: Map<String, InlineTextContent>, val text: AnnotatedString)