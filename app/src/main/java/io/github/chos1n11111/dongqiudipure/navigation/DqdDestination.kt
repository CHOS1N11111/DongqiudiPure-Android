package io.github.chos1n11111.dongqiudipure.navigation

import androidx.annotation.DrawableRes
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons

/**
 * 根目的地。
 *
 * 固定三项，不因登录状态增减（DECISIONS.md D-016）。
 * 搜索、榜单、实体资料是内容目的地，从页面入口或 deep link 进入，
 * 不占用根 tab —— 底栏项超过五个就会失去「一眼可辨」的价值。
 */
enum class DqdDestination(
    val route: String,
    val label: String,
    @param:DrawableRes val icon: Int,
) {
    Home(route = "home", label = "资讯", icon = DqdIcons.News),
    Matches(route = "matches", label = "比赛", icon = DqdIcons.Calendar),
    Account(route = "me", label = "我的", icon = DqdIcons.Person),
}

/** 非根目的地的路由常量。参数只传稳定 ID（ARCHITECTURE.md §5.1）。 */
object DqdRoutes {
    const val ARTICLE = "article/{articleId}"
    const val MATCH = "match/{matchId}"
    const val SEARCH = "search"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val LICENSE = "license"

    fun article(articleId: String) = "article/$articleId"
    fun match(matchId: String) = "match/$matchId"
}
