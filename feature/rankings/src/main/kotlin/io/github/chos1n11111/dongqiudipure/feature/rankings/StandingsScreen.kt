package io.github.chos1n11111.dongqiudipure.feature.rankings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.chos1n11111.dongqiudipure.core.designsystem.R as DesignR
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.TeamId

/**
 * 单个赛事的榜单详情页。
 *
 * 从文章等赛事入口进入，赛事固定，没有切换器 ——
 * 用户是带着「我要看这个赛事」的意图来的。
 * 「数据」根 tab 的入口见 [DataHubRoute]，两者共用 [RankingsContent]。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandingsRoute(
    competitionId: CompetitionId,
    onBack: () -> Unit,
    onTeamClick: (TeamId) -> Unit,
    onMatchClick: (MatchId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StandingsViewModel = hiltViewModel(),
) {
    LaunchedEffect(competitionId) { viewModel.load(competitionId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            uiState.competitionName.ifEmpty {
                                stringResource(R.string.data_title)
                            },
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                painter = painterResource(DqdIcons.ArrowBack),
                                contentDescription = stringResource(DesignR.string.ds_action_back),
                                modifier = Modifier.size(DqdSize.iconMedium),
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
                SeasonSwitcher(
                    seasons = uiState.seasons,
                    selected = uiState.selectedSeason,
                    onSelect = viewModel::selectSeason,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        RankingsContent(
            uiState = uiState,
            onTeamClick = onTeamClick,
            onMatchClick = onMatchClick,
            onRetry = viewModel::retry,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        )
    }
}
