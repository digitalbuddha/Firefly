package com.slack.exercise.search.topbar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Icon Button which expects caller to handle [onClearQuery] when clicked
 * Since compose doesn't have a View.Hide, we set alpha to 0 when "hiding"
 * This keeps rest of layout from jumping
 */
@Composable
fun ClearButton(onClearQuery: () -> Unit, searchFocused: Boolean, query: TextFieldValue) {
  val alpha: Float by animateFloatAsState(if (searchFocused && query.text.isNotEmpty()) 1f else 0f)
  IconButton(onClick = onClearQuery, modifier = Modifier.alpha(alpha)) {
    Icon(
        imageVector = Icons.Rounded.Clear,
        contentDescription = "Clear Icon"
    )
  }
}

