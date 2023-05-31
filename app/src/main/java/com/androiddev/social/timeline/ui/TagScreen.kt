package com.androiddev.social.timeline.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.androiddev.social.timeline.data.FeedType
import com.androiddev.social.timeline.ui.model.UI
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TagScreen(
    navController: NavHostController,
    tag: String,
    goToConversation: (UI) -> Unit,
    showBackBar: Boolean,
    goToProfile: (String) -> Unit,
    goToTag: (String) -> Unit
) {
    val component = LocalAuthComponent.current

    val homePresenter by remember(key1 = tag) {
        mutableStateOf(
            component.homePresenter()
        )
    }
    val submitPresenter = component.submitPresenter()
    val scope = rememberCoroutineScope()
    LaunchedEffect(key1 = tag) {
        homePresenter.start(scope)
    }

    LaunchedEffect(key1 = tag) {
        submitPresenter.start()
    }
    val feedType = FeedType.Hashtag
    feedType.tagName = tag

    val colorScheme = MaterialTheme.colorScheme
    LaunchedEffect(key1 = { tag }) {

        homePresenter.handle(TimelinePresenter.Load(feedType, colorScheme = colorScheme))
    }


    val pullRefreshState = rememberPullRefreshState(false, {
        homePresenter.handle(TimelinePresenter.Load(feedType, colorScheme = colorScheme))
    })

    val items = homePresenter.model.hashtagStatuses?.collectAsLazyPagingItems()
    Column(Modifier.fillMaxSize()) {
        CustomViewPullRefreshView(
            pullRefreshState, refreshTriggerDistance = 4.dp, isRefreshing = false
        )
        if (showBackBar)
            BackBar(navController, "#$tag")
        items?.let {
            val lazyListState = rememberLazyListState()

            LaunchedEffect(key1 = tag) {
                //very unexact way to run after the first append/prepend ran
                //otherwise infinite scroll never calls append on first launch
                // and I have no idea why
                delay(200)
                if (it.itemCount == 0) items.refresh()
            }
            TimelineRows(
                goToProfile,
                goToTag,
                items,
                replyToStatus = { content, visiblity, replyToId, replyCount, uris ->
                    submitPresenter.handle(
                        SubmitPresenter.PostMessage(
                            content = content,
                            visibility = visiblity,
                            replyStatusId = replyToId,
                            replyCount = replyCount,
                            uris = uris
                        )
                    )
                },
                boostStatus = { statusId, boosted ->
                    submitPresenter.handle(
                        SubmitPresenter
                            .BoostMessage(statusId, FeedType.Hashtag, boosted)
                    )
                },
                favoriteStatus = { statusId, favourited ->
                    submitPresenter.handle(
                        SubmitPresenter
                            .FavoriteMessage(statusId, FeedType.Hashtag, favourited)
                    )
                },
                rememberModalBottomSheetState(
                    ModalBottomSheetValue.Hidden,
                    SwipeableDefaults.AnimationSpec,
                    skipHalfExpanded = true
                ),
                { false },
                goToConversation = goToConversation,
                onProfileClick = { _, _ -> },
                lazyListState
            )
        }

    }
}


