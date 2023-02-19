package com.androiddev.social.timeline.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.androiddev.social.R
import com.androiddev.social.theme.PaddingSize0_5
import com.androiddev.social.theme.PaddingSize1
import com.androiddev.social.theme.PaddingSize3

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Boosted(boostedBy: String?, boostedAvatar: String?) {
    boostedBy?.let {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = PaddingSize0_5, bottom = PaddingSize1)
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
                label = { Text(boostedBy, style = MaterialTheme.typography.labelLarge) },
                leadingIcon = {
                    AvatarImage(size = PaddingSize3, url = boostedAvatar)
                },
                trailingIcon = {
                    Image(
                        modifier = Modifier.height(PaddingSize3),
                        painter = painterResource(R.drawable.rocket3),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(colorScheme.onPrimary.copy(alpha = .5f)),
                    )

                }
            )
        }
    }
}