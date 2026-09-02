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
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.MissingValue
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.labelRes
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SectionHeader
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme
import io.github.chos1n11111.dongqiudipure.core.model.LineupPlayer
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
    side: LineupSide,
    onSideChange: (LineupSide) -> Unit,
    onPlayerClick: (PlayerId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val team = when (side) {
        LineupSide.Home -> lineup.home
        LineupSide.Away -> lineup.away
    }

    Column(modifier = modifier.fillMaxWidth()) {
        SideToggle(
            homeName = lineup.home.team.name,
            awayName = lineup.away.team.name,
            selected = side,
            onSelect = onSideChange,
        )

        FormationBar(team)

        if (team.hasFormationGrid) {
            FormationPitch(
                starters = team.starters,
                onPlayerClick = onPlayerClick,
            )
        } else {
            // 服务端没给站位坐标。降级为列表，而不是按位置猜一个阵型画出来 ——
            // 半张编造的阵型图比一份诚实的名单更容易误导。
            NoGridNotice()
            PlayerList(
                players = team.starters,
                onPlayerClick = onPlayerClick,
            )
        }

        if (team.substitutes.isNotEmpty()) {
            SectionHeader(title = stringResource(R.string.lineup_substitutes))
            PlayerList(players = team.substitutes, onPlayerClick = onPlayerClick)
        }

        SectionHeader(title = stringResource(R.string.lineup_coach))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(
                    horizontal = DqdSpacing.listHorizontal,
                    vertical = DqdSpacing.md,
                ),
        ) {
            val coach = team.coach
            if (coach != null) {
                Text(
                    text = coach,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            } else {
                MissingValue(style = MaterialTheme.typography.bodySmall)
            }
        }

        if (team.absentees.isNotEmpty()) {
            SectionHeader(title = stringResource(R.string.lineup_absentees))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer),
            ) {
                team.absentees.forEach { absentee ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = DqdSpacing.listHorizontal,
                                vertical = DqdSpacing.md,
                            ),
                        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = absentee.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        // 缺阵原因未提供时显示「—」，不猜「伤病」。
                        val reason = absentee.reason
                        if (reason != null) {
                            Text(
                                text = reason,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            MissingValue(style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            val number = player.shirtNumber
            if (number != null) {
                Text(
                    text = number.toString(),
                    style = DqdTheme.dataText.statValue,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            } else {
                MissingValue(style = DqdTheme.dataText.statValue)
            }
        }
        Text(
            text = player.name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
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
        val number = player.shirtNumber
        Box(modifier = Modifier.width(20.dp), contentAlignment = Alignment.Center) {
            if (number != null) {
                Text(
                    text = number.toString(),
                    style = DqdTheme.dataText.tableCell,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                MissingValue(style = DqdTheme.dataText.tableCell)
            }
        }
        Text(
            text = player.name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
