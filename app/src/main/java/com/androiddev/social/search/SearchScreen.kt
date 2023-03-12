package com.androiddev.social.search

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.androiddev.social.search.SearchPresenter.SearchModel
import com.androiddev.social.search.topbar.TopBar
import com.androiddev.social.timeline.ui.model.UI
import com.slack.exercise.search.SearchState
import com.slack.exercise.search.rememberSearchState
import com.androiddev.social.search.results.SearchResults


/**
 * [SearchScreen] and [SearchView] are separated for testability.
 * Think of [SearchScreen] as the coordinator of state and callbacks
 * This allows [SearchView] to stay pure, accepting only state properties and event callbacks
 */
@Composable
fun SearchScreen(
    model: SearchModel,
    navController: NavController,
    onQueryChange: (String) -> Unit,
    goToProfile: (String) -> Unit,
    goToConversation: (UI) -> Unit,
) {
    SearchView(state = model, navController = navController, onQueryChange = onQueryChange, goToProfile = goToProfile, goToConversation = goToConversation)
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SearchView(
    state: SearchModel,
    navController: NavController,
    modifier: Modifier = Modifier,
    onQueryChange: (String) -> Unit,
    goToProfile: (String) -> Unit,
    goToConversation: (UI) -> Unit,
) {
    val searchState: SearchState = rememberSearchState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar(navController,onQueryChange = onQueryChange, state.isLoading, searchState)
        }
    )
    {
        SearchResults(state, goToProfile, goToConversation)
    }
}


//@Preview
//@Composable
//fun SearchLoadingPreview() = SearchView(state = SearchState(isLoading = true), onQueryChange = {},)
//
//
//@Preview
//@Composable
//fun SearchErrorPreview() = SearchView(
//    state = SearchState(
//        error = stringResource(
//            R.string.error_string
//        )
//    ),
//    onQueryChange = {},
//)

