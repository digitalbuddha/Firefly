package com.slack.exercise.search.topbar

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun SearchAllButton(onSearchAll: () -> Unit, query: TextFieldValue) {
  if (query.text.isEmpty()) {
    IconButton(onClick = onSearchAll) {
      Icon(
          imageVector = Icons.Rounded.Search,
          contentDescription = "Search All"
      )
    }
  }
}