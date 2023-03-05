package com.androiddev.social.timeline.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.androiddev.social.theme.PaddingSize1

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Visibility( defaultVisibility:String, onClick: (String) -> Unit) {
    var visibility by remember { mutableStateOf(defaultVisibility) }

    Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
            .padding(top = PaddingSize1, bottom = PaddingSize1, end = PaddingSize1)
            .fillMaxWidth()
    ) {
        AssistChip(
            colors = AssistChipDefaults.assistChipColors(

                containerColor = MaterialTheme.colorScheme.surface,
                leadingIconContentColor = MaterialTheme.colorScheme.secondary, labelColor = MaterialTheme.colorScheme.tertiary
            ),
            shape = RoundedCornerShape(50, 50, 50, 50),
            onClick = {
                when (visibility) {
                    "Public" -> visibility = "Private"
                    "Private" -> visibility = "Unlisted"
                    "Unlisted" -> visibility = "Direct"
                    "Direct" -> visibility = "Public"
                }
                onClick(visibility)
            },
            label = { Text(visibility, style = MaterialTheme.typography.labelLarge, modifier = Modifier.wrapContentWidth()) },
            leadingIcon = {
//
            },
            trailingIcon = {


            }
        )
    }
}
