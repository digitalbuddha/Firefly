@file:OptIn(ExperimentalMaterialApi::class)

package com.androiddev.social.timeline.ui

import androidx.compose.foundation.background
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
import com.androiddev.social.timeline.data.Status
import com.androiddev.social.timeline.data.mapStatus
import com.androiddev.social.timeline.data.toStatusDb
import com.androiddev.social.timeline.ui.model.UI
import kotlinx.coroutines.flow.MutableSharedFlow

@ExperimentalMaterialApi
@Composable
fun Conversation(status: UI) {
    val provider = LocalAuthComponent.current.conversationPresenter().get()
    var presenter by remember { mutableStateOf(provider) }
    val submitPresenter = LocalAuthComponent.current.submitPresenter()

    LaunchedEffect(key1 = status) {
        presenter.start()
    }
    LaunchedEffect(key1 = status) {
        presenter.handle(ConversationPresenter.Load(status.remoteId))
    }
    val afterStatus: Map<String, List<Status>> =
        presenter.model.after
    val after: List<UI>? =
        afterStatus[status.remoteId]?.map { it.toStatusDb(FeedType.Home).mapStatus() }
    val configuration = LocalConfiguration.current

    val screenHeight = configuration.screenHeightDp
    LazyColumn(modifier = Modifier.heightIn(1.dp, max = (screenHeight*.8).dp)) {
        if (!after.isNullOrEmpty()) {
//            Replies()
            after.take(10).forEach { inner ->
                item {
                    if (status == inner) {
                        card(
                            Modifier.background(MaterialTheme.colorScheme.background.copy(alpha = .5f)),
                            inner,
                            submitPresenter.events
                        )
                    } else {
                        card(Modifier, inner, submitPresenter.events)
                    }
                }
            }
        }
    }
}


@Composable
fun card(
    modifier: Modifier,
    status: UI,
    events: MutableSharedFlow<SubmitPresenter.SubmitEvent>
) {
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
    )
}
