package com.androiddev.social.timeline.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.androiddev.social.R

import com.androiddev.social.theme.PaddingSize3
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DirectMessage(directMessage: Boolean) {
    if (directMessage) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            AssistChip(
                border = AssistChipDefaults.assistChipBorder(borderColor = colorScheme.onErrorContainer),
                colors = AssistChipDefaults.assistChipColors(
                    leadingIconContentColor = colorScheme.secondary.copy(
                        alpha = .5f
                    ), labelColor = colorScheme.secondary.copy(alpha = .9f),
                    containerColor = colorScheme.onErrorContainer

                ),
                shape = RoundedCornerShape(50, 50, 50, 50),
                onClick = { /* Do something! */ },
                label = { Text("Private Message", style = MaterialTheme.typography.labelLarge) },
                leadingIcon = {
                    Image(
                        modifier = Modifier.height(PaddingSize3),
                        painter = painterResource(R.drawable.mail),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(Color.White.copy(alpha = .8f)),
                    )
                },
            )
        }
    }
}