package com.androiddev.social.timeline.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.androiddev.social.timeline.data.FeedType
import com.androiddev.social.timeline.data.Type
import com.androiddev.social.timeline.data.mapStatus
import com.androiddev.social.timeline.data.toStatusDb
import com.androiddev.social.timeline.ui.model.UI
import social.androiddev.firefly.R
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NotificationsScreen(
    navController: NavHostController,
    goToConversation: (UI) -> Unit,
    goToProfile: (String) -> Unit,
    goToTag: (String) -> Unit
) {
    val component = LocalAuthComponent.current
    val userComponent = LocalUserComponent.current

    val notificationPresenter = component.notificationPresenter()
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = userComponent.request()) {
        notificationPresenter.start(scope)
    }
    LaunchedEffect(key1 = userComponent.request()) {
        notificationPresenter.handle(NotificationPresenter.Load)
    }
    val statuses = notificationPresenter.model.statuses
    LaunchedEffect(key1 = userComponent.request()) {
        component.submitPresenter().start()
    }
    val pullRefreshState = rememberPullRefreshState(false, {
        notificationPresenter.handle(NotificationPresenter.Load)
    })


    Box(
        Modifier
            .background(MaterialTheme.colorScheme.surface)
            .pullRefresh(pullRefreshState)
            .padding(top = 0.dp)
            .fillMaxSize()
    ) {
        LazyColumn(
            Modifier
                .wrapContentHeight()
                .padding(top = 60.dp)
        ) {
            itemsIndexed(items = statuses, key = { a, status -> status.id }) { index, it ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (it.realType == Type.favourite) {
                        Boosted(
                            modifier = Modifier.height(30.dp),
                            boostedBy = (if (it.account.displayName.isNullOrEmpty()) it.account.username else it.account.displayName) + " favorited",
                            boostedAvatar = it.account.avatar,
                            boostedEmojis = it.account.emojis,
                            drawable = R.drawable.star,
                                    containerColor = colorScheme.surface,
                            onClick = {
                                goToProfile(it.account.id)
                            }
                        )
                    }
                    if (it.realType == Type.reblog) {
                        Boosted(
                            modifier = Modifier.height(30.dp),
                            boostedBy = (if (it.account.displayName.isNullOrEmpty()) it.account.username else it.account.displayName) + " boosted",
                            boostedAvatar = it.account.avatar,
                            boostedEmojis = it.account.emojis,
                            containerColor = colorScheme.surface,
                            onClick = {
                                goToProfile(it.account.id)
                            },
                            drawable = R.drawable.rocket3
                        )
                    }
                    card(
                        modifier = Modifier.background(Color.Transparent),
                        status = it.status!!.toStatusDb(FeedType.Home).mapStatus(colorScheme),
                        events = component.submitPresenter().events,
                        showInlineReplies = false,
                        goToConversation = goToConversation,
                        goToProfile = goToProfile,
                        goToTag=goToTag
                    )
                }

            }
        }
    }
    CustomViewPullRefreshView(
        pullRefreshState, refreshTriggerDistance = 4.dp, isRefreshing = false
    )
    BackBar(navController, "Notifications")
}


@Composable
fun BackBar(navController: NavHostController, title: String) {
    Column {
        TopAppBar(
            backgroundColor = MaterialTheme.colorScheme.surface,
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

