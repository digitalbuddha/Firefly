package com.androiddev.social.search

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.androiddev.social.search.SearchPresenter.SearchModel
import com.androiddev.social.search.results.SearchResults
import com.androiddev.social.search.topbar.TopBar
import com.androiddev.social.theme.PaddingSize1
import com.androiddev.social.timeline.ui.BottomSheetContent
import com.androiddev.social.timeline.ui.BottomSheetContentProvider
import com.androiddev.social.timeline.ui.model.CardUI
import com.androiddev.social.timeline.ui.model.UI
import com.slack.exercise.search.SearchState
import com.slack.exercise.search.rememberSearchState


/**
 * [SearchScreen] and [SearchView] are separated for testability.
 * Think of [SearchScreen] as the coordinator of state and callbacks
 * This allows [SearchView] to stay pure, accepting only state properties and event callbacks
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SearchScreen(
    model: SearchModel,
    navController: NavController,
    onQueryChange: (String) -> Unit,
    goToProfile: (String) -> Unit,
    goToTag: (String) -> Unit,
    goToConversation: (UI) -> Unit,
    onOpenCard: (CardUI) -> Unit,
) {
    val bottomState: ModalBottomSheetState =
        rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val bottomSheetContentProvider = remember { BottomSheetContentProvider(bottomState) }

    SearchView(
        state = model,
        navController = navController,
        bottomSheetContentProvider = bottomSheetContentProvider,
        onQueryChange = onQueryChange,
        goToProfile = goToProfile,
        goToTag = goToTag,
        goToConversation = goToConversation,
        onOpenCard = onOpenCard,
    )
}

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SearchView(
    state: SearchModel,
    navController: NavController,
    bottomSheetContentProvider: BottomSheetContentProvider,
    onQueryChange: (String) -> Unit,
    goToProfile: (String) -> Unit,
    goToTag: (String) -> Unit,
    goToConversation: (UI) -> Unit,
    onOpenCard: (CardUI) -> Unit,
) {
    ModalBottomSheetLayout(
        sheetState = bottomSheetContentProvider.bottomState,
        sheetShape = RoundedCornerShape(topStart = PaddingSize1, topEnd = PaddingSize1),
        sheetContent = {
            BottomSheetContent(
                bottomSheetContentProvider = bottomSheetContentProvider,
                onShareStatus = {},
                onDelete = { statusId -> },
                onMessageSent = { _, _, _ -> },
                goToProfile = goToProfile,
                goToTag = goToTag,
                goToConversation = goToConversation,
                onMuteAccount = {},
                onBlockAccount = {},
            )
        },
    ) {
        val searchState: SearchState = rememberSearchState()
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopBar(navController, onQueryChange = onQueryChange, state.isLoading, searchState)
            }
        )
        {
            SearchResults(
                model = state,
                goToBottomSheet = bottomSheetContentProvider::showContent,
                goToProfile = goToProfile,
                goToTag = goToTag,
                goToConversation = goToConversation,
                onOpenCard = onOpenCard,
            )
        }
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

