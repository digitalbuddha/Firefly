package com.androiddev.social.search.results

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.androiddev.social.accounts.AccountTab
import com.androiddev.social.search.SearchPresenter
import com.androiddev.social.theme.PaddingSize0_5
import com.androiddev.social.theme.PaddingSize1
import com.androiddev.social.timeline.data.Tag
import com.androiddev.social.timeline.ui.LocalAuthComponent
import com.androiddev.social.timeline.ui.SubmitPresenter
import com.androiddev.social.timeline.ui.card
import com.androiddev.social.timeline.ui.model.UI
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.m3.style.m3ChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.entry.entryModelOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun SearchResults(
    results: SearchPresenter.SearchModel,
    goToProfile: (String) -> Unit,
    goToTag: (String) -> Unit,
    goToConversation: (UI) -> Unit,
) {
    val padding = PaddingSize1
    val pagerState = rememberPagerState()

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(
            // Our selected tab is our current page
            selectedTabIndex = pagerState.currentPage,
            // Override the indicator, using the provided pagerTabIndicatorOffset modifier
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                )
            }
        ) {
            val scope = rememberCoroutineScope()
            val tabs = listOf("Accounts", "Statuses", "Hashtags")
            // Add tabs for all of our pages
            tabs.forEachIndexed { index, title ->
                Tab(
                    modifier = Modifier.background(MaterialTheme.colorScheme.background),
                    text = { Text(title, color = colorScheme.primary) },
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.scrollToPage(index) } },
                )
            }
        }
        val component = LocalAuthComponent.current
        val submitPresenter = component.submitPresenter()
        LaunchedEffect(key1 = Unit) {
            submitPresenter.start()
        }

        HorizontalPager(
            count = 3,
            state = pagerState,
        ) { page ->
            when (page) {
                0 -> {
                    if (results.accounts != null) {
                        AccountTab(results = results.accounts, null, goToProfile)
                    } else {
                        Surface(
                            color = Color.Transparent,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Transparent)
                        ) {

                        }
                    }
                }

                1 -> {
                    if (results.statuses != null) {
                        StatusTab(
                            results.statuses,
                            goToProfile,
                            goToTag,
                            goToConversation,
                            submitPresenter
                        )
                    } else {
                        Surface(
                            color = Color.Transparent,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Transparent)
                        ) {

                        }
                    }
                }

                2 -> {
                    if (results.hashtags != null) {
                        HashTagTab(results.hashtags, goToProfile, submitPresenter)
                    } else {
                        Surface(
                            color = Color.Transparent,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Transparent)
                        ) {

                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun StatusTab(
    results: List<UI>,
    goToProfile: (String) -> Unit,
    goToTag: (String) -> Unit,
    goToConversation: (UI) -> Unit,
    submitPresenter: SubmitPresenter
) {
    LazyColumn(
        Modifier
            .wrapContentHeight()
            .padding(top = 0.dp)
            .fillMaxSize()
    ) {
        items(results, key = { it.remoteId }) {
            card(
                modifier = Modifier.background(Color.Transparent),
                status = it,
                events = submitPresenter.events,
                showInlineReplies = false,
                goToConversation = goToConversation,
                goToProfile = goToProfile,
                goToTag = goToTag
            )
        }
    }
}

@Composable
private fun HashTagTab(
    results: List<Tag>,
    goToProfile: (String) -> Unit,
    submitPresenter: SubmitPresenter
) {
    LazyColumn(Modifier.fillMaxSize()) {
        items(results, key = { it.name }) {
            Row(
                modifier = Modifier
                    .clickable { }
                    .fillMaxWidth()
                    .padding(PaddingSize1),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                androidx.compose.material3.Text(
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.onSurface,
                    modifier = Modifier
                        .padding(PaddingSize0_5),
                    text = it.name,
                )

                val map = it.history?.map { it.uses.toInt() }?.toTypedArray()
                if (map != null) {
                    ProvideChartStyle(m3ChartStyle(axisLineColor = colorScheme.primary)) {

                        Chart(
                            modifier = Modifier
                                .height(50.dp)
                                .weight(1f),
                            chart = lineChart(),
                            model = entryModelOf(*map),
                            startAxis = null,
                            bottomAxis = null,
                        )
                    }
                }
                var text by remember { mutableStateOf(if (it.isFollowing == false) "follow" else "unfollow") }


                ElevatedButton(onClick = {
                    submitPresenter.handle(
                        SubmitPresenter.FollowTag(
                            it.name,
                            it.isFollowing == true
                        )
                    )
                    if (text == "unfollow") text = "follow" else text = "unfollow"
                }) {
                    Text(
                        text = text,
                        color = colorScheme.onSurface
                    )
                }
            }
        }
    }
}











