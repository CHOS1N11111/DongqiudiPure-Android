package io.github.chos1n11111.dongqiudipure.feature.rankings

import androidx.compose.foundation.background
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.MatchRow
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SectionContainer
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.TeamCrest
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.ValueText
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.SportsColors
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.StandingRow
import io.github.chos1n11111.dongqiudipure.core.model.StandingZone
import io.github.chos1n11111.dongqiudipure.core.model.TeamId

/**
 * 榜单内容体。
 *
 * 不含 Scaffold 与顶栏 —— 因为它有两个入口且外壳不同：
 *  - 「数据」根 tab（[DataHubRoute]）：顶栏是标题 + 搜索，上方多一条赛事切换器
 *  - 从搜索或文章赛事 chip 进入的详情页（[StandingsRoute]）：顶栏是返回 + 赛事名
 *
 * 分栏与表格完全共用，避免两套实现分叉。
 */
@Composable
fun RankingsContent(
    uiState: RankingsUiState,
    onTeamClick: (TeamId) -> Unit,
    onPlayerClick: (PlayerId) -> Unit,
    onMatchClick: (MatchId) -> Unit,
    onTabSelect: (RankingTab) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        RankingTabRow(selected = uiState.selectedTab, onSelect = onTabSelect)
        Box(modifier = Modifier.fillMaxSize()) {
            when (uiState.selectedTab) {
                RankingTab.Scorers -> SectionContainer(
                    state = uiState.scorers,
                    onRetry = onRetry,
                    emptyTitle = stringResource(R.string.scorers_empty_title),
                    emptyDescription = stringResource(R.string.scorers_empty_description),
                ) { scorers ->
                    PlayerRankingList(table = scorers, onPlayerClick = onPlayerClick)
                }

                RankingTab.Assists -> SectionContainer(
                    state = uiState.assists,
                    onRetry = onRetry,
                    emptyTitle = stringResource(R.string.assists_empty_title),
                    emptyDescription = stringResource(R.string.assists_empty_description),
                ) { assists ->
                    PlayerRankingList(table = assists, onPlayerClick = onPlayerClick)
                }

                RankingTab.Fixtures -> SectionContainer(
                    state = uiState.fixtures,
                    onRetry = onRetry,
                    emptyTitle = stringResource(R.string.fixtures_empty_title),
                    emptyDescription = stringResource(R.string.fixtures_empty_description),
                ) { fixtures ->
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(fixtures, key = { it.id.raw }) { match ->
                            MatchRow(
                                match = match,
                                onClick = { onMatchClick(match.id) },
                                modifier = Modifier.background(
                                    MaterialTheme.colorScheme.surfaceContainer,
                                ),
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }

                RankingTab.Standings -> SectionContainer(
                state = uiState.table,
                onRetry = onRetry,
                emptyTitle = stringResource(R.string.standings_empty_title),
                emptyDescription = stringResource(R.string.standings_empty_description),
            ) { table ->
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item(key = "season") { SeasonBar(uiState.seasonLabel) }
                    item(key = "header") { TableHeader() }

                    val rows = table.rows
                    rows.forEachIndexed { index, row ->
                        val previous = rows.getOrNull(index - 1)

                        // 分区分隔行：非颜色编码的第二重提示。
                        // 只在存在上一行时判断 —— 「没有上一行」与「上一行没有分区」
                        // 是两回事，混淆会漏掉降级分界（上方相邻行往往无分区）。
                        val separatorRes = previous?.let { zoneSeparatorLabelRes(it.zone, row.zone) }
                        if (separatorRes != null) {
                            item(key = "sep-$index") { ZoneSeparator(stringResource(separatorRes)) }
                        }

                        // 名次不连续说明中间的行未展示，如实标出，不假装榜单是完整的。
                        if (previous != null && row.rank > previous.rank + 1) {
                            item(key = "gap-$index") {
                                RankGap(from = previous.rank + 1, to = row.rank - 1)
                            }
                        }

                        item(key = "row-${row.team.id.raw}") {
                            StandingRowItem(
                                row = row,
                                onClick = { onTeamClick(row.team.id) },
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }

                    item(key = "legend") { ZoneLegend(rows.mapNotNull { it.zone }.distinct()) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RankingTabRow(
    selected: RankingTab,
    onSelect: (RankingTab) -> Unit,
) {
    PrimaryTabRow(
        selectedTabIndex = RankingTab.entries.indexOf(selected),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        RankingTab.entries.forEach { tab ->
            val isSelected = tab == selected
            Tab(
                selected = isSelected,
                onClick = { onSelect(tab) },
                // 选中态在颜色与字重上都不同，不只靠颜色区分。
                selectedContentColor = MaterialTheme.colorScheme.onSurface,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                text = {
                    Text(
                        text = stringResource(tab.labelRes),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                },
            )
        }
    }
}

/**
 * 分区分界标签。
 *
 * 这是「颜色不是唯一状态提示」的落点：左侧色条之外，
 * 用一行文字明确说出分界在哪里，色觉差异用户同样能读懂。
 */
@StringRes
private fun zoneSeparatorLabelRes(previous: StandingZone?, current: StandingZone?): Int? = when {
    previous == current -> null
    // 降级区先判断：它上方相邻的行通常没有分区，
    // 若先按 previous 分支判断会直接落到 else 而漏掉这条分界。
    current == StandingZone.Relegation -> R.string.standings_zone_boundary_relegation
    previous == StandingZone.ChampionsLeague -> R.string.standings_zone_boundary_ucl
    previous == StandingZone.EuropaLeague -> R.string.standings_zone_boundary_uel
    previous == StandingZone.ConferenceLeague -> R.string.standings_zone_boundary_uecl
    previous == StandingZone.Promotion -> R.string.standings_zone_boundary_promotion
    else -> null
}

@StringRes
private fun StandingZone.labelRes(): Int = when (this) {
    StandingZone.ChampionsLeague -> R.string.standings_zone_ucl
    StandingZone.EuropaLeague -> R.string.standings_zone_uel
    StandingZone.ConferenceLeague -> R.string.standings_zone_uecl
    StandingZone.Promotion -> R.string.standings_zone_promotion
    StandingZone.Relegation -> R.string.standings_zone_relegation
}

private fun StandingZone.color(sports: SportsColors): Color = when (this) {
    StandingZone.ChampionsLeague -> sports.zoneChampions
    StandingZone.EuropaLeague -> sports.zoneEuropa
    StandingZone.ConferenceLeague -> sports.zoneConference
    StandingZone.Promotion -> sports.zonePromotion
    StandingZone.Relegation -> sports.zoneRelegation
}

@Composable
private fun SeasonBar(seasonLabel: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
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

private val NumColumnWidth = 30.dp
private val RankColumnWidth = 30.dp

@Composable
private fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(vertical = DqdSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.width(RankColumnWidth))
        Text(
            text = stringResource(R.string.standings_column_team),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        listOf(
            R.string.standings_column_played,
            R.string.standings_column_won,
            R.string.standings_column_drawn,
            R.string.standings_column_lost,
            R.string.standings_column_goal_diff,
            R.string.standings_column_points,
        ).forEach { labelRes ->
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(NumColumnWidth),
            )
        }
        Box(modifier = Modifier.width(DqdSpacing.sm))
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun StandingRowItem(row: StandingRow, onClick: () -> Unit) {
    val sports = DqdTheme.sports
    val zoneColor = row.zone?.color(sports)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .defaultMinSize(minHeight = DqdSize.touchTarget),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 左侧色条：分区的第一重编码。
        Box(
            modifier = Modifier
                .width(RankColumnWidth)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            if (zoneColor != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        // 不贴屏幕边缘：紧贴时在圆角屏上会被误读为裁切。
                        .padding(start = 6.dp)
                        .width(3.dp)
                        .height(22.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(zoneColor)
                        .clearAndSetSemantics { },
                )
            }
            Text(
                text = row.rank.toString(),
                style = DqdTheme.dataText.tableCell,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = DqdSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
        ) {
            TeamCrest(
                teamId = row.team.id,
                teamName = row.team.name,
                crestUrl = row.team.crestUrl,
                size = 17.dp,
            )
            Text(
                text = row.team.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // 每个数值都可能缺失。ValueText 会在 null 时渲染虚线破折号，
        // 与真实的 0 在视觉上可区分。
        NumCell(row.played)
        NumCell(row.won)
        NumCell(row.drawn)
        NumCell(row.lost)
        NumCell(row.goalDifference, signed = true)
        NumCell(row.points, strong = true)
        Box(modifier = Modifier.width(DqdSpacing.sm))
    }
}

@Composable
private fun NumCell(value: Int?, strong: Boolean = false, signed: Boolean = false) {
    val text = value?.let { if (signed && it > 0) "+$it" else it.toString() }
    Box(
        modifier = Modifier.width(NumColumnWidth),
        contentAlignment = Alignment.Center,
    ) {
        ValueText(
            value = text,
            style = if (strong) {
                DqdTheme.dataText.tableCellStrong.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                )
            } else {
                DqdTheme.dataText.tableCell.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
    }
}

@Composable
private fun ZoneSeparator(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = DqdSpacing.md, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

/** 名次不连续时的省略行。如实说明中间还有多少支球队未展示。 */
@Composable
private fun RankGap(from: Int, to: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = DqdSpacing.md, vertical = DqdSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        Text(
            text = "⋯",
            style = DqdTheme.dataText.tableCell,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(RankColumnWidth - DqdSpacing.md),
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.standings_rank_gap, from, to),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

/** 图例：分区的第三重编码。 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ZoneLegend(zones: List<StandingZone>) {
    if (zones.isEmpty()) return
    val sports = DqdTheme.sports

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        zones.forEach { zone ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(11.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(zone.color(sports)),
                )
                Text(
                    text = stringResource(zone.labelRes()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

