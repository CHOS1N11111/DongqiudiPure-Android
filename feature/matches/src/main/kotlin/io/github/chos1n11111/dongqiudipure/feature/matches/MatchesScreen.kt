package io.github.chos1n11111.dongqiudipure.feature.matches

import androidx.compose.foundation.background
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.chos1n11111.dongqiudipure.core.designsystem.R as DesignR
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.MatchRow
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SectionContainer
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SkeletonBox
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import java.time.LocalDate

@Composable
fun MatchesRoute(
    onMatchClick: (MatchId) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MatchesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MatchesScreen(
        uiState = uiState,
        onMatchClick = onMatchClick,
        onSearchClick = onSearchClick,
        onDateSelect = viewModel::selectDate,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesScreen(
    uiState: MatchesUiState,
    onMatchClick: (MatchId) -> Unit,
    onSearchClick: () -> Unit,
    onDateSelect: (LocalDate) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.matches_title)) },
                    actions = {
                        IconButton(onClick = onSearchClick) {
                            Icon(
                                painter = painterResource(DqdIcons.Search),
                                contentDescription = stringResource(DesignR.string.ds_action_search),
                                modifier = Modifier.size(DqdSize.iconMedium),
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
                DateStrip(
                    days = uiState.days,
                    selectedDate = uiState.selectedDate,
                    onSelect = onDateSelect,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        SectionContainer(
            state = uiState.groups,
            onRetry = onRetry,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            emptyTitle = stringResource(R.string.matches_empty_title),
            emptyDescription = stringResource(R.string.matches_empty_description),
            loading = { MatchesSkeleton() },
        ) { groups ->
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                groups.forEach { group ->
                    item(key = "hd-${group.competition.id.raw}") {
                        CompetitionHeader(
                            name = group.competition.name,
                            round = group.competition.roundLabel,
                        )
                    }
                    items(
                        items = group.matches,
                        key = { it.id.raw },
                    ) { match ->
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
        }
    }
}

@Composable
private fun DateStrip(
    days: List<MatchDay>,
    selectedDate: LocalDate,
    onSelect: (LocalDate) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = DqdSpacing.sm, vertical = DqdSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        items(days, key = { it.date.toString() }) { day ->
            val selected = day.date == selectedDate
            DateCell(day = day, selected = selected, onClick = { onSelect(day.date) })
        }
    }
}

@Composable
private fun DateCell(
    day: MatchDay,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent
    val content = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    // 展示文本在 UI 层生成，ViewModel 只给原始日期。
    val weekdayLabel = if (day.isToday) {
        stringResource(R.string.matches_date_today)
    } else {
        stringResource(weekdayLabelRes(day.date))
    }
    val dayLabel = day.date.dayOfMonth.toString()
    val a11y = if (day.hasLiveMatch) {
        stringResource(R.string.matches_date_a11y_has_live, weekdayLabel, day.date.dayOfMonth)
    } else {
        stringResource(R.string.matches_date_a11y, weekdayLabel, day.date.dayOfMonth)
    }

    Column(
        modifier = Modifier
            .width(52.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
            .semantics {
                this.selected = selected
                contentDescription = a11y
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = weekdayLabel,
            style = MaterialTheme.typography.labelSmall,
            color = content,
            textAlign = TextAlign.Center,
        )
        Text(
            text = dayLabel,
            style = DqdTheme.dataText.scoreMedium,
            color = content,
            textAlign = TextAlign.Center,
        )
        // 当日有进行中比赛的标记。语义已在 contentDescription 中说明，
        // 因此这个圆点只是视觉冗余，不是唯一提示。
        Box(
            modifier = Modifier
                .padding(top = 1.dp)
                .size(4.dp)
                .clip(CircleShape)
                .background(
                    if (day.hasLiveMatch) {
                        DqdTheme.sports.live
                    } else {
                        androidx.compose.ui.graphics.Color.Transparent
                    },
                ),
        )
    }
}

@Composable
private fun CompetitionHeader(name: String, round: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(
                horizontal = DqdSpacing.listHorizontal,
                vertical = DqdSpacing.sm,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (round != null) "$name · $round" else name,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MatchesSkeleton() {
    Column(modifier = Modifier.fillMaxWidth()) {
        repeat(4) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DqdSpacing.md, vertical = DqdSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md),
            ) {
                SkeletonBox(Modifier.width(48.dp).height(30.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    SkeletonBox(Modifier.fillMaxWidth(0.6f).height(19.dp))
                    SkeletonBox(Modifier.fillMaxWidth(0.5f).height(19.dp))
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Preview(name = "比赛 · 深色", showBackground = true)
@Composable
private fun MatchesDarkPreview() {
    DqdTheme(darkTheme = true) {
        MatchesScreen(
            uiState = previewState(),
            onMatchClick = {},
            onSearchClick = {},
            onDateSelect = {},
            onRetry = {},
        )
    }
}

@Preview(name = "比赛 · 浅色", showBackground = true)
@Composable
private fun MatchesLightPreview() {
    DqdTheme(darkTheme = false) {
        MatchesScreen(
            uiState = previewState(),
            onMatchClick = {},
            onSearchClick = {},
            onDateSelect = {},
            onRetry = {},
        )
    }
}

@StringRes
private fun weekdayLabelRes(date: LocalDate): Int = when (date.dayOfWeek.value) {
    1 -> R.string.matches_weekday_mon
    2 -> R.string.matches_weekday_tue
    3 -> R.string.matches_weekday_wed
    4 -> R.string.matches_weekday_thu
    5 -> R.string.matches_weekday_fri
    6 -> R.string.matches_weekday_sat
    else -> R.string.matches_weekday_sun
}

private fun previewState(): MatchesUiState {
    val today = LocalDate.of(2026, 9, 1)
    val samples = io.github.chos1n11111.dongqiudipure.core.sampledata.SampleMatches.matches
    return MatchesUiState(
        days = listOf(
            MatchDay(today.minusDays(2), isToday = false, hasLiveMatch = true),
            MatchDay(today.minusDays(1), isToday = false, hasLiveMatch = false),
            MatchDay(today, isToday = true, hasLiveMatch = true),
            MatchDay(today.plusDays(1), isToday = false, hasLiveMatch = false),
        ),
        selectedDate = today,
        groups = SectionState.Content(
            samples.groupBy { it.competition }
                .map { (competition, matches) -> CompetitionGroup(competition, matches) },
        ),
    )
}
