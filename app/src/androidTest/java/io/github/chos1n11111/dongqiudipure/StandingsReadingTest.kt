package io.github.chos1n11111.dongqiudipure

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.test.platform.app.InstrumentationRegistry
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionRef
import io.github.chos1n11111.dongqiudipure.core.model.KnockoutStage
import io.github.chos1n11111.dongqiudipure.core.model.KnockoutTie
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.StandingGroup
import io.github.chos1n11111.dongqiudipure.core.model.StandingRow
import io.github.chos1n11111.dongqiudipure.core.model.StandingTable
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.model.TeamRef
import io.github.chos1n11111.dongqiudipure.feature.rankings.RankingsContent
import io.github.chos1n11111.dongqiudipure.feature.rankings.RankingsUiState
import io.github.chos1n11111.dongqiudipure.feature.rankings.R as RankingsR
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class StandingsReadingTest {
    @get:Rule val compose = createComposeRule()
    private val competition = CompetitionRef(CompetitionId("test"), "Test competition", null)
    private val teamHeader get() = InstrumentationRegistry.getInstrumentation().targetContext
        .getString(RankingsR.string.standings_column_team)

    @Test
    fun leagueColumnHeaderStaysVisibleWhenReadingLowerRanks() {
        showTable(StandingTable(competition, "2026", rows("Team", 40)))
        val initialTop = compose.onNodeWithText(teamHeader).getUnclippedBoundsInRoot().top

        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Team 25"))

        compose.onNodeWithText(teamHeader).assertIsDisplayed()
        assertEquals(initialTop, compose.onNodeWithText(teamHeader).getUnclippedBoundsInRoot().top)
        compose.onNodeWithText("Team 25").assertIsDisplayed()
    }

    @Test
    fun groupHeaderChangesAndDoesNotRemainOverKnockoutMatches() {
        showTable(
            StandingTable(
                competition = competition,
                seasonLabel = "2026",
                rows = emptyList(),
                groups = listOf(StandingGroup("Group A", rows("A", 40)), StandingGroup("Group B", rows("B", 40))),
                knockoutStages = listOf(
                    KnockoutStage("Finals", List(30) {
                        KnockoutTie(team("Final home $it"), team("Final away $it"), "1 : 0", null, emptyList())
                    }),
                ),
            ),
        )
        val initialTop = compose.onNodeWithText("Group A").getUnclippedBoundsInRoot().top

        compose.onNode(hasScrollAction()).performScrollToNode(hasText("B 25"))
        compose.onNodeWithText("Group B").assertIsDisplayed()
        compose.onNodeWithText(teamHeader).assertIsDisplayed()
        assertEquals(initialTop, compose.onNodeWithText("Group B").getUnclippedBoundsInRoot().top)

        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Final home 25"))
        compose.onNodeWithText("Finals").assertIsDisplayed()
        compose.onNodeWithText(teamHeader).assertDoesNotExist()
        assertEquals(initialTop, compose.onNodeWithText("Finals").getUnclippedBoundsInRoot().top)
    }

    private fun showTable(table: StandingTable) {
        compose.setContent {
            DqdTheme {
                RankingsContent(
                    uiState = RankingsUiState(table = SectionState.Content(table)),
                    onTeamClick = {},
                    onMatchClick = {},
                    onRetry = {},
                )
            }
        }
    }

    private fun team(name: String) = TeamRef(TeamId(name), name, null)

    private fun rows(prefix: String, count: Int) = List(count) {
        StandingRow(it + 1, team("$prefix ${it + 1}"), 10, 5, 3, 2, 4, 18, null)
    }
}
