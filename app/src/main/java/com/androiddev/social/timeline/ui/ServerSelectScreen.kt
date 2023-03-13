package com.androiddev.social.timeline.ui

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.androiddev.social.theme.*
import social.androiddev.firefly.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerSelectScreen(
    onServerSelected: (selected: String) -> Unit
) {
    var current by remember { mutableStateOf("") }
    FireflyTheme {
        Surface(
            color = colorScheme.background
        ) {
            Image(modifier=Modifier.fillMaxSize().scale(2f).blur(2.dp).alpha(.5f), painter = painterResource(id = R.drawable.background), contentDescription = null)


            Column(
                Modifier
                    .padding(PaddingSize2)
                    .fillMaxWidth(1f)
                    .fillMaxHeight()
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.Center


            ) {
                Text(
                    color = colorScheme.primary,
                    modifier = Modifier.padding(bottom = PaddingSize2),
                    text = "Welcome to Firefly",
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    color = colorScheme.primary,
//                    modifier = Modifier.padding(
//                        horizontal = PaddingSize2,
//                        vertical = PaddingSize1
//                    ),
                    text = "Enter Server Name",
                    style = MaterialTheme.typography.headlineMedium
                )
                val focusRequester = remember { FocusRequester() }
                LaunchedEffect(key1 = Unit) {
                    focusRequester.requestFocus()
                }

                TextField(modifier = Modifier
                    .focusRequester(focusRequester =focusRequester)
                    .wrapContentHeight()
                    .fillMaxWidth(.99f)
                    .padding(top = PaddingSize8, start = PaddingSize1, end = PaddingSize1),
                    textStyle = LocalTextStyle.current.copy(
                        //                                        textAlign = TextAlign.Cewn
                    ),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = colorScheme.onSurface.copy(alpha = .8f),
                        cursorColor = Color.Black,
                        disabledLabelColor = colorScheme.onSecondaryContainer,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        textColor = colorScheme.surface
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
//                        .alpha(.8f)
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
                        onClick = { if(current.isNotEmpty()) onServerSelected(current) }
                    )
                }
            }
        }
    }
}


