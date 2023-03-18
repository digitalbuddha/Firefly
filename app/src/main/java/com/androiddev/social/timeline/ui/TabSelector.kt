package com.androiddev.social.timeline.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.androiddev.social.tabselector.Tab
import com.androiddev.social.tabselector.TabSelectorDetail
import com.androiddev.social.theme.PaddingSize4

@Composable
fun TabSelector(items: MutableList<Tab>, selectedIndex: Int, expanded:Boolean, expand: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.clickable(onClick = {
            expand(true)
        }),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            modifier = Modifier
                .size(PaddingSize4)
                .padding(4.dp),
            painter = painterResource(items[selectedIndex].image),
            contentDescription = "",
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
        )
        Row() {
            Text(

                text = items[selectedIndex].name,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge
            )
            Icon(
                imageVector = Icons.Outlined.ArrowDropDown,
                contentDescription = "down arrow",
                modifier = Modifier.align(Bottom),
                tint = MaterialTheme.colorScheme.secondary
            )
            DropdownMenu(
                offset = DpOffset(x = (-100).dp, y = (10).dp),
                expanded = expanded,
                onDismissRequest = { expand(false) },
                modifier = Modifier
                    .wrapContentSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .clip(RoundedCornerShape(4.dp))
            ) {

                TabSelectorDetail(
                    items, selectedIndex
                )
            }
        }


    }
}

data class TabPosition(var position: Int)
