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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.MatchStatusBadge
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.PlayerAvatar
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.MissingValue
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.ValueText
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.TeamCrest
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.ArticleSummary
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.MatchStatus
import io.github.chos1n11111.dongqiudipure.core.model.MatchSummary
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerSeasonStat
import io.github.chos1n11111.dongqiudipure.core.model.SquadMember
import io.github.chos1n11111.dongqiudipure.core.model.TeamSquadGroup
import io.github.chos1n11111.dongqiudipure.core.model.TeamMemberGroupKind
import io.github.chos1n11111.dongqiudipure.core.model.hasScore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 阵容名单。
 *
 * 分组和组内顺序都来自服务端。Repository 已将教练、工作人员放在球员之前，
 * 球员依次为前锋、中场、后卫、门将。
 */
@Composable
fun SquadList(
    squad: List<TeamSquadGroup>,
    onPlayerClick: (PlayerId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        squad.forEach { group ->
            Text(
                text = group.title,
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
            if (
                group.kind == TeamMemberGroupKind.Coaches ||
                group.kind == TeamMemberGroupKind.Staff
            ) {
                StaffGrid(group.members, onPlayerClick)
            } else {
                SquadColumnHeader(group.statisticLabels)
                group.members.forEach { member ->
                    SquadRow(
                        member = member,
                        statisticLabels = group.statisticLabels,
                        onClick = { onPlayerClick(member.id) },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun StaffGrid(members: List<SquadMember>, onPlayerClick: (PlayerId) -> Unit) {
    var expanded by remember(members) { mutableStateOf(false) }
    val visibleMembers = if (expanded) members else members.take(6)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = DqdSpacing.sm, vertical = DqdSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        visibleMembers.chunked(2).forEach { rowMembers ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
            ) {
                rowMembers.forEach { member ->
                    StaffCell(
                        member = member,
                        onClick = { onPlayerClick(member.id) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowMembers.size == 1) Box(Modifier.weight(1f))
            }
        }
        if (members.size > 6) {
            Text(
                text = stringResource(
                    if (expanded) R.string.team_collapse_all else R.string.team_expand_all,
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = DqdSpacing.sm),
            )
        }
    }
}

@Composable
private fun StaffCell(
    member: SquadMember,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .defaultMinSize(minHeight = DqdSize.touchTarget)
            .padding(DqdSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        PlayerAvatar(member.id, member.name, member.avatarUrl, 38.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = member.name,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            member.roleLabel?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            val details = listOfNotNull(member.ageLabel, member.nationality).joinToString(" · ")
            if (details.isNotEmpty()) {
                Text(
                    text = details,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SquadColumnHeader(labels: List<String>) {
    if (labels.isEmpty()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f))
        labels.take(4).forEachIndexed { index, label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width(if (index == 3) 54.dp else 38.dp),
            )
        }
    }
}

@Composable
private fun SquadRow(
    member: SquadMember,
    statisticLabels: List<String>,
    onClick: () -> Unit,
) {
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

        PlayerAvatar(
            playerId = member.id,
            playerName = member.name,
            avatarUrl = member.avatarUrl,
            size = 32.dp,
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (member.isCaptain) {
                    Text(
                        text = stringResource(R.string.team_captain),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            val detail = listOfNotNull(
                member.roleLabel,
                member.ageLabel,
                member.nationality,
            ).joinToString(" · ")
            if (detail.isNotEmpty()) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        val stats = statisticLabels.take(4).map { label ->
            member.stats.firstOrNull { it.label == label }?.value
        }
        stats.forEachIndexed { index, value ->
            Box(
                modifier = Modifier.width(if (index == 3) 54.dp else 38.dp),
                contentAlignment = Alignment.Center,
            ) {
                ValueText(
                    value = value,
                    style = if (index == 3) DqdTheme.dataText.tableCellStrong
                    else DqdTheme.dataText.tableCell,
                )
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
        fixtures.groupBy { it.dateLabel?.take(7) }.forEach { (month, matches) ->
            month?.let {
                Text(
                    text = formatMonthLabel(it),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
                )
            }
            matches.forEach { match ->
                EntityFixtureRow(
                    match = match,
                    onClick = { onMatchClick(match.id) },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
fun EntityFixtureRow(
    match: MatchSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .defaultMinSize(minHeight = 66.dp)
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        Column(Modifier.width(82.dp)) {
            Text(
                text = listOfNotNull(match.dateLabel?.takeLast(5), match.kickoffLabel)
                    .joinToString(" "),
                style = DqdTheme.dataText.minuteLabel,
                maxLines = 1,
            )
            Text(
                text = listOfNotNull(
                    match.dateLabel.weekdayLabel(),
                    match.matchInfoLabel ?: listOfNotNull(
                        match.competition.name,
                        match.competition.roundLabel,
                    ).joinToString(" · "),
                ).joinToString(" "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        FixtureTeam(match.home, home = true, Modifier.weight(1f))
        Box(Modifier.width(56.dp), contentAlignment = Alignment.Center) {
            if (match.status.hasScore) {
                Text(
                    text = "${match.homeScore ?: "—"}-${match.awayScore ?: "—"}",
                    style = DqdTheme.dataText.scoreMedium,
                    color = if (match.status is MatchStatus.Live || match.status == MatchStatus.HalfTime) {
                        DqdTheme.sports.live
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            } else {
                MatchStatusBadge(match.status)
            }
        }
        FixtureTeam(match.away, home = false, Modifier.weight(1f))
    }
}

@Composable
private fun FixtureTeam(
    team: io.github.chos1n11111.dongqiudipure.core.model.TeamRef,
    home: Boolean,
    modifier: Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (home) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        TeamCrest(
            teamId = team.id,
            teamName = team.name,
            crestUrl = team.crestUrl,
            size = 28.dp,
        )
        Text(
            text = team.name,
            style = MaterialTheme.typography.labelSmall,
            textAlign = if (home) TextAlign.End else TextAlign.Start,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun String?.weekdayLabel(): String? = runCatching {
    val date = this?.take(10)?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }
        ?: return null
    "周" + date.dayOfWeek.getDisplayName(java.time.format.TextStyle.NARROW, Locale.CHINA)
}.getOrNull()

private fun formatMonthLabel(raw: String): String {
    val parts = raw.split('-')
    val month = parts.getOrNull(1)?.toIntOrNull() ?: return raw
    return "${parts.firstOrNull().orEmpty()}年${month}月"
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
    columns: Int = 2,
) {
    val sorted = stats.sortedBy { it.displayOrder }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer),
    ) {
        sorted.chunked(columns).forEach { pair ->
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
                repeat(columns - pair.size) { Box(modifier = Modifier.weight(1f)) }
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
                    article.tag?.let { tag ->
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    article.commentCount?.let { count ->
                        Text(
                            text = stringResource(R.string.entity_news_comments, count),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}
