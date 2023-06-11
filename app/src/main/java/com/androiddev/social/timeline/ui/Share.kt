package com.androiddev.social.timeline.ui

import android.content.Context
import android.content.Intent
import com.androiddev.social.timeline.ui.model.UI

fun Context.shareStatus(
    status: UI,
) {
    status.contentEmojiText?.text?.let {
        share(
            title ="Status",
            text = "${it.substring(30)}\n${status.sharingUri}",
        )
    }
}

fun  Context.share(
    title: String? = null,
    text: String,
) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    startActivity(Intent.createChooser(sendIntent, title))
}