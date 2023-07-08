package com.androiddev.social.timeline.ui

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.androiddev.social.theme.PaddingSize1
import com.androiddev.social.timeline.data.FeedType
import com.androiddev.social.timeline.ui.model.UI
import kotlinx.coroutines.flow.MutableSharedFlow
import java.net.URI

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ConversationScreen(
    navController: NavHostController, statusId: String, type: String,
    code: String,
    goToConversation: (UI) -> Unit,
    goToProfile: (String) -> Unit,
    goToTag: (String) -> Unit,
) {
    val component = LocalAuthComponent.current
    val userComponent = LocalUserComponent.current

    val submitPresenter = component.submitPresenter()
    val presenter by remember { mutableStateOf(component.conversationPresenter().get()) }

    LaunchedEffect(key1 = userComponent.request()) {
        presenter.start()
    }
    LaunchedEffect(key1 = userComponent.request()) {
        submitPresenter.start()
    }
    val colorScheme = MaterialTheme.colorScheme
    LaunchedEffect(key1 = statusId, type) {
        presenter.handle(
            ConversationPresenter.Load(
                statusId, FeedType.valueOf(type), colorScheme,
            )
        )
    }
    val uriPresenter = remember { component.urlPresenter().get() }
    LaunchedEffect(key1 = statusId, type) {
        uriPresenter.start()
    }
    OpenHandledUri(uriPresenter, navController, code)

    val conversation = presenter.model.conversations.get(statusId)
    val after = conversation?.after ?: emptyList()
    val before = conversation?.before ?: emptyList()
    val status = listOfNotNull(conversation?.status)

    val statuses = before + status + after

    val pullRefreshState = rememberPullRefreshState(false, {
        presenter.handle(
            ConversationPresenter.Load(statusId, FeedType.valueOf(type), colorScheme)
        )
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
                    submitPresenter.handle(
                        SubmitPresenter.MuteAccount(it, true)
                    )
                },
                onBlockAccount = {
                    submitPresenter.handle(
                        SubmitPresenter.BlockAccount(it, true)
                    )
                },
            )
        },
    ) {
        ScaffoldParent(
            navController = navController,
            pullRefreshState = pullRefreshState,
            mainConversationStatusId = statusId,
            before = before,
            statuses = statuses,
            presenter = presenter,
            submitPresenter = submitPresenter,
            uriPresenter = uriPresenter,
            goToConversation = goToConversation,
            goToBottomSheet = bottomSheetContentProvider::showContent,
            goToProfile = goToProfile,
            goToTag = goToTag,
        )
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
private fun ScaffoldParent(
    navController: NavHostController,
    pullRefreshState: PullRefreshState,
    mainConversationStatusId: String,
    before: List<UI>,
    statuses: List<UI>,
    presenter: ConversationPresenter,
    submitPresenter: SubmitPresenter,
    uriPresenter: UriPresenter,
    goToConversation: (UI) -> Unit,
    goToBottomSheet: suspend (SheetContentState) -> Unit,
    goToProfile: (String) -> Unit,
    goToTag: (String) -> Unit,
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
            mainConversationStatusId = mainConversationStatusId,
            presenter = presenter,
            goToConversation = goToConversation,
            state = state,
            goToProfile = goToProfile,
            goToTag = goToTag,
            onOpenURI = { uri, type ->
                uriPresenter.handle(UriPresenter.Open(uri, type))
            },
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
    mainConversationStatusId: String,
    presenter: ConversationPresenter,
    goToConversation: (UI) -> Unit,
    state: LazyListState,
    goToProfile: (String) -> Unit,
    goToTag: (String) -> Unit,
    onOpenURI: (URI, FeedType) -> Unit,
) {
    val statuses = this

    LazyColumn(
        state = state,
        modifier = Modifier
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        items(statuses, key = { it.remoteId }) {
            card(
                modifier = Modifier,
                status = it,
                account = presenter.model.account,
                mainConversationStatusId = mainConversationStatusId,
                events = mutableSharedFlow,
                goToBottomSheet = goToBottomSheet,
                goToConversation = goToConversation,
                goToProfile = goToProfile,
                goToTag = goToTag,
                onOpenURI = onOpenURI,
            )
        }

    }
}
