package com.androiddev.social.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.androiddev.social.R
import com.androiddev.social.ui.model.UI
import com.androiddev.social.ui.theme.EbonyTheme
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox

@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EbonyTheme {
                Scaffold(
                    topBar = {
                        SmallTopAppBar(
                            modifier = Modifier.background(Color.Red),
                            colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
                            title = {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box() { Profile("FriendlyMike") }
                                    Box(Modifier.align(Alignment.CenterVertically)) { TabSelector() }
                                    Search()
                                }
                            }
                        )
                    },
                    floatingActionButtonPosition = FabPosition.End,
                    floatingActionButton = {
                        SmallFloatingActionButton(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape),
                            content = {
                                Image(
                                    modifier = Modifier.size(40.dp),
                                    painter = painterResource(R.drawable.elephant),
                                    contentDescription = "",
                                    colorFilter = ColorFilter.tint(Color.White),
                                )
                            },
                            onClick = { /* fab click handler */ }
                        )
                    },
                    content = {
                        Column(Modifier.padding(paddingValues = it)) {
                            Timeline()
                        }
                    },
                )
            }
        }
    }
}

@Composable
fun Timeline() {
    LazyColumn {
        item { TimelineCard(UI()) }
        item { TimelineCard(UI()) }
        item { TimelineCard(UI()) }
        item { TimelineCard(UI()) }
        item { TimelineCard(UI()) }
        item { TimelineCard(UI()) }
        item { TimelineCard(UI()) }
    }

}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TimelineCard(ui: UI) {
    val star = SwipeAction(
        icon = painterResource(id = R.drawable.star),
        background = MaterialTheme.colorScheme.tertiary.copy(alpha = .5f),
        onSwipe = { }
    )

    val reply = SwipeAction(
        icon = painterResource(id = R.drawable.reply_o),
        background = MaterialTheme.colorScheme.tertiary.copy(alpha = .5f),
        onSwipe = { }
    )

    val snooze = SwipeAction(
        icon = painterResource(id = R.drawable.reply_all),
        background = MaterialTheme.colorScheme.tertiary.copy(alpha = .5f),
        isUndo = true,
        onSwipe = { },
    )

    SwipeableActionsBox(
        startActions = listOf(star, reply),
        endActions = listOf(snooze)
    ) {
        Row(Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = .6f))) {
            Column {
                Row(Modifier.padding(start = 20.dp, end = 12.dp)) {
                    Avatar(60.dp, "https://placekitten.com/301/300")
                }
            }
            Column {
                Column {
                    Text(ui.displayName, fontWeight = FontWeight.Bold)
                    LinkifyText2(ui.content.toString())
                    ButtonBar()
                    Divider(Modifier.padding(12.dp), color = Color.Gray.copy(alpha = .5f))

                }
            }
        }
    }
}


