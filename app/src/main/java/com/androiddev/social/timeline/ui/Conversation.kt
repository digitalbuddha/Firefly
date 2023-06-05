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
fun After(
    status: UI, goToConversation: (UI) -> Unit,
    goToProfile: (String) -> Unit,
    goToTag: (String) -> Unit,
) {
    val provider = LocalAuthComponent.current.conversationPresenter().get()
    var presenter by remember { mutableStateOf(provider) }

    LaunchedEffect(key1 = status) {
        presenter.start()
    }
    val colorScheme = MaterialTheme.colorScheme
    LaunchedEffect(key1 = status) {
        presenter.handle(ConversationPresenter.Load(status.remoteId, status.type, colorScheme))
    }
    val afterStatus =
        presenter.model.conversations.get(status.remoteId)?.after
    val after =
        afterStatus?.map { it.toStatusDb(FeedType.Home).mapStatus(MaterialTheme.colorScheme) }


    InnerLazyColumn(after, goToConversation, goToProfile, goToTag)
}

@Composable
fun InnerLazyColumn(
    items: List<UI>?,
    goToConversation: (UI) -> Unit,
    goToProfile: (String) -> Unit,
    goToTag: (String) -> Unit,
) {
    val submitPresenter = LocalAuthComponent.current.submitPresenter()
    LaunchedEffect(key1 = items) {
        submitPresenter.start()
    }
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
                        showInlineReplies = true,
                        goToConversation = goToConversation,
                        goToProfile = goToProfile,
                        goToTag = goToTag,
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
    goToConversation: (UI) -> Unit,
    goToProfile: (String) -> Unit,
    goToTag: (String) -> Unit,
) {

    var eagerStatus by remember { mutableStateOf(status) }


    AnimatedVisibility(true) {
        Column {
            TimelineCard(
                goToProfile = goToProfile,
                goToTag = goToTag,
                ui = eagerStatus,
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
                    eagerStatus = eagerStatus.copy(replyCount = eagerStatus.replyCount + 1)
                },
                boostStatus = { statusId, boosted ->
                    events.tryEmit(
                        SubmitPresenter
                            .BoostMessage(statusId, status.type, boosted)
                    )
                    eagerStatus =
                        eagerStatus.copy(boostCount = eagerStatus.boostCount + 1, boosted = true)

                },
                favoriteStatus = { statusId, favourited ->
                    events.tryEmit(
                        SubmitPresenter
                            .FavoriteMessage(statusId, status.type, favourited)
                    )
                    eagerStatus = eagerStatus.copy(
                        favoriteCount = eagerStatus.favoriteCount + 1,
                        favorited = true
                    )

                },
                state = null,
                goToConversation = goToConversation,
                isReplying = { },
                showInlineReplies = showInlineReplies,
                modifier = modifier,
                onVote = { statusId, pollId, choices ->
                    events.tryEmit(SubmitPresenter.VotePoll(statusId, pollId, choices))
                },
            )
        }
    }


}
