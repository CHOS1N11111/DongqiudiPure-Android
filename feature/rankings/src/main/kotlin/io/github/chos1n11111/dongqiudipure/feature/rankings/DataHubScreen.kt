package io.github.chos1n11111.dongqiudipure.feature.rankings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionRef
import io.github.chos1n11111.dongqiudipure.core.model.TeamId

/**
 * 「数据」根 tab。
 *
 * 不做成「赛事目录 → 点进去看榜单」：那样打开时几乎是空的，
 * 一个根 tab 承载一份可点列表太薄。改为**赛事切换器 + 榜单直接铺在页面上**，
 * 打开即有内容。
 *
 * 与 [StandingsRoute] 共用 [RankingsContent]，分栏与表格不分叉。
 *
 * 球员、球队没有合法的目录页 —— 没有服务端目录接口，按热门写死又违反
 * FEATURES.md 的「不写死永久名单」。它们的入口是榜单行点击，
 * 本页不假装拥有一个球员目录。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataHubRoute(
    onTeamClick: (TeamId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StandingsViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) { viewModel.loadHub() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.data_title)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
                CompetitionSwitcher(
                    competitions = uiState.competitions,
                    selected = uiState.selectedCompetition,
                    onSelect = viewModel::selectCompetition,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        RankingsContent(
            uiState = uiState,
            onTeamClick = onTeamClick,
            onRetry = viewModel::retry,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        )
    }
}

/**
 * 赛事切换器。
 *
 * 选中态同时用底色与文字颜色表达，并通过 `selected` 语义暴露给读屏 ——
 * 颜色不是唯一提示。
 */
@Composable
private fun CompetitionSwitcher(
    competitions: List<CompetitionRef>,
    selected: CompetitionRef?,
    onSelect: (CompetitionRef) -> Unit,
) {
    if (competitions.isEmpty()) return

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = DqdSpacing.sm, vertical = DqdSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(competitions, key = { it.id.raw }) { competition ->
            val isSelected = competition.id == selected?.id
            Text(
                text = competition.name,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                    )
                    .clickable { onSelect(competition) }
                    .padding(horizontal = DqdSpacing.md, vertical = 7.dp)
                    .semantics { this.selected = isSelected },
            )
        }
    }
}
