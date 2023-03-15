package com.androiddev.social.tabselector

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.androiddev.social.theme.PaddingSize0_5
import com.androiddev.social.theme.PaddingSize2



data class Tab(val name: String, val image: Int, val onClick: () -> Unit)

@Composable
fun TabSelectorDetail(
    tabs: List<Tab>,
    selectedIndex: Int
) {
    val configuration = LocalConfiguration.current

    val screenHeight = configuration.screenWidthDp
    LazyVerticalGrid(
        // on below line we are setting the
        // column count for our grid view.
        columns = GridCells.Fixed(3),
        // on below line we are adding padding
        // from all sides to our grid view.

        modifier = Modifier
            .width((screenHeight).dp)
            .height(98.dp)
            .clip(RoundedCornerShape(4.dp))

    ) {
        itemsIndexed(tabs) { index, collection ->
            TabGrid(collection, index, selectedIndex)
        }
    }

}

@Composable
private fun TabGrid(
    tab: Tab,
    index: Int,
    selectedIndex: Int
) {
    Column() {

        val color1 = colorScheme.tertiary
        val color2 = colorScheme.onTertiary

        val gradient = when (index % 2) {
            0 -> listOf(color1, color2) //was gradient
            else -> listOf(color1, color2) //was gradient
        }
        TabContent(
            tab = tab
        )
    }
    Spacer(Modifier.height(4.dp))
}


private val MinImageSize = 134.dp
private val CategoryShape = RoundedCornerShape(10.dp)
private const val CategoryTextProportion = 0.55f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabContent(
    tab: Tab
) {

    AssistChip(
        modifier =  Modifier
            .height(48.dp)
            .width(130.dp)
            .padding(horizontal = PaddingSize0_5, vertical = PaddingSize0_5),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = colorScheme.surface,
            leadingIconContentColor = colorScheme.secondary, labelColor = colorScheme.primary
        ),
        shape = RoundedCornerShape(25, 25, 25, 25),
        onClick = tab.onClick,
        label = {

            Text(
                modifier=Modifier.wrapContentSize(),
                text = tab.name,
                style = MaterialTheme.typography.labelSmall.copy(color = colorScheme.primary),
                maxLines = 1
            )
        },
        trailingIcon = {
            Image(
                modifier = Modifier.height(PaddingSize2),
                painter = painterResource(tab.image),
                contentDescription = "",
                colorFilter = ColorFilter.tint(colorScheme.secondary),
            )

        }
    )

}

