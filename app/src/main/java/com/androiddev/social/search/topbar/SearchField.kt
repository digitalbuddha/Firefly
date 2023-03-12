package com.androiddev.social.search.topbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun RowScope.SearchField(query: TextFieldValue, onQueryChange: (TextFieldValue) -> Unit, onSearchFocusChange: (Boolean) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(key1 = Unit){
        focusRequester.requestFocus()
    }


    BasicTextField(
      value = query,
      onValueChange = onQueryChange,
      singleLine = true,
      modifier = Modifier
          .focusRequester(focusRequester)
          .padding(start = 12.dp, top = 8.dp)
          .weight(1f)
          .onFocusChanged {
              onSearchFocusChange(it.isFocused)
          }
  )
}