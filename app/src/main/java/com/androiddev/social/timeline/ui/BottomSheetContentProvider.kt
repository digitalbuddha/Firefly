package com.androiddev.social.timeline.ui

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androiddev.social.theme.PaddingSize1
import com.androiddev.social.theme.PaddingSize2
import com.androiddev.social.theme.PaddingSize3
import com.androiddev.social.timeline.data.Account
import com.androiddev.social.timeline.ui.model.UI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import social.androiddev.firefly.R

@OptIn(ExperimentalMaterialApi::class)
class BottomSheetContentProvider(
    val bottomState: ModalBottomSheetState,
) {
    private var internalState = MutableStateFlow<SheetContentState>(SheetContentState.None)

    val state: StateFlow<SheetContentState>
        get() = internalState

    suspend fun showContent(state: SheetContentState) {
        internalState.value = state
        bottomState.show()
    }

    suspend fun hide() {
        bottomState.hide()
    }
}

sealed interface SheetContentState {

    object None : SheetContentState

    data class StatusMenu(
        val status: UI,
    ) : SheetContentState

    data class OwnedStatusMenu(
        val status: UI,
    ) : SheetContentState

    data class UserInput(
        val account: Account,

    ) : SheetContentState
}

@Composable
fun BottomSheetContent(
    bottomSheetContentProvider: BottomSheetContentProvider,
    onMessageSent: (String, String, Set<Uri>) -> Unit,
    goToConversation: (UI) -> Unit,
    goToProfile: (String) -> Unit,
    goToTag: (String) -> Unit,
    onShareStatus: (UI) -> Unit,
    onDelete: (statusId: String) -> Unit,
) {
    when (
        val state: SheetContentState = bottomSheetContentProvider.state
            .collectAsStateWithLifecycle(SheetContentState.None).value
    ) {
        is SheetContentState.None -> {
            Box(Modifier.defaultMinSize(minHeight = 1.dp)) {}
        }

        is SheetContentState.StatusMenu -> {
            StatusMenu(bottomSheetContentProvider, state, onShareStatus)
        }

        is SheetContentState.OwnedStatusMenu -> {
            OwnedStatusMenu(bottomSheetContentProvider, state, onShareStatus, onDelete)
        }

        is SheetContentState.UserInput -> {
            UserInputSheetContent(
                bottomSheetContentProvider, state,
                onMessageSent, goToConversation, goToProfile, goToTag
            )
        }
    }
}

@Composable
fun StatusMenu(
    bottomSheetContentProvider: BottomSheetContentProvider,
    state: SheetContentState.StatusMenu,
    onShareStatus: (UI) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ShareButton(bottomSheetContentProvider, state.status, onShareStatus)
    }
}

@Composable
fun OwnedStatusMenu(
    bottomSheetContentProvider: BottomSheetContentProvider,
    state: SheetContentState.OwnedStatusMenu,
    onShareStatus: (UI) -> Unit,
    onDelete: (statusId: String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ShareButton(bottomSheetContentProvider, state.status, onShareStatus)
        DeleteButton(bottomSheetContentProvider, state.status, onDelete)
    }
}

@Composable
private fun ShareButton(
    bottomSheetContentProvider: BottomSheetContentProvider,
    status: UI,
    onShareStatus: (UI) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    TextButton(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = {
            coroutineScope.launch {
                bottomSheetContentProvider.hide()
            }
            onShareStatus(status)
        }
    ) {
        Row(Modifier.fillMaxWidth()) {
            Image(
                modifier = Modifier
                    .padding(start = PaddingSize1)
                    .align(Alignment.CenterVertically)
                    .size(PaddingSize3),
                painter = painterResource(R.drawable.share),
                contentDescription = "",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
            )
            Text(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = PaddingSize3),
                text = "Share",
                style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DeleteButton(
    bottomSheetContentProvider: BottomSheetContentProvider,
    status: UI,
    onDelete: (statusId: String) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    TextButton(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = {
            coroutineScope.launch {
                bottomSheetContentProvider.hide()
            }
            onDelete(status.remoteId)
        }
    ) {
        Row(Modifier.fillMaxWidth()) {
            Image(
                modifier = Modifier
                    .padding(start = PaddingSize1)
                    .align(Alignment.CenterVertically)
                    .size(PaddingSize3),
                painter = painterResource(R.drawable.delete),
                contentDescription = "",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
            )
            Text(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = PaddingSize3),
                text = "Delete",
                style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary),
                maxLines = 1
            )
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UserInputSheetContent(
    bottomSheetContentProvider: BottomSheetContentProvider,
    state: SheetContentState.UserInput,
    onMessageSent: (String, String, Set<Uri>) -> Unit,
    goToConversation: (UI) -> Unit,
    goToProfile: (String) -> Unit,
    goToTag: (String) -> Unit
) {
    UserInput(
        status = null,
        account = state.account,
        modifier = Modifier.padding(bottom = 0.dp),
        goToBottomSheet = bottomSheetContentProvider::showContent,
        onMessageSent = onMessageSent,
        participants = "",
        showReplies = false,
        goToConversation = goToConversation,
        goToProfile = goToProfile,
        goToTag = goToTag
    )
}