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
fun BottomBar(replyCount: Int? = null, boostCount: Int? = null) {
    val size = 36
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()

    ) {
        OutlinedButton(
            contentPadding = PaddingValues(0.dp, 8.dp),
            border = BorderStroke(1.dp, Color.Transparent),
            onClick = { }
        ) {
            Image(
                modifier = Modifier.size(size.dp),
                painter = painterResource(R.drawable.house),
                contentDescription = "",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary)
            )
            replyCount?.let {
                Text(color = MaterialTheme.colorScheme.secondary, text = " $it")
            }
        }

        //placeholder for spacing
        OutlinedButton(
            border = BorderStroke(1.dp, Color.Transparent),
            onClick = { }
        ) {

        }


        OutlinedButton(
            contentPadding = PaddingValues(0.dp, 8.dp),
            border = BorderStroke(1.dp, Color.Transparent),
            onClick = { }
        ) {
            Image(
                modifier = Modifier.size(size.dp),
                painter = painterResource(R.drawable.search),
                contentDescription = "",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
            )
        }
    }
}