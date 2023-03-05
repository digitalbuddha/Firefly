@file:OptIn(ExperimentalMaterialApi::class)

package com.androiddev.social.timeline.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.androiddev.social.timeline.data.FeedType
import com.androiddev.social.timeline.data.mapStatus
import com.androiddev.social.timeline.data.toStatusDb
import com.androiddev.social.timeline.ui.model.UI
import kotlinx.coroutines.flow.MutableSharedFlow

@ExperimentalMaterialApi
@Composable
fun After(status: UI, goToConversation: (String) -> Unit) {
    val provider = LocalAuthComponent.current.conversationPresenter().get()
    var presenter by remember { mutableStateOf(provider) }

    LaunchedEffect(key1 = status) {
        presenter.start()
    }
    LaunchedEffect(key1 = status) {
        presenter.handle(ConversationPresenter.Load(status.remoteId))
    }
    val afterStatus =
        presenter.model.conversations.get(status.remoteId)?.after
    val after =
        afterStatus?.map { it.toStatusDb(FeedType.Home).mapStatus() }


    InnerLazyColumn(after, goToConversation)
}

@ExperimentalMaterialApi
@Composable
fun Before(status: UI, goToConversation: (String) -> Unit = {}) {
    val provider = LocalAuthComponent.current.conversationPresenter().get()
    var presenter by remember { mutableStateOf(provider) }

    LaunchedEffect(key1 = status) {
        presenter.start()
    }
    LaunchedEffect(key1 = status) {
        presenter.handle(ConversationPresenter.Load(status.remoteId))
    }
    val beforeStatus =
        presenter.model.conversations.get(status.remoteId)?.before
    val after =
        beforeStatus?.map { it.toStatusDb(FeedType.Home).mapStatus() }

    InnerLazyColumn(after, goToConversation = goToConversation)
}


@Composable
fun InnerLazyColumn(
    items: List<UI>?,
    goToConversation: (String) -> Unit
) {
    val submitPresenter = LocalAuthComponent.current.submitPresenter()
    val configuration = LocalConfiguration.current

    val screenHeight = configuration.screenHeightDp
    LazyColumn(modifier = Modifier.heightIn(1.dp, max = (screenHeight * .8).dp)) {
        if (!items.isNullOrEmpty()) {
            items.take(10).forEach { inner ->
                item {
                    card(
                        Modifier.background(MaterialTheme.colorScheme.background),
                        inner,
                        submitPresenter.events,
                        goToConversation = goToConversation,
                        showInlineReplies = true
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun card(
    modifier: Modifier,
    status: UI,
    events: MutableSharedFlow<SubmitPresenter.SubmitEvent>,
    showInlineReplies: Boolean,
    goToConversation: (String) -> Unit,

) { AnimatedVisibility(true) {
    Column {
        TimelineCard(
            modifier = modifier,
            ui = status,
            replyToStatus = { content, visiblity, replyToId, replyCount, uris ->
                events.tryEmit(
                    SubmitPresenter.PostMessage(
                        content = content,
                        visibility = visiblity,
                        replyStatusId = replyToId,
                        replyCount = replyCount,
                        uris = uris
                    )
                )
            },
            boostStatus = {},
            favoriteStatus = {},
            state = null,
            isReplying = { false },
            alwaysShowButtonBar = true,
            showInlineReplies = showInlineReplies,
            goToConversation = goToConversation
        )
    }
}


}
