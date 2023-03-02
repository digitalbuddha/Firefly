package com.androiddev.social.timeline.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.androiddev.social.timeline.data.FeedType
import com.androiddev.social.timeline.data.Status
import com.androiddev.social.timeline.data.mapStatus
import com.androiddev.social.timeline.data.toStatusDb

@ExperimentalMaterialApi
@Composable
fun Conversation(statusId: String) {
    val provider = LocalAuthComponent.current.conversationPresenter().get()
    var presenter by remember { mutableStateOf(provider) }
    val submitPresenter = LocalAuthComponent.current.submitPresenter()

    LaunchedEffect(key1 = statusId) {
        presenter.start()
    }
    LaunchedEffect(key1 = statusId) {
        presenter.handle(ConversationPresenter.Load(statusId))
    }
    //replies only not parents
    val statuses: Map<String, List<Status>> =
        presenter.model.after

    val myModel: List<Status>? = statuses[statusId]
    Column(modifier = Modifier.animateContentSize()) {
        myModel?.forEach { status ->
            TimelineCard(
                ui = status.toStatusDb(FeedType.Home).mapStatus(),
                replyToStatus = { content, visiblity, replyToId, replyCount, uris ->
                    submitPresenter.events.tryEmit(
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
    }
}
