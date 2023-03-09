@file:OptIn(ExperimentalMaterial3Api::class)

package com.androiddev.social.timeline.ui

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.*
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.ImageLoader
import com.androiddev.social.AuthRequiredComponent
import com.androiddev.social.UserComponent
import com.androiddev.social.auth.data.AccessTokenRequest
import com.androiddev.social.theme.BottomBarElevation
import com.androiddev.social.theme.PaddingSize2
import com.androiddev.social.theme.PaddingSize8
import com.androiddev.social.theme.PaddingSizeNone
import com.androiddev.social.timeline.data.Account
import com.androiddev.social.timeline.data.FeedType
import com.androiddev.social.timeline.data.StatusDB
import com.androiddev.social.timeline.data.mapStatus
import com.androiddev.social.timeline.ui.model.UI
import com.androiddev.social.ui.Search
import dev.marcellogalhardo.retained.compose.retain
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

val LocalAuthComponent = compositionLocalOf<AuthRequiredInjector> { error("No component found!") }
val LocalUserComponent = compositionLocalOf<UserComponent> { error("No component found!") }
val LocalImageLoader = compositionLocalOf<ImageLoader> { error("No component found!") }

@OptIn(
    ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun TimelineScreen(
    accessTokenRequest: AccessTokenRequest,
    userComponent: UserComponent,
    onChangeTheme: () -> Unit,
    onNewAccount: () -> Unit,
    onProfileClick: (accountId: String, isCurrent: Boolean) -> Unit = { a, b -> },
    goToMentions: () -> Unit,
    goToNotifications: () -> Unit,
    goToConversation: (UI) -> Unit,
    goToProfile: (String) -> Unit
) {
    val component =
        retain(
            key = accessTokenRequest.domain ?: ""
        ) { (userComponent as AuthRequiredComponent.ParentComponent).createAuthRequiredComponent() } as AuthRequiredInjector
    CompositionLocalProvider(LocalAuthComponent provides component) {

        val homePresenter = component.homePresenter().get()
        val submitPresenter = component.submitPresenter()
        val avatarPresenter = component.avatarPresenter()
        val scope = rememberCoroutineScope()
        LaunchedEffect(key1 = accessTokenRequest) {
            homePresenter.start(scope)
        }
        LaunchedEffect(key1 = accessTokenRequest) {
            avatarPresenter.start()
        }
        LaunchedEffect(key1 = accessTokenRequest) {
            submitPresenter.start()
        }
        val state = rememberBottomSheetScaffoldState(
//            initialValue = ModalBottomSheetValue.Hidden,
        )
        var replying by remember { mutableStateOf(false) }
        var tabToLoad: FeedType by rememberSaveable { mutableStateOf(FeedType.Home) }
        if (!state.bottomSheetState.isExpanded) replying = false

        Scaffold(
            topBar = {
                TopAppBar(modifier = Modifier,
                    backgroundColor = MaterialTheme.colorScheme.background,

                    title = {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            LaunchedEffect(key1 = accessTokenRequest.domain) {
                                avatarPresenter.start()
                            }



                            LaunchedEffect(key1 = accessTokenRequest.domain) {
                                avatarPresenter.events.tryEmit(AvatarPresenter.Load)
                            }
                            Box {
                                AccountChooser(
                                    model = avatarPresenter.model,
                                    onChangeTheme = onChangeTheme,
                                    onNewAccount = onNewAccount,
                                    onProfileClick = onProfileClick
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
                            Search()
                        }
                    })
            },


            backgroundColor = Color.Transparent,
            bottomBar = {
                AnimatedVisibility(!replying, enter = fadeIn(), exit = fadeOut()) {
                    BottomAppBar(
                        modifier = Modifier.height(PaddingSize8),
                        contentPadding = PaddingValues(PaddingSizeNone, PaddingSizeNone),
                        elevation = BottomBarElevation,
                        backgroundColor = MaterialTheme.colorScheme.surface,
                    ) {
                        BottomBar(
                            goToMentions = goToMentions,
                            goToNotifications = goToNotifications,
                        )
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
                            state.bottomSheetState.expand()
                        }
                    }
                }
            }) { padding ->
            Box() {
                BottomSheetScaffold(
                    backgroundColor = Color.Transparent,
                    sheetElevation = PaddingSize2,
                    scaffoldState = state,
                    sheetContent = {
                        var done by remember { mutableStateOf(false) }
                        if (done) {
                            LaunchedEffect(Unit) {
                                state.bottomSheetState.collapse()
                            }
                        }
                        UserInput(
                            status = null,
                            modifier = Modifier.padding(bottom = 0.dp),
                            onMessageSent = { it, visibility, uris ->
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
                            showReplies = false,
                            goToConversation = goToConversation,
                            goToProfile = goToProfile
                        )
                    }) {
                    val model = homePresenter.model

                    when (tabToLoad) {
                        FeedType.Home -> {
                            timelineScreen(
                                goToProfile,
                                accessTokenRequest.domain,
                                homePresenter.events,
                                submitPresenter.events,
                                FeedType.Home,
                                items = model.homeStatuses?.collectAsLazyPagingItems(),
                                account = model.account,
                                state.bottomSheetState,
                                goToConversation,
                                isReplying = {
                                    replying = it
                                },
                                onProfileClick = onProfileClick

                            )
                        }

                        FeedType.Local -> {
                            timelineScreen(
                                goToProfile,
                                accessTokenRequest.domain,
                                homePresenter.events,
                                submitPresenter.events,
                                FeedType.Local,
                                model.localStatuses?.collectAsLazyPagingItems(),
                                account = model.account,
                                state.bottomSheetState,
                                goToConversation,
                                { replying = it },
                                onProfileClick
                            )
                        }

                        FeedType.Federated -> {
                            timelineScreen(
                                goToProfile,
                                accessTokenRequest.domain,
                                homePresenter.events,
                                submitPresenter.events,
                                FeedType.Federated,
                                model.federatedStatuses?.collectAsLazyPagingItems(),
                                account = model.account,
                                state.bottomSheetState,
                                goToConversation,
                                { replying = it },
                                onProfileClick,
                            )
                        }

                        FeedType.Trending -> {
                            timelineScreen(
                                goToProfile,
                                accessTokenRequest.domain,
                                homePresenter.events,
                                submitPresenter.events,
                                FeedType.Trending,
                                model.trendingStatuses?.collectAsLazyPagingItems(),
                                account = model.account,
                                state.bottomSheetState,
                                goToConversation,
                                { replying = it },
                                onProfileClick,
                            )
                        }

                        FeedType.User -> {

                        }

                        FeedType.UserWithMedia -> {}
                        FeedType.UserWithReplies -> {

                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun timelineScreen(
    goToProfile: (String) -> Unit,
    domain: String?,
    events: MutableSharedFlow<TimelinePresenter.HomeEvent>,
    submitEvents: MutableSharedFlow<SubmitPresenter.SubmitEvent>,
    tabToLoad: FeedType,
    items: LazyPagingItems<StatusDB>?,
    account: Account?,
    state: BottomSheetState,
    goToConversation: (UI) -> Unit,
    isReplying: (Boolean) -> Unit,
    onProfileClick: (accountId: String, isCurrent: Boolean) -> Unit,
) {
    LaunchedEffect(key1 = tabToLoad, key2 = domain) {
        events.tryEmit(TimelinePresenter.Load(tabToLoad))
    }

    val refreshing = items?.loadState?.refresh is LoadState.Loading
    val pullRefreshState = rememberPullRefreshState(refreshing, {
        items?.refresh()
    })

    Box(
        Modifier
            .pullRefresh(pullRefreshState)
            .fillMaxSize()
    ) {
        items?.let {
            LaunchedEffect(key1 = tabToLoad, key2 = domain) {
                //very unexact way to run after the first append/prepend ran
                //otherwise infinite scroll never calls append on first launch
                // and I have no idea why
                delay(200)
                items.refresh()
            }
            TimelineRows(
                goToProfile,
                items,
                account = account,
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
                    submitEvents.tryEmit(
                        SubmitPresenter
                            .BoostMessage(it, tabToLoad)
                    )
                },
                {
                    submitEvents.tryEmit(
                        SubmitPresenter
                            .FavoriteMessage(it, tabToLoad)
                    )
                },
                state,
                isReplying,
                goToConversation = goToConversation,
                onProfileClick = onProfileClick
            )
        }
        CustomViewPullRefreshView(
            pullRefreshState, refreshTriggerDistance = 4.dp, isRefreshing = refreshing
        )
    }
}

@Composable
fun <T : Any> LazyPagingItems<T>.rememberLazyListState(): LazyListState {
    // After recreation, LazyPagingItems first return 0 items, then the cached items.
    // This behavior/issue is resetting the LazyListState scroll position.
    // Below is a workaround. More info: https://issuetracker.google.com/issues/177245496.
    return when (itemCount) {
        // Return a different LazyListState instance.
        0 -> remember(this) { LazyListState(0, 0) }
        // Return rememberLazyListState (normal case).
        else -> androidx.compose.foundation.lazy.rememberLazyListState()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TimelineRows(
    goToProfile: (String) -> Unit,
    ui: LazyPagingItems<StatusDB>,
    account: Account?,
    replyToStatus: (String, String, String, Int, Set<Uri>) -> Unit,
    boostStatus: (String) -> Unit,
    favoriteStatus: (String) -> Unit,
    state: BottomSheetState,
    isReplying: (Boolean) -> Unit,
    goToConversation: (UI) -> Unit,
    onProfileClick: (accountId: String, isCurrent: Boolean) -> Unit
) {

    val lazyListState = ui.rememberLazyListState()
    LazyColumn(state = lazyListState) {
        items(items = ui, key = { "${it.originalId}  ${it.reblogsCount} ${it.repliesCount}" }) {
            it?.mapStatus().let { ui ->
                TimelineCard(
                    goToProfile,
                    ui,
                    replyToStatus,
                    boostStatus,
                    favoriteStatus,
                    state,
                    goToConversation,
                    isReplying,
                    false,
                    onProfileClick = onProfileClick
                )
            }
        }
    }
}
