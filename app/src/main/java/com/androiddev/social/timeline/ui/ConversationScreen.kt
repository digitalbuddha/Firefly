@file:OptIn(ExperimentalFoundationApi::class)

package com.androiddev.social.timeline.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.androiddev.social.timeline.data.FeedType
import com.androiddev.social.timeline.data.mapStatus
import com.androiddev.social.timeline.data.toStatusDb
import com.androiddev.social.timeline.ui.model.ReplyType
import com.androiddev.social.timeline.ui.model.UI
import kotlinx.coroutines.flow.MutableSharedFlow

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ConversationScreen(
    navController: NavHostController, statusId: String, type: String
) {
    val component = LocalAuthComponent.current
    val userComponent = LocalUserComponent.current

    val provider = component.conversationPresenter().get()
    val presenter by remember { mutableStateOf(provider) }

    LaunchedEffect(key1 = userComponent.request()) {
        presenter.start()
    }
    LaunchedEffect(key1 = userComponent.request()) {
        component.submitPresenter().start()
    }
    LaunchedEffect(key1 = System.nanoTime()) {
        presenter.handle(ConversationPresenter.Load(statusId, FeedType.valueOf(type)))
    }
    val conversation = presenter.model.conversations.get(statusId)
    val after = conversation?.after?.map { it.toStatusDb(FeedType.Home).mapStatus() }
        ?.map { it.copy(replyType = ReplyType.CHILD) } ?: emptyList()
    val before =
        conversation?.before?.map { it.toStatusDb(FeedType.Home).mapStatus() }
            ?.map { it.copy(replyType = ReplyType.PARENT) } ?: emptyList()
    val status =
        listOf(conversation?.status).filterNotNull()


    val statuses = before + status + after.map { it.copy(replyType = ReplyType.CHILD) }


    val pullRefreshState = rememberPullRefreshState(false, {
        presenter.handle(ConversationPresenter.Load(statusId, FeedType.valueOf(type)))
    })
    BackBar(navController, "Conversation")

    AnimatedVisibility(true, enter = fadeIn(animationSpec = TweenSpec(durationMillis = 10000))) {

        Box(
            Modifier
                .pullRefresh(pullRefreshState)
                .padding(top = 56.dp)
                .background(Color.Transparent)
                .fillMaxSize()
        ) {
            statuses.render(
                component.submitPresenter().events,
                goToNowhere,
                scrollToPosition = before.size
            )

            CustomViewPullRefreshView(
                pullRefreshState, refreshTriggerDistance = 4.dp, isRefreshing = false
            )
        }
    }

}

@Composable
private fun List<UI>.render(
    mutableSharedFlow: MutableSharedFlow<SubmitPresenter.SubmitEvent>,
    goToConversation: (UI) -> Unit,
    scrollToPosition: Int,

    ) {

    val statuses = this
    val state = LazyListState(firstVisibleItemIndex = scrollToPosition)
    LazyColumn(
        state = state,
        modifier = Modifier
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
//            .padding(top = if (addPadding) 40.dp else 0.dp)
    ) {
        items(statuses) {
            card(
                modifier = Modifier
                    .animateItemPlacement(),
                status = it,
                events = mutableSharedFlow,
                showInlineReplies = true,
                goToConversation = goToConversation
            )
        }

    }
}


val goToNowhere: (UI) -> Unit = { string -> string.toString() }

