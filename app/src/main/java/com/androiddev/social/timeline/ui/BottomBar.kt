package com.androiddev.social.timeline.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import social.androiddev.R
import com.androiddev.social.theme.PaddingSize1
import com.androiddev.social.theme.PaddingSizeNone
import com.androiddev.social.theme.ThickSm

@Composable
fun BottomBar(replyCount: Int? = null, boostCount: Int? = null) {
    val size = 24
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()

    ) {
        OutlinedButton(
            contentPadding = PaddingValues(PaddingSizeNone, PaddingSize1),
            border = BorderStroke(ThickSm, Color.Transparent),
            onClick = { }
        ) {
            Image(
                modifier = Modifier.size(size.dp),
                painter = painterResource(R.drawable.at),
                contentDescription = "",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
            )
            replyCount?.let {
                Text(color = MaterialTheme.colorScheme.secondary, text = " $it")
            }
        }

        //placeholder for spacing
        OutlinedButton(
            border = BorderStroke(ThickSm, Color.Transparent),
            onClick = { }
        ) {
//            Image(
//                modifier = Modifier.size(100.dp),
//                painter = painterResource(R.drawable.search),
//                contentDescription = "",
//                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
//            )
        }


        OutlinedButton(
            contentPadding = PaddingValues(PaddingSizeNone, PaddingSize1),
            border = BorderStroke(ThickSm, Color.Transparent),
            onClick = { }
        ) {
            Image(
                modifier = Modifier
                    .size(size.dp)
                    .rotate(-30f),
                painter = painterResource(R.drawable.search),
                contentDescription = "",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
            )
        }
    }
}
