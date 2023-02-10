package com.androiddev.social.ui

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
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            contentPadding =  PaddingValues(8.dp, 8.dp),
            border = BorderStroke(1.dp, Color.Transparent),
            onClick = {  }
        ) {
            Image(
                modifier = Modifier.size(20.dp),
                painter = painterResource(R.drawable.reply_o),
                contentDescription = "",
                colorFilter = ColorFilter.tint(Color.White)
            )
            replyCount?.let {
                Text(color = MaterialTheme.colorScheme.secondary,text= " $it")
            }
        }

        OutlinedButton(
            contentPadding =  PaddingValues(8.dp, 8.dp),
            border = BorderStroke(1.dp, Color.Transparent),
            onClick = {  }
        ) {
            Image(
                modifier = Modifier.size(20.dp),
                painter = painterResource(R.drawable.rocket),
                contentDescription = "",
                colorFilter = ColorFilter.tint(Color.White),
            )
            boostCount?.let {
                Text(color = MaterialTheme.colorScheme.secondary, text = " $it")
            }
        }
        OutlinedButton(
            contentPadding =  PaddingValues(8.dp, 8.dp),
            border = BorderStroke(1.dp, Color.Transparent),
            onClick = {  }
        ) {
            Image(
                modifier = Modifier.size(20.dp),
                painter = painterResource(R.drawable.bookmark_48px),
                contentDescription = "",
                colorFilter = ColorFilter.tint(Color.White),
            )
        }
        OutlinedButton(
            contentPadding =  PaddingValues(8.dp, 8.dp),
            border = BorderStroke(1.dp, Color.Transparent),
            onClick = {  }
        ) {
            Image(
                modifier = Modifier.size(20.dp),
                painter = painterResource(R.drawable.share),
                contentDescription = "",
                colorFilter = ColorFilter.tint(Color.White),
            )
        }

        OutlinedButton(
            contentPadding =  PaddingValues(8.dp, 8.dp),
            border = BorderStroke(1.dp, Color.Transparent),
            onClick = {  }
        ) {
            Image(
                modifier = Modifier.size(20.dp),
                painter = painterResource(R.drawable.settings),
                contentDescription = "",
                colorFilter = ColorFilter.tint(Color.White)
            )
        }
    }
}