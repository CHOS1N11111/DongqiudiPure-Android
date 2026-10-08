package io.github.chos1n11111.dongqiudipure.feature.rankings

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

@OptIn(ExperimentalCoroutinesApi::class)
class StandingsViewModelTest {
    private val store = ViewModelStore()

    @Before
    fun setUp() { Dispatchers.setMain(StandardTestDispatcher()) }

    @After
    fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun `catalog failure is visible and retry reloads a non-default competition`() = runTest {
        val repository = RecordingRankings()
        val viewModel = StandingsViewModel(repository, repository)
        store.put("rankings", viewModel)
        viewModel.loadHub(setOf("18"))
        runCurrent()
        assertTrue(viewModel.uiState.value.table is SectionState.Failed)
        assertNull(viewModel.uiState.value.selectedCompetition)

        repository.catalogResult = DataResult.Success(listOf(
            CompetitionCatalogGroup("Other", listOf(CompetitionRef(CompetitionId("18"), "杯赛", null))),
        ))
        viewModel.retry()
        runCurrent()
        assertEquals(2, repository.catalogCalls)
        assertEquals("18", viewModel.uiState.value.selectedCompetition!!.id.raw)
        assertEquals(SectionState.Empty, viewModel.uiState.value.table)
        assertEquals(listOf(CompetitionId("18")), repository.seasonRequests)

        viewModel.loadHub(setOf("18"))
        runCurrent()
        assertEquals(2, repository.catalogCalls)
    }

    @Test
    fun `returning to the same hub after catalog failure retries the catalog`() = runTest {
        val repository = RecordingRankings()
        val viewModel = StandingsViewModel(repository, repository)
        store.put("rankings", viewModel)
        viewModel.loadHub(setOf("18"))
        runCurrent()
        viewModel.loadHub(setOf("18"))
        runCurrent()
        assertEquals(2, repository.catalogCalls)
        assertTrue(viewModel.uiState.value.statisticTable is SectionState.Failed)
    }

    private class RecordingRankings : StandingsRepository, FootballCatalogRepository {
        var catalogResult: DataResult<List<CompetitionCatalogGroup>> = DataResult.Failure(AppError.Network(NetworkKind.NoConnection))
        var catalogCalls = 0
        val seasonRequests = mutableListOf<CompetitionId>()
        override val importantCompetitions = emptyList<CompetitionRef>()
        override val defaultRankingCompetitions = emptyList<CompetitionRef>()
        override val defaultCompetitions = emptyList<CompetitionRef>()
        override suspend fun loadCompetitionCatalog(): DataResult<List<CompetitionCatalogGroup>> {
            catalogCalls++
            return catalogResult
        }
        override suspend fun loadSeasons(competitionId: CompetitionId): DataResult<List<SeasonOption>> {
            seasonRequests += competitionId
            return DataResult.Success(emptyList())
        }
        override suspend fun loadStandings(competition: CompetitionRef, seasonId: SeasonId?): DataResult<StandingTable?> = error("Unused")
        override suspend fun loadRankingMetrics(competitionId: CompetitionId, section: RankingSection, seasonId: SeasonId?): DataResult<List<RankingMetric>> = error("Unused")
        override suspend fun loadRanking(competition: CompetitionRef, section: RankingSection, metric: RankingMetric, seasonId: SeasonId?): DataResult<StatisticRankingTable?> = error("Unused")
    }

}
