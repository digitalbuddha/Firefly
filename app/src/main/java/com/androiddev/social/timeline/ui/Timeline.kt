@file:OptIn(ExperimentalMaterial3Api::class)

package com.androiddev.social.timeline.ui

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.ImageLoader
import com.androiddev.social.AuthRequiredComponent
import com.androiddev.social.UserComponent
import com.androiddev.social.auth.data.AccessTokenRequest
import com.androiddev.social.tabselector.Tab
import com.androiddev.social.theme.BottomBarElevation
import com.androiddev.social.theme.PaddingSize1
import com.androiddev.social.theme.PaddingSize2
import com.androiddev.social.theme.PaddingSize8
import com.androiddev.social.theme.PaddingSizeNone
import com.androiddev.social.timeline.data.Account
import com.androiddev.social.timeline.data.FeedType
import com.androiddev.social.timeline.ui.model.UI
import com.androiddev.social.ui.Search
import dev.marcellogalhardo.retained.compose.retainInActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import social.androiddev.firefly.R

val LocalAuthComponent = compositionLocalOf<AuthRequiredInjector> { error("No component found!") }
val LocalUserComponent = compositionLocalOf<UserComponent> { error("No component found!") }
val LocalImageLoader = compositionLocalOf<ImageLoader> { error("No component found!") }

@OptIn(
    ExperimentalMaterialApi::class
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
    goToSearch: () -> Unit,
    goToConversation: (UI) -> Unit,
    goToProfile: (String) -> Unit,
    goToTag: (String) -> Unit,
) {
    val component =
        retainInActivity(
            owner = LocalContext.current as ViewModelStoreOwner,
            key = accessTokenRequest.domain ?: ""
        ) { (userComponent as AuthRequiredComponent.ParentComponent).createAuthRequiredComponent() } as AuthRequiredInjector
    CompositionLocalProvider(LocalAuthComponent provides component) {
        val homePresenter = component.homePresenter()
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

        val bottomState: ModalBottomSheetState =
            rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
        val bottomSheetContentProvider = remember { BottomSheetContentProvider(bottomState) }
        val context = LocalContext.current

        ModalBottomSheetLayout(
            sheetState = bottomState,
            sheetShape = RoundedCornerShape(topStart = PaddingSize1, topEnd = PaddingSize1),
            sheetElevation = PaddingSize2,
            sheetBackgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            sheetContent = {
                BottomSheetContent(
                    bottomSheetContentProvider = bottomSheetContentProvider,
                    onShareStatus = { context.shareStatus(it) },
                    onDelete = { statusId->
                        submitPresenter.handle(SubmitPresenter.DeleteStatus(statusId))
                    },
                    onMessageSent = { it, visibility, uris ->
                        submitPresenter.handle(
                            SubmitPresenter.PostMessage(
                                content = it,
                                visibility = visibility,
                                uris = uris
                            )
                        )
                        scope.launch {
                            bottomSheetContentProvider.hide()
                        }
                    },
                    goToConversation = goToConversation,
                    goToProfile = goToProfile,
                    goToTag = goToTag,
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
                accessTokenRequest = accessTokenRequest,
                avatarPresenter = avatarPresenter,
                onChangeTheme = onChangeTheme,
                onNewAccount = onNewAccount,
                onProfileClick = onProfileClick,
                goToSearch = goToSearch,
                goToMentions = goToMentions,
                goToNotifications = goToNotifications,
                homePresenter = homePresenter,
                bottomSheetContentProvider = bottomSheetContentProvider,
                submitPresenter = submitPresenter,
                goToConversation = goToConversation,
                goToProfile = goToProfile,
                goToTag = goToTag,
            )
        }
    }
}

@Composable
@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterial3Api::class
)
private fun ScaffoldParent(
    accessTokenRequest: AccessTokenRequest,
    avatarPresenter: AvatarPresenter,
    onChangeTheme: () -> Unit,
    onNewAccount: () -> Unit,
    onProfileClick: (accountId: String, isCurrent: Boolean) -> Unit,
    goToSearch: () -> Unit,
    goToMentions: () -> Unit,
    goToNotifications: () -> Unit,
    homePresenter: TimelinePresenter,
    bottomSheetContentProvider: BottomSheetContentProvider,
    submitPresenter: SubmitPresenter,
    goToConversation: (UI) -> Unit,
    goToProfile: (String) -> Unit,
    goToTag: (String) -> Unit,
) {
    var tabToLoad: FeedType by rememberSaveable { mutableStateOf(FeedType.Home) }
    var refresh: Boolean by remember { mutableStateOf(false) }
    var expanded: Boolean by remember { mutableStateOf(false) }
    var isReplying: Boolean by remember { mutableStateOf(false) }
    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    val items = mutableListOf(
        Tab(
            "Home",
            R.drawable.house,
            onClick = {
                tabToLoad = FeedType.valueOf("Home"); selectedIndex = 0; expanded = false
            }),
        Tab(
            "Local",
            R.drawable.local,
            onClick = {
                tabToLoad = FeedType.valueOf("Local"); selectedIndex = 1; expanded = false
            }),
        Tab(
            "Federated",
            R.drawable.world,
            onClick = {
                tabToLoad = FeedType.valueOf("Federated"); selectedIndex = 2; expanded = false
            }),
        Tab(
            "Trending",
            R.drawable.trend,
            onClick = {
                tabToLoad = FeedType.valueOf("Trending"); selectedIndex = 3; expanded = false
            }),
        Tab(
            "Bookmarks",
            R.drawable.bookmark,
            onClick = {
                tabToLoad = FeedType.valueOf("Bookmarks"); selectedIndex = 4; expanded = false
            }),
        Tab(
            "Favorites",
            R.drawable.star,
            onClick = {
                tabToLoad = FeedType.valueOf("Favorites"); selectedIndex = 5; expanded = false
            }),

        )
    Scaffold(
        topBar = {
            TopAppBar(modifier = Modifier.clickable {
                refresh = true
            },
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
                            TabSelector(items, selectedIndex, expanded) { expanded = !expanded }
                        }
                        Search {
                            goToSearch()
                        }
                    }
                })
        },

        backgroundColor = Color.Transparent,
        bottomBar = {
            AnimatedVisibility(!isReplying, enter = fadeIn(), exit = fadeOut()) {
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
            if (!isReplying) {
                FAB(MaterialTheme.colorScheme) {
                    scope.launch {
                        homePresenter.model.account?.let {
                            bottomSheetContentProvider.showContent(SheetContentState.UserInput(it))
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box {
            val model = homePresenter.model

            when (tabToLoad) {
                FeedType.Home -> {
                    timelineTab(
                        goToBottomSheet = bottomSheetContentProvider::showContent,
                        goToProfile = goToProfile,
                        goToTag = goToTag,
                        domain = accessTokenRequest.domain,
                        events = homePresenter.events,
                        submitEvents = submitPresenter.events,
                        tabToLoad = FeedType.Home,
                        items = model.homeStatuses?.collectAsLazyPagingItems(),
                        account = model.account,
                        goToConversation = goToConversation,
                        onReplying = { isReplying = it },
                        onProfileClick = onProfileClick,
                        refresh = refresh,
                        doneRefreshing = { refresh = false },
                    )
                }

                FeedType.Local -> {
                    timelineTab(
                        goToBottomSheet = bottomSheetContentProvider::showContent,
                        goToProfile = goToProfile,
                        goToTag = goToTag,
                        domain = accessTokenRequest.domain,
                        events = homePresenter.events,
                        submitEvents = submitPresenter.events,
                        tabToLoad = FeedType.Local,
                        items = model.localStatuses?.collectAsLazyPagingItems(),
                        account = model.account,
                        goToConversation = goToConversation,
                        onReplying = { isReplying = it },
                        onProfileClick = onProfileClick,
                        refresh = refresh,
                        doneRefreshing = { refresh = false },
                    )
                }

                FeedType.Federated -> {
                    timelineTab(
                        goToBottomSheet = bottomSheetContentProvider::showContent,
                        goToProfile = goToProfile,
                        goToTag = goToTag,
                        domain = accessTokenRequest.domain,
                        events = homePresenter.events,
                        submitEvents = submitPresenter.events,
                        tabToLoad = FeedType.Federated,
                        items = model.federatedStatuses?.collectAsLazyPagingItems(),
                        account = model.account,
                        goToConversation = goToConversation,
                        onReplying = { isReplying = it },
                        onProfileClick = onProfileClick,
                        refresh = refresh,
                        doneRefreshing = { refresh = false },
                    )
                }

                FeedType.Trending -> {
                    timelineTab(
                        goToBottomSheet = bottomSheetContentProvider::showContent,
                        goToProfile = goToProfile,
                        goToTag = goToTag,
                        domain = accessTokenRequest.domain,
                        events = homePresenter.events,
                        submitEvents = submitPresenter.events,
                        tabToLoad = FeedType.Trending,
                        items = model.trendingStatuses?.collectAsLazyPagingItems(),
                        account = model.account,
                        goToConversation = goToConversation,
                        onReplying = { isReplying = it },
                        onProfileClick = onProfileClick,
                        refresh = refresh,
                        doneRefreshing = { refresh = false },
                    )
                }
                FeedType.User -> {}
                FeedType.UserWithMedia -> {}
                FeedType.UserWithReplies -> {}
                FeedType.Bookmarks -> {
                    timelineTab(
                        goToBottomSheet = bottomSheetContentProvider::showContent,
                        goToProfile = goToProfile,
                        goToTag = goToTag,
                        domain = accessTokenRequest.domain,
                        events = homePresenter.events,
                        submitEvents = submitPresenter.events,
                        tabToLoad = FeedType.Bookmarks,
                        items = model.bookmarkedStatuses?.collectAsLazyPagingItems(),
                        account = model.account,
                        goToConversation = goToConversation,
                        onReplying = { isReplying = it },
                        onProfileClick = onProfileClick,
                        refresh = refresh,
                        doneRefreshing = { refresh = false },
                    )
                }

                FeedType.Favorites -> {
                    timelineTab(
                        goToBottomSheet = bottomSheetContentProvider::showContent,
                        goToProfile = goToProfile,
                        goToTag = goToTag,
                        domain = accessTokenRequest.domain,
                        events = homePresenter.events,
                        submitEvents = submitPresenter.events,
                        tabToLoad = FeedType.Favorites,
                        items = model.favoriteStatuses?.collectAsLazyPagingItems(),
                        account = model.account,
                        goToConversation = goToConversation,
                        onReplying = { isReplying = it },
                        onProfileClick = onProfileClick,
                        refresh = refresh,
                        doneRefreshing = { refresh = false },
                    )
                }

                FeedType.Hashtag -> {
                    timelineTab(
                        goToBottomSheet = bottomSheetContentProvider::showContent,
                        goToProfile = goToProfile,
                        goToTag = goToTag,
                        domain = accessTokenRequest.domain,
                        events = homePresenter.events,
                        submitEvents = submitPresenter.events,
                        tabToLoad = FeedType.Hashtag,
                        items = model.hashtagStatuses?.collectAsLazyPagingItems(),
                        account = model.account,
                        goToConversation = goToConversation,
                        onReplying = { isReplying = it },
                        onProfileClick = onProfileClick,
                        refresh = refresh,
                        doneRefreshing = { refresh = false },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun timelineTab(
    goToBottomSheet: suspend (SheetContentState) -> Unit,
    goToProfile: (String) -> Unit,
    goToTag: (String) -> Unit,
    domain: String?,
    events: MutableSharedFlow<TimelinePresenter.HomeEvent>,
    submitEvents: MutableSharedFlow<SubmitPresenter.SubmitEvent>,
    tabToLoad: FeedType,
    items: LazyPagingItems<UI>?,
    account: Account?,
    goToConversation: (UI) -> Unit,
    onReplying: (Boolean) -> Unit,
    onProfileClick: (accountId: String, isCurrent: Boolean) -> Unit,
    refresh: Boolean,
    doneRefreshing: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    LaunchedEffect(key1 = tabToLoad, key2 = domain, key3 = tabToLoad.tagName) {
        events.tryEmit(TimelinePresenter.Load(tabToLoad, colorScheme = colorScheme))
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
            val lazyListState = rememberLazyListState()
            if (refresh) {
                LaunchedEffect(key1 = Unit) {
                    lazyListState.scrollToItem(0)
                    items.refresh()
                    doneRefreshing()
                }
            }
            LaunchedEffect(key1 = tabToLoad, key2 = domain) {
                //very unexact way to run after the first append/prepend ran
                //otherwise infinite scroll never calls append on first launch
                // and I have no idea why
                delay(200)
                if (items.itemCount == 0) items.refresh()
            }
            TimelineRows(
                goToBottomSheet = goToBottomSheet,
                goToProfile,
                goToTag,
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
                boostStatus = { statusId, boosted ->
                    submitEvents.tryEmit(
                        SubmitPresenter
                            .BoostMessage(statusId, tabToLoad, boosted)
                    )
                },
                favoriteStatus = { statusId, favourited ->
                    submitEvents.tryEmit(
                        SubmitPresenter
                            .FavoriteMessage(statusId, tabToLoad, favourited)
                    )
                },
                onReplying = onReplying,
                goToConversation = goToConversation,
                onProfileClick = onProfileClick,
                lazyListState,
                onVote = { statusId, pollId, choices ->
                    submitEvents.tryEmit(SubmitPresenter.VotePoll(statusId, pollId, choices))
                },
            )
        }
        CustomViewPullRefreshView(
            pullRefreshState, refreshTriggerDistance = 4.dp, isRefreshing = refreshing
        )
    }
}

private val SaveMap = mutableMapOf<String, ScrollKeyParams>()

private data class ScrollKeyParams(
    val value: Int
)


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TimelineRows(
    goToBottomSheet: suspend (SheetContentState) -> Unit,
    goToProfile: (String) -> Unit,
    goToTag: (String) -> Unit,
    ui: LazyPagingItems<UI>,
    account: Account?,
    replyToStatus: (String, String, String, Int, Set<Uri>) -> Unit,
    boostStatus: (remoteId: String, boosted: Boolean) -> Unit,
    favoriteStatus: (remoteId: String, favourited: Boolean) -> Unit,
    onReplying: (Boolean) -> Unit,
    goToConversation: (UI) -> Unit,
    onProfileClick: (accountId: String, isCurrent: Boolean) -> Unit,
    lazyListState: LazyListState,
    onVote: (statusId: String, pollId: String, choices: List<Int>) -> Unit,
) {


    Crossfade(targetState = ui, label = "") { item ->
        if (item.itemCount == 0) {
            LazyColumn {
                items(3) {
                    TimelineCard(
                        goToBottomSheet = goToBottomSheet,
                        goToProfile,
                        goToTag = goToTag,
                        null,
                        null,
                        replyToStatus,
                        boostStatus,
                        favoriteStatus,
                        goToConversation,
                        onReplying = onReplying,
                        false,
                        onProfileClick = onProfileClick,
                        onVote = onVote,
                    )
                }
            }
        } else {
            LazyColumn(state = lazyListState) {
                items(
                    items = item,
                    key = { "${it.originalId}  ${it.boostCount} ${it.replyCount}" }) {
                    TimelineCard(
                        goToBottomSheet = goToBottomSheet,
                        goToProfile,
                        goToTag = goToTag,
                        it,
                        account = account,
                        replyToStatus,
                        boostStatus,
                        favoriteStatus,
                        goToConversation,
                        onReplying = onReplying,
                        false,
                        onProfileClick = onProfileClick,
                        onVote = onVote,
                    )
                }
            }
        }
    }
}
