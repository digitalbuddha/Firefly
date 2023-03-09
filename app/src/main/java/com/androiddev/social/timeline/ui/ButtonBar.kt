@file:OptIn(ExperimentalMaterialApi::class)

package com.androiddev.social.timeline.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
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
import androidx.compose.ui.unit.dp
import com.androiddev.social.theme.PaddingSize0_5
import com.androiddev.social.theme.PaddingSize1
import com.androiddev.social.theme.PaddingSize3
import com.androiddev.social.theme.ThickSm
import com.androiddev.social.timeline.ui.model.UI
import kotlinx.coroutines.launch
import social.androiddev.R

@Composable
fun ButtonBar(
    status: UI,
    replyCount: Int? = null,
    boostCount: Int? = null,
    favoriteCount: Int? = null,
    favorited: Boolean,
    boosted: Boolean,
    hasParent: Boolean,
    showInlineReplies: Boolean,
    onBoost: () -> Unit,
    onFavorite: () -> Unit,
    onReply: () -> Unit,
    showReply: Boolean,
    onShowReplies: () -> Unit,
    goToConversation: (UI) -> Unit,
    goToProfile: (String) -> Unit
) {
    Column {
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                contentPadding = PaddingValues(PaddingSize1, 0.dp),
                border = BorderStroke(ThickSm, Color.Transparent),
                onClick = onReply
            ) {
                Image(
                    modifier = Modifier.size(PaddingSize3),
                    painter = painterResource(R.drawable.reply),
                    contentDescription = "",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
            }

            val icon = if (boosted) R.drawable.rocketfilled else R.drawable.rocket3
            SpringyButton(onBoost, icon, boostCount)
            SpringyButton(
                onFavorite,
                if (favorited) R.drawable.starfilled else R.drawable.star,
                favoriteCount
            )
            if ((replyCount != null && replyCount > 0) || hasParent) {
                OutlinedButton(
                    contentPadding = PaddingValues(PaddingSize1, 0.dp),
                    border = BorderStroke(ThickSm, Color.Transparent),
                    onClick = {
                        onShowReplies()
                    }
                ) {
                    Image(
                        modifier = Modifier.size(PaddingSize3),
                        painter = painterResource(R.drawable.chat),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )
                    replyCount?.let {
                        Text(
                            text = " $it"
                        )
                    }
                }
            } else {
                //placeholder I am bad at code
                OutlinedButton(
                    contentPadding = PaddingValues(PaddingSize1, 0.dp),
                    border = BorderStroke(ThickSm, Color.Transparent),
                    onClick = {
                    }
                ) {
                    Image(
                        modifier = Modifier.size(PaddingSize3),
                        painter = painterResource(R.drawable.chat),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(Color.Gray)
                    )
                }
            }
        }
        AnimatedVisibility(visible = showReply && showInlineReplies) {
            After(status = status, goToConversation = goToConversation, goToProfile = goToProfile)
        }
    }

}

@Composable
private fun SpringyButton(
    onClick: () -> Unit,
    icon: Int,
    count: Int?
) {
    val iconSize = PaddingSize3

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
        contentPadding = PaddingValues(PaddingSize1, 0.dp),
        border = BorderStroke(ThickSm, Color.Transparent),
        onClick = {
            clicked = !clicked
            scope.launch {
//                delay(500)
//                if (count != null && count + 1 >= localCount!!) localCount = count + 1
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
                .rotate(45f),
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