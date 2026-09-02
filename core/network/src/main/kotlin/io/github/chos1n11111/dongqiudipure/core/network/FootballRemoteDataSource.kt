package io.github.chos1n11111.dongqiudipure.core.network

import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.SeasonId
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchListEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.SeasonDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.StandingEnvelopeDto

interface FootballRemoteDataSource {
    suspend fun loadImportantMatches(): ApiResult<MatchListEnvelopeDto>
    suspend fun loadSeasons(competitionId: CompetitionId): ApiResult<List<SeasonDto>>
    suspend fun loadStandings(seasonId: SeasonId): ApiResult<StandingEnvelopeDto>
}
