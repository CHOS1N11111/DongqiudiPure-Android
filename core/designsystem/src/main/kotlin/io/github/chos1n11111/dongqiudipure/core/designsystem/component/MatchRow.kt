package io.github.chos1n11111.dongqiudipure.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.chos1n11111.dongqiudipure.core.designsystem.R
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme
import io.github.chos1n11111.dongqiudipure.core.model.MatchStatus
import io.github.chos1n11111.dongqiudipure.core.model.MatchSummary
import io.github.chos1n11111.dongqiudipure.core.model.TeamRef
import io.github.chos1n11111.dongqiudipure.core.model.hasScore

/**
 * 比赛行。
 *
 * 行高在所有状态下保持恒定（左侧状态列固定宽度、比分列等宽数字），
 * 这样实时比分变化时列表不跳动、不丢失滚动位置（PLAN.md M4 退出条件）。
 */
@Composable
fun MatchRow(
    match: MatchSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLive = match.status is MatchStatus.Live || match.status == MatchStatus.HalfTime
    val homeWon = (match.homeScore ?: 0) > (match.awayScore ?: 0)
    val awayWon = (match.awayScore ?: 0) > (match.homeScore ?: 0)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .defaultMinSize(minHeight = DqdSize.touchTarget)
            .padding(horizontal = DqdSpacing.md, vertical = DqdSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md),
    ) {
        MatchStatusBadge(
            status = match.status,
            modifier = Modifier.width(48.dp),
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            TeamScoreRow(
                team = match.home,
                score = match.homeScore,
                rank = match.homeRank,
                yellowCards = match.homeYellowCards,
                redCards = match.homeRedCards,
                showScore = match.status.hasScore,
                isLive = isLive,
                // 完场后败方降一级，胜方保持正文色 —— 但胜负本身由比分表达，
                // 颜色只是辅助，不是唯一提示。
                dimmed = match.status == MatchStatus.Finished && !homeWon,
            )
            TeamScoreRow(
                team = match.away,
                score = match.awayScore,
                rank = match.awayRank,
                yellowCards = match.awayYellowCards,
                redCards = match.awayRedCards,
                showScore = match.status.hasScore,
                isLive = isLive,
                dimmed = match.status == MatchStatus.Finished && !awayWon,
            )
            MatchMetaLine(match)
        }

        Icon(
            painter = painterResource(DqdIcons.ChevronRight),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(DqdSize.iconSmall)
                .clearAndSetSemantics { },
        )
    }
}

@Composable
private fun TeamScoreRow(
    team: TeamRef,
    score: Int?,
    rank: String?,
    yellowCards: Int?,
    redCards: Int?,
    showScore: Boolean,
    isLive: Boolean,
    dimmed: Boolean,
) {
    val contentColor = if (dimmed) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        Box(modifier = Modifier.width(24.dp), contentAlignment = Alignment.CenterEnd) {
            rank?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
        TeamCrest(
            teamId = team.id,
            teamName = team.name,
            crestUrl = team.crestUrl,
            size = DqdSize.crestSmall,
        )
        Text(
            text = team.name,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        yellowCards?.takeIf { it > 0 }?.let {
            Text(
                text = stringResource(R.string.ds_match_yellow_cards, it),
                style = MaterialTheme.typography.labelSmall,
                color = DqdTheme.sports.yellowCard,
            )
        }
        redCards?.takeIf { it > 0 }?.let {
            Text(
                text = stringResource(R.string.ds_match_red_cards, it),
                style = MaterialTheme.typography.labelSmall,
                color = DqdTheme.sports.redCard,
            )
        }

        ScoreCell(score = score, showScore = showScore, isLive = isLive, dimmed = dimmed)
    }
}

@Composable
private fun MatchMetaLine(match: MatchSummary) {
    val labels = listOfNotNull(
        match.liveLabel,
        pairedScore(match.homeHalfScore, match.awayHalfScore)?.let {
            stringResource(R.string.ds_match_half_score, it)
        },
        pairedScore(match.homeAggregateScore, match.awayAggregateScore)?.let {
            stringResource(R.string.ds_match_aggregate_score, it)
        },
        pairedScore(match.homePenaltyScore, match.awayPenaltyScore)?.let {
            stringResource(R.string.ds_match_penalty_score, it)
        },
        pairedScore(match.homeCorners, match.awayCorners)?.let {
            stringResource(R.string.ds_match_corners, it)
        },
    )
    if (labels.isEmpty()) return
    Text(
        text = labels.joinToString(" · "),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(start = 24.dp + DqdSpacing.sm),
    )
}

private fun pairedScore(home: Int?, away: Int?): String? =
    if (home != null && away != null) "$home-$away" else null

/**
 * 比分格。
 *
 * 三种情况在视觉上必须可区分：
 *  - 有比分            → 数字
 *  - 未开始（无比分）  → 短横占位 `–`，中性色
 *  - 数据缺失          → [MissingValue] 的虚线破折号 `—`
 *
 * 「未开始所以没有比分」与「服务端没给比分」不是同一件事。
 */
@Composable
private fun ScoreCell(
    score: Int?,
    showScore: Boolean,
    isLive: Boolean,
    dimmed: Boolean,
) {
    val style = DqdTheme.dataText.scoreMedium
    Row(
        modifier = Modifier.width(20.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        when {
            !showScore -> Text(
                text = "–",
                style = style,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
            )

            score == null -> MissingValue(style = style)

            else -> Text(
                text = score.toString(),
                style = style,
                color = when {
                    isLive -> DqdTheme.sports.live
                    dimmed -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.onSurface
                },
                textAlign = TextAlign.End,
            )
        }
    }
}
