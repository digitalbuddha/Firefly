@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)

package com.androiddev.social.timeline.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.FabPosition
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.androiddev.social.AuthOptionalComponent.ParentComponent
import com.androiddev.social.AuthOptionalScope
import com.androiddev.social.EbonyApp
import com.androiddev.social.auth.ui.SignInContent
import com.androiddev.social.auth.ui.SignInPresenter
import com.androiddev.social.timeline.ui.theme.EbonyTheme
import com.squareup.anvil.annotations.ContributesTo
import javax.inject.Inject


@OptIn(ExperimentalMaterial3Api::class)
@ContributesTo(AuthOptionalScope::class)
interface MainActivityInjector {
    fun inject(activity: MainActivity)
}

@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var homePresenter: HomePresenter

    @Inject
    lateinit var signInPresenter: SignInPresenter

    @Inject
    lateinit var avatarPresenter: AvatarPresenter
    override fun onStop() {
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (noAuthComponent as MainActivityInjector).inject(this)
//        homePresenter.events.tryEmit(HomePresenter.LoadSomething)

        setContent {
            val signedIn = signInPresenter.model.signedIn
            LaunchedEffect(key1 = Unit) {
                signInPresenter.start()
            }
            LaunchedEffect(key1 = "Start") {
            }

            EbonyTheme {
                androidx.compose.material.Scaffold(
                    bottomBar = {
                        androidx.compose.material.BottomAppBar(
                            modifier = Modifier.height(60.dp),
                            contentPadding = PaddingValues(0.dp, 0.dp),
                            elevation = 0.dp,
//                            cutoutShape = CutCornerShape(50),
                            backgroundColor = colorScheme.surface.copy(alpha = .9f),
                        ) {
                            BottomBar()
                        }
                    },
                    topBar = {
                        SmallTopAppBar(
                            colors = TopAppBarDefaults.smallTopAppBarColors(
                                containerColor = colorScheme.surface.copy(
                                    alpha = .9f
                                )
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
                                    if (signedIn && homePresenter.model.statuses!=null) {
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
                    },
                    floatingActionButtonPosition = FabPosition.Center,
                    isFloatingActionButtonDocked = true,
                    floatingActionButton = {
                        FAB(colorScheme)
                    },
                    content = { it ->
                        val scope = rememberCoroutineScope()
                        if (signedIn) {
                            LaunchedEffect(key1 = "start") {
                                homePresenter.start()
                            }
                            LaunchedEffect(key1 = HomePresenter.LoadSomething) {
                                homePresenter.events.tryEmit(HomePresenter.LoadSomething)
                            }
                            Column(Modifier.padding(paddingValues = it)) {
                                homePresenter.model.statuses?.let {
                                    Timeline(
                                        it.collectAsLazyPagingItems()
                                    )
                                }

                            }
                        } else {
                            SignInContent(
                                oauthAuthorizeUrl = signInPresenter.model.oauthAuthorizeUrl,
                                error = signInPresenter.model.error,
                                onErrorFromOAuth = {
//                                TODO("Implement onError")
                                },
                                onCloseClicked = {

                                },
                                shouldCancelLoadingUrl = {
                                    signInPresenter.shouldCancelLoadingUrl(it, scope)
                                }

                            )
                        }
                    },
                )
            }
        }
    }


    private val noAuthComponent by lazy {
        ((applicationContext as EbonyApp).component as ParentComponent).createAuthOptionalComponent()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }
}

