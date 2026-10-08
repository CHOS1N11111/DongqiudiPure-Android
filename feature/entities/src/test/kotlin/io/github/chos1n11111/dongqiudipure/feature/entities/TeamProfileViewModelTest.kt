package io.github.chos1n11111.dongqiudipure.feature.entities

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
import androidx.paging.PagingData
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class TeamProfileViewModelTest {
    private val store = ViewModelStore()

    @Before
    fun setUp() { Dispatchers.setMain(StandardTestDispatcher()) }

    @After
    fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun `empty schedule and squad keep seasons and allow selecting another season`() = runTest {
        val repository = EmptyTeamRepository()
        val viewModel = TeamProfileViewModel(repository)
        store.put("team", viewModel)
        viewModel.load(TeamId("50000513"))
        runCurrent()
        val initial = viewModel.uiState.value
        assertEquals(repository.seasons, (initial.schedule as SectionState.Content).value.seasons)
        assertEquals(repository.seasons, (initial.squad as SectionState.Content).value.seasons)

        viewModel.selectScheduleSeason("old")
        viewModel.selectSquadSeason("old")
        runCurrent()
        val state = viewModel.uiState.value
        val schedule = (state.schedule as SectionState.Content).value
        val squad = (state.squad as SectionState.Content).value
        assertEquals("old", schedule.selectedSeasonId)
        assertEquals("old", squad.selectedSeasonId)
        assertEquals(repository.seasons, schedule.seasons)
        assertEquals(repository.seasons, squad.seasons)
    }

    private class EmptyTeamRepository : FootballEntityRepository {
        val seasons = listOf(SeasonOption("new", "2026", true), SeasonOption("old", "2025", false))
        override suspend fun loadTeamProfile(teamId: TeamId): DataResult<TeamProfile?> = DataResult.Success(null)
        override suspend fun loadTeamStatistics(teamId: TeamId, seasonId: String?): DataResult<TeamStatistics?> = DataResult.Success(null)
        override suspend fun loadTeamSchedule(teamId: TeamId, seasonId: String?) = DataResult.Success(TeamScheduleData(seasons, seasonId ?: "new", emptyList()))
        override suspend fun loadTeamSquad(teamId: TeamId, seasonId: String?) = DataResult.Success(TeamSquadData(seasons, seasonId ?: "new", emptyList()))
        override suspend fun loadTeamTransfers(teamId: TeamId, windowId: String?) = DataResult.Success(TeamTransferData(emptyList(), null, emptyList()))
        override fun pagedTeamNews(teamId: TeamId) = flowOf(PagingData.empty<ArticleSummary>())
        override fun pagedTeamCircle(groupId: String) = flowOf(PagingData.empty<TeamCirclePost>())
        override fun pagedPlayerNews(playerId: PlayerId) = flowOf(PagingData.empty<ArticleSummary>())
        override suspend fun searchEntities(query: String): DataResult<EntitySearchResults> = error("Unused")
        override suspend fun loadPlayerOverview(playerId: PlayerId): DataResult<PlayerOverview?> = error("Unused")
        override suspend fun loadPlayerCareer(playerId: PlayerId): DataResult<List<CareerEntry>> = error("Unused")
        override suspend fun loadPlayerStatistics(playerId: PlayerId): DataResult<PlayerStatisticsData> = error("Unused")
        override suspend fun loadPlayerMatches(playerId: PlayerId, page: Int): DataResult<PlayerMatchPage> = error("Unused")
        override suspend fun loadPlayerHeatMap(playerId: PlayerId, seasonId: String, teamId: TeamId): DataResult<PlayerHeatMap> = error("Unused")
        override suspend fun loadPlayerShotMap(playerId: PlayerId, matchId: MatchId): DataResult<PlayerShotMap> = error("Unused")
        override suspend fun loadPlayerAbility(playerId: PlayerId): DataResult<PlayerAbility?> = error("Unused")
    }

}
