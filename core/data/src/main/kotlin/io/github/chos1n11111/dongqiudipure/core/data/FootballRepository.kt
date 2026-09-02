package io.github.chos1n11111.dongqiudipure.core.data

import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionRef
import io.github.chos1n11111.dongqiudipure.core.model.DataResult
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.MatchSummary
import io.github.chos1n11111.dongqiudipure.core.model.StandingTable
import java.time.LocalDate

interface MatchRepository {
    suspend fun loadMatches(date: LocalDate): DataResult<List<MatchSummary>>
    suspend fun loadMatch(matchId: MatchId): DataResult<MatchSummary?>
}

interface StandingsRepository {
    val supportedCompetitions: List<CompetitionRef>
    suspend fun loadStandings(competitionId: CompetitionId): DataResult<StandingTable?>
}
