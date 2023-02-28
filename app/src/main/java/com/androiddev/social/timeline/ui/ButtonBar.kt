package com.androiddev.social.timeline.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.androiddev.social.theme.PaddingSize0_5
import com.androiddev.social.theme.PaddingSize1
import com.androiddev.social.theme.PaddingSize3
import com.androiddev.social.theme.ThickSm
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import social.androiddev.R

@Composable
fun ButtonBar(
    replyCount: Int? = null, boostCount: Int? = null, onBoost: () -> Unit, onReply: () -> Unit,
) {
    val iconSize = PaddingSize3



    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            contentPadding = PaddingValues(PaddingSize1, PaddingSize1),
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
                    text = " $it"
                )
            }
        }

        val icon = R.drawable.rocket3
        springyButton(onBoost, iconSize, icon, boostCount)
        OutlinedButton(
            contentPadding = PaddingValues(PaddingSize1, PaddingSize1),
            border = BorderStroke(ThickSm, Color.Transparent),
            onClick = { }
        ) {
            Image(
                modifier = Modifier.size(iconSize),
                painter = painterResource(R.drawable.bookmark_48px),
                contentDescription = "",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),

                )
        }
        OutlinedButton(
            contentPadding = PaddingValues(PaddingSize1, PaddingSize1),
            border = BorderStroke(ThickSm, Color.Transparent),
            onClick = { }
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

@Composable
private fun springyButton(
    onClick: () -> Unit,
    iconSize: Dp,
    icon: Int,
    count: Int?
) {
    var clicked by remember { mutableStateOf(false) }
    var localCount by remember { mutableStateOf(count) }

    val imageSize: Float by animateFloatAsState(
        if (clicked) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium // with medium speed
        )
    )
    if (imageSize == 1.1f) clicked = false
    val scope = rememberCoroutineScope()

    OutlinedButton(
        contentPadding = PaddingValues(PaddingSize1, PaddingSize1),
        border = BorderStroke(ThickSm, Color.Transparent),
        onClick = {
            clicked = !clicked
            scope.launch {
                delay(500)
                if (count!! + 1 >= localCount!!) localCount = count + 1
                onClick()
            }
        }
    ) {
        Image(
            modifier = Modifier
                .padding(end = PaddingSize0_5)
                .size(iconSize)
                .scale(1 * imageSize)
                .rotate(imageSize * -45f)
                .offset(y = (1 * imageSize).dp, x = (2 * imageSize).dp)
                .rotate(50f),
            painter = painterResource(icon),
            contentDescription = "",
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
        )
        localCount?.let {
            Text(
                text = " $it"
            )
        }
    }
}