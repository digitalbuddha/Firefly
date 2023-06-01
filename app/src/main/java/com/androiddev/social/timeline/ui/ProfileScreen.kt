package com.androiddev.social.timeline.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BackdropScaffold
import androidx.compose.material.BackdropValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults.Indicator
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.androiddev.social.theme.FireflyTheme
import com.androiddev.social.theme.PaddingSize0_5
import com.androiddev.social.theme.PaddingSize2
import com.androiddev.social.timeline.data.FeedType
import com.androiddev.social.timeline.ui.model.UI
import com.androiddev.social.ui.util.emojiText
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import social.androiddev.firefly.R

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
@OptIn(ExperimentalMaterialApi::class)
fun ProfileScreen(
    component: AuthRequiredInjector,
    navController: NavHostController,
    code: String,
    accountId: String,
    goToFollowers: () -> Unit,
    goToFollowing: () -> Unit
) {
    val homePresenter by remember(key1 = accountId) {
        mutableStateOf(
            component.homePresenter()
        )
    }
    val submitPresenter = component.submitPresenter()
    val scope = rememberCoroutineScope()
    LaunchedEffect(key1 = accountId) {
        homePresenter.start(scope)
    }

    LaunchedEffect(key1 = accountId) {
        submitPresenter.start()
    }

    val presenter = component.profilePresenter()
    LaunchedEffect(key1 = { accountId }) {
        presenter.start()
    }
    LaunchedEffect(key1 = { accountId }) {
        presenter.handle(ProfilePresenter.Load(accountId))
    }

    FireflyTheme {
        var clicked by remember { mutableStateOf(false) }

        val scaffoldState = rememberBackdropScaffoldState(BackdropValue.Concealed)
        BackdropScaffold(
            scaffoldState = scaffoldState,
            frontLayerBackgroundColor = MaterialTheme.colorScheme.surface,
            backLayerBackgroundColor = MaterialTheme.colorScheme.surface,
            frontLayerScrimColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.30f),
            modifier = Modifier.background(Color.Transparent),
            appBar = {
                TopAppBar(
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    title = {
                        Text(
                            text = "Profile",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    actions = {
                        val account = presenter.model.account
                        var text = if (account?.isFollowed == true) "unfollow" else "follow"

                        TextButton(
                            onClick = {
                                submitPresenter.handle(SubmitPresenter.Follow(accountId = accountId))
                                text = if (text == "follow") "unfollow" else "follow"
                            }
                        ) {

                            Text(text = text, color = MaterialTheme.colorScheme.primary)
                            Image(
                                modifier = Modifier
                                    .size(24.dp),
                                painter = if (text == "follow") painterResource(R.drawable.add) else painterResource(
                                    R.drawable.remove
                                ),
                                contentDescription = "",
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                            )
                        }
                    },
                    navigationIcon = if (navController.previousBackStackEntry != null) {
                        {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = "search"
                                )
                            }
                        }
                    } else {
                        null
                    }
                )
            },
            backLayerContent = {
                profile(presenter, goToFollowers, goToFollowing)
            },
            frontLayerContent = {
                val userStatuses = homePresenter.model.userStatuses
                val withReplies = homePresenter.model.userWithRepliesStatuses
                val withMedia = homePresenter.model.userWithMediaStatuses
                val account = presenter.model.account
                val colorScheme = MaterialTheme.colorScheme
                LaunchedEffect(key1 = accountId) {
                    homePresenter.handle(
                        TimelinePresenter.Load(
                            FeedType.UserWithReplies,
                            accountId,
                            colorScheme
                        )
                    )
                }


                val pagingListUserStatus = userStatuses?.collectAsLazyPagingItems()
                val pagingListWithReplies = withReplies?.collectAsLazyPagingItems()
                val pagingListWithMedia = withMedia?.collectAsLazyPagingItems()
//                LaunchedEffect(key1 = account?.id) {
//                    //very unexact way to run after the first append/prepend ran
//                    //otherwise infinite scroll never calls append on first launch
//                    // and I have no idea why
//                    delay(200)
//                    pagingList?.refresh()
//                }

                val events: MutableSharedFlow<SubmitPresenter.SubmitEvent> =
                    submitPresenter.events
                posts(
                    navController = navController,
                    pagingListUserStatus,
                    pagingListWithReplies,
                    pagingListWithMedia,
                    events,
                    code
                ) {
                    scope.launch {
                        scaffoldState.conceal()
                    }
                }
            },
            // Defaults to BackdropScaffoldDefaults.PeekHeight
            peekHeight = 106.dp,
            // Defaults to BackdropScaffoldDefaults.HeaderHeight
            headerHeight = 200.dp,
            // Defaults to true
//                        gesturesEnabled = false
        )
        LaunchedEffect(key1 = accountId) {
            scaffoldState.reveal()
        }
    }
}

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterialApi::class)
@Composable
private fun posts(
    navController: NavHostController,
    statuses: LazyPagingItems<UI>?,
    withReplies: LazyPagingItems<UI>?,
    withMedia: LazyPagingItems<UI>?,
    events: MutableSharedFlow<SubmitPresenter.SubmitEvent>,
    code: String,
    changeHeight: (Int) -> Unit
) {
    val pagerState = rememberPagerState()
    Column {
        TabRow(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            // Our selected tab is our current page
            selectedTabIndex = pagerState.currentPage,
            // Override the indicator, using the provided pagerTabIndicatorOffset modifier
            indicator = { tabPositions ->
                Indicator(
                    Modifier.pagerTabIndicatorOffset(
                        pagerState,
                        tabPositions
                    )
                )
            }
        ) {
            // Add tabs for all of our pages
            val scope = rememberCoroutineScope()
            Tab(
                modifier = Modifier.background(MaterialTheme.colorScheme.background),
                text = { Text("Posts", color = MaterialTheme.colorScheme.secondary) },
                selected = pagerState.currentPage == 0,
                onClick = {
                    scope.launch {
                        changeHeight(500)
                        pagerState.scrollToPage(0)
                    }
                }
            )
            Tab(
                modifier = Modifier.background(MaterialTheme.colorScheme.background),
                text = { Text(" With Replies", color = MaterialTheme.colorScheme.secondary) },
                selected = pagerState.currentPage == 0,
                onClick = {
                    scope.launch {
                        changeHeight(500)
                        pagerState.scrollToPage(1)
                    }
                },
            )
            Tab(
                modifier = Modifier.background(MaterialTheme.colorScheme.background),
                text = { Text("Media Only", color = MaterialTheme.colorScheme.secondary) },
                selected = pagerState.currentPage == 0,
                onClick = {
                    scope.launch {
                        changeHeight(500)
                        pagerState.scrollToPage(2)
                    }
                },
            )

        }

        HorizontalPager(
            count = 3,
            state = pagerState,
        ) { page ->
            val ui = when (page) {
                0 -> {
                    statuses
                }

                1 -> {
                    withReplies
                }

                else -> {
                    withMedia
                }
            }
            ui?.let {
                TimelineRows(
                    goToProfile = { accountId: String ->
                        navController.navigate("profile/${code}/${accountId}")
                    },
                    goToTag = { tag: String ->
                        navController.navigate("tag/${code}/${tag}")
                    },

                    ui = it,
                    replyToStatus = { content, visiblity, replyToId, replyCount, uris ->
                        events.tryEmit(
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
                        events.tryEmit(
                            SubmitPresenter
                                .BoostMessage(statusId, FeedType.User, boosted)
                        )

                    },
                    favoriteStatus = { statusId, favourited ->
                        events.tryEmit(
                            SubmitPresenter
                                .FavoriteMessage(statusId, FeedType.User, favourited)
                        )

                    },
                    state = rememberModalBottomSheetState(
                        ModalBottomSheetValue.Hidden,
                        SwipeableDefaults.AnimationSpec,
                        skipHalfExpanded = true
                    ),
                    isReplying = { },
                    goToConversation = { status: UI ->
                        navController.navigate("conversation/${code}/${status.remoteId}/${status.type.type}")
                    },
                    onProfileClick ={ accountId, _ ->
                        navController.navigate("profile/${code}/${accountId}")
                    },
                    rememberLazyListState()
                )
            }
        }
    }
}

@Composable
private fun profile(
    presenter: ProfilePresenter,
    goToFollowers: () -> Unit,
    goToFollowing: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        presenter.model.account?.let { account ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(account.header)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile background",
                contentScale = ContentScale.FillHeight,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(8.dp)

            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = .6f))
                    .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally

            ) {


                val emojis = account.emojis
                val unformatted = account.displayName
                val (inlineContentMap, text) = inlineEmojis(
                    unformatted,
                    emojis
                )
                Text(
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier
                        .padding(PaddingSize0_5)
                        .align(Alignment.CenterHorizontally),
                    text = text,
                    inlineContent = inlineContentMap
                )

                ContentImage(
                    listOf(account.avatar),
                    modifier = Modifier
                        .height(200.dp)
                        .aspectRatio(1f)
                        .align(Alignment.CenterHorizontally)
                )
                Text(
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier
                        .padding(PaddingSize0_5)
                        .align(Alignment.CenterHorizontally),
                    text = "@${account.username}",
                )



                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(PaddingSize2),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Boosted(
                        "Followers ${account.followersCount}",
                        null,
                        null,
                        null,
                        modifier = Modifier.height(30.dp),
                        onClick = goToFollowers
                    )
                    Boosted(
                        "Following ${account.followingCount}",
                        null,
                        null,
                        null,
                        modifier = Modifier.height(30.dp),
                        onClick = goToFollowing

                    )
                }

                val (mapping, noteText) = emojiText(
                    account.note,
                    emptyList(),
                    emptyList(),
                    account.emojis,
                    MaterialTheme.colorScheme
                )
                val scroll = rememberScrollState(0)


                Text(
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .verticalScroll(scroll)
                        .padding(PaddingSize0_5)
                        .align(Alignment.CenterHorizontally),
                    text = noteText,
                    inlineContent = mapping
                )
            }

        }
    }
}