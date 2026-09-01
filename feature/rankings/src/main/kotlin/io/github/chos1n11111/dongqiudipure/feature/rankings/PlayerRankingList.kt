package io.github.chos1n11111.dongqiudipure.feature.rankings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.MissingValue
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.TeamCrest
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.ValueText
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerRankingRow
import io.github.chos1n11111.dongqiudipure.core.model.PlayerRankingTable

/**
 * 球员榜单。
 *
 * 射手榜与助攻榜共用这一套版式，只有数值列的表头不同
 * （由 [PlayerRankingTable.valueColumnLabel] 决定）——
 * 两种榜单的信息结构完全一致，没有理由各写一份。
 */
@Composable
fun PlayerRankingList(
    table: PlayerRankingTable,
    onPlayerClick: (PlayerId) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        item(key = "season") { SeasonChipBar(table.seasonLabel) }
        item(key = "header") { RankingHeader(table.valueColumnLabel) }

        items(table.rows, key = { "r-${it.player.id.raw}" }) { row ->
            PlayerRankingRowItem(row = row, onClick = { onPlayerClick(row.player.id) })
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
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
        Box(modifier = Modifier.width(36.dp))
        Text(
            text = "球员",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "出场",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(40.dp),
        )
        Text(
            text = valueLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(44.dp),
        )
        Box(modifier = Modifier.width(DqdSpacing.sm))
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun PlayerRankingRowItem(row: PlayerRankingRow, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .defaultMinSize(minHeight = DqdSize.touchTarget)
            .padding(vertical = DqdSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = row.rank.toString(),
            style = DqdTheme.dataText.tableCell,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(36.dp),
        )

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = DqdSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = row.player.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // 所属球队缺失（如转会窗口期间未确定）时显示「—」，不猜一个队。
            val team = row.team
            if (team != null) {
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
                    )
                }
            } else {
                MissingValue(style = MaterialTheme.typography.labelSmall)
            }
        }

        Box(modifier = Modifier.width(40.dp), contentAlignment = Alignment.Center) {
            ValueText(
                value = row.appearances,
                style = DqdTheme.dataText.tableCell.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
        Box(modifier = Modifier.width(44.dp), contentAlignment = Alignment.Center) {
            ValueText(
                value = row.primaryValue,
                style = DqdTheme.dataText.tableCellStrong.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }
        Box(modifier = Modifier.width(DqdSpacing.sm))
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
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 7.dp, vertical = 3.dp),
        )
    }
}
