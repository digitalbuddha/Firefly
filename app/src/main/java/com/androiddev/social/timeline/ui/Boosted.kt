package com.androiddev.social.timeline.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.androiddev.social.R
import com.androiddev.social.timeline.ui.theme.Pink40

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Boosted(boostedBy: String?) {
    boostedBy?.let {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 8.dp)
        ) {
            AssistChip(
                border = AssistChipDefaults.assistChipBorder(borderColor =  Pink40.copy(alpha = .2f)),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Pink40.copy(alpha = .2f),
                    leadingIconContentColor = colorScheme.secondary.copy(
                        alpha = .5f
                    ), labelColor = colorScheme.secondary.copy(alpha = .7f)
                ),
                shape = RoundedCornerShape(50, 50, 50, 50),
                onClick = { /* Do something! */ },
                label = { Text(boostedBy) },
                leadingIcon = {
                    Image(size = 24.dp, showIcon = false)
                },
                trailingIcon = {
                    Image(
                        modifier = Modifier.height(24.dp),
                        painter = painterResource(R.drawable.rocket3),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(Color.White.copy(alpha = .5f)),
                    )
                }
            )
        }
    }
}