package io.github.chos1n11111.dongqiudipure.feature.rankings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.PlayerAvatar
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SectionContainer
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.TeamCrest
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.ValueText
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.RankingRow
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.StatisticRankingTable
import io.github.chos1n11111.dongqiudipure.core.model.TeamId

@Composable
fun StatisticRankingContent(
    state: SectionState<StatisticRankingTable>,
    onTeamClick: (TeamId) -> Unit,
    onPlayerClick: (PlayerId) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionContainer(
        state = state,
        onRetry = onRetry,
        modifier = modifier.fillMaxSize(),
        emptyTitle = stringResource(R.string.statistic_ranking_empty_title),
        emptyDescription = stringResource(R.string.statistic_ranking_empty_description),
    ) { table ->
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item(key = "season") { SeasonChipBar(table.seasonLabel) }
            item(key = "header") { RankingHeader(table.valueColumnLabel) }
            itemsIndexed(
                items = table.rows,
                key = { index, row ->
                    row.playerId?.let { "p-${it.raw}" }
                        ?: row.team?.let { "t-${it.id.raw}" }
                        ?: "r-$index-${row.name}"
                },
            ) { _, row ->
                StatisticRankingRow(
                    row = row,
                    onTeamClick = onTeamClick,
                    onPlayerClick = onPlayerClick,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun RankingHeader(valueLabel: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(vertical = DqdSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.width(40.dp))
        Text(
            text = stringResource(R.string.statistic_ranking_column_name),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = valueLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(72.dp),
        )
        Box(modifier = Modifier.width(DqdSpacing.sm))
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun StatisticRankingRow(
    row: RankingRow,
    onTeamClick: (TeamId) -> Unit,
    onPlayerClick: (PlayerId) -> Unit,
) {
    val playerId = row.playerId
    val team = row.team
    val onClick = when {
        playerId != null -> ({ onPlayerClick(playerId) })
        team != null -> ({ onTeamClick(team.id) })
        else -> null
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .defaultMinSize(minHeight = DqdSize.touchTarget)
            .padding(horizontal = DqdSpacing.sm, vertical = DqdSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        Text(
            text = row.rankLabel,
            style = DqdTheme.dataText.tableCell,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(32.dp),
        )

        if (playerId != null) {
            PlayerAvatar(
                playerId = playerId,
                playerName = row.name,
                avatarUrl = row.imageUrl,
                size = 34.dp,
            )
        } else if (team != null) {
            TeamCrest(
                teamId = team.id,
                teamName = team.name,
                crestUrl = row.imageUrl ?: team.crestUrl,
                size = 30.dp,
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = row.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (playerId != null && team != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    TeamCrest(
                        teamId = team.id,
                        teamName = team.name,
                        crestUrl = team.crestUrl,
                        size = 13.dp,
                    )
                    Text(
                        text = team.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        Box(modifier = Modifier.width(72.dp), contentAlignment = Alignment.Center) {
            ValueText(
                value = row.value,
                style = DqdTheme.dataText.tableCellStrong.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }
    }
}

@Composable
private fun SeasonChipBar(seasonLabel: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
    ) {
        Text(
            text = seasonLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 7.dp, vertical = 3.dp),
        )
    }
}
