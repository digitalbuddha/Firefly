package com.androiddev.social.timeline.ui

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.pullrefresh.PullRefreshState
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
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.androiddev.social.theme.PaddingSize1
import com.androiddev.social.timeline.data.FeedType
import com.androiddev.social.timeline.ui.model.UI
import kotlinx.coroutines.delay
import java.net.URI

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TagScreen(
    navController: NavHostController,
    code: String,
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
    val uriPresenter = remember { component.urlPresenter().get() }
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
    LaunchedEffect(key1 = tag) {
        uriPresenter.start()
    }
    OpenHandledUri(uriPresenter, navController, code)

    val pullRefreshState = rememberPullRefreshState(false, {
        homePresenter.handle(TimelinePresenter.Load(feedType, colorScheme = colorScheme))
    })

    val items = homePresenter.model.hashtagStatuses?.collectAsLazyPagingItems()
    val bottomState: ModalBottomSheetState =
        rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val bottomSheetContentProvider = remember { BottomSheetContentProvider(bottomState) }

    ModalBottomSheetLayout(
        sheetState = bottomState,
        sheetShape = RoundedCornerShape(topStart = PaddingSize1, topEnd = PaddingSize1),
        sheetContent = {
            BottomSheetContent(
                bottomSheetContentProvider = bottomSheetContentProvider,
                onShareStatus = {},
                onDelete = { statusId->
                    submitPresenter.handle(SubmitPresenter.DeleteStatus(statusId))
                },
                onMessageSent = { newMessage ->
                    submitPresenter.handle(newMessage.toSubmitPostMessage())
                },
                goToProfile = goToProfile,
                goToTag = goToTag,
                goToConversation = {},
                onMuteAccount = {
                    submitPresenter.handle(SubmitPresenter.MuteAccount(it, true))
                },
                onBlockAccount = {
                    submitPresenter.handle(SubmitPresenter.BlockAccount(it, true))
                },
            )
        },
    ) {
        ScaffoldParent(
            pullRefreshState = pullRefreshState,
            showBackBar = showBackBar,
            navController = navController,
            tag = tag,
            items = items,
            goToBottomSheet = bottomSheetContentProvider::showContent,
            goToProfile = goToProfile,
            goToTag = goToTag,
            homePresenter = homePresenter,
            submitPresenter = submitPresenter,
            goToConversation = goToConversation,
            onOpenURI = { uri, type ->
                uriPresenter.handle(UriPresenter.Open(uri, type))
            },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
private fun ScaffoldParent(
    pullRefreshState: PullRefreshState,
    showBackBar: Boolean,
    navController: NavHostController,
    tag: String,
    items: LazyPagingItems<UI>?,
    goToBottomSheet: suspend (SheetContentState) -> Unit,
    goToProfile: (String) -> Unit,
    goToTag: (String) -> Unit,
    homePresenter: TimelinePresenter,
    submitPresenter: SubmitPresenter,
    goToConversation: (UI) -> Unit,
    onOpenURI: (URI, FeedType) -> Unit,
) {
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
                goToBottomSheet = goToBottomSheet,
                goToProfile,
                goToTag,
                items,
                currentAccount = homePresenter.model.currentAccount,
                replyToStatus = {
                    submitPresenter.handle(it.toSubmitPostMessage())
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
                { false },
                goToConversation = goToConversation,
                onProfileClick = { _, _ -> },
                lazyListState,
                onVote = { statusId, pollId, choices ->
                    submitPresenter.handle(SubmitPresenter.VotePoll(statusId, pollId, choices))
                },
                onOpenURI = onOpenURI,
            )
        }

    }
}
