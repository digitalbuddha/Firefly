package com.androiddev.social.timeline.ui

import android.net.Uri
import android.util.Log
import com.androiddev.social.SkeletonScope
import com.androiddev.social.timeline.ui.model.UI
import com.squareup.anvil.annotations.ContributesBinding
import java.net.URI
import javax.inject.Inject

interface UrlHandlerMediator {
    fun givenUrl(
        ui: UI,
        url: String?,
        isValidUrl: (String) -> Boolean,
        openUri: (String) -> Unit,
        goToTag: (String) -> Unit,
        goToProfile: (String) -> Unit,
        goToConversation: (UI) -> Unit,
    )
}

@ContributesBinding(SkeletonScope::class, boundType = UrlHandlerMediator::class)
class RealUrlHandlerMediator @Inject constructor() : UrlHandlerMediator{

    override fun givenUrl(
        ui: UI,
        url: String?,
        isValidUrl: (String) -> Boolean,
        openUri: (String) -> Unit,
        goToTag: (String) -> Unit,
        goToProfile: (String) -> Unit,
        goToConversation: (UI) -> Unit,
    ) {
        when {
            url != null && isValidUrl(url) -> {
                val uri = Uri.parse(url)
                val fixedUri = URI(
                    uri.scheme?.lowercase(), uri.authority,
                    uri.path, uri.query, uri.fragment
                )
                openUri(fixedUri.toASCIIString())
                Log.d("Clicked URL", url)
            }

            url?.startsWith("###TAG") == true ->{
                goToTag(url.removePrefix("###TAG"))
            }
            url != null -> goToProfile(url)
            ui.replyCount > 0 || ui.inReplyTo != null -> {
                goToConversation(ui)
            }
        }
    }
}
