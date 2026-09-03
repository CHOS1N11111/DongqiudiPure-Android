package io.github.chos1n11111.dongqiudipure.feature.matches

import androidx.compose.foundation.Canvas
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.MissingValue
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.ImagePlaceholder
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.PlayerAvatar
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.labelRes
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SectionHeader
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.TeamCrest
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme
import io.github.chos1n11111.dongqiudipure.core.model.LineupPlayer
import io.github.chos1n11111.dongqiudipure.core.model.MatchInfo
import io.github.chos1n11111.dongqiudipure.core.model.MatchLineup
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerPosition
import io.github.chos1n11111.dongqiudipure.core.model.TeamLineup

/** 阵容分栏当前展示哪一侧。 */
enum class LineupSide(@param:StringRes val labelRes: Int) {
    Home(R.string.lineup_side_home),
    Away(R.string.lineup_side_away),
}
@Composable
fun LineupContent(
    lineup: MatchLineup,
    onPlayerClick: (PlayerId) -> Unit,
    info: MatchInfo? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (lineup.hasCombinedCoordinatePitch()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PitchGreen),
            ) {
                LineupMetaHeader(info = info, onPitch = true)
                LineupVenue(info?.venue, onPitch = true)
                CombinedFormationPitch(
                    lineup = lineup,
                    onPlayerClick = onPlayerClick,
                )
            }
        } else {
            LineupMetaHeader(info = info, onPitch = false)
            LineupVenue(info?.venue, onPitch = false)
            DualFormationBar(lineup)
            NoGridNotice()
            SectionHeader(title = stringResource(R.string.lineup_starters))
            DualPlayerColumns(
                home = lineup.home,
                away = lineup.away,
                players = { it.starters },
                includeCoach = false,
                onPlayerClick = onPlayerClick,
            )
        }

        if (
            lineup.home.substitutes.isNotEmpty() || lineup.away.substitutes.isNotEmpty() ||
            lineup.home.coach != null || lineup.away.coach != null
        ) {
            SectionHeader(title = stringResource(R.string.lineup_substitutes))
            DualPlayerColumns(
                home = lineup.home,
                away = lineup.away,
                players = { it.substitutes },
                includeCoach = true,
                onPlayerClick = onPlayerClick,
            )
        }

        if (lineup.home.absentees.isNotEmpty() || lineup.away.absentees.isNotEmpty()) {
            SectionHeader(title = stringResource(R.string.lineup_absentees))
            DualAbsenteeColumns(lineup.home, lineup.away)
        }
    }
}

@Composable
private fun LineupMetaHeader(info: MatchInfo?, onPitch: Boolean) {
    val details = listOfNotNull(info?.weather, info?.temperature, info?.altitude)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.lineup_starters),
            style = MaterialTheme.typography.titleSmall,
            color = if (onPitch) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (details.isNotEmpty()) {
            Text(
                text = details.joinToString(" · "),
                style = MaterialTheme.typography.labelSmall,
                color = if (onPitch) Color.White.copy(alpha = 0.88f) else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun LineupVenue(venue: String?, onPitch: Boolean) {
    if (venue == null) return
    Text(
        text = venue,
        style = MaterialTheme.typography.labelMedium,
        color = if (onPitch) Color.White else MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DqdSpacing.listHorizontal)
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (onPitch) Color.Black.copy(alpha = 0.10f) else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
            )
            .padding(horizontal = DqdSpacing.md, vertical = DqdSpacing.sm),
    )
}

private fun MatchLineup.hasCombinedCoordinatePitch(): Boolean {
    val starters = home.starters + away.starters
    return home.starters.isNotEmpty() && away.starters.isNotEmpty() && starters.all { player ->
        val x = player.gridColumn
        val y = player.gridRow
        x != null && y != null && x in 0..100 && y in 0..100
    } && starters.any { (it.gridColumn ?: 0) > 10 || (it.gridRow ?: 0) > 10 }
}

@Composable
private fun DualFormationBar(lineup: MatchLineup) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md),
    ) {
        TeamFormation(lineup.home, isHome = true, Modifier.weight(1f))
        TeamFormation(lineup.away, isHome = false, Modifier.weight(1f))
    }
}

@Composable
private fun TeamFormation(team: TeamLineup, isHome: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isHome) Arrangement.End else Arrangement.Start,
    ) {
        if (!isHome) {
            TeamCrest(
                teamId = team.team.id,
                teamName = team.team.name,
                crestUrl = team.team.crestUrl,
                size = 24.dp,
            )
            Box(Modifier.width(DqdSpacing.sm))
        }
        Column(horizontalAlignment = if (isHome) Alignment.End else Alignment.Start) {
            Text(
                text = team.team.name,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val formation = team.formation
            if (formation != null) {
                Text(
                    text = formation,
                    style = DqdTheme.dataText.statValue,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                MissingValue(style = MaterialTheme.typography.labelSmall)
            }
        }
        if (isHome) {
            Box(Modifier.width(DqdSpacing.sm))
            TeamCrest(
                teamId = team.team.id,
                teamName = team.team.name,
                crestUrl = team.team.crestUrl,
                size = 24.dp,
            )
        }
    }
}

@Composable
private fun CombinedFormationPitch(
    lineup: MatchLineup,
    onPlayerClick: (PlayerId) -> Unit,
) {
    val pitchLine = Color.White.copy(alpha = 0.42f)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DqdSpacing.md)
            .clip(RoundedCornerShape(8.dp))
            .background(PitchGreen)
            .aspectRatio(0.48f),
    ) {
        val width = maxWidth
        val height = maxHeight
        Canvas(modifier = Modifier.fillMaxSize()) {
            val inset = 10.dp.toPx()
            val stroke = Stroke(width = 1.5.dp.toPx())
            val fieldWidth = size.width - inset * 2
            val fieldHeight = size.height - inset * 2
            drawRect(pitchLine, Offset(inset, inset), Size(fieldWidth, fieldHeight), style = stroke)
            drawLine(
                pitchLine,
                Offset(inset, inset + fieldHeight / 2f),
                Offset(inset + fieldWidth, inset + fieldHeight / 2f),
                strokeWidth = stroke.width,
            )
            drawCircle(
                pitchLine,
                radius = fieldWidth * 0.13f,
                center = Offset(inset + fieldWidth / 2f, inset + fieldHeight / 2f),
                style = stroke,
            )
            val boxWidth = fieldWidth * 0.52f
            val boxHeight = fieldHeight * 0.14f
            drawRect(
                pitchLine,
                Offset(inset + (fieldWidth - boxWidth) / 2f, inset),
                Size(boxWidth, boxHeight),
                style = stroke,
            )
            drawRect(
                pitchLine,
                Offset(inset + (fieldWidth - boxWidth) / 2f, inset + fieldHeight - boxHeight),
                Size(boxWidth, boxHeight),
                style = stroke,
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = DqdSpacing.md),
        ) {
            PitchTeamSummary(lineup.home)
            PitchTeamSummary(lineup.away)
        }

        listOf(true to lineup.home, false to lineup.away).forEach { (isHome, team) ->
            team.starters.forEach { player ->
                val sourceX = requireNotNull(player.gridColumn).coerceIn(0, 100) / 100f
                val sourceY = requireNotNull(player.gridRow).coerceIn(0, 100) / 100f
                val xFraction = 0.06f + (if (isHome) sourceX else 1f - sourceX) * 0.88f
                val yFraction = if (isHome) {
                    0.07f + sourceY * 0.40f
                } else {
                    0.93f - sourceY * 0.40f
                }
                PlayerMarker(
                    player = player,
                    isHome = isHome,
                    onClick = { onPlayerClick(player.id) },
                    modifier = Modifier
                        .width(68.dp)
                        .offset(
                            x = width * xFraction - 34.dp,
                            y = height * yFraction - 30.dp,
                        ),
                )
            }
        }
    }
}

@Composable
private fun PitchTeamSummary(team: TeamLineup) {
    val rightLabel = listOfNotNull(
        team.marketValueLabel?.let { stringResource(R.string.lineup_team_market_value, it) },
        team.averageAgeLabel?.let { stringResource(R.string.lineup_team_average_age, it) },
    ).joinToString("  ")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TeamCrest(
            teamId = team.team.id,
            teamName = team.team.name,
            crestUrl = team.team.crestUrl,
            size = 15.dp,
        )
        team.formation?.let {
            Text(it, style = MaterialTheme.typography.labelSmall, color = Color.White)
        }
        Box(modifier = Modifier.weight(1f))
        if (rightLabel.isNotEmpty()) {
            Text(
                text = rightLabel,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.88f),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun DualPlayerColumns(
    home: TeamLineup,
    away: TeamLineup,
    players: (TeamLineup) -> List<LineupPlayer>,
    includeCoach: Boolean,
    onPlayerClick: (PlayerId) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = DqdSpacing.sm, vertical = DqdSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        CompactTeamPlayers(home, players(home), true, includeCoach, onPlayerClick, Modifier.weight(1f))
        CompactTeamPlayers(away, players(away), false, includeCoach, onPlayerClick, Modifier.weight(1f))
    }
}

@Composable
private fun CompactTeamPlayers(
    team: TeamLineup,
    players: List<LineupPlayer>,
    isHome: Boolean,
    includeCoach: Boolean,
    onPlayerClick: (PlayerId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.padding(horizontal = DqdSpacing.sm, vertical = DqdSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            TeamCrest(
                teamId = team.team.id,
                teamName = team.team.name,
                crestUrl = team.team.crestUrl,
                size = 22.dp,
            )
            Text(
                text = team.team.name,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (includeCoach && (team.coach != null || team.coachRole != null)) {
            CompactCoachRow(team)
        }
        players.forEach { player ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlayerClick(player.id) }
                    .padding(horizontal = DqdSpacing.sm, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isHome) PitchHomeMarker else PitchAwayMarker),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = player.shirtNumber?.toString() ?: "–",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = player.name,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    PlayerEventText(player)
                }
                player.ratingLabel?.let {
                    Text(it, style = DqdTheme.dataText.statValue, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun CompactCoachRow(team: TeamLineup) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DqdSpacing.sm, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ImagePlaceholder(
            url = team.coachAvatarUrl,
            cornerRadius = 16.dp,
            modifier = Modifier.size(32.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            team.coach?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
            Text(
                text = team.coachRole ?: stringResource(R.string.lineup_coach),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun DualAbsenteeColumns(home: TeamLineup, away: TeamLineup) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        listOf(home, away).forEach { team ->
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    TeamCrest(
                        teamId = team.team.id,
                        teamName = team.team.name,
                        crestUrl = team.team.crestUrl,
                        size = 22.dp,
                    )
                    Text(
                        team.team.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                team.absentees.forEach { absentee ->
                    Text(absentee.name, style = MaterialTheme.typography.bodySmall)
                    absentee.reason?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun SideToggle(
    homeName: String,
    awayName: String,
    selected: LineupSide,
    onSelect: (LineupSide) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        listOf(LineupSide.Home to homeName, LineupSide.Away to awayName).forEach { (sideValue, name) ->
            val isSelected = sideValue == selected
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                    )
                    .clickable { onSelect(sideValue) }
                    .padding(vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun FormationBar(team: TeamLineup) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.lineup_formation),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val formation = team.formation
        if (formation != null) {
            Text(
                text = formation,
                style = DqdTheme.dataText.statValue,
                color = MaterialTheme.colorScheme.onSurface,
            )
        } else {
            MissingValue(style = DqdTheme.dataText.statValue)
        }
    }
}

/**
 * 阵型图。
 *
 * 站位完全来自服务端给的行列坐标，客户端不做任何推断。
 * 竖向球场：本方球门在下，进攻方向朝上 —— 与转播图示的习惯一致。
 */
@Composable
private fun FormationPitch(
    starters: List<LineupPlayer>,
    onPlayerClick: (PlayerId) -> Unit,
) {
    val rows = starters
        .groupBy { it.gridRow ?: 0 }
        .toSortedMap()
    val rowCount = rows.size.coerceAtLeast(1)
    val pitchLine = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    val pitchFill = MaterialTheme.colorScheme.surfaceContainerHigh

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DqdSpacing.md)
            .clip(RoundedCornerShape(8.dp))
            .background(pitchFill)
            .aspectRatio(0.74f),
    ) {
        val widthPx = maxWidth
        val heightPx = maxHeight

        Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(0.74f)) {
            val inset = 10.dp.toPx()
            val stroke = Stroke(width = 1.5.dp.toPx())
            val w = size.width - inset * 2
            val h = size.height - inset * 2

            // 边线
            drawRect(
                color = pitchLine,
                topLeft = Offset(inset, inset),
                size = Size(w, h),
                style = stroke,
            )
            // 中线
            drawLine(
                color = pitchLine,
                start = Offset(inset, inset + h / 2f),
                end = Offset(inset + w, inset + h / 2f),
                strokeWidth = 1.5.dp.toPx(),
            )
            // 中圈
            drawCircle(
                color = pitchLine,
                radius = w * 0.13f,
                center = Offset(inset + w / 2f, inset + h / 2f),
                style = stroke,
            )
            // 两侧禁区
            val boxW = w * 0.52f
            val boxH = h * 0.14f
            drawRect(
                color = pitchLine,
                topLeft = Offset(inset + (w - boxW) / 2f, inset),
                size = Size(boxW, boxH),
                style = stroke,
            )
            drawRect(
                color = pitchLine,
                topLeft = Offset(inset + (w - boxW) / 2f, inset + h - boxH),
                size = Size(boxW, boxH),
                style = stroke,
            )
        }

        rows.entries.forEachIndexed { rowIndex, (_, playersInRow) ->
            val sorted = playersInRow.sortedBy { it.gridColumn ?: 0 }
            val columnCount = sorted.size.coerceAtLeast(1)

            sorted.forEachIndexed { columnIndex, player ->
                // row 0（门将）在最下方，序号越大越靠前场。
                val yFraction = 1f - (rowIndex + 0.5f) / rowCount
                val xFraction = (columnIndex + 0.5f) / columnCount

                PlayerMarker(
                    player = player,
                    isHome = true,
                    onClick = { onPlayerClick(player.id) },
                    modifier = Modifier
                        .width(64.dp)
                        .offset(
                            x = widthPx * xFraction - 32.dp,
                            y = heightPx * yFraction - 26.dp,
                        ),
                )
            }
        }
    }
}

@Composable
private fun PlayerMarker(
    player: LineupPlayer,
    isHome: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isHome) PitchHomeMarker else PitchAwayMarker),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = player.shirtNumber?.toString() ?: "–",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    maxLines = 1,
                )
            }
            PitchEventBadges(
                events = player.events,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 5.dp, y = (-3).dp),
            )
        }
        Text(
            text = player.name,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        player.ratingLabel?.let { rating ->
            Text(
                text = rating,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(PitchRatingBackground)
                    .padding(horizontal = 3.dp, vertical = 1.dp),
            )
        }
    }
}

@Composable
private fun PitchEventBadges(
    events: List<io.github.chos1n11111.dongqiudipure.core.model.LineupPlayerEvent>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
    events.filter { it.type.uppercase() in PitchEventTypes }.take(2).forEach { event ->
        Row(
            horizontalArrangement = Arrangement.spacedBy(1.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (event.type.uppercase()) {
                "G", "PG", "PSG", "OG", "AS" -> Icon(
                    painter = painterResource(DqdIcons.Ball),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp),
                )
                "YC" -> Box(Modifier.size(7.dp, 10.dp).background(PitchYellowCard))
                "RC", "Y2C", "SY" -> Box(Modifier.size(7.dp, 10.dp).background(PitchRedCard))
                "SI", "SO" -> Icon(
                    painter = painterResource(DqdIcons.Substitution),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp),
                )
                else -> Unit
            }
            event.minuteLabel?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                    color = Color.White,
                )
            }
        }
    }
    }
}

@Composable
private fun PlayerEventText(player: LineupPlayer) {
    val event = player.events.firstOrNull() ?: return
    val label = when (event.type.uppercase()) {
        "G" -> stringResource(R.string.match_event_goal)
        "PG", "PSG" -> stringResource(R.string.match_event_penalty)
        "OG" -> stringResource(R.string.match_event_own_goal)
        "YC" -> stringResource(R.string.match_event_yellow_card)
        "RC" -> stringResource(R.string.match_event_red_card)
        "Y2C", "SY" -> stringResource(R.string.match_event_second_yellow)
        "SI", "SO" -> stringResource(R.string.match_event_substitution)
        else -> event.type
    }
    Text(
        text = listOfNotNull(label, event.minuteLabel).joinToString(" ") +
            if (player.events.size > 1) " +${player.events.size - 1}" else "",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
@Composable
private fun NoGridNotice() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            painter = painterResource(DqdIcons.Info),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(12.dp),
        )
        Text(
            text = stringResource(R.string.lineup_no_grid_notice),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlayerList(
    players: List<LineupPlayer>,
    onPlayerClick: (PlayerId) -> Unit,
) {
    val grouped = players.groupBy { it.position }
    val order = listOf(
        PlayerPosition.Goalkeeper,
        PlayerPosition.Defender,
        PlayerPosition.Midfielder,
        PlayerPosition.Forward,
        PlayerPosition.Unknown,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer),
    ) {
        order.forEach { position ->
            val group = grouped[position].orEmpty()
            if (group.isEmpty()) return@forEach

            Text(
                text = stringResource(position.labelRes()),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = DqdSpacing.listHorizontal,
                    top = DqdSpacing.md,
                    bottom = DqdSpacing.xs,
                ),
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.xs),
                horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
            ) {
                group.forEach { player ->
                    PlayerChip(player = player, onClick = { onPlayerClick(player.id) })
                }
            }
        }
        Box(modifier = Modifier.padding(bottom = DqdSpacing.md))
    }
}

@Composable
private fun PlayerChip(player: LineupPlayer, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .defaultMinSize(minHeight = 36.dp)
            .padding(horizontal = DqdSpacing.sm, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        PlayerAvatar(player.id, player.name, player.avatarUrl, 26.dp)
        Column {
            Text(
                text = listOfNotNull(player.shirtNumber?.toString(), player.name).joinToString(" "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val detail = listOfNotNull(player.nationality, player.ratingLabel).joinToString(" · ")
            if (detail.isNotEmpty()) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private val PitchGreen = Color(0xFF2EAD58)
private val PitchHomeMarker = Color(0xFF7A1F45)
private val PitchAwayMarker = Color(0xFF17324D)
private val PitchRatingBackground = Color(0xFF176E39)
private val PitchYellowCard = Color(0xFFFFD43B)
private val PitchRedCard = Color(0xFFE23D3D)
private val PitchEventTypes = setOf("G", "PG", "PSG", "OG", "AS", "YC", "RC", "Y2C", "SY", "SI", "SO")
