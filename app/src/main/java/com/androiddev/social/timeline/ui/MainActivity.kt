@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)

package com.androiddev.social.timeline.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.FabPosition
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.androiddev.social.AuthOptionalComponent.ParentComponent
import com.androiddev.social.AuthOptionalScope
import com.androiddev.social.EbonyApp
import com.androiddev.social.R
import com.androiddev.social.timeline.data.Status
import com.androiddev.social.timeline.data.TimelineApi
import com.androiddev.social.timeline.data.mapStatus
import com.androiddev.social.timeline.ui.model.UI
import com.androiddev.social.timeline.ui.theme.EbonyTheme
import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.anvil.annotations.ContributesTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
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

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (noAuthComponent as MainActivityInjector).inject(this)
//        homePresenter.events.tryEmit(HomePresenter.LoadSomething)

        setContent {
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
                                    Box() { Profile("FriendlyMike") }
                                    Box(Modifier.align(Alignment.CenterVertically)) { TabSelector() }
                                    NotifIcon()
                                }
                            }
                        )
                    },
                    floatingActionButtonPosition = FabPosition.Center,
                    isFloatingActionButtonDocked = true,
                    floatingActionButton = {
                        val shape = CircleShape
                        LargeFloatingActionButton(
                            shape = shape,
                            containerColor = colorScheme.tertiary.copy(alpha = .9f),
                            modifier = Modifier
                                .offset(y = 36.dp)
                                .clip(shape),
                            content = {
                                Image(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .offset(y = -12.dp),
                                    painter = painterResource(R.drawable.elephant),
                                    contentDescription = "",
                                    colorFilter = ColorFilter.tint(colorScheme.background),
                                )

                            },
                            onClick = { /* fab click handler */ }
                        )
                    },
                    content = { it ->
                        Column(Modifier.padding(paddingValues = it)) {
//                            homePresenter.events.tryEmit(HomePresenter.LoadSomething)
                            Timeline(
                                homePresenter.model.statuses?.mapStatus() ?: listOf(
                                    UI(
                                        mentions = emptyList(),
                                        tags = emptyList()
                                    )
                                )
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

}

@ContributesBinding(AuthOptionalScope::class, boundType = HomePresenter::class)
class RealHomePresenter @Inject constructor(
    private val timelineApi: TimelineApi,
) : HomePresenter() {
    init {
        CoroutineScope(Dispatchers.IO).launch {
            val list =
                timelineApi.getTimeline(" Bearer o4i6i5EmNEqmN8PiecyY5EGHHKEQTT7fIZrPovH8S1s")
            model = model.copy(loading = false, statuses = list)
        }
    }

    override suspend fun eventHandler(event: HomeEvent) {

        when (event) {
            is LoadSomething -> {
//                throw RuntimeException("foo")
            }
        }
    }
}


interface BasePresenter

abstract class Presenter<Event, Model, Effect>(
    initialState: Model,
) : BasePresenter {
    var model: Model by mutableStateOf(initialState)

    val events: MutableSharedFlow<Event> =
        MutableSharedFlow(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    val effects: MutableSharedFlow<Effect> = MutableSharedFlow(extraBufferCapacity = 1)

    suspend fun start() {
        events.collect {
            eventHandler(it)
        }
    }

    abstract suspend fun eventHandler(event: Event)


}

abstract class HomePresenter :
    Presenter<HomePresenter.HomeEvent, HomePresenter.HomeModel, HomePresenter.HomeEffect>(
        HomeModel(true)
    ) {
    sealed interface HomeEvent
    object LoadSomething : HomeEvent

    data class HomeModel(
        val loading: Boolean,
        val statuses: List<Status>? = listOf()
    )

    sealed interface HomeEffect
}