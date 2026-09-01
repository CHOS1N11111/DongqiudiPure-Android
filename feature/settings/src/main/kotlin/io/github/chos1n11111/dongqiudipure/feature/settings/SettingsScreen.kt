package io.github.chos1n11111.dongqiudipure.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SectionHeader
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme

/**
 * 设置。
 *
 * 纯本机功能，不需要网络。第一阶段只有外观设置 ——
 * 缓存管理、阅读历史、屏蔽等属于 M16，此处不放置占位入口。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(DqdIcons.ArrowBack),
                            contentDescription = "返回",
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
            SectionHeader(title = "外观")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    // 单选组：读屏会把这几项作为一组播报，而不是三个独立按钮。
                    .selectableGroup(),
            ) {
                ThemeMode.entries.forEachIndexed { index, mode ->
                    ThemeOptionRow(
                        mode = mode,
                        selected = mode == themeMode,
                        onSelect = { onThemeModeChange(mode) },
                    )
                    if (index != ThemeMode.entries.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }

            Text(
                text = "深色与浅色两套配色各自独立取值并分别验证过对比度，" +
                    "不是互相反相得到的。",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    horizontal = DqdSpacing.listHorizontal,
                    vertical = DqdSpacing.md,
                ),
            )
        }
    }
}

@Composable
private fun ThemeOptionRow(
    mode: ThemeMode,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelect,
                role = Role.RadioButton,
            )
            .defaultMinSize(minHeight = DqdSize.touchTarget)
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        RadioButton(
            selected = selected,
            // 点击由整行的 selectable 承担，避免嵌套两个可点区域。
            onClick = null,
        )
        Text(
            text = mode.label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** 关于页。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    appVersion: String,
    onBack: () -> Unit,
    onLicenseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("关于") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(DqdIcons.ArrowBack),
                            contentDescription = "返回",
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
                .verticalScroll(rememberScrollState())
                .padding(DqdSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(DqdSpacing.md),
        ) {
            Text(
                text = "DongqiudiPure",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "版本 $appVersion",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Box(modifier = Modifier.size(DqdSpacing.sm))

            AboutParagraph(
                "本应用为非官方第三方客户端，与懂球帝及其官方运营方无隶属、授权或认可关系。" +
                    "「懂球帝」及相关名称与标识归其各自权利人所有。",
            )
            AboutParagraph(
                "本应用不展示广告，不展示赔率、盘口或任何博彩与体育投注相关内容，" +
                    "也不接入第三方统计分析。",
            )
            AboutParagraph(
                "公开内容无需登录即可完整浏览。应用不为统计或推荐额外采集用户数据；" +
                    "密码不落盘，凭据不进入日志。",
            )
            AboutParagraph(
                "本应用不保证非官方接口长期可用。接口变化时，" +
                    "受影响的板块会单独降级，其余内容不受影响。",
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = DqdSize.touchTarget)
                    .selectable(selected = false, onClick = onLicenseClick),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "开源许可",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "GPL-3.0-only",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Icon(
                    painter = painterResource(DqdIcons.ChevronRight),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = DqdSpacing.sm)
                        .size(DqdSize.iconSmall),
                )
            }
        }
    }
}

/** 开源许可页。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("开源许可") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(DqdIcons.ArrowBack),
                            contentDescription = "返回",
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
                .verticalScroll(rememberScrollState())
                .padding(DqdSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(DqdSpacing.md),
        ) {
            Text(
                text = "DongqiudiPure",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            AboutParagraph(
                "本项目以 GPL-3.0-only 发布，不提供任何担保。" +
                    "完整条款见源码仓库根目录的 LICENSE 文件。",
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Text(
                text = "第三方依赖",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            ThirdParty("Android Jetpack（Compose、Navigation、Lifecycle、DataStore）", "Apache-2.0")
            ThirdParty("Kotlin 标准库与 Coroutines", "Apache-2.0")

            // TODO(release): 发布前用构建期生成的依赖清单替换这份手写列表，
            //  避免与实际依赖脱节（PLAN.md M17）。
            AboutParagraph(
                "以上为主要依赖。发布前将由构建期生成完整的第三方声明。",
            )
        }
    }
}

@Composable
private fun AboutParagraph(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ThirdParty(name: String, license: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = license,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(name = "设置 · 深色", showBackground = true)
@Composable
private fun SettingsDarkPreview() {
    DqdTheme(darkTheme = true) {
        SettingsScreen(
            themeMode = ThemeMode.System,
            onThemeModeChange = {},
            onBack = {},
        )
    }
}

@Preview(name = "关于 · 浅色", showBackground = true)
@Composable
private fun AboutLightPreview() {
    DqdTheme(darkTheme = false) {
        AboutScreen(appVersion = "0.1.0", onBack = {}, onLicenseClick = {})
    }
}
