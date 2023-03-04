package com.androiddev.social.timeline.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.androiddev.social.theme.PaddingSize3
import social.androiddev.R

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
//                border = AssistChipDefaults.assistChipBorder(borderColor =  Pink40.copy(alpha = .2f)),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = colorScheme.primary,
                    leadingIconContentColor = colorScheme.secondary.copy(
                        alpha = .5f
                    ), labelColor = colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(50, 50, 50, 50),
                onClick = { /* Do something! */ },
                label = {
                    Text("Private Mention", style = MaterialTheme.typography.labelLarge)
                },
                leadingIcon = {
                    Image(
                        modifier = Modifier.height(PaddingSize3),
                        painter = painterResource(R.drawable.mail),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(colorScheme.onPrimary.copy(alpha = .5f)),
                    )
                },
            )
        }
    }
}