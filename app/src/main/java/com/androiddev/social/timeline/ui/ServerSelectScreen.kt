package com.androiddev.social.timeline.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.androiddev.social.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerSelectScreen(
    onServerSelected: (selected: String) -> Unit
) {
    var current by remember { mutableStateOf("androiddev.social") }
    EbonyTheme {
        Surface(
            modifier = Modifier.clip(RoundedCornerShape(8.dp)),
            color = colorScheme.surface.copy(alpha = .8f)
        ) {

            Column(
                Modifier
                    .padding(PaddingSize2)
                    .fillMaxWidth(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center


            ) {
                Text(
                    color = colorScheme.onSurface,
                    modifier = Modifier.padding(PaddingSize2),
                    text = "Welcome!",
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    color = colorScheme.onSurface,
                    modifier = Modifier.padding(
                        horizontal = PaddingSize2,
                        vertical = PaddingSize1
                    ),
                    text = "Which Server should we connect to?",
                    style = MaterialTheme.typography.headlineMedium
                )

                TextField(modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(.99f)
                    .padding(top = PaddingSize8, start = PaddingSize1, end = PaddingSize1),
                    textStyle = LocalTextStyle.current.copy(
                        //                                        textAlign = TextAlign.Cewn
                    ),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = colorScheme.onSecondaryContainer,
                        cursorColor = Color.Black,
                        disabledLabelColor = colorScheme.onSecondaryContainer,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        textColor = colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp),
                    value = current,
                    onValueChange = {
                        current = it
                    },
                    trailingIcon = {
                        Icon(Icons.Default.Clear,
                            contentDescription = "clear text",
                            modifier = Modifier.clickable {
                                current = ""
                            })
                    })
                Box(
                    modifier = Modifier
                        .alpha(.8f)
                        .fillMaxWidth()

                ) {
                    ExtendedFloatingActionButton(backgroundColor = colorScheme.primary,
                        modifier = Modifier
                            .wrapContentWidth()
                            .align(Alignment.Center)
                            .padding(PaddingSize5),
                        text = {
                            Text("Continue to Server")
                        },
                        onClick = { onServerSelected(current) }
                    )
                }
            }
        }
    }
}


