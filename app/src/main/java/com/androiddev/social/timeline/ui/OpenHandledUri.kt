package com.androiddev.social.timeline.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation.NavController


@Composable
fun OpenHandledUri(
    uriPresenter: UriPresenter,
    navController: NavController,
    code: String,
) {
    val uriHandler = LocalUriHandler.current
    LaunchedEffect(uriPresenter.model.handledUri) {
        Log.d("QQQ", "handledUri: ${uriPresenter.model.handledUri}")
        when (val handledUri = uriPresenter.model.handledUri) {
            is UriPresenter.ConversationHandledUri -> {
                navController.navigate("conversation/$code/${handledUri.statusId}/${handledUri.type}")
                uriPresenter.handle(UriPresenter.Reset)
            }

            is UriPresenter.ProfileHandledUri -> {
                navController.navigate("profile/$code/${handledUri.accountId}")
                uriPresenter.handle(UriPresenter.Reset)
            }

            is UriPresenter.UnknownHandledUri -> {
                uriHandler.openUri(handledUri.uri.toASCIIString())
                uriPresenter.handle(UriPresenter.Reset)
            }

            else -> {}
        }
    }
}