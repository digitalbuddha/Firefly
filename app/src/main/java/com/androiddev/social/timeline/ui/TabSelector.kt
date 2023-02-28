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
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import social.androiddev.R
import com.androiddev.social.theme.PaddingSize4
import com.androiddev.social.theme.ThickSm

@Composable
fun TabSelector( onClick: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val items = listOf(
        "Home" to R.drawable.house,
        "Local" to R.drawable.local,
        "Federated" to R.drawable.world,
        "Trending" to R.drawable.star
    )
    var selectedIndex by remember { mutableStateOf(0) }
    Row(modifier = Modifier.clickable(onClick = { expanded = true })) {
        Image(
            modifier =Modifier.size(PaddingSize4).align(CenterVertically),
            painter = painterResource(items[selectedIndex].second),
            contentDescription = "",
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary)
        )
        Text(
            modifier = Modifier.align(CenterVertically),
            text = items[selectedIndex].first,
            color = MaterialTheme.colorScheme.secondary,
            style =  MaterialTheme.typography.headlineSmall
        )
        Icon(
            imageVector = Icons.Outlined.ArrowDropDown,
            contentDescription = "down arrow",
            modifier = Modifier.align(Bottom),
            tint = MaterialTheme.colorScheme.secondary
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
                    onClick( items[selectedIndex].first)
                }, text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Image(
                            modifier = Modifier.size(PaddingSize4),
                            painter = painterResource(items[index].second),
                            contentDescription = "",
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary)
                        )
                        Text(text = " " + items[index].first)
                    }

                })
                if (index != items.lastIndex)
                    Divider(thickness = ThickSm, color = Color.Gray.copy(alpha = .1f))
            }
        }
    }
}
