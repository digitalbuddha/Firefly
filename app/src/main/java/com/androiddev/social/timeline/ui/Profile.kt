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
import com.androiddev.social.theme.PaddingSize0_5
import com.androiddev.social.theme.ThickSm
import com.androiddev.social.timeline.data.Account

@Composable
fun Profile(
    onProfileClick: () -> Unit = {}, onSettingsClicked: () -> Unit = {}, account: Account?
) {
    var expanded by remember { mutableStateOf(false) }
    Row(modifier = Modifier.clickable(onClick = { expanded = true })) {
        account?.let { AvatarImage(url = it.avatar) }
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
                    account?.let { it ->
                        AvatarImage(url = it.avatar)
                        val emojis = account.emojis


                        val unformatted = account.displayName
                        val (inlineContentMap, text) = inlineEmojis(
                            unformatted,
                            emojis
                        )

                        Text(
                            modifier = Modifier
                                .padding(PaddingSize0_5)
                                .align(Alignment.CenterVertically),
                            text = text,
                            inlineContent = inlineContentMap
                        )
                    }

                }

            })

            Divider(thickness = ThickSm, color = Color.Gray)

            DropdownMenuItem(onClick = {
                expanded = false
                onSettingsClicked()
            }, text = {
                Row {
                    Icon(
                        Icons.Outlined.Settings, contentDescription = "Localized description"
                    )
                    Text(
                        "Settings",
                        modifier = Modifier
                            .padding(PaddingSize0_5)
                            .align(Alignment.CenterVertically)
                    )
                }
            })
        }
    }
}

