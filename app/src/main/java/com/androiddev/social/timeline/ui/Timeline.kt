package com.androiddev.social.timeline.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androiddev.social.R
import com.androiddev.social.timeline.data.LinkListener
import com.androiddev.social.timeline.data.setClickableText
import com.androiddev.social.timeline.ui.model.UI
import com.androiddev.social.timeline.ui.model.parseAsMastodonHtml
import com.androiddev.social.timeline.ui.model.toAnnotatedString
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox

@Composable
fun Timeline(ui: List<UI>) {
    LazyColumn {
        ui.forEach {
            item { TimelineCard(it) }
//            item { TimelineCard(it.copy(directMessage = true)) }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TimelineCard(ui: UI) {
    val star = SwipeAction(
        icon = painterResource(id = R.drawable.star),
        background = colorScheme.tertiary.copy(alpha = .5f),
        onSwipe = { }
    )

    val reply = SwipeAction(
        icon = painterResource(id = R.drawable.reply_o),
        background = colorScheme.tertiary.copy(alpha = .5f),
        onSwipe = { }
    )

    val snooze = SwipeAction(
        icon = painterResource(id = R.drawable.reply_all),
        background = colorScheme.tertiary.copy(alpha = .5f),
        isUndo = true,
        onSwipe = { },
    )

    SwipeableActionsBox(
        startActions = listOf(star, reply),
        endActions = listOf(snooze)
    ) {
        Column(
            Modifier
                .background(colorScheme.primary.copy(alpha = .7f))
        ) {
            DirectMessage(ui.directMessage)
            Boosted(ui.boostedBy)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Avatar(52.dp, "https://placekitten.com/301/300")
                Column(Modifier.padding(start = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            modifier = Modifier.padding(bottom = 4.dp),
                            text = ui.displayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(text = "${ui.timePosted}", fontSize = 18.sp)
                    }
                    Text(text = ui.userName, fontSize = 14.sp)
                }
            }
            Row(Modifier) {
                Column {
                    val parseAsMastodonHtml = ui.content.parseAsMastodonHtml()
                    println(parseAsMastodonHtml)
                    val prettyText = setClickableText(
                        parseAsMastodonHtml,
                        ui.self!!.mentions ?: emptyList(),
                        ui.self.tags,
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
                    val text = prettyText.toAnnotatedString(colorScheme.tertiary)
                    ClickableText( style = TextStyle.Default.copy(color = colorScheme.secondary),
                        modifier = Modifier.padding(horizontal = 8.dp), text = text,
                        onClick = {
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
                    ui.imageUrl?.let { ContentImage(it) }
                    ButtonBar(1, 2)
                    Divider(Modifier.padding(12.dp), color = Color.Gray.copy(alpha = .5f))
                }
            }
        }
    }
}

