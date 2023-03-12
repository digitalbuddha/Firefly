package com.slack.exercise.search.topbar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun SearchHint() {
  Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
          .fillMaxSize()
          .wrapContentSize()
  ) {
    Text(
        text = "Search",
    )
  }
}