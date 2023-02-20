package com.androiddev.social.timeline.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.androiddev.social.R
import com.androiddev.social.theme.PaddingSize1
import com.androiddev.social.theme.PaddingSize3
import com.androiddev.social.theme.ThickSm

@Composable
fun ButtonBar(replyCount: Int? = null, boostCount: Int? = null, onReply: () -> Unit, ){
    val iconSize = PaddingSize3
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            contentPadding =  PaddingValues(PaddingSize1, PaddingSize1),
            border = BorderStroke(ThickSm, Color.Transparent),
            onClick = onReply
        ) {
            Image(
                modifier = Modifier.size(iconSize),
                painter = painterResource(R.drawable.reply),
                contentDescription = "",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )
            replyCount?.let {
                Text(
//                    color = MaterialTheme.color??Scheme.onSurface,
                    text= " $it")
            }
        }

        OutlinedButton(
            contentPadding =  PaddingValues(PaddingSize1, PaddingSize1),
            border = BorderStroke(ThickSm, Color.Transparent),
            onClick = {  }
        ) {
            Image(
                modifier = Modifier.size(iconSize),
                painter = painterResource(R.drawable.rocket3),
                contentDescription = "",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            )
            boostCount?.let {
                Text(
//                    color = MaterialTheme.colorScheme.tertiary,
                    text = " $it")
            }
        }
        OutlinedButton(
            contentPadding =  PaddingValues(PaddingSize1, PaddingSize1),
            border = BorderStroke(ThickSm, Color.Transparent),
            onClick = {  }
        ) {
            Image(
                modifier = Modifier.size(iconSize),
                painter = painterResource(R.drawable.bookmark_48px),
                contentDescription = "",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),

            )
        }
        OutlinedButton(
            contentPadding =  PaddingValues(PaddingSize1, PaddingSize1),
            border = BorderStroke(ThickSm, Color.Transparent),
            onClick = {  }
        ) {
            Image(
                modifier = Modifier.size(iconSize),
                painter = painterResource(R.drawable.share),
                contentDescription = "",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),

            )
        }
    }
}