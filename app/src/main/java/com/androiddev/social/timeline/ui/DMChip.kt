package com.androiddev.social.timeline.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.dp
import com.androiddev.social.theme.PaddingSize2
import social.androiddev.firefly.R
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DirectMessage(directMessage: Boolean) {
    if (directMessage) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .height(20.dp)
        ) {
            AssistChip(
//                border = AssistChipDefaults.assistChipBorder(borderColor =  Pink40.copy(alpha = .2f)),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = colorScheme.primary,
                    leadingIconContentColor = colorScheme.secondary, labelColor = colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(25, 25, 25, 25),
                onClick = { /* Do something! */ },
                label = {
                    Text("Private", style = MaterialTheme.typography.labelSmall)
                },
                leadingIcon = {
                    Image(
                        modifier = Modifier.height(PaddingSize2),
                        painter = painterResource(R.drawable.mail),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(colorScheme.onPrimary),
                    )
                },
            )
        }
    }
}