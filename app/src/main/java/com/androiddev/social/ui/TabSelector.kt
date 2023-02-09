package com.androiddev.social.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TabSelector() {
    var expanded by remember { mutableStateOf(false) }
    val items = listOf("Home", "Local", "Federated")
    var selectedIndex by remember { mutableStateOf(0) }
    Row(modifier = Modifier.clickable(onClick = { expanded = true })) {
        Text(
            text = items[selectedIndex],
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
                    MaterialTheme.colorScheme.primary.copy(alpha = .1f)
                )
        ) {
            items.forEachIndexed { index, s ->
                DropdownMenuItem(onClick = {
                    selectedIndex = index
                    expanded = false
                }, text = { Text(items[index]) })
                if (index != items.lastIndex)
                    Divider(thickness = 1.dp, color = Color.Gray.copy(alpha = .1f))
            }
        }
    }
}
