package com.androiddev.social.timeline.ui

import android.net.Uri
import android.util.Log
import com.androiddev.social.SkeletonScope
import com.androiddev.social.timeline.data.FeedType
import com.androiddev.social.timeline.ui.model.UI
import com.squareup.anvil.annotations.ContributesBinding
import java.net.URI
import javax.inject.Inject

interface UrlHandlerMediator {
    fun givenUri(
        ui: UI,
        uri: String?,
        isValidUrl: (String) -> Boolean,
        onOpenURI: (URI, FeedType) -> Unit,
        goToTag: (String) -> Unit,
        goToProfile: (String) -> Unit,
        goToConversation: (UI) -> Unit,
    )
}

@ContributesBinding(SkeletonScope::class, boundType = UrlHandlerMediator::class)
class RealUrlHandlerMediator @Inject constructor() : UrlHandlerMediator {

    override fun givenUri(
        ui: UI,
        uri: String?,
        isValidUrl: (String) -> Boolean,
        onOpenURI: (URI, FeedType) -> Unit,
        goToTag: (String) -> Unit,
        goToProfile: (String) -> Unit,
        goToConversation: (UI) -> Unit,
    ) {
        when {
            uri != null && isValidUrl(uri) -> {
                val parsedUri = Uri.parse(uri)
                val fixedUri = URI(
                    parsedUri.scheme?.lowercase(), parsedUri.authority,
                    parsedUri.path, parsedUri.query, parsedUri.fragment
                )
                onOpenURI(fixedUri, ui.type)
                Log.d("Clicked URI", uri)
            }

            uri?.startsWith("###TAG") == true -> {
                goToTag(uri.removePrefix("###TAG"))
            }

            uri != null -> goToProfile(uri)
            ui.replyCount > 0 || ui.inReplyTo != null -> {
                goToConversation(ui)
            }
        }
    }
}
