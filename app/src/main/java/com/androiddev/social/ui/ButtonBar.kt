package com.androiddev.social.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.androiddev.social.R

@Composable
fun ButtonBar(){
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        IconButton(
            onClick = {  }
        ) {
            Image(
                painter = painterResource(R.drawable.reply_o),
                contentDescription = "",
                colorFilter = ColorFilter.tint(Color.White)
            )
        }
        IconButton(
            onClick = {  }
        ) {
            Image(
                painter = painterResource(R.drawable.rocket),
                contentDescription = "",
                colorFilter = ColorFilter.tint(Color.White),
            )

        }
        IconButton(
            onClick = {  }
        ) {
            Image(
                painter = painterResource(R.drawable.bookmark_48px),
                contentDescription = "",
                colorFilter = ColorFilter.tint(Color.White),
            )
        }
        IconButton(
            onClick = {  }
        ) {
            Image(
                painter = painterResource(R.drawable.share),
                contentDescription = "",
                colorFilter = ColorFilter.tint(Color.White),
            )
        }
        IconButton(
            onClick = {  }
        ) {
            Image(
                painter = painterResource(R.drawable.settings),
                contentDescription = "",
                colorFilter = ColorFilter.tint(Color.White),
            )
        }
    }
}