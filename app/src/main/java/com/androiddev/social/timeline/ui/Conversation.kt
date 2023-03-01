package com.androiddev.social.timeline.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.androiddev.social.timeline.data.FeedType
import com.androiddev.social.timeline.data.mapStatus
import com.androiddev.social.timeline.data.toStatusDb
import com.androiddev.social.timeline.ui.model.UI

@ExperimentalMaterialApi
@Composable
fun Conversation(statusId: String) {
    val presenter = LocalAuthComponent.current.conversationPresenter()
   LaunchedEffect(key1 = statusId){
       presenter.start()
   }
    LaunchedEffect(key1 = statusId){
        presenter.handle(ConversationPresenter.Load(statusId))
    }

    val statuses: List<UI> =
        presenter.model.statuses.map { it.toStatusDb(FeedType.Home).mapStatus() }
    Column(modifier = Modifier.animateContentSize()) {

        statuses.forEach {
            TimelineCard(
                ui = it,
                replyToStatus = { _, _, _ -> },
                boostStatus = {},
                state = null,
                isReplying = { false }
            )
        }
    }

}
