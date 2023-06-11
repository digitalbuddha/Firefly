@file:OptIn(ExperimentalFoundationApi::class)

package com.androiddev.social.timeline.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.MaterialTheme
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
import com.androiddev.social.theme.PaddingSize1
import com.androiddev.social.timeline.data.FeedType
import com.androiddev.social.timeline.data.mapStatus
import com.androiddev.social.timeline.data.toStatusDb
import com.androiddev.social.timeline.ui.model.ReplyType
import com.androiddev.social.timeline.ui.model.UI
import kotlinx.coroutines.flow.MutableSharedFlow

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ConversationScreen(
    navController: NavHostController, statusId: String, type: String,
    goToProfile: (String) -> Unit,
    goToTag: (String) -> Unit
) {
    val component = LocalAuthComponent.current
    val userComponent = LocalUserComponent.current

    val submitPresenter = component.submitPresenter()
    val provider = component.conversationPresenter().get()
    val presenter by remember { mutableStateOf(provider) }

    LaunchedEffect(key1 = userComponent.request()) {
        presenter.start()
    }
    LaunchedEffect(key1 = userComponent.request()) {
        submitPresenter.start()
    }
    val colorScheme = MaterialTheme.colorScheme
    LaunchedEffect(key1 = statusId, type) {
        presenter.handle(ConversationPresenter.Load(statusId, FeedType.valueOf(type), colorScheme))
    }
    val conversation = presenter.model.conversations.get(statusId)
    val after = conversation?.after?.map { it.toStatusDb(FeedType.Home).mapStatus(colorScheme) }
        ?.map { it.copy(replyType = ReplyType.CHILD) } ?: emptyList()
    val before =
        conversation?.before?.map { it.toStatusDb(FeedType.Home).mapStatus(colorScheme) }
            ?.map { it.copy(replyType = ReplyType.PARENT) } ?: emptyList()
    val status =
        listOf(conversation?.status).filterNotNull()


    val statuses = before + status + after.map { it.copy(replyType = ReplyType.CHILD) }


    val pullRefreshState = rememberPullRefreshState(false, {
        presenter.handle(ConversationPresenter.Load(statusId, FeedType.valueOf(type), colorScheme))
    })
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
                onMessageSent = { _, _, _ -> },
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
            navController = navController,
            pullRefreshState = pullRefreshState,
            before = before,
            statuses = statuses,
            presenter = presenter,
            submitPresenter = submitPresenter,
            goToBottomSheet = bottomSheetContentProvider::showContent,
            goToProfile = goToProfile,
            goToTag = goToTag
        )
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
private fun ScaffoldParent(
    navController: NavHostController,
    pullRefreshState: PullRefreshState,
    before: List<UI>,
    statuses: List<UI>,
    presenter: ConversationPresenter,
    submitPresenter: SubmitPresenter,
    goToBottomSheet: suspend (SheetContentState) -> Unit,
    goToProfile: (String) -> Unit,
    goToTag: (String) -> Unit
) {
    BackBar(navController, "Conversation")

//    AnimatedVisibility(true, enter = fadeIn(animationSpec = TweenSpec(durationMillis = 10000))) {

    Box(
        Modifier
            .pullRefresh(pullRefreshState)
            .padding(top = 56.dp)
            .background(Color.Transparent)
            .fillMaxSize()
    ) {
        val state = rememberLazyListState(initialFirstVisibleItemIndex = before.size)

        statuses.render(
            mutableSharedFlow = submitPresenter.events,
            goToBottomSheet = goToBottomSheet,
            presenter = presenter,
            goToConversation = goToNowhere,
            state = state,
            goToProfile = goToProfile,
            goToTag = goToTag,
        )

        CustomViewPullRefreshView(
            pullRefreshState, refreshTriggerDistance = 4.dp, isRefreshing = false
        )
    }
}

@Composable
private fun List<UI>.render(
    mutableSharedFlow: MutableSharedFlow<SubmitPresenter.SubmitEvent>,
    goToBottomSheet: suspend (SheetContentState) -> Unit,
    presenter: ConversationPresenter,
    goToConversation: (UI) -> Unit,
    state: LazyListState,
    goToProfile: (String) -> Unit,
    goToTag: (String) -> Unit,
) {
    val statuses = this
    var shimmer by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = this)
    {
        if (size > 1) {
//            delay(1000)
            shimmer = false

        }
    }
    LazyColumn(
        state = state,
        modifier = Modifier
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
//            .padding(top = if (addPadding) 40.dp else 0.dp)
    ) {
        items(statuses, key = { it.remoteId }) {
            card(
                modifier = Modifier
//                    .animateItemPlacement()
                   ,
                status = it,
                account = presenter.model.account,
                events = mutableSharedFlow,
                showInlineReplies = true,
                goToBottomSheet = goToBottomSheet,
                goToConversation = goToConversation,
                goToProfile = goToProfile,
                goToTag=goToTag
            )
        }

    }
}


val goToNowhere: (UI) -> Unit = { string -> string.toString() }

