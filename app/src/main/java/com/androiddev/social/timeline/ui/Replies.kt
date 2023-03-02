package com.androiddev.social.timeline.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Replies() {
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
                Text("Replies", style = MaterialTheme.typography.labelLarge)
            },
        )
    }
}
