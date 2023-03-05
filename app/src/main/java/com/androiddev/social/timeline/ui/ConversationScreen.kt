package com.androiddev.social.timeline.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.androiddev.social.timeline.data.FeedType
import com.androiddev.social.timeline.data.mapStatus
import com.androiddev.social.timeline.data.toStatusDb
import com.androiddev.social.timeline.ui.model.ReplyType
import com.androiddev.social.timeline.ui.model.UI
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material3.placeholder
import com.google.accompanist.placeholder.material3.shimmer
import kotlinx.coroutines.flow.MutableSharedFlow

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ConversationScreen(
    navController: NavHostController, statusId: String,
    goToConversation: (String) -> Unit = {}
) {
    val component = LocalAuthComponent.current
    val userComponent = LocalUserComponent.current

    val provider = component.conversationPresenter().get()
    val presenter by remember { mutableStateOf(provider) }

    LaunchedEffect(key1 = userComponent.request()) {
        presenter.start()
    }
    LaunchedEffect(key1 = userComponent.request()) {
        presenter.handle(ConversationPresenter.Load(statusId))
    }
    val conversation = presenter.model.conversations.get(statusId)
    val after = conversation?.after?.map { it.toStatusDb(FeedType.Home).mapStatus() }
        ?.map { it.copy(replyType = ReplyType.CHILD) } ?: emptyList()
    val before =
        conversation?.before?.map { it.toStatusDb(FeedType.Home).mapStatus() }
            ?.map { it.copy(replyType = ReplyType.PARENT) } ?: emptyList()
    val status =
        listOf(conversation?.status?.toStatusDb(FeedType.Home)?.mapStatus()).filterNotNull()

    var showParent by remember(statusId) { mutableStateOf(false) }


    val statuses =
        if (showParent) before + status + after.map { it.copy(replyType = ReplyType.CHILD) } else status + after

    val pullRefreshState = rememberPullRefreshState(false, {
        presenter.handle(ConversationPresenter.Load(statusId))
    })
    BackBar(navController, "Conversation")


    Box(
        Modifier
            .pullRefresh(pullRefreshState)
            .padding(top = 56.dp)
            .background(Color.Transparent)
            .fillMaxSize()
    ) {
        if (before.isNotEmpty())
            Parent(if (!showParent) "Show Full Thread" else "Show Replies Only") {
                showParent = !showParent
            }
        statuses.render(component.submitPresenter().events, goToConversation)

        CustomViewPullRefreshView(
            pullRefreshState, refreshTriggerDistance = 4.dp, isRefreshing = false
        )
    }
}


@Composable
private fun List<UI>.render(
    mutableSharedFlow: MutableSharedFlow<SubmitPresenter.SubmitEvent>,
    goToConversation: (String) -> Unit,

    ) {
    val statuses = this
    LazyColumn(
        Modifier
            .wrapContentHeight()
            .padding(top = 40.dp)
    ) {
        if (statuses.isEmpty()) {
            items(3) {
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
                card(
                    modifier = Modifier.background(if (it.replyType == ReplyType.PARENT) colorScheme.surface else if (it.replyType == ReplyType.CHILD) colorScheme.surfaceVariant else Color.Transparent),
                    status = it,
                    events = mutableSharedFlow,
                    goToConversation = goToConversation
                )
            }
        }
    }
}


