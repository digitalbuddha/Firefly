@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)

package com.androiddev.social.timeline.ui

import android.annotation.SuppressLint
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.constraintlayout.motion.widget.MotionLayout
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.VideoFrameDecoder
import com.androiddev.social.AuthOptionalComponent.ParentComponent
import com.androiddev.social.AuthOptionalScope
import com.androiddev.social.AuthRequiredScope
import com.androiddev.social.FireflyApp
import com.androiddev.social.auth.ui.SignInPresenter
import com.androiddev.social.search.SearchPresenter
import com.androiddev.social.theme.*
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.squareup.anvil.annotations.ContributesTo
import social.androiddev.firefly.R
import javax.inject.Provider


@OptIn(ExperimentalMaterial3Api::class)
@ContributesTo(AuthOptionalScope::class)
interface AuthOptionalInjector {
    fun signInPresenter(): SignInPresenter
}

@OptIn(ExperimentalMaterial3Api::class)
@ContributesTo(AuthRequiredScope::class)
interface AuthRequiredInjector {
    fun avatarPresenter(): AvatarPresenter
    fun profilePresenter(): ProfilePresenter
    fun searchPresenter(): SearchPresenter
    fun homePresenter():TimelinePresenter
    fun mentionsPresenter(): MentionsPresenter
    fun notificationPresenter(): NotificationPresenter
    fun submitPresenter(): SubmitPresenter
    fun followerPresenter(): FollowerPresenter
    fun conversationPresenter(): Provider<ConversationPresenter>
}

@OptIn(ExperimentalMaterialNavigationApi::class)
@ExperimentalTextApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {

    override fun onStop() {
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
    }

    @OptIn(ExperimentalMaterialNavigationApi::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        findViewById<MotionLayout>(R.id.motionLayout).setTransitionListener(object :
            MotionLayout.TransitionListener {
            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
                content()
            }

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {}

            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {}

            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {}
        })


    }

    fun content() =
        setContent {
            val loader = ImageLoader.Builder(this)
                .components {
                    if (SDK_INT >= 28) {
                        add(ImageDecoderDecoder.Factory())
                    } else {
                        add(GifDecoder.Factory())
                    }
                    add(VideoFrameDecoder.Factory())
                }
                .build()

            CompositionLocalProvider(LocalImageLoader provides loader) {
                var isDynamicTheme by remember { mutableStateOf(true) }
                FireflyTheme(isDynamicColor = isDynamicTheme) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        val scope = rememberCoroutineScope()
                        val sheetState = rememberModalBottomSheetState(
                            ModalBottomSheetValue.Hidden,
                            SwipeableDefaults.AnimationSpec,
                            skipHalfExpanded = true
                        )
                        val bottomSheetNavigator = remember(sheetState) {
                            BottomSheetNavigator(sheetState = sheetState)
                        }

                        val navController = rememberAnimatedNavController(bottomSheetNavigator)
                        ModalBottomSheetLayout(bottomSheetNavigator) {
                            Navigator(
                                navController, scope,
                            ) {
                                isDynamicTheme = !isDynamicTheme
                            }
                        }
                    }
                }
            }
        }



    fun noAuthComponent() =
        ((applicationContext as FireflyApp).component as ParentComponent).createAuthOptionalComponent()

    fun AuthComponent() =
        ((applicationContext as FireflyApp).component as ParentComponent).createAuthOptionalComponent()
}
