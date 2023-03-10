package com.androiddev.social.timeline.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.androiddev.social.theme.PaddingSize1
import com.androiddev.social.theme.PaddingSizeNone
import com.androiddev.social.theme.ThickSm
import social.androiddev.firefly.R

@Composable
fun BottomBar(
    goToMentions: () -> Unit,
    goToNotifications: () -> Unit,
) {
    val size = 24
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()

    ) {
        OutlinedButton(
            contentPadding = PaddingValues(PaddingSizeNone, PaddingSize1),
            border = BorderStroke(ThickSm, Color.Transparent),
            onClick = goToMentions
        ) {
            Image(
                modifier = Modifier.size(size.dp),
                painter = painterResource(R.drawable.at),
                contentDescription = "",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )
        }

        //placeholder for spacing
        OutlinedButton(
            border = BorderStroke(ThickSm, Color.Transparent),
            onClick = { }
        ) {
        }


        OutlinedButton(
            contentPadding = PaddingValues(PaddingSizeNone, PaddingSize1),
            border = BorderStroke(ThickSm, Color.Transparent),
            onClick = goToNotifications
        ) {
            Image(
                modifier = Modifier
                    .size(28.dp)
                    .rotate(0f),
                painter = painterResource(R.drawable.notification),
                contentDescription = "",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            )
        }
    }
}
