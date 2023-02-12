package com.androiddev.social.timeline.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Profile(
    userName: String,
    onProfileClick: () -> Unit = {},
    onSettingsClicked: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    Row(modifier = Modifier.clickable(onClick = { expanded = true })) {
        Avatar(showIcon = false)
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .wrapContentSize()
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = .9f)
                )
        ) {

            DropdownMenuItem(onClick = {
                expanded = false
                onProfileClick()
            }, text = {
                Row {
                    Avatar(showIcon = false)
                    Text(
                        userName,
                        modifier = Modifier
                            .padding(4.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            }
            )

            Divider(thickness = 1.dp, color = Color.Gray)

            DropdownMenuItem(onClick = {
                expanded = false
                onSettingsClicked()
            },
                text = {
                    Row {
                        Icon(
                            Icons.Outlined.Settings,
                            contentDescription = "Localized description"
                        )
                        Text(
                            "Settings",
                            modifier = Modifier
                                .padding(4.dp)
                                .align(Alignment.CenterVertically)
                        )
                    }
                }
            )
        }
    }
}