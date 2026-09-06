package io.github.chos1n11111.dongqiudipure.feature.article

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import io.github.chos1n11111.dongqiudipure.core.designsystem.R as DesignR
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme
import kotlin.math.min
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ArticleImageViewer(url: String, caption: String?, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val request = remember(context, url) {
        // Decode enough detail for zooming, with a bounded bitmap size.
        ImageRequest.Builder(context).data(url).size(4096, 4096).build()
    }
    var scale by remember(url) { mutableFloatStateOf(1f) }
    var offset by remember(url) { mutableStateOf(Offset.Zero) }
    var viewport by remember { mutableStateOf(IntSize.Zero) }
    var imageSize by remember(url) { mutableStateOf(IntSize.Zero) }
    var loading by remember(url) { mutableStateOf(true) }
    var failed by remember(url) { mutableStateOf(false) }
    var retryGeneration by remember(url) { mutableIntStateOf(0) }
    val ready = !loading && !failed
    val zoomLabel = stringResource(R.string.article_image_zoom_percent, (scale * 100).roundToInt())
    val imageDescription = caption ?: stringResource(R.string.article_image)

    fun boundedOffset(value: Offset, zoom: Float): Offset {
        if (imageSize.width == 0 || imageSize.height == 0) return Offset.Zero
        val fit = min(
            viewport.width.toFloat() / imageSize.width,
            viewport.height.toFloat() / imageSize.height,
        )
        val maxX = ((imageSize.width * fit * zoom - viewport.width) / 2).coerceAtLeast(0f)
        val maxY = ((imageSize.height * fit * zoom - viewport.height) / 2).coerceAtLeast(0f)
        return Offset(value.x.coerceIn(-maxX, maxX), value.y.coerceIn(-maxY, maxY))
    }

    fun transform(zoom: Float, centroid: Offset, pan: Offset = Offset.Zero) {
        if (loading || failed) return
        val nextScale = (scale * zoom).coerceIn(1f, 5f)
        val ratio = nextScale / scale
        val anchor = centroid - Offset(viewport.width / 2f, viewport.height / 2f)
        // Keep the point between the fingers stationary while changing scale.
        offset = boundedOffset(offset * ratio + anchor * (1f - ratio) + pan, nextScale)
        scale = nextScale
    }

    fun reset() {
        scale = 1f
        offset = Offset.Zero
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        val view = LocalView.current
        DisposableEffect(view) {
            (view.parent as? DialogWindowProvider)?.window?.let { window ->
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
            onDispose { }
        }
        DqdTheme(darkTheme = true) {
            Scaffold(
                containerColor = Color.Black,
                contentColor = Color.White,
                topBar = {
                    TopAppBar(
                        title = { Text(zoomLabel) },
                        navigationIcon = {
                            ArticleActionButton(
                                icon = DqdIcons.Close,
                                label = stringResource(R.string.article_close_image),
                                onClick = onDismiss,
                            )
                        },
                        actions = {
                            ArticleActionButton(
                                icon = DqdIcons.ZoomOut,
                                label = stringResource(R.string.article_image_zoom_out),
                                enabled = ready && scale > 1f,
                                onClick = { transform(1f / 1.5f, Offset(viewport.width / 2f, viewport.height / 2f)) },
                            )
                            ArticleActionButton(
                                icon = DqdIcons.ZoomIn,
                                label = stringResource(R.string.article_image_zoom_in),
                                enabled = ready && scale < 5f,
                                onClick = { transform(1.5f, Offset(viewport.width / 2f, viewport.height / 2f)) },
                            )
                            ArticleActionButton(
                                icon = DqdIcons.Refresh,
                                label = stringResource(R.string.article_image_reset),
                                enabled = ready && scale > 1f,
                                onClick = ::reset,
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Black,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White,
                        ),
                    )
                },
                bottomBar = {
                    if (!caption.isNullOrBlank()) {
                        Text(
                            text = caption,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
                        )
                    }
                },
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .clipToBounds()
                        .onSizeChanged {
                            viewport = it
                            offset = boundedOffset(offset, scale)
                        }
                        .pointerInput(url) {
                            detectTransformGestures { centroid, pan, zoom, _ ->
                                transform(zoom, centroid, pan)
                            }
                        }
                        .pointerInput(url) {
                            detectTapGestures(onDoubleTap = { position ->
                                if (scale > 1f) reset() else transform(3f, position)
                            })
                        }
                        .semantics {
                            contentDescription = imageDescription
                            stateDescription = zoomLabel
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    key(retryGeneration) {
                        AsyncImage(
                            model = request,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            onLoading = { loading = true; failed = false },
                            onSuccess = {
                                imageSize = IntSize(it.result.image.width, it.result.image.height)
                                offset = boundedOffset(offset, scale)
                                loading = false
                            },
                            onError = { loading = false; failed = true },
                            modifier = Modifier.fillMaxSize().graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                            },
                        )
                    }
                    if (loading) CircularProgressIndicator()
                    if (failed) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(stringResource(R.string.article_image_error))
                            OutlinedButton(onClick = { reset(); retryGeneration++ }) {
                                Text(stringResource(DesignR.string.ds_action_retry))
                            }
                        }
                    }
                }
            }
        }
    }
}
