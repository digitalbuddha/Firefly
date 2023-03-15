package com.androiddev.social.accounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.items
import com.androiddev.social.theme.PaddingSize1
import com.androiddev.social.theme.PaddingSize7
import com.androiddev.social.timeline.data.Account
import com.androiddev.social.timeline.ui.AvatarImage
import com.androiddev.social.ui.util.emojiText

@Composable
fun AccountTab(
    results: List<Account>? = null,
    resultsPaging: LazyPagingItems<Account>? = null,
    goToProfile: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (results != null)
            items(results, key = { it.id }) {
                Column {
                    content(goToProfile, it)
                }
                Divider()
            }
        else {
            items(
                items = resultsPaging!!,
                key = { it.id }) {
                if (it != null)
                    Column {
                        content(goToProfile, it)
                    }
                Divider()
            }
        }
    }
}

@Composable
private fun content(
    goToProfile: (String) -> Unit,
    it: Account
) {
    Row(modifier = Modifier
        .clickable { goToProfile(it.id) }
        .padding(PaddingSize1)) {
        AvatarImage(PaddingSize7, it.avatar, onClick = { goToProfile(it.id) })
        Column(Modifier.padding(start = PaddingSize1)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val text = emojiText(
                    it.displayName,
                    emptyList(),
                    emptyList(),
                    it.emojis,
                    colorScheme
                )
                Text(
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.Top),
                    text = text.text,
                    style = MaterialTheme.typography.titleLarge,
                    inlineContent = text.mapping,
                )
                Text(
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = colorScheme.secondary,
                    modifier = Modifier
                        .align(Alignment.Top),
                    text = "Followers ${it.followersCount}",
                    style = MaterialTheme.typography.titleMedium,
                )

            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    color = colorScheme.secondary,
                    text = it.username,
                    style = MaterialTheme.typography.titleMedium,
                )

                Text(
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = colorScheme.secondary,
                    modifier = Modifier
                        .align(Alignment.Top),
                    text = "Following ${it.followingCount} ",
                    style = MaterialTheme.typography.titleMedium,
                )

            }
        }
    }
}