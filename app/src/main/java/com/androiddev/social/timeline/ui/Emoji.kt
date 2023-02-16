package com.androiddev.social.timeline.ui

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androiddev.social.timeline.data.Emoji

@Composable
fun inlineEmojis(
    unformatted: String,
    emojis: List<Emoji>
): Pair<MutableMap<String, InlineTextContent>, AnnotatedString> {
    val inlineContentMap: MutableMap<String, InlineTextContent> = mutableMapOf()
    val text = buildAnnotatedString {
        val split = unformatted.split(":").filter { group -> group != "" }
        split.forEach { token ->
            val emoji = emojis.firstOrNull { it.shortcode == token }
            if (emoji != null) {
                appendInlineContent(token, token)
                inlineContentMap[token] = InlineTextContent(
                    Placeholder(
                        20.sp, 20.sp, PlaceholderVerticalAlign.TextCenter
                    ), children = {
                        Image(20.dp, url = emoji.url)
                    })
            } else {
                append(token)
            }
        }
    }
    return Pair(inlineContentMap, text)
}