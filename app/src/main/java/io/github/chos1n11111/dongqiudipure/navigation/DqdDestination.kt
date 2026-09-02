package io.github.chos1n11111.dongqiudipure.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import io.github.chos1n11111.dongqiudipure.R
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons

/**
 * 根目的地。
 *
 * 固定四项，不因登录状态增减（DECISIONS.md D-016，第三次修订）。
 *
 * 切分维度不同，所以 [Matches] 与 [Data] 不重复：
 *  - [Matches] 是**日期视角** —— 今天有哪些比赛
 *  - [Data] 是**赛事视角** —— 某个联赛的榜单与赛程
 *
 * 实体详情是内容目的地，从页面入口或 deep link 进入，不占用根 tab ——
 * 底栏项超过五个就会失去「一眼可辨」的价值。
 */
enum class DqdDestination(
    val route: String,
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val icon: Int,
) {
    Home(route = "home", labelRes = R.string.nav_news, icon = DqdIcons.News),
    Matches(route = "matches", labelRes = R.string.nav_matches, icon = DqdIcons.Calendar),
    Data(route = "data", labelRes = R.string.nav_data, icon = DqdIcons.Data),
    Account(route = "me", labelRes = R.string.nav_account, icon = DqdIcons.Person),
}

internal const val ARG_ARTICLE_ID = "articleId"
internal const val ARG_COMMENT_ID = "commentId"
internal const val ARG_MATCH_ID = "matchId"
internal const val ARG_TEAM_ID = "teamId"
internal const val ARG_COMPETITION_ID = "competitionId"
internal const val ARG_PLAYER_ID = "playerId"

/**
 * 非根目的地的路由。
 *
 * 参数只传稳定 ID（ARCHITECTURE.md §5.1）。这些路由同时也是
 * deep link 的落点，M8 接入外部链接解析时复用同一套 pattern。
 */
object DqdRoutes {
    const val ARTICLE = "article/{$ARG_ARTICLE_ID}"
    const val COMMENT = "article/{$ARG_ARTICLE_ID}/comment/{$ARG_COMMENT_ID}"
    const val MATCH = "match/{$ARG_MATCH_ID}"
    const val TEAM = "team/{$ARG_TEAM_ID}"
    const val STANDINGS = "standings/{$ARG_COMPETITION_ID}"
    const val PLAYER = "player/{$ARG_PLAYER_ID}"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val LICENSE = "license"

    fun article(articleId: String) = "article/$articleId"
    fun comment(articleId: String, commentId: String) = "article/$articleId/comment/$commentId"
    fun match(matchId: String) = "match/$matchId"
    fun team(teamId: String) = "team/$teamId"
    fun standings(competitionId: String) = "standings/$competitionId"
    fun player(playerId: String) = "player/$playerId"
}
