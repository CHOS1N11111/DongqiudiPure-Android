package io.github.chos1n11111.dongqiudipure.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme
import io.github.chos1n11111.dongqiudipure.core.model.MatchStatus
import io.github.chos1n11111.dongqiudipure.core.model.MatchSummary
import io.github.chos1n11111.dongqiudipure.core.model.MatchListEvent
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
    showDate: Boolean = false,
) {
    val isLive = match.status is MatchStatus.Live || match.status == MatchStatus.HalfTime
    val homeWon = (match.homeScore ?: 0) > (match.awayScore ?: 0)
    val awayWon = (match.awayScore ?: 0) > (match.homeScore ?: 0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .defaultMinSize(minHeight = DqdSize.touchTarget)
            .padding(horizontal = DqdSpacing.md, vertical = DqdSpacing.md),
        verticalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        MatchHeaderLine(match, showDate)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
        ) {
            TeamMatchSide(
                team = match.home,
                events = match.homeEvents,
                isHome = true,
                dimmed = match.status == MatchStatus.Finished && !homeWon,
                modifier = Modifier.weight(1f),
            )
            MatchCenter(
                match = match,
                isLive = isLive,
                modifier = Modifier.width(72.dp),
            )
            TeamMatchSide(
                team = match.away,
                events = match.awayEvents,
                isHome = false,
                dimmed = match.status == MatchStatus.Finished && !awayWon,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MatchHeaderLine(match: MatchSummary, showDate: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        listOfNotNull(match.dateLabel.takeIf { showDate }, match.kickoffLabel)
            .joinToString(" ")
            .takeIf(String::isNotEmpty)
            ?.let {
            Text(
                text = it,
                style = DqdTheme.dataText.minuteLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            }
        Text(
            text = match.matchInfoLabel
                ?: listOfNotNull(match.competition.name, match.competition.roundLabel)
                    .joinToString(" · "),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        match.liveLabel?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun TeamMatchSide(
    team: TeamRef,
    events: List<MatchListEvent>,
    isHome: Boolean,
    dimmed: Boolean,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (dimmed) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = modifier,
        horizontalAlignment = if (isHome) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
        ) {
            if (!isHome) {
                TeamCrest(
                    teamId = team.id,
                    teamName = team.name,
                    crestUrl = team.crestUrl,
                    size = 34.dp,
                )
            }
            Text(
                text = team.name,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = if (isHome) TextAlign.End else TextAlign.Start,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (isHome) {
                TeamCrest(
                    teamId = team.id,
                    teamName = team.name,
                    crestUrl = team.crestUrl,
                    size = 34.dp,
                )
            }
        }
        events.forEach { event ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (!isHome) EventMarker(event.code)
                Text(
                    text = event.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = if (isHome) TextAlign.End else TextAlign.Start,
                    maxLines = 2,
                )
                if (isHome) EventMarker(event.code)
            }
        }
    }
}

@Composable
private fun EventMarker(code: String?) {
    val normalized = code.orEmpty().uppercase()
    when (normalized) {
        "RC" -> Box(
            Modifier
                .size(width = 7.dp, height = 10.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(DqdTheme.sports.redCard),
        )
        "YC" -> Box(
            Modifier
                .size(width = 7.dp, height = 10.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(DqdTheme.sports.yellowCard),
        )
        "G", "PG", "OG" -> Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface),
        )
        else -> if (normalized in setOf("SI", "SO") || normalized.contains("SUB")) {
            Text("⇄", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun MatchCenter(
    match: MatchSummary,
    isLive: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        if (match.status.hasScore) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                ScoreCell(match.homeScore, showScore = true, isLive = isLive, dimmed = false)
                Text(
                    text = " : ",
                    style = DqdTheme.dataText.scoreMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                ScoreCell(match.awayScore, showScore = true, isLive = isLive, dimmed = false)
            }
        }
        MatchStatusBadge(status = match.status)
    }
}

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
