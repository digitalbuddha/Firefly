package com.androiddev.social.timeline.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.items
import com.androiddev.social.timeline.ui.model.UI

@Composable
fun TimelineScreen(ui: LazyPagingItems<UI>) {
    LazyColumn {
        items(ui) {
            it?.let {
                TimelineCard(it)
            }
        }
    }
}
