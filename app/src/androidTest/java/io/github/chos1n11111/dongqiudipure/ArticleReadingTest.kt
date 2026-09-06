package io.github.chos1n11111.dongqiudipure

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pinch
import androidx.compose.ui.test.swipeUp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.paging.PagingData
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.platform.app.InstrumentationRegistry
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme
import io.github.chos1n11111.dongqiudipure.core.model.ArticleBlock
import io.github.chos1n11111.dongqiudipure.core.model.ArticleDetail
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.Comment
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.feature.article.ArticleScreen
import io.github.chos1n11111.dongqiudipure.feature.article.ArticleUiState
import io.github.chos1n11111.dongqiudipure.feature.article.R as ArticleR
import io.github.chos1n11111.dongqiudipure.core.designsystem.R as DesignR
import java.io.File
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

class ArticleReadingTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val temporaryImages = mutableListOf<File>()

    @After
    fun cleanUp() {
        temporaryImages.forEach { it.delete() }
    }

    @Test
    fun commentsShortcutReturnsToThePreviousReadingPosition() {
        val paragraphs = List(20) { ArticleBlock.Paragraph("Paragraph $it. " + "Reading content. ".repeat(15)) }
        showArticle(paragraphs)
        compose.onNode(hasScrollAction()).performTouchInput { swipeUp() }
        val before = compose.onNodeWithText(paragraphs[1].text).getUnclippedBoundsInRoot().top.value

        compose.onNodeWithContentDescription(context.getString(ArticleR.string.article_go_to_comments)).performClick()
        compose.onNodeWithText("Comment 0").assertIsDisplayed()
        compose.onNodeWithContentDescription(context.getString(ArticleR.string.article_back_to_body)).performClick()

        val after = compose.onNodeWithText(paragraphs[1].text).getUnclippedBoundsInRoot().top.value
        assertEquals(before, after, 1f)
    }

    @Test
    fun readingShortcutsAlsoWorkWithAnEmptyCommentSection() {
        showArticle(listOf(ArticleBlock.Paragraph("Short article")), commentCount = 0)
        compose.onNodeWithContentDescription(context.getString(ArticleR.string.article_go_to_comments)).performClick()
        compose.onNodeWithText(context.getString(ArticleR.string.article_comments_empty_title)).assertIsDisplayed()
        compose.onNodeWithContentDescription(context.getString(ArticleR.string.article_back_to_body)).performClick()
        compose.onNodeWithText("Reading test").assertIsDisplayed()
    }

    @Test
    fun imageOpensPinchesDoubleTapsAndClosesWithoutMovingTheArticle() {
        val file = imageFile().also(::writeImage)
        showArticle(listOf(ArticleBlock.Image(file.toURI().toString(), "Test image", 0.6f)))
        val before = compose.onNodeWithContentDescription("Test image").getUnclippedBoundsInRoot()
        compose.onNodeWithContentDescription("Test image").performClick()
        waitForImage()
        val image = compose.onNode(
            hasContentDescription("Test image") and
                SemanticsMatcher.keyIsDefined(SemanticsProperties.StateDescription),
        )
        val originalPixels = image.captureToImage().toPixelMap()
        image.performTouchInput {
            pinch(
                start0 = center + Offset(-width * 0.1f, 0f),
                end0 = center + Offset(-width * 0.35f, 0f),
                start1 = center + Offset(width * 0.1f, 0f),
                end1 = center + Offset(width * 0.35f, 0f),
            )
        }
        val zoom = image.fetchSemanticsNode().config[SemanticsProperties.StateDescription]
        assertTrue(zoom.removeSuffix("%").toInt() > 100)
        val zoomedPixels = image.captureToImage().toPixelMap()
        assertTrue((2..8).any { step ->
            val x = originalPixels.width * step / 10
            val y = originalPixels.height * step / 10
            originalPixels[x, y] != zoomedPixels[x, y]
        })

        image.performTouchInput { doubleClick() }
        compose.onNodeWithText("100%").assertIsDisplayed()
        image.performTouchInput { doubleClick() }
        compose.onNodeWithText("300%").assertIsDisplayed()
        compose.onNodeWithContentDescription(context.getString(ArticleR.string.article_image_reset)).performClick()
        compose.onNodeWithText("100%").assertIsDisplayed()
        compose.onNodeWithContentDescription(context.getString(ArticleR.string.article_close_image)).performClick()

        assertEquals(before, compose.onNodeWithContentDescription("Test image").getUnclippedBoundsInRoot())
    }

    @Test
    fun failedImageCanBeRetriedInTheViewer() {
        val file = imageFile()
        file.delete()
        showArticle(listOf(ArticleBlock.Image(file.toURI().toString(), "Test image", 0.6f)))
        compose.onNodeWithContentDescription("Test image").performClick()
        compose.waitUntil(10_000) {
            compose.onAllNodes(
                androidx.compose.ui.test.hasText(context.getString(ArticleR.string.article_image_error)),
            ).fetchSemanticsNodes().isNotEmpty()
        }
        writeImage(file)
        compose.onNodeWithText(context.getString(DesignR.string.ds_action_retry)).performClick()
        waitForImage()
        compose.onNodeWithContentDescription(context.getString(ArticleR.string.article_image_zoom_in)).performClick()
        compose.onNodeWithText("150%").assertIsDisplayed()
    }

    @Test
    fun systemBackClosesTheImageAndArticleBackReturnsToTheList() {
        val file = imageFile().also(::writeImage)
        showArticle(listOf(ArticleBlock.Image(file.toURI().toString(), "Test image", 0.6f)))
        compose.onNodeWithContentDescription("Test image").performClick()
        waitForImage()

        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        compose.onNodeWithContentDescription(context.getString(ArticleR.string.article_close_image))
            .assertDoesNotExist()
        compose.onNodeWithContentDescription("Test image").assertIsDisplayed()
        compose.onNodeWithContentDescription(context.getString(DesignR.string.ds_action_back)).performClick()
        compose.onNodeWithText("Article list").assertIsDisplayed()
    }

    @Test
    fun edgeBackClosesTheImageWithoutBlockingArticleNavigation() {
        assumeTrue(Build.VERSION.SDK_INT >= 29)
        assumeTrue(Settings.Secure.getInt(context.contentResolver, "navigation_mode", 0) == 2)
        val file = imageFile().also(::writeImage)
        showArticle(listOf(ArticleBlock.Image(file.toURI().toString(), "Test image", 0.6f)))
        compose.onNodeWithContentDescription("Test image").performClick()
        waitForImage()

        val metrics = context.resources.displayMetrics
        val downTime = SystemClock.uptimeMillis()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        repeat(22) { step ->
            val action = when (step) {
                0 -> MotionEvent.ACTION_DOWN
                21 -> MotionEvent.ACTION_UP
                else -> MotionEvent.ACTION_MOVE
            }
            val event = MotionEvent.obtain(
                downTime, SystemClock.uptimeMillis(), action,
                1f + metrics.widthPixels * 0.4f * step / 21,
                metrics.heightPixels * 0.5f, 0,
            ).apply { source = InputDevice.SOURCE_TOUCHSCREEN }
            instrumentation.uiAutomation.injectInputEvent(event, true)
            event.recycle()
            SystemClock.sleep(16)
        }
        compose.waitUntil(5_000) {
            compose.onAllNodes(hasContentDescription(context.getString(ArticleR.string.article_close_image)))
                .fetchSemanticsNodes().isEmpty()
        }
        compose.onNodeWithContentDescription("Test image").assertIsDisplayed()
        compose.onNodeWithContentDescription(context.getString(DesignR.string.ds_action_back)).performClick()
        compose.onNodeWithText("Article list").assertIsDisplayed()
    }

    private fun showArticle(blocks: List<ArticleBlock>, commentCount: Int = 25) {
        val article = ArticleDetail(
            id = ArticleId("reading-test"),
            title = "Reading test",
            source = "Test",
            publishedLabel = "2026-09-06",
            blocks = blocks,
            relatedEntities = emptyList(),
            commentCount = commentCount,
        )
        compose.setContent {
            val navController = rememberNavController()
            val comments = remember {
                flowOf(PagingData.from(
                    data = List(commentCount) {
                        Comment("$it", "Reader", "Comment $it", "Today", 0)
                    },
                    sourceLoadStates = LoadStates(
                        refresh = LoadState.NotLoading(false),
                        prepend = LoadState.NotLoading(true),
                        append = LoadState.NotLoading(true),
                    ),
                ))
            }.collectAsLazyPagingItems()
            DqdTheme {
                NavHost(navController = navController, startDestination = "list") {
                    composable("list") {
                        Text("Article list", Modifier.clickable { navController.navigate("article") })
                    }
                    composable("article") {
                        ArticleScreen(
                            uiState = ArticleUiState(detail = SectionState.Content(article)),
                            comments = comments,
                            onBack = navController::popBackStack,
                            onShare = {},
                            onEntityClick = {},
                            onLinkClick = {},
                            onCommentClick = {},
                            onRetryDetail = {},
                            onRetryComments = {},
                            onSortToggle = {},
                        )
                    }
                }
            }
        }
        compose.onNodeWithText("Article list").performClick()
    }

    private fun waitForImage() {
        val zoomIn = hasContentDescription(context.getString(ArticleR.string.article_image_zoom_in))
        compose.waitUntil(10_000) {
            compose.onAllNodes(zoomIn).fetchSemanticsNodes().any {
                !it.config.contains(SemanticsProperties.Disabled)
            }
        }
    }

    private fun imageFile() = File.createTempFile("reading-image-", ".png", context.cacheDir)
        .also(temporaryImages::add)

    private fun writeImage(file: File) {
        val bitmap = Bitmap.createBitmap(600, 1000, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        repeat(6) { column ->
            repeat(10) { row ->
                paint.color = Color.rgb(column * 40, row * 25, (column + row) * 15)
                canvas.drawRect(column * 100f, row * 100f, (column + 1) * 100f, (row + 1) * 100f, paint)
            }
        }
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
}
