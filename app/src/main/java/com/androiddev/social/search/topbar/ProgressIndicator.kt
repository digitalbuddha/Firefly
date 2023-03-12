package com.slack.exercise.search.topbar

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.ButtonDefaults.IconSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProgressIndicator(progress: Boolean) {
  if (progress) {
    CircularProgressIndicator(
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .size(36.dp)
    )
  } else {
    Spacer(Modifier.width(IconSize)) // balance arrow icon
  }
}