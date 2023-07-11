package com.androiddev.social.timeline.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import social.androiddev.firefly.R

@Composable
fun FAB(
    visible: Boolean,
    colorScheme: ColorScheme,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val size: Float by animateFloatAsState(
        if (visible) 1f else 0f,
        animationSpec = TweenSpec(durationMillis = 150), label = ""
    )
    val imageSize: Float by animateFloatAsState(
        if (visible) 1f else 1.2f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium // with medium speed
        ), label = ""
    )

    val shape = CircleShape

    LargeFloatingActionButton(
        shape = shape,
        containerColor = colorScheme.primary,
        modifier = modifier
            .offset(y = 30.dp)
            .clip(shape)
            .size((90 * size).dp),
        content = {
            if (visible) {
                Image(
                    modifier = Modifier
                        .size(60.dp)
                        .scale(1 * imageSize)
                        .rotate(imageSize * -45f)
                        .offset(y = (-4 * imageSize).dp, x = (10f / imageSize).dp)
                        .rotate(60f),
                    painter = painterResource(R.drawable.horn),
                    contentDescription = "",
                    colorFilter = ColorFilter.tint(colorScheme.background),
                )
            }
        },
        onClick = {
            onClick()
        }
    )
}

