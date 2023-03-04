package com.androiddev.social.timeline.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.androiddev.social.timeline.data.FeedType
import com.androiddev.social.timeline.data.mapStatus
import com.androiddev.social.timeline.data.toStatusDb
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material3.placeholder
import com.google.accompanist.placeholder.material3.shimmer

@Composable
fun MentionsScreen(navController: NavHostController) {
    val component = LocalAuthComponent.current
    val userComponent = LocalUserComponent.current

    val mentionsPresenter = component.mentionsPresenter()
    LaunchedEffect(key1 = userComponent.request()) {
        mentionsPresenter.start()
    }
    LaunchedEffect(key1 = userComponent.request()) {
        component.mentionsPresenter().handle(MentionsPresenter.Load)
    }
    val statuses = mentionsPresenter.model.statuses
    LaunchedEffect(key1 = userComponent.request()) {
        component.submitPresenter().start()
    }
    BackBar(navController, "Mentions")

    LazyColumn(Modifier.wrapContentHeight().padding(top=60.dp)) {
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
                card(
                    modifier = Modifier,
                    status = it.toStatusDb(FeedType.Home).mapStatus(),
                    events = component.submitPresenter().events
                )
            }
        }
    }
}

