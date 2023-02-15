package com.androiddev.social.timeline.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
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
                painter = painterResource(R.drawable.home),
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
//            Image(
//                modifier = Modifier.size(100.dp),
//                painter = painterResource(R.drawable.search),
//                contentDescription = "",
//                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
//            )
        }


        OutlinedButton(
            contentPadding = PaddingValues(0.dp, 8.dp),
            border = BorderStroke(1.dp, Color.Transparent),
            onClick = { }
        ) {
            Image(
                modifier = Modifier.size(size.dp).rotate(-30f),
                painter = painterResource(R.drawable.search),
                contentDescription = "",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FAB(colorScheme: ColorScheme) {
    var clicked by remember { mutableStateOf(false) }

    val size: Float by animateFloatAsState(
        if (clicked) 1.2f else 1f,
        animationSpec = TweenSpec(durationMillis = 150)
    )
    val imageSize: Float by animateFloatAsState(
        if (clicked) 1.4f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium // with medium speed
        )
    )
    if (size == 1.2f) clicked = false
    val shape = CircleShape
    LargeFloatingActionButton(
        shape = shape,
        containerColor = colorScheme.tertiary,
        modifier = Modifier
            .offset(y = 30.dp)
            .clip(shape)
            .size((90*size).dp)
        ,
        content = {
            Image(
                modifier = Modifier
                    .size(60.dp)
                    .scale(1*imageSize)
                    .rotate(imageSize*-45f)
                    .offset(y = (-4*imageSize).dp, x = (10f/imageSize).dp)
                    .rotate(60f),
                painter = painterResource(R.drawable.horn),
                contentDescription = "",
                colorFilter = ColorFilter.tint(colorScheme.background),
            )

        },
        onClick = { clicked = !clicked }
    )
}