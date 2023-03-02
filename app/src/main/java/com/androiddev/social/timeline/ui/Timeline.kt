package com.androiddev.social.timeline.ui

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.androiddev.social.AuthRequiredComponent
import com.androiddev.social.UserComponent
import com.androiddev.social.theme.BottomBarElevation
import com.androiddev.social.theme.PaddingSize2
import com.androiddev.social.theme.PaddingSize8
import com.androiddev.social.theme.PaddingSizeNone
import com.androiddev.social.timeline.data.FeedType
import com.androiddev.social.timeline.data.StatusDB
import com.androiddev.social.timeline.data.mapStatus
import dev.marcellogalhardo.retained.compose.retain
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

val LocalAuthComponent = compositionLocalOf<AuthRequiredInjector> { error("No component found!") }

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun TimelineScreen(userComponent: UserComponent,  onChangeTheme: () -> Unit ) {
    val context = LocalContext.current
    val component =
        retain { (userComponent as AuthRequiredComponent.ParentComponent).createAuthRequiredComponent() } as AuthRequiredInjector
    CompositionLocalProvider(LocalAuthComponent provides component) {

        val homePresenter = component.homePresenter()
        val submitPresenter = component.submitPresenter()
        val avatarPresenter = component.avatarPresenter()
        val scope = rememberCoroutineScope()
        LaunchedEffect(key1 = "start") {
            homePresenter.start(scope)
        }
        LaunchedEffect(key1 = "start") {
            avatarPresenter.start()
        }
        LaunchedEffect(key1 = "start") {
            submitPresenter.start()
        }
        val state = rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
        )
        var replying by remember { mutableStateOf(false) }
        var tabToLoad: FeedType by remember { mutableStateOf(FeedType.Home) }
        if (!state.isVisible) replying = false

        Scaffold(
            backgroundColor = Color.Transparent,
            bottomBar = {
                AnimatedVisibility(!replying, enter = fadeIn(), exit = fadeOut()) {
                    BottomAppBar(
                        modifier = Modifier.height(PaddingSize8),
                        contentPadding = PaddingValues(PaddingSizeNone, PaddingSizeNone),
                        elevation = BottomBarElevation,
                        backgroundColor = MaterialTheme.colorScheme.surface,
                    ) {
                        BottomBar()
                    }
                }

            },
            floatingActionButtonPosition = FabPosition.Center,
            isFloatingActionButtonDocked = true,
            floatingActionButton = {
                val scope = rememberCoroutineScope()
                if (!replying) {
                    FAB(MaterialTheme.colorScheme) {
                        replying = true
                        scope.launch {
                            state.show()
                        }
                    }
                }
            }) { padding ->
            Box {
                ModalBottomSheetLayout(sheetBackgroundColor = MaterialTheme.colorScheme.surface.copy(
                    alpha = .5f
                ),
                    sheetElevation = PaddingSize2,
                    sheetState = state,
                    sheetContent = {
                        var done by remember { mutableStateOf(false) }
                        if (done) {
                            LaunchedEffect(Unit) {
                                state.hide()
                            }
                        }
                        UserInput(
                            modifier = Modifier.padding(bottom = 0.dp),
                            onMessageSent = { it, visibility, uris->
                                submitPresenter.handle(
                                    SubmitPresenter.PostMessage(
                                        content = it,
                                        visibility = visibility,
                                        uris = uris
                                    )
                                )
                                done = true
                            },
                            participants = "",
                            status = null,
                            showReplies = false
                        )
                    }) {
                    val model = homePresenter.model

                    when (tabToLoad) {
                        FeedType.Home -> {
                            timelineScreen(
                                homePresenter.events,
                                submitPresenter.events,
                                FeedType.Home,
                                items = model.homeStatuses?.collectAsLazyPagingItems(),
                                state
                            ) {
                                replying = it
                            }
                        }

                        FeedType.Local -> {
                            timelineScreen(
                                homePresenter.events,
                                submitPresenter.events,
                                FeedType.Local,
                                model.localStatuses?.collectAsLazyPagingItems(),
                                state
                            ) { replying = it }
                        }

                        FeedType.Federated -> {
                            timelineScreen(
                                homePresenter.events,
                                submitPresenter.events,
                                FeedType.Federated,
                                model.federatedStatuses?.collectAsLazyPagingItems(),
                                state
                            ) { replying = it }
                        }

                        FeedType.Trending -> {
                            timelineScreen(
                                homePresenter.events,
                                submitPresenter.events,
                                FeedType.Trending,
                                model.trendingStatuses?.collectAsLazyPagingItems(),
                                state
                            ) { replying = it }
                        }
                    }
                }
            }
            TopAppBar(modifier = Modifier
                .height(60.dp)
                .background(Color.Transparent),
                backgroundColor = Color.Transparent,

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
                                account = avatarPresenter.model.account,
                                onChangeTheme = onChangeTheme
                            )
                        }
                        Box(Modifier.align(Alignment.CenterVertically)) {
                            TabSelector { it ->
                                tabToLoad = when (it) {
                                    FeedType.Home.type -> {
                                        FeedType.Home
                                    }

                                    FeedType.Local.type -> {
                                        FeedType.Local
                                    }

                                    FeedType.Federated.type -> {
                                        FeedType.Federated
                                    }

                                    FeedType.Trending.type -> {
                                        FeedType.Trending
                                    }

                                    else -> {
                                        FeedType.Home
                                    }
                                }
                            }
                        }
                        NotifIcon()
                    }
                })
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun timelineScreen(
    events: MutableSharedFlow<TimelinePresenter.HomeEvent>,
    submitEvents: MutableSharedFlow<SubmitPresenter.SubmitEvent>,
    tabToLoad: FeedType,
    items: LazyPagingItems<StatusDB>?,
    state: ModalBottomSheetState,
    isReplying: (Boolean) -> Unit
) {
    LaunchedEffect(key1 = tabToLoad) {
        events.tryEmit(TimelinePresenter.Load(tabToLoad))
    }

    val refreshing = items?.loadState?.refresh is LoadState.Loading
    val pullRefreshState = rememberPullRefreshState(refreshing, {
        items?.refresh()
    })

    Box(
        Modifier
            .pullRefresh(pullRefreshState)
            .padding(top = 60.dp)
            .background(MaterialTheme.colorScheme.surface)
            .fillMaxSize()
    ) {
        items?.let {
            LaunchedEffect(key1 = tabToLoad) {
                //very unexact way to run after the first append/prepend ran
                //otherwise infinite scroll never calls append on first launch
                // and I have no idea why
                delay(200)
                items.refresh()
            }
            TimelineRows(
                items,
                replyToStatus = { content, visiblity, replyToId, replyCount, uris ->
                    submitEvents.tryEmit(
                        SubmitPresenter.PostMessage(
                            content = content,
                            visibility = visiblity,
                            replyStatusId = replyToId,
                            replyCount = replyCount,
                            uris = uris
                        )
                    )
                },
                {
                    events.tryEmit(
                        TimelinePresenter
                            .BoostMessage(it)
                    )
                },
                {
                    events.tryEmit(
                        TimelinePresenter
                            .FavoriteMessage(it)
                    )
                },
                state,
                isReplying
            )
        }
        CustomViewPullRefreshView(
            pullRefreshState, refreshTriggerDistance = 4.dp, isRefreshing = refreshing
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TimelineRows(
    ui: LazyPagingItems<StatusDB>,
    replyToStatus: (String, String, String, Int, Set<Uri>) -> Unit,
    boostStatus: (String) -> Unit,
    favoriteStatus: (String) -> Unit,
    state: ModalBottomSheetState,
    isReplying: (Boolean) -> Unit
) {
    LazyColumn {
        items(items = ui, key = { "${it.originalId}  ${it.reblogsCount} ${it.repliesCount}" }) {
            it?.mapStatus()?.let { ui ->
                TimelineCard(ui, replyToStatus, boostStatus, favoriteStatus, state, isReplying)
            }
        }
    }
}
