package com.androiddev.social.timeline.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.androiddev.social.timeline.data.FeedType
import com.androiddev.social.timeline.data.Type
import com.androiddev.social.timeline.data.mapStatus
import com.androiddev.social.timeline.data.toStatusDb
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material3.placeholder
import com.google.accompanist.placeholder.material3.shimmer
import social.androiddev.R

@Composable
fun NotificationsScreen(navController: NavHostController) {
    val component = LocalAuthComponent.current
    val userComponent = LocalUserComponent.current

    val notificationPresenter = component.notificationPresenter()
    LaunchedEffect(key1 = userComponent.request()) {
        notificationPresenter.start()
    }
    LaunchedEffect(key1 = userComponent.request()) {
        notificationPresenter.handle(NotificationPresenter.Load)
    }
    val statuses = notificationPresenter.model.statuses
    LaunchedEffect(key1 = userComponent.request()) {
        component.submitPresenter().start()
    }
    BackBar(navController, "Notifications")
    LazyColumn(
        Modifier
            .wrapContentHeight()
            .padding(top = 60.dp)
    ) {
        if (statuses.isEmpty()) {
            items(5) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(16.dp)
                        .placeholder(
                            visible = true,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                ) {

                }
            }
        } else {
            items(statuses) {
                Column {
                    if (it.realType == Type.favourite) {
                        Boosted(
                            boostedBy = (if (it.account.displayName.isNullOrEmpty()) it.account.username else it.account.displayName)+" favorited",
                            boostedAvatar = it.account.avatar,
                            boostedEmojis = it.account.emojis,
                            drawable = R.drawable.star
                        )
                    }
                    if (it.realType == Type.reblog) {
                        Boosted(
                            boostedBy = (if (it.account.displayName.isNullOrEmpty()) it.account.username else it.account.displayName)+" boosted",
                            boostedAvatar = it.account.avatar,
                            boostedEmojis = it.account.emojis,
                            drawable = R.drawable.rocket3
                        )
                    }
                    card(
                        modifier = Modifier,
                        status = it.status!!.toStatusDb(FeedType.Home).mapStatus(),
                        events = component.submitPresenter().events
                    )
                }

            }
        }
    }

}

@Composable
fun BackBar(navController: NavHostController, title: String) {
    Column {
        TopAppBar(
            backgroundColor = MaterialTheme.colorScheme.surface.copy(
                alpha = .9f
            ),
            title = { Text(text = title, color = MaterialTheme.colorScheme.onSurface) },
            navigationIcon = if (navController.previousBackStackEntry != null) {
                {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            tint = MaterialTheme.colorScheme.onSurface,
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "search"
                        )
                    }
                }
            } else {
                null
            }
        )
    }
}

