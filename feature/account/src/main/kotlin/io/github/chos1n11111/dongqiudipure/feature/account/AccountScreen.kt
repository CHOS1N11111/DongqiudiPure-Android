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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chos1n11111.dongqiudipure.core.designsystem.R as DesignR
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
    onAppInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
    appVersion: String = "0.1.0",
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.account_title)) },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            painter = painterResource(DqdIcons.Settings),
                            contentDescription = stringResource(DesignR.string.ds_action_settings),
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
                    label = stringResource(R.string.account_row_settings),
                    onClick = onSettingsClick,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingsRow(
                    icon = DqdIcons.Info,
                    label = stringResource(R.string.account_row_app_info),
                    value = appVersion,
                    onClick = onAppInfoClick,
                )
            }

            Text(
                text = stringResource(R.string.account_disclaimer),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppInfoScreen(
    appVersion: String,
    onAboutClick: () -> Unit,
    onLicenseClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.account_app_info_title)) },
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
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surfaceContainer),
        ) {
            SettingsRow(
                icon = DqdIcons.Info,
                label = stringResource(R.string.account_row_about),
                value = appVersion,
                onClick = onAboutClick,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingsRow(
                icon = DqdIcons.Link,
                label = stringResource(R.string.account_row_source),
                subtitle = stringResource(R.string.account_repository_url),
                onClick = { runCatching { uriHandler.openUri(PROJECT_URL) } },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingsRow(
                icon = DqdIcons.File,
                label = stringResource(R.string.account_row_license),
                value = "GPL-3.0",
                onClick = onLicenseClick,
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
            text = stringResource(R.string.account_anonymous_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            text = stringResource(R.string.account_anonymous_hint),
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
                text = stringResource(R.string.account_login_cta),
                modifier = Modifier.padding(start = DqdSpacing.sm),
            )
        }

        Text(
            text = stringResource(R.string.account_login_pending),
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
    subtitle: String? = null,
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
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
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

private const val PROJECT_URL = "https://github.com/CHOS1N11111/DongqiudiPure-Android"

@Preview(name = "我的 · 深色", showBackground = true)
@Composable
private fun AccountDarkPreview() {
    DqdTheme(darkTheme = true) {
        AccountRoute(onSettingsClick = {}, onAppInfoClick = {})
    }
}

@Preview(name = "我的 · 浅色", showBackground = true)
@Composable
private fun AccountLightPreview() {
    DqdTheme(darkTheme = false) {
        AccountRoute(onSettingsClick = {}, onAppInfoClick = {})
    }
}
