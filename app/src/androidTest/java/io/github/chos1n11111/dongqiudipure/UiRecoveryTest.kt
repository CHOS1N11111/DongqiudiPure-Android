package io.github.chos1n11111.dongqiudipure

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.platform.app.InstrumentationRegistry
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme
import io.github.chos1n11111.dongqiudipure.core.model.*
import io.github.chos1n11111.dongqiudipure.feature.entities.TeamProfileScreen
import io.github.chos1n11111.dongqiudipure.feature.entities.TeamProfileUiState
import io.github.chos1n11111.dongqiudipure.feature.entities.TeamTab
import io.github.chos1n11111.dongqiudipure.feature.home.HomeScreen
import io.github.chos1n11111.dongqiudipure.feature.home.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import io.github.chos1n11111.dongqiudipure.feature.home.R as HomeR
import io.github.chos1n11111.dongqiudipure.feature.entities.R as EntitiesR

class UiRecoveryTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun refreshingFeedToEmptyDoesNotCrash() {
        val article = ArticleSummary(ArticleId("1"), "Fixture article", "Fixture", "Today", 0, ArticleMedia.None)
        val idle = LoadStates(LoadState.NotLoading(false), LoadState.NotLoading(true), LoadState.NotLoading(true))
        val pages = MutableStateFlow(PagingData.from(listOf(article), idle))
        val category = NewsCategory("1", "Fixture")
        var refreshes = 0
        compose.setContent {
            val articles = pages.collectAsLazyPagingItems()
            DqdTheme {
                HomeScreen(
                    uiState = HomeUiState(listOf(category), category),
                    articles = articles,
                    onArticleClick = {}, onCategorySelect = {},
                    onRefresh = {
                        refreshes++
                        pages.value = PagingData.from(listOf(article), idle.copy(refresh = LoadState.Loading))
                    },
                    modifier = Modifier.testTag("feed"),
                )
            }
        }
        compose.onNodeWithText("Fixture article").assertIsDisplayed()
        compose.onNodeWithTag("feed").performTouchInput {
            swipeDown(startY = height * 0.35f, endY = height * 0.9f)
        }
        compose.runOnIdle {
            assertEquals(1, refreshes)
            pages.value = PagingData.from(emptyList(), idle)
        }
        val title = InstrumentationRegistry.getInstrumentation().targetContext
            .getString(HomeR.string.home_feed_empty_title)
        compose.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun teamTabsKeepIndependentScrollAndEmptySquadKeepsSeasonPicker() {
        val profile = TeamProfile(
            id = TeamId("1"), name = "Fixture team", crestUrl = null,
            competitionName = null, venue = null, foundedLabel = null, recentForm = emptyList(),
            facts = List(60) { TeamFact("Fact $it", "Value $it") },
        )
        val state = mutableStateOf(TeamProfileUiState(
            entityId = profile.id,
            profile = SectionState.Content(profile),
            selectedTab = TeamTab.Info,
            squad = SectionState.Content(TeamSquadData(
                seasons = listOf(SeasonOption("2026", "2026 season", true)),
                selectedSeasonId = "2026", groups = emptyList(),
            )),
        ))
        val news = flowOf(PagingData.empty<ArticleSummary>())
        val circle = flowOf(PagingData.empty<TeamCirclePost>())
        compose.setContent {
            DqdTheme {
                TeamProfileScreen(
                    uiState = state.value,
                    news = news.collectAsLazyPagingItems(), circle = circle.collectAsLazyPagingItems(),
                    onBack = {}, onArticleClick = {}, onMatchClick = {}, onTeamClick = {}, onPlayerClick = {},
                    onTabSelect = { state.value = state.value.copy(selectedTab = it) },
                    onScheduleSeasonSelect = {}, onSquadSeasonSelect = {}, onStatisticsSeasonSelect = {},
                    onTransferWindowSelect = {}, onRetry = {}, onRetryTab = {},
                    showTopBar = false, showProfileHeader = false,
                )
            }
        }
        compose.onNodeWithText("Fact 30").performScrollTo()
        val before = compose.onNodeWithText("Fact 30").getUnclippedBoundsInRoot()
        compose.runOnIdle { state.value = state.value.copy(selectedTab = TeamTab.Players) }
        compose.onNodeWithText("2026 season").assertIsDisplayed()
        val empty = InstrumentationRegistry.getInstrumentation().targetContext
            .getString(EntitiesR.string.team_squad_empty_description)
        compose.onNodeWithText(empty).assertIsDisplayed()
        compose.runOnIdle { state.value = state.value.copy(selectedTab = TeamTab.Info) }
        compose.onNodeWithText("Fact 30").assertIsDisplayed()
        assertEquals(before, compose.onNodeWithText("Fact 30").getUnclippedBoundsInRoot())
        compose.runOnIdle {
            state.value = state.value.copy(entityId = TeamId("2"), profile = SectionState.Content(profile.copy(id = TeamId("2"))))
        }
        compose.onNodeWithText("Fact 0").assertIsDisplayed()
    }
}
