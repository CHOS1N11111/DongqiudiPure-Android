package io.github.chos1n11111.dongqiudipure.feature.matches

import androidx.lifecycle.ViewModelStore
import io.github.chos1n11111.dongqiudipure.core.data.*
import io.github.chos1n11111.dongqiudipure.core.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class MatchesViewModelTest {
    private val store = ViewModelStore()

    @Before
    fun setUp() { Dispatchers.setMain(StandardTestDispatcher()) }

    @After
    fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun `midnight follows today but keeps another selected day until it leaves the window`() = runTest {
        var today = LocalDate.of(2026, 10, 8)
        val repository = RecordingMatches()
        val viewModel = MatchesViewModel(repository, EmptyCatalog()) { today }
        store.put("matches", viewModel)
        runCurrent()
        today = today.plusDays(1)
        viewModel.refreshCalendar()
        runCurrent()
        assertEquals(today, viewModel.uiState.value.selectedDate)
        assertEquals(today, viewModel.uiState.value.days.single { it.isToday }.date)
        assertEquals(today, repository.dates.last())
        assertEquals(7, viewModel.uiState.value.days.size)

        val browsingDate = today.minusDays(1)
        viewModel.selectDate(browsingDate)
        runCurrent()
        val previousCalls = repository.dates.size
        today = today.plusDays(1)
        viewModel.refreshCalendar()
        runCurrent()
        assertEquals(browsingDate, viewModel.uiState.value.selectedDate)
        assertEquals(previousCalls, repository.dates.size)

        today = today.plusDays(3)
        viewModel.refreshCalendar()
        runCurrent()
        assertEquals(today, viewModel.uiState.value.selectedDate)
        assertEquals(today, repository.dates.last())
    }

    @Test
    fun `foreground reloads the visible date and unchanged calendar checks do not poll matches`() = runTest {
        val today = LocalDate.of(2026, 10, 8)
        val repository = RecordingMatches()
        val viewModel = MatchesViewModel(repository, EmptyCatalog()) { today }
        store.put("matches", viewModel)
        runCurrent()
        viewModel.refreshCalendar()
        runCurrent()
        assertEquals(1, repository.dates.size)
        viewModel.onForeground()
        runCurrent()
        assertEquals(listOf(today, today), repository.dates)
    }

    private class EmptyCatalog : FootballCatalogRepository {
        override val importantCompetitions = emptyList<CompetitionRef>()
        override val defaultRankingCompetitions = emptyList<CompetitionRef>()
        override suspend fun loadCompetitionCatalog() = DataResult.Success(emptyList<CompetitionCatalogGroup>())
    }

    private class RecordingMatches : MatchRepository {
        val dates = mutableListOf<LocalDate>()
        override suspend fun loadMatches(date: LocalDate, competition: CompetitionRef?): DataResult<List<MatchSummary>> {
            dates += date
            return DataResult.Success(emptyList())
        }
        override suspend fun loadMatch(matchId: MatchId): DataResult<MatchSummary?> = error("Unused")
        override suspend fun loadMatchOverview(matchId: MatchId): DataResult<MatchOverview> = error("Unused")
        override suspend fun loadMatchLineup(matchId: MatchId): DataResult<MatchLineupBundle?> = error("Unused")
        override suspend fun loadMatchUserRatings(matchId: MatchId, playerIds: List<PlayerId>): DataResult<Map<PlayerId, String>> = error("Unused")
        override suspend fun loadMatchAnalysis(matchId: MatchId): DataResult<MatchAnalysis> = error("Unused")
    }

}
