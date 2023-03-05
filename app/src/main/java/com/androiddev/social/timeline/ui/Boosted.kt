package com.androiddev.social.timeline.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.androiddev.social.theme.PaddingSize0_5
import com.androiddev.social.theme.PaddingSize1
import com.androiddev.social.theme.PaddingSize3
import com.androiddev.social.timeline.data.Emoji
import com.androiddev.social.ui.util.emojiText

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Boosted(boostedBy: String?, boostedAvatar: String?, boostedEmojis: List<Emoji>?, drawable: Int) {
    boostedBy?.let {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = PaddingSize0_5, bottom = PaddingSize1)
        ) {
            AssistChip(
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = colorScheme.primary,
                    leadingIconContentColor = colorScheme.secondary, labelColor = colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(50, 50, 50, 50),
                onClick = { /* Do something! */ },
                label = {
                    val (mapping, text) = emojiText(
                        boostedBy,
                        emptyList(),
                        emptyList(),
                        boostedEmojis
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge,
                        inlineContent = mapping
                    )
                },
                leadingIcon = {
                    AvatarImage(size = PaddingSize3, url = boostedAvatar)
                },
                trailingIcon = {
                    Image(
                        modifier = Modifier.height(PaddingSize3),
                        painter = painterResource(drawable),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(colorScheme.onPrimary),
                    )

                }
            )
        }
    }
}