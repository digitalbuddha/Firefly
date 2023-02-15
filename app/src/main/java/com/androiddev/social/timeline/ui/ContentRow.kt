package com.androiddev.social.timeline.ui

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Divider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androiddev.social.timeline.data.LinkListener
import com.androiddev.social.timeline.data.setClickableText
import com.androiddev.social.timeline.ui.model.UI
import com.androiddev.social.timeline.ui.model.parseAsMastodonHtml
import com.androiddev.social.timeline.ui.model.toAnnotatedString
import com.androiddev.social.timeline.ui.theme.Pink80

@Composable
fun ContentRow(ui: UI) {
    Row(Modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val parseAsMastodonHtml = ui.content.parseAsMastodonHtml()
            println(parseAsMastodonHtml)
            val prettyText = setClickableText(
                parseAsMastodonHtml,
                ui.mentions,
                ui.tags,
                object : LinkListener {
                    override fun onViewTag(tag: String) {
                        TODO("Not yet implemented")
                    }

                    override fun onViewAccount(id: String) {
                        TODO("Not yet implemented")
                    }

                    override fun onViewUrl(url: String) {
                        TODO("Not yet implemented")
                    }
                })
            val uriHandler = LocalUriHandler.current
            val text = prettyText.toAnnotatedString(Pink80)
            var clicked by remember { mutableStateOf(false) }

            ClickableText(style = TextStyle.Default.copy(
                color = colorScheme.secondary,
                fontSize = 16.sp,
                lineHeight = 22.sp
            ),
                modifier = Modifier
                    .fillMaxWidth(), text = text,
                onClick = {
                    clicked = !clicked
                    text.getStringAnnotations(
                        tag = "URL", start = it,
                        end = it
                    )
                        .firstOrNull()?.let { annotation ->
                            // If yes, we log its value
                            uriHandler.openUri(annotation.item)
                            Log.d("Clicked URL", annotation.item)
                        }
                }
            )
            ui.imageUrl?.let { ContentImage(it, clicked) { clicked = !clicked } }
            AnimatedVisibility(visible = clicked) {
                ButtonBar(ui.replyCount, ui.boostCount)
            }
            Divider(Modifier.padding(0.dp), color = Color.Gray.copy(alpha = .5f))
        }
    }
}