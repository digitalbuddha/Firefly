package com.androiddev.social.ui.util

import android.text.Spanned
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import com.androiddev.social.timeline.data.Emoji
import com.androiddev.social.timeline.data.Mention
import com.androiddev.social.timeline.data.Tag
import com.androiddev.social.timeline.data.setClickableText
import com.androiddev.social.timeline.ui.empty
import com.androiddev.social.timeline.ui.model.parseAsMastodonHtml
import com.androiddev.social.timeline.ui.model.toAnnotatedString

@Composable
fun emojiText(
    content:String,
    mentions:List<Mention>,
    tags:List<Tag>,
    emojis:List<Emoji>?,
): Pair<MutableMap<String, InlineTextContent>, AnnotatedString> {
    val parseAsMastodonHtml: Spanned = content.parseAsMastodonHtml()
    println(parseAsMastodonHtml)
    val prettyText = setClickableText(
        parseAsMastodonHtml,
        mentions,
        tags,
        empty
    )

    val mapping by remember(content, mentions, tags) { mutableStateOf(mutableMapOf<String, InlineTextContent>()) }
    val linkColor = MaterialTheme.colorScheme.primary
    val text by remember(content, mentions, tags) {
        mutableStateOf(
            prettyText.toAnnotatedString(
                linkColor,
                emojis,
                mapping
            )
        )
    }
    return Pair(mapping, text)
}