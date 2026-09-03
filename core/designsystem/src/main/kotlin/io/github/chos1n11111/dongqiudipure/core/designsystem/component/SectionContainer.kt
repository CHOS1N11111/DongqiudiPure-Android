package io.github.chos1n11111.dongqiudipure.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.chos1n11111.dongqiudipure.core.designsystem.R
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.model.SectionState

/**
 * 承载单个 section 四种状态的容器。
 *
 * **失败隔离靠这个组件强制执行，而不是靠每个页面自觉。**
 * 页面把内容拆成若干 [SectionContainer]，每个持有自己的 [SectionState]，
 * 于是「单个 endpoint、页面 section 失效时不破坏无关页面」
 * （PRODUCT.md §2.6）成为默认行为而非需要额外注意的事项。
 *
 * @param loading 默认给一个通用骨架。各 section 应传入与自身真实版式尺寸一致的骨架，
 *   以保证内容到达时零位移。
 */
@Composable
fun <T> SectionContainer(
    state: SectionState<T>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    emptyTitle: String = stringResource(R.string.ds_section_empty_title),
    emptyDescription: String = stringResource(R.string.ds_section_empty_description),
    @DrawableRes emptyIcon: Int = DqdIcons.Inbox,
    forceRetry: Boolean = false,
    loading: @Composable () -> Unit = { DefaultSectionSkeleton() },
    content: @Composable (T) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer),
    ) {
        if (title != null) {
            SectionHeader(title = title, trailing = trailing)
        }

        when (state) {
            SectionState.Loading -> loading()

            is SectionState.Content -> content(state.value)

            SectionState.Empty -> DqdEmptyState(
                title = emptyTitle,
                description = emptyDescription,
                icon = emptyIcon,
            )

            is SectionState.Failed -> DqdErrorState(
                error = state.error,
                onRetry = onRetry,
                forceRetry = forceRetry,
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = DqdSpacing.listHorizontal,
                end = DqdSpacing.listHorizontal,
                top = DqdSpacing.md,
                bottom = DqdSpacing.sm,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        trailing?.invoke()
    }
}

/** section 标题右侧的次要动作，如「最热」「查看全部」。 */
@Composable
fun SectionAction(
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Icon(
            painter = painterResource(DqdIcons.ChevronRight),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp),
        )
    }
}

@Composable
private fun DefaultSectionSkeleton() {
    Column(
        modifier = Modifier.padding(
            horizontal = DqdSpacing.listHorizontal,
            vertical = DqdSpacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(14.dp))
        SkeletonBox(modifier = Modifier.fillMaxWidth(0.7f).height(14.dp))
        SkeletonBox(modifier = Modifier.fillMaxWidth(0.45f).height(14.dp))
    }
}
