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
import androidx.compose.ui.unit.dp
import com.androiddev.social.R

@Composable
fun ButtonBar(replyCount:Int? = null, boostCount:Int? = null, ){
    val iconSize = 24.dp
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            contentPadding =  PaddingValues(8.dp, 8.dp),
            border = BorderStroke(1.dp, Color.Transparent),
            onClick = {  }
        ) {
            Image(
                modifier = Modifier.size(iconSize),
                painter = painterResource(R.drawable.reply),
                contentDescription = "",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary)
            )
            replyCount?.let {
                Text(color = MaterialTheme.colorScheme.tertiary,text= " $it")
            }
        }

        OutlinedButton(
            contentPadding =  PaddingValues(8.dp, 8.dp),
            border = BorderStroke(1.dp, Color.Transparent),
            onClick = {  }
        ) {
            Image(
                modifier = Modifier.size(iconSize),
                painter = painterResource(R.drawable.rocket3),
                contentDescription = "",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
            )
            boostCount?.let {
                Text(color = MaterialTheme.colorScheme.tertiary, text = " $it")
            }
        }
        OutlinedButton(
            contentPadding =  PaddingValues(8.dp, 8.dp),
            border = BorderStroke(1.dp, Color.Transparent),
            onClick = {  }
        ) {
            Image(
                modifier = Modifier.size(iconSize),
                painter = painterResource(R.drawable.bookmark_48px),
                contentDescription = "",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
            )
        }
        OutlinedButton(
            contentPadding =  PaddingValues(8.dp, 8.dp),
            border = BorderStroke(1.dp, Color.Transparent),
            onClick = {  }
        ) {
            Image(
                modifier = Modifier.size(iconSize),
                painter = painterResource(R.drawable.share),
                contentDescription = "",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
            )
        }
    }
}