package com.androiddev.social.timeline.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.androiddev.social.theme.PaddingSize0_5
import com.androiddev.social.theme.PaddingSize1
import com.androiddev.social.theme.TonalSurfaceElevation
import com.androiddev.social.timeline.data.FeedType
import com.androiddev.social.timeline.ui.model.CardUI
import java.net.URI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentCard(
    card: CardUI,
    feedType: FeedType,
    onOpenURI: (URI, FeedType) -> Unit,
) {
    Surface(
        modifier = Modifier
            .padding(top = PaddingSize1)
            .fillMaxWidth(),
        tonalElevation = TonalSurfaceElevation,
        shape = RoundedCornerShape(PaddingSize1),
        onClick = {
            onOpenURI(URI(card.url), feedType)
        }
    ) {
        Column(
            modifier = Modifier.padding(PaddingSize1),
        ) {
            Text(
                modifier = Modifier.padding(PaddingSize0_5),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 16.sp
                ),
                text = card.title
            )
            Text(
                modifier = Modifier.padding(PaddingSize0_5),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 12.sp
                ),
                text = card.description
            )
        }
    }
}
