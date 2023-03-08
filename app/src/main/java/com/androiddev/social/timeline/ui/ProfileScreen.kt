package com.androiddev.social.timeline.ui

import android.annotation.SuppressLint
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
import androidx.compose.material.BackdropScaffold
import androidx.compose.material.BackdropValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults.Indicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.androiddev.social.theme.EbonyTheme
import com.androiddev.social.theme.PaddingSize0_5
import com.androiddev.social.theme.PaddingSize2
import com.androiddev.social.timeline.data.Account
import com.androiddev.social.timeline.data.FeedType
import com.androiddev.social.timeline.data.StatusDB
import com.androiddev.social.timeline.ui.model.UI
import com.androiddev.social.ui.util.emojiText
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
@OptIn(ExperimentalMaterialApi::class)
fun ProfileScreen(
    component: AuthRequiredInjector,
    it: NavBackStackEntry,
    navController: NavHostController,
    scope: CoroutineScope,
    code: String,
    accountId: String
) {
    val homePresenter by remember(key1 = accountId) {
        mutableStateOf(
            component.homePresenter().get()
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

    EbonyTheme {
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
                profile(presenter)
            },
            frontLayerContent = {
                val userStatuses = homePresenter.model.userWithRepliesStatuses
                val account = presenter.model.account
                LaunchedEffect(key1 = accountId) {
                    homePresenter.handle(
                        TimelinePresenter.Load(
                            FeedType.UserWithReplies,
                            accountId
                        )
                    )
                }


                val pagingList = userStatuses?.collectAsLazyPagingItems()
                LaunchedEffect(key1 = account?.id) {
                    //very unexact way to run after the first append/prepend ran
                    //otherwise infinite scroll never calls append on first launch
                    // and I have no idea why
                    delay(200)
                    pagingList?.refresh()
                }
                pagingList?.let { pagingData ->
                    val events: MutableSharedFlow<SubmitPresenter.SubmitEvent> =
                        submitPresenter.events
                    posts(navController = navController, account, pagingData, events, code) {
                        scope.launch {
                            scaffoldState.conceal()
                        }
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
    account: Account?,
    statuses: LazyPagingItems<StatusDB>,
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
                text = { Text("Posts/Replies", color = MaterialTheme.colorScheme.secondary) },
                selected = pagerState.currentPage == 0,
                onClick = {
                    scope.launch {
                        changeHeight(500)
                        pagerState.scrollToPage(0)
                    }
                }
            )
//            Tab(
//                modifier = Modifier.background(MaterialTheme.colorScheme.background),
//                text = { Text("Post/Replies", color = MaterialTheme.colorScheme.secondary) },
//                selected = pagerState.currentPage == 0,
//                onClick = {
//                    scope.launch {
//                        changeHeight(500)
//                        pagerState.scrollToPage(1)
//                    }
//                },
//            )
//            Tab(
//                modifier = Modifier.background(MaterialTheme.colorScheme.background),
//                text = { Text("Media", color = MaterialTheme.colorScheme.secondary) },
//                selected = pagerState.currentPage == 0,
//                onClick = {
//                    scope.launch {
//                        changeHeight(500)
//                        pagerState.scrollToPage(2)
//                    }
//                },
//            )

        }

        HorizontalPager(
            count = 3,
            state = pagerState,
        ) { page ->

            TimelineRows(
                ui = statuses,
                account = account,
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
                boostStatus = {
                    events.tryEmit(
                        SubmitPresenter
                            .BoostMessage(it, FeedType.User)
                    )

                },
                favoriteStatus = {
                    events.tryEmit(
                        SubmitPresenter
                            .FavoriteMessage(it, FeedType.User)
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
                goToProfile = { status: UI ->
                    navController.navigate("profile/${code}/${status.accountId}")
                }
            )
        }
    }
}

@Composable
private fun profile(presenter: ProfilePresenter) {
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
                    .padding(16.dp)


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
                    account.avatar,
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
                    )
                    Boosted(
                        "Following ${account.followingCount}",
                        null,
                        null,
                        null,
                        modifier = Modifier.height(30.dp),
                    )
                }

                val (mapping, noteText) = emojiText(
                    account.note,
                    emptyList(),
                    emptyList(),
                    account.emojis
                )

                Text(
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .padding(PaddingSize0_5)
                        .align(Alignment.CenterHorizontally),
                    text = noteText,
                    inlineContent = mapping
                )
            }

        }
    }
}