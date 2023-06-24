package com.androiddev.social.timeline.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.androiddev.social.theme.PaddingSize1
import com.androiddev.social.timeline.data.FeedType
import com.androiddev.social.timeline.data.mapStatus
import com.androiddev.social.timeline.data.toStatusDb
import com.androiddev.social.timeline.ui.model.CardUI
import com.androiddev.social.timeline.ui.model.UI

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MentionsScreen(
    navController: NavHostController,
    goToConversation: (UI) -> Unit,
    showBackBar: Boolean,
    goToProfile: (String) -> Unit,
    goToTag: (String) -> Unit,
    onOpenCard: (CardUI) -> Unit,
) {
    val component = LocalAuthComponent.current
    val userComponent = LocalUserComponent.current

    val mentionsPresenter = component.mentionsPresenter()
    val submitPresenter = component.submitPresenter()
    LaunchedEffect(key1 = userComponent.request()) {
        mentionsPresenter.start()
    }
    LaunchedEffect(key1 = userComponent.request()) {
        mentionsPresenter.handle(MentionsPresenter.Load)
    }
    val statuses = mentionsPresenter.model.statuses.map {
        it.toStatusDb(FeedType.Home).mapStatus(MaterialTheme.colorScheme)
    }
    LaunchedEffect(key1 = userComponent.request()) {
        submitPresenter.start()
    }

    val pullRefreshState = rememberPullRefreshState(false, {
        component.mentionsPresenter().handle(MentionsPresenter.Load)
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
            pullRefreshState = pullRefreshState,
            statuses = statuses,
            mentionsPresenter = mentionsPresenter,
            submitPresenter = submitPresenter,
            goToBottomSheet = bottomSheetContentProvider::showContent,
            goToConversation = goToConversation,
            goToProfile = goToProfile,
            goToTag = goToTag,
            showBackBar = showBackBar,
            navController = navController,
            onOpenCard = onOpenCard,
        )
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
private fun ScaffoldParent(
    pullRefreshState: PullRefreshState,
    statuses: List<UI>,
    mentionsPresenter: MentionsPresenter,
    submitPresenter: SubmitPresenter,
    goToBottomSheet: suspend (SheetContentState) -> Unit,
    goToConversation: (UI) -> Unit,
    goToProfile: (String) -> Unit,
    goToTag: (String) -> Unit,
    onOpenCard: (CardUI) -> Unit,
    showBackBar: Boolean,
    navController: NavHostController,
) {
    Box(
        Modifier
            .background(MaterialTheme.colorScheme.surface)
            .pullRefresh(pullRefreshState)
            .padding(top = 56.dp)
            .fillMaxSize()
    ) {

        LazyColumn(
            Modifier
                .wrapContentHeight()
                .padding(top = 0.dp)
        ) {
            items(statuses, key = { it.remoteId }) {
                card(
                    modifier = Modifier.background(Color.Transparent),
                    status = it,
                    account = mentionsPresenter.model.account,
                    events = submitPresenter.events,
                    goToBottomSheet = goToBottomSheet,
                    goToConversation = goToConversation,
                    goToProfile = goToProfile,
                    goToTag = goToTag,
                    onOpenCard = onOpenCard,
                )
            }
        }
    }
    CustomViewPullRefreshView(
        pullRefreshState, refreshTriggerDistance = 4.dp, isRefreshing = false
    )
    if (showBackBar) {
        BackBar(navController, "Mentions")
    }
}


