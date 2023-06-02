package com.androiddev.social.search.topbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.slack.exercise.search.SearchState

@Composable
fun TopBar(
    navController: NavController,
    onQueryChange: (String) -> Unit,
    isLoading: Boolean,
    state: SearchState
) {
    TopAppBar(
        backgroundColor = MaterialTheme.colorScheme.surface,
        title = {  state.searching = isLoading
            SearchBar(
                query = state.query,
                onQueryChange = {
                    state.query = it
                    onQueryChange(it.text)
                },
                searchFocused = state.focused,
                onSearchFocusChange = { state.focused = it },
                onClearQuery = { state.query = TextFieldValue(""); },
                searching = state.searching,
                onSearchAll = { }
            ) },
        navigationIcon = {
            if (navController.previousBackStackEntry != null) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        tint = MaterialTheme.colorScheme.onSurface,
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "search"
                    )
                }
            } else {
                null
            }
        },
        modifier = Modifier.background(Color.White).height(40.dp),
        actions = {
        }
    )
}

