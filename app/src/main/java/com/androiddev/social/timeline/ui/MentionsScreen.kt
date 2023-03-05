package com.androiddev.social.timeline.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.androiddev.social.timeline.data.FeedType
import com.androiddev.social.timeline.data.mapStatus
import com.androiddev.social.timeline.data.toStatusDb

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MentionsScreen(navController: NavHostController, goToConversation: (String) -> Unit) {
    val component = LocalAuthComponent.current
    val userComponent = LocalUserComponent.current

    val mentionsPresenter = component.mentionsPresenter()
    LaunchedEffect(key1 = userComponent.request()) {
        mentionsPresenter.start()
    }
    LaunchedEffect(key1 = userComponent.request()) {
        component.mentionsPresenter().handle(MentionsPresenter.Load)
    }
    val statuses = mentionsPresenter.model.statuses.map { it.toStatusDb(FeedType.Home).mapStatus() }
    LaunchedEffect(key1 = userComponent.request()) {
        component.submitPresenter().start()
    }

    val pullRefreshState = rememberPullRefreshState(false, {
        component.mentionsPresenter().handle(MentionsPresenter.Load)
    })
    Box(
        Modifier
            .background(MaterialTheme.colorScheme.surface)
            .pullRefresh(pullRefreshState)
            .padding(top = 56.dp)
            .fillMaxSize()
    ) {

        LazyColumn(
            Modifier
                .wrapContentHeight()
                .padding(top = 0.dp)) {
            items(statuses, key = { it.remoteId }) {
                card(
                    modifier = Modifier.background(Color.Transparent),
                    status = it,
                    events = component.submitPresenter().events,
                    goToConversation = goToConversation,
                    showInlineReplies = false
                )
            }
        }
    }
    CustomViewPullRefreshView(
        pullRefreshState, refreshTriggerDistance = 4.dp, isRefreshing = false
    )
    BackBar(navController, "Mentions")
}


