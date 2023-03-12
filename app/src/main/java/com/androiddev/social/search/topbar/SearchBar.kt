package com.slack.exercise.search.topbar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.androiddev.social.search.topbar.SearchField
import com.androiddev.social.theme.PaddingSize0_5

/**
 * Composable used for getting search input from a user
 *
 * A parent composable is expected to pass in a deconstructed [SearchState]
 * and handle the following functionality:
 * [onQueryChange] - called anytime a user types something in [BasicTextField],
 * caller should handle [query] which is the input text
 * [onSearchFocusChange] - called when search field is focused
 * [onClearQuery] - called when clear icon is clicked
 * [onSearchAll] - called when Search All icon clicked
 */
@Composable
fun SearchBar(
    query: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit,
    searchFocused: Boolean,
    onSearchFocusChange: (Boolean) -> Unit,
    onClearQuery: () -> Unit,
    searching: Boolean,
    modifier: Modifier = Modifier,
    onSearchAll: () -> Unit
) {
  val searchBarHeight = 56.dp
  val paddingHalf = PaddingSize0_5
  Surface(
      modifier = modifier
          .fillMaxWidth()
          .height(searchBarHeight)
          .padding(top = 12.dp, bottom = 0.dp, end = 12.dp)
          .clip(RoundedCornerShape(4.dp))
  ) {
    Box(Modifier.fillMaxSize()) {
//      if (!searchFocused) SearchHint()
      Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
              .fillMaxSize()
              .wrapContentHeight()
      ) {
        SearchField(query, onQueryChange, onSearchFocusChange)
        ProgressIndicator(searching)
        ClearButton(onClearQuery, searchFocused, query)
        SearchAllButton(onSearchAll, query)
      }
    }
  }
}




