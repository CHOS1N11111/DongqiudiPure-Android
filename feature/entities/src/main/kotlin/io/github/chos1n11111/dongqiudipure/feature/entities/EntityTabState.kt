package io.github.chos1n11111.dongqiudipure.feature.entities

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.saveable.rememberSaveableStateHolder

@Composable
internal fun EntityTabState(entityId: String?, tab: String, content: @Composable () -> Unit) {
    key(entityId) {
        val stateHolder = rememberSaveableStateHolder()
        stateHolder.SaveableStateProvider(tab, content)
    }
}
