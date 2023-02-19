package com.androiddev.social.timeline.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.androiddev.social.R
import com.androiddev.social.theme.PaddingSize4
import com.androiddev.social.theme.ThickSm

@Composable
fun TabSelector() {
    var expanded by remember { mutableStateOf(false) }
    val items = listOf(
        "Home" to R.drawable.house,
        "Local" to R.drawable.local,
        "Federated" to R.drawable.world,
        "Favorites" to R.drawable.star
    )
    var selectedIndex by remember { mutableStateOf(0) }
    Row(modifier = Modifier.clickable(onClick = { expanded = true })) {
        Image(
            modifier =Modifier.size(28.dp),
            painter = painterResource(items[selectedIndex].second),
            contentDescription = "",
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary)
        )
        Text(
            text = items[selectedIndex].first,
            color = MaterialTheme.colorScheme.secondary
        )
        Icon(
            Icons.Outlined.ArrowDropDown,
            contentDescription = "down arrow",
            modifier = Modifier.align(Bottom),
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .wrapContentSize()
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = .9f)
                )
        ) {
            items.forEachIndexed { index, s ->
                DropdownMenuItem(onClick = {
                    selectedIndex = index
                    expanded = false
                }, text = {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Image(
                            modifier =Modifier.size(PaddingSize4),
                            painter = painterResource(items[index].second),
                            contentDescription = "",
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary)
                        )
                        Text(text =   " "+items[index].first,)
                    }

                })
                if (index != items.lastIndex)
                    Divider(thickness = ThickSm, color = Color.Gray.copy(alpha = .1f))
            }
        }
    }
}
