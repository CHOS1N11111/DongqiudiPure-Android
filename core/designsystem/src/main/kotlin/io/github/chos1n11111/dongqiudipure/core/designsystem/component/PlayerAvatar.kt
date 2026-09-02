package io.github.chos1n11111.dongqiudipure.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId

@Composable
fun PlayerAvatar(
    playerId: PlayerId,
    playerName: String,
    avatarUrl: String?,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    var loaded by remember(avatarUrl) { mutableStateOf(false) }
    var attempt by remember(avatarUrl) { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        if (!loaded) {
            Text(
                text = playerName.take(1),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (avatarUrl != null) {
            key(playerId, attempt) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    onLoading = { loaded = false },
                    onSuccess = { loaded = true },
                    onError = {
                        loaded = false
                        if (attempt < MAX_AVATAR_RETRIES) attempt += 1
                    },
                    modifier = Modifier.size(size),
                )
            }
        }
    }
}

private const val MAX_AVATAR_RETRIES = 2
