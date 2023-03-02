package com.androiddev.social.timeline.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.androiddev.social.theme.PaddingSize0_5
import com.androiddev.social.theme.PaddingSize3
import com.androiddev.social.theme.ThickSm
import com.androiddev.social.timeline.data.Account
import social.androiddev.R

@Composable
fun Profile(
    onProfileClick: () -> Unit = {},
    onChangeTheme: () -> Unit = {},
    onNewAccount: () -> Unit = {},
    account: Account?
) {
    var expanded by remember { mutableStateOf(false) }

    Row(modifier = Modifier) {
        AvatarImage(url = account?.avatar, onClick = { expanded = true })
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
                        AvatarImage(url = it.avatar, onClick = { expanded = true })
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
                onChangeTheme()
            }, text = {
                Row {
                    Image(
                        modifier = Modifier.size(PaddingSize3),
                        painter = painterResource(R.drawable.theme),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        "Switch Theme",
                        modifier = Modifier
                            .padding(PaddingSize0_5)
                            .align(Alignment.CenterVertically)
                    )
                }
            })

        }
    }

}
