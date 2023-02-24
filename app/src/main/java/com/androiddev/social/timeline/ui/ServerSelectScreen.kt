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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.androiddev.social.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerSelectScreen(
    scope: CoroutineScope,
    navController: NavHostController
) {
    var server by remember { mutableStateOf("allthingstech.social") }

    EbonyTheme {
        Surface(
            modifier = Modifier.clip(RoundedCornerShape(8.dp)),
            color = colorScheme.surface.copy(alpha = .8f)
        ) {
            val configuration = LocalConfiguration.current

            val screenHeight = configuration.screenHeightDp

            Column(
                Modifier
                    .padding(
                        PaddingSize2
                    )
                    .fillMaxWidth(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .height((screenHeight * .5f).dp),
                verticalArrangement = Arrangement.SpaceBetween


            ) {
                Text(
                    color = colorScheme.onSurface,
                    modifier = Modifier
                        .padding(
                            PaddingSize1
                        ),
                    text = "Welcome!",
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    color = colorScheme.onSurface,
                    modifier = Modifier
                        .padding(
                            PaddingSize1
                        ),
                    text = "Which Server should we connect to?",
                    style = MaterialTheme.typography.headlineMedium
                )

                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .wrapContentWidth(),
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
                    value = server,
                    onValueChange = {
                        server = it
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "clear text",
                            modifier = Modifier
                                .clickable {
                                    server = ""
                                }
                        )
                    })
                Box(
                    modifier = Modifier
                        .alpha(.8f)
                        .fillMaxWidth()

                ) {
                    ExtendedFloatingActionButton(
                        backgroundColor = colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth(.7f)
                            .align(Alignment.Center),
                        text = {
                            Text("Continue to Server")
                        },
                        onClick =
                        {
                            scope.launch {
                                navController.navigate("login/$server")
                            }
                        })
                }
            }
        }
    }
}