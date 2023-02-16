package com.androiddev.social.timeline.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.items
import com.androiddev.social.R
import com.androiddev.social.timeline.ui.model.UI
import com.androiddev.social.timeline.ui.theme.Purple50
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox

@Composable
fun Timeline(ui: LazyPagingItems<UI>) {
    LazyColumn {
        items(ui) {
            it?.let { TimelineCard(ui = it) }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TimelineCard(ui: UI) {
    val rocket = SwipeAction(
        icon = {
            Image(
                modifier = Modifier.size(80.dp),
                painter = painterResource(R.drawable.rocket3),
                contentDescription = "",
                colorFilter = ColorFilter.tint(colorScheme.tertiary)
            )
        },
        background = Purple50,
        onSwipe = { }
    )
    val reply = SwipeAction(
        icon = {
            Image(
                modifier = Modifier.size(80.dp),
                painter = painterResource(R.drawable.reply),
                contentDescription = "",
                colorFilter = ColorFilter.tint(colorScheme.tertiary)
            )
        },
        background = Purple50,
        onSwipe = { }
    )

    val replyAll = SwipeAction(
        icon = {
            Image(
                modifier = Modifier.size(80.dp),
                painter = painterResource(R.drawable.reply_all),
                contentDescription = "",
                colorFilter = ColorFilter.tint(colorScheme.tertiary)
            )
        },
        background = Purple50,
        isUndo = true,
        onSwipe = { },
    )

    SwipeableActionsBox(
        startActions = listOf(rocket),
        endActions = listOf(reply, replyAll)
    ) {
        Column(
            Modifier
                .background(colorScheme.primary.copy(alpha = .7f))
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            DirectMessage(ui.directMessage)
            Boosted(ui.boostedBy)
            UserInfo(ui)
            ContentRow(ui)
        }
    }
}

@Composable
private fun UserInfo(ui: UI) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        ui.avatar?.let { Image(52.dp, it) }
        ui.emojis?.let {
            val (inlineContentMap, text) = inlineEmojis(
                ui.displayName,
                it
            )

            Column(Modifier.padding(start = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        color = colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 4.dp),
                        text = text,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        inlineContent = inlineContentMap
                    )
                    Text(color = colorScheme.secondary, text = ui.timePosted, fontSize = 18.sp)
                }
                Text(color = colorScheme.secondary, text = ui.userName, fontSize = 14.sp)
            }

        }


    }
}
