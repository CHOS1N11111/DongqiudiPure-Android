package io.github.chos1n11111.dongqiudipure.feature.account

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme

/**
 * 「我的」页面。
 *
 * 第一阶段这一屏**只有登录入口与本机设置**。
 *
 * 收藏、消息、关注动态属于 M13（账号只读），点赞与关注属于 M14（远端写操作），
 * 两者当前都不可达 —— 因此这里不放置任何禁用状态的占位入口。
 * 画一个点不动的按钮比不画更糟：它承诺了一个不存在的能力。
 *
 * 文案主动说明公开内容无需登录（PRODUCT.md §2.1：登录不是访问前置条件）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountRoute(
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onLicenseClick: () -> Unit,
    modifier: Modifier = Modifier,
    appVersion: String = "0.1.0",
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("我的") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            painter = painterResource(DqdIcons.Settings),
                            contentDescription = "设置",
                            modifier = Modifier.size(DqdSize.iconMedium),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            AnonymousHeader()

            Column(
                modifier = Modifier
                    .padding(top = DqdSpacing.sm)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer),
            ) {
                SettingsRow(
                    icon = DqdIcons.Settings,
                    label = "设置",
                    onClick = onSettingsClick,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingsRow(
                    icon = DqdIcons.Info,
                    label = "关于 DongqiudiPure",
                    value = appVersion,
                    onClick = onAboutClick,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingsRow(
                    icon = DqdIcons.File,
                    label = "开源许可",
                    value = "GPL-3.0",
                    onClick = onLicenseClick,
                )
            }

            Text(
                text = "本应用为非官方第三方客户端，\n与懂球帝及其官方运营方无隶属、授权或认可关系。",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DqdSpacing.lg, vertical = DqdSpacing.xl),
            )
        }
    }
}

@Composable
private fun AnonymousHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = DqdSpacing.lg, vertical = DqdSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DqdSpacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(DqdIcons.Person),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp),
            )
        }

        Text(
            text = "未登录",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            text = "资讯、比赛、榜单和资料\n无需登录即可完整浏览",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        // TODO(data): 登录属于 M9，需要会话管理与加密存储。
        //  在 :core:data 的 SessionManager 就绪前，此入口保持禁用。
        //  见 docs/engineering/BACKEND-CONTRACT-TODO.md §2.8
        Button(
            onClick = {},
            enabled = false,
            modifier = Modifier
                .padding(top = DqdSpacing.xs)
                .widthIn(max = 240.dp)
                .fillMaxWidth(),
        ) {
            Icon(
                painter = painterResource(DqdIcons.Login),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "登录以进入主队",
                modifier = Modifier.padding(start = DqdSpacing.sm),
            )
        }

        Text(
            text = "登录功能尚未实现（M9）",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingsRow(
    @DrawableRes icon: Int,
    label: String,
    onClick: () -> Unit,
    value: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .defaultMinSize(minHeight = DqdSize.touchTarget)
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(DqdSize.iconSmall),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            painter = painterResource(DqdIcons.ChevronRight),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(DqdSize.iconSmall),
        )
    }
}

@Preview(name = "我的 · 深色", showBackground = true)
@Composable
private fun AccountDarkPreview() {
    DqdTheme(darkTheme = true) {
        AccountRoute(onSettingsClick = {}, onAboutClick = {}, onLicenseClick = {})
    }
}

@Preview(name = "我的 · 浅色", showBackground = true)
@Composable
private fun AccountLightPreview() {
    DqdTheme(darkTheme = false) {
        AccountRoute(onSettingsClick = {}, onAboutClick = {}, onLicenseClick = {})
    }
}
