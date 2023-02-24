package com.androiddev.social.timeline.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.androiddev.social.AuthRequiredComponent
import com.androiddev.social.UserComponent
import com.androiddev.social.theme.BottomBarElevation
import com.androiddev.social.theme.PaddingSize2
import com.androiddev.social.theme.PaddingSize8
import com.androiddev.social.theme.PaddingSizeNone
import com.androiddev.social.timeline.data.StatusDB
import com.androiddev.social.timeline.data.mapStatus
import dev.marcellogalhardo.retained.compose.retain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun TimelineScreen(userComponent: UserComponent) {
    val context = LocalContext.current
    val component =
        retain { (userComponent as AuthRequiredComponent.ParentComponent).createAuthRequiredComponent() } as AuthRequiredInjector
    val homePresenter = component.homePresenter()
    val avatarPresenter = component.avatarPresenter()

    LaunchedEffect(key1 = "start") {
        homePresenter.start()
    }
    LaunchedEffect(key1 = "start") {
        avatarPresenter.start()
    }
    val state = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
    )
    var replying by remember { mutableStateOf(false) }
    if (!state.isVisible) replying = false

    Scaffold(
        bottomBar = {
            AnimatedVisibility(!replying, enter = fadeIn(), exit = fadeOut()) {
                BottomAppBar(
                    modifier = Modifier.height(PaddingSize8),
                    contentPadding = PaddingValues(PaddingSizeNone, PaddingSizeNone),
                    elevation = BottomBarElevation,
                    backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = .5f),
                ) {
                    BottomBar()
                }
            }

        },
        floatingActionButtonPosition = FabPosition.Center,
        isFloatingActionButtonDocked = true,
        floatingActionButton = {
            val scope = rememberCoroutineScope()
            if (!state.isVisible) {
                FAB(MaterialTheme.colorScheme) {
                    replying = true
                    scope.launch {
                        state.show()
                    }
                }
            }
        }
    ) { padding ->
        Box {
            ModalBottomSheetLayout(
                sheetBackgroundColor = MaterialTheme.colorScheme.surface.copy(
                    alpha = .5f
                ),
                sheetElevation = PaddingSize2,
                sheetState = state,
                sheetContent = {
                    var done by remember { mutableStateOf(false) }
                    if(done) {
                        LaunchedEffect(Unit) {
                            state.hide()
                        }
                    }
                    UserInput(
                        onMessageSent = {
                            homePresenter.handle(HomePresenter.PostMessage(it))
                            done = true
                        },
                        modifier = Modifier.padding(bottom = 0.dp)
                    )
                }) {
                timelineScreen(homePresenter.events, homePresenter.model.statuses)
            }
            TopAppBar(
                modifier = Modifier.height(60.dp),
                backgroundColor = MaterialTheme.colorScheme.surface.copy(
                    alpha = .9f
                ),

                title = {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LaunchedEffect(key1 = "avatar") {
                            avatarPresenter.start()
                        }
                        LaunchedEffect(key1 = "avatar") {
                            avatarPresenter.events.tryEmit(AvatarPresenter.Load)
                        }

                        Box {
                            Profile(
                                account = avatarPresenter.model.account
                            )
                        }
                        Box(Modifier.align(Alignment.CenterVertically)) { TabSelector() }
                        NotifIcon()
                    }
                }
            )
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun timelineScreen(
    events: MutableSharedFlow<HomePresenter.HomeEvent>,
    statuses: Flow<PagingData<StatusDB>>?
) {
    LaunchedEffect(key1 = Unit) {
        events.tryEmit(HomePresenter.Load)
    }
    val items: LazyPagingItems<StatusDB>? = statuses?.collectAsLazyPagingItems()
    val refreshing = items?.loadState?.refresh is LoadState.Loading
    val pullRefreshState = rememberPullRefreshState(refreshing, {
        items?.refresh()
    })

    Box(
        Modifier
            .pullRefresh(pullRefreshState)
            .padding(top = 60.dp)
    ) {
        statuses?.let {
            TimelineRows(
                items!!
            )
        }
        CustomViewPullRefreshView(
            pullRefreshState,
            refreshTriggerDistance = 4.dp,
            isRefreshing = refreshing
        )
    }
}

@Composable
fun TimelineRows(ui: LazyPagingItems<StatusDB>) {
    LazyColumn {
        items(ui) {
            it?.mapStatus()?.let { ui ->
                TimelineCard(ui)
            }
        }
    }
}
