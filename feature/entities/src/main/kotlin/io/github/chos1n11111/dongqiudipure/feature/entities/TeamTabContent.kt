package io.github.chos1n11111.dongqiudipure.feature.entities

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
import androidx.compose.foundation.shape.CircleShape
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
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.MatchRow
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.MissingValue
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.labelRes
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.ValueText
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.ArticleSummary
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.MatchSummary
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerPosition
import io.github.chos1n11111.dongqiudipure.core.model.PlayerSeasonStat
import io.github.chos1n11111.dongqiudipure.core.model.SquadMember

/**
 * 阵容名单。
 *
 * 按位置分组，组内保持服务端给的顺序。位置为 [PlayerPosition.Unknown] 的
 * 成员单列一组，不强行塞进某个位置。
 */
@Composable
fun SquadList(
    squad: List<SquadMember>,
    onPlayerClick: (PlayerId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val grouped = squad.groupBy { it.position }
    val order = listOf(
        PlayerPosition.Goalkeeper,
        PlayerPosition.Defender,
        PlayerPosition.Midfielder,
        PlayerPosition.Forward,
        PlayerPosition.Unknown,
    )

    Column(modifier = modifier.fillMaxWidth()) {
        order.forEach { position ->
            val group = grouped[position].orEmpty()
            if (group.isEmpty()) return@forEach

            Text(
                text = stringResource(position.labelRes()),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(
                        horizontal = DqdSpacing.listHorizontal,
                        vertical = DqdSpacing.sm,
                    ),
            )
            group.forEach { member ->
                SquadRow(member = member, onClick = { onPlayerClick(member.id) })
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun SquadRow(member: SquadMember, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .defaultMinSize(minHeight = DqdSize.touchTarget)
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md),
    ) {
        // 球衣号码缺失时显示「—」，不留空也不补 0。
        Box(modifier = Modifier.width(26.dp), contentAlignment = Alignment.Center) {
            ValueText(
                value = member.shirtNumber,
                style = DqdTheme.dataText.tableCell.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        )

        Text(
            text = member.name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        Text(
            text = member.nationality.orEmpty(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Box(modifier = Modifier.width(48.dp), contentAlignment = Alignment.CenterEnd) {
            val age = member.ageLabel
            if (age != null) {
                Text(
                    text = age,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                MissingValue(style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun TeamFixtureList(
    fixtures: List<MatchSummary>,
    onMatchClick: (MatchId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        fixtures.forEach { match ->
            MatchRow(
                match = match,
                onClick = { onMatchClick(match.id) },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer),
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

/**
 * 球队数据。
 *
 * 指标集合由服务端驱动，两列布局。缺失项与零值在同一张表里必须可区分 ——
 * 「零封 2」和「预期进球 —」的差别一眼就要看出来。
 */
@Composable
fun TeamStatsGrid(
    stats: List<PlayerSeasonStat>,
    modifier: Modifier = Modifier,
) {
    val sorted = stats.sortedBy { it.displayOrder }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer),
    ) {
        sorted.chunked(2).forEach { pair ->
            Row(modifier = Modifier.fillMaxWidth()) {
                pair.forEach { stat ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(
                                horizontal = DqdSpacing.listHorizontal,
                                vertical = DqdSpacing.md,
                            ),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        ValueText(
                            value = stat.value,
                            style = DqdTheme.dataText.scoreMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                        )
                        Text(
                            text = stat.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                // 奇数项时补一个空格子，保持两列对齐。
                if (pair.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

/**
 * 球队关联资讯。
 *
 * 这里用紧凑版式而不是复用资讯流的四种条目形态 ——
 * 在球队页里图片会喧宾夺主，读者要的是「最近发生了什么」。
 */
@Composable
fun TeamNewsList(
    news: List<ArticleSummary>,
    onArticleClick: (ArticleId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        news.forEach { article ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .clickable { onArticleClick(article.id) }
                    .padding(
                        horizontal = DqdSpacing.listHorizontal,
                        vertical = DqdSpacing.md,
                    ),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm)) {
                    Text(
                        text = article.source,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = article.publishedLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}
