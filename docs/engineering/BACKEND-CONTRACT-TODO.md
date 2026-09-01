# 待接入数据层清单

> **这是一份临时文档。** 当本文所有条目都已接入且校验通过时，
> 连同 `:core:sampledata` 模块一起删除本文件。
>
> 存在的原因：UI 层已经先于数据层实现。为了不让「哪些地方还是假的」
> 散落在代码注释里，全部集中记录在这里。
> 代码中所有相关位置都标记为 `TODO(data)`，可用
> `grep -rn "TODO(data)" --include=*.kt` 一次列出。

## 0. 当前状态

**前端已完成。** 全部页面、状态、样式与资产均已实现并在模拟器验证；
缺的只有真实数据源。

| 层 | 状态 |
| --- | --- |
| `:core:model` | 已完成。Domain model、`MatchStatus`、`AppError`、`SectionState` 均已定义，**不需要改动** |
| `:core:designsystem` | 已完成。深浅两套主题、语义色、组件、状态视图、18 个图标 |
| `:core:sampledata` | **临时**。接入后整个模块删除 |
| `:core:network` | 未创建 |
| `:core:data` | 未创建 |
| `:feature:home` | 资讯流 UI 已完成 |
| `:feature:article` | 文章详情 + 评论 UI 已完成 |
| `:feature:matches` | 比赛列表 + 比赛详情（事件 / 统计）UI 已完成；阵容待定义 model |
| `:feature:rankings` | 积分榜 UI 已完成；射手 / 助攻 / 赛程分栏待实现 |
| `:feature:entities` | 球队资料 UI 已完成；阵容 / 赛程 / 数据 / 资讯分栏待实现 |
| `:feature:search` | 搜索 UI 已完成 |
| `:feature:account` | 「我的」未登录态已完成；登录属于 M9 |
| `:feature:settings` | 已完成，且**不需要网络**（主题偏好走 DataStore） |

UI 层的状态编排（loading / content / empty / error、分类切换、日期切换、
分栏切换、重试）已经按真实形态写好，接入时**只需替换数据来源，
不需要改状态逻辑** —— 这正是把页面状态建模成 `SectionState<T>` 的目的。

## 1. 接入方式

每个 ViewModel 里都有一个私有的 `loadXxx()`，当前形如：

```kotlin
private fun loadFeed() {
    viewModelScope.launch {
        delay(SAMPLE_LOAD_DELAY_MS)          // 删除
        _uiState.update { it.copy(feed = SectionState.Content(SampleFeed.articles)) }
    }
}
```

接入后应形如：

```kotlin
private fun loadFeed() {
    viewModelScope.launch {
        _uiState.update { it.copy(feed = SectionState.Loading) }
        val result = articleRepository.loadHomeFeed(category)
        _uiState.update {
            it.copy(
                feed = result.fold(
                    onSuccess = { list ->
                        if (list.isEmpty()) SectionState.Empty else SectionState.Content(list)
                    },
                    onFailure = { error -> SectionState.Failed(error.toAppError()) },
                ),
            )
        }
    }
}
```

`SectionState.Failed` 一旦带上 `AppError`，`SectionContainer` 会自动渲染
对应的文案、诊断串与「重试」按钮（不可重试的错误不显示按钮）。
**不需要在页面里写错误分支。**

各 ViewModel 中的 `delay(...)` 只是为了让 Loading 骨架在开发期真实可见，
接入时全部删除。

## 2. 逐项清单

### 2.1 资讯流 · `:feature:home`

| 项 | 需要的能力 | 当前替身 |
| --- | --- | --- |
| 分类列表 | 官方公开分类（M2 归档，不写死名单） | `SampleFeed.categories` |
| 资讯流 | `loadHomeFeed(category, cursor)`，支持分页与去重 | `SampleFeed.articles` |
| 缩略图 / 封面图 | 图片加载库（Coil），`ImagePlaceholder` 作为 loading / error 回退 | 占位块 |

补充说明：

- `ArticleSummary.commentCount` 为 `Int?`。服务端未提供时**必须保持 null**，
  UI 会渲染为 `—`。**不要在 mapper 里写 `?: 0`**。
- 分页与滚动位置恢复尚未实现（`LazyColumn` 已用稳定 key，接入分页后不会跳动）。
- 下拉刷新尚未实现。

Milestone M3。

### 2.2 文章详情 · `:feature:article`

UI 已完成：标题、来源、正文块、图片位、关联实体 chip、评论列表与排序切换。
正文与评论是**两个独立 section**，评论失败不影响正文。

需要：`loadArticle(id)`、`loadComments(id, sort, cursor)`。

- 正文富文本需按 `ARCHITECTURE.md §7` 先清理危险 scheme 与不可控 embed。
- `Comment.replyCount` 为 null 时 UI 显示 `—`，不要补 0。
- 顶栏的「分享」当前是空实现，需接入系统 `ACTION_SEND`（标记为 `TODO(share)`）。
- 评论分页尚未实现。

**注意**：本页**刻意没有**点赞 / 收藏 / 发表评论入口 ——
它们属于 M14 / M15 的远端写操作。接入数据时不要顺手加上。

Milestone M3。

### 2.3 比赛列表 · `:feature:matches`

| 项 | 需要的能力 | 当前替身 |
| --- | --- | --- |
| 按日期取比赛 | `observeMatchesByDate(date)` | `SampleMatches.matches` |
| 日期条的「当日有进行中比赛」标记 | 服务端当日状态摘要 | `MatchesViewModel.buildDays()` 里写死 |
| 实时刷新 | 可取消、感知前后台、终场停止 | 未实现 |
| 队徽 | 图片加载 | `TeamCrest` 由 `TeamId` 派生占位色 |

实时刷新的判定逻辑已经有了：`MatchStatus.needsLiveRefresh`
在 `Live` 与 `HalfTime` 时为 `true`，其余为 `false`。轮询调度器接上即可。

`MatchStatus.Unknown(rawValue)` 分支已在模拟器验证可用
（示例数据里的 `AWARD` 会显示为「AWARDED / 未知状态」）。
**mapper 遇到不认识的状态时必须走这个分支，不能 fallback 到 `Finished`。**

Milestone M4。

### 2.4 比赛详情 · `:feature:matches`

UI 已完成：比分头（含 LIVE 状态）、事件时间线、技术统计对比条。
每个 section 一个独立 `SectionState`，互不等待、互不影响。

需要按 section 分别接入：

| Section | 状态 |
| --- | --- |
| 比分头 | UI 完成，需 `loadMatch(id)` |
| 事件 | UI 完成，需 `loadEvents(id)` |
| 技术统计 | UI 完成，需 `loadStats(id)` |
| 阵容 | **未实现** —— 需先定义 Domain model（首发 / 替补 / 教练 / 阵型 / 位置 / 号码 / 缺阵） |
| 赛前 / 赛后信息 | 未实现 |

- 技术统计必须用服务端驱动的开放模型（`StatItem` 的 `id` + `name` + `displayOrder`），
  **不要写死指标集合**。新增指标应自动出现。
- `StatItem.homeValue` 为 null 表示该赛事未提供此项，与值为 `"0"` 是两回事。
  UI 已分别处理：前者是虚线破折号 + 虚线空槽，后者是数字 + 实心零长条。
- 事件的 `MatchEventKind.Unknown` 会原样显示服务端返回值，不丢弃。

Milestone M5。

### 2.5 榜单 · `:feature:rankings`

积分榜 UI 已完成：分区色条 + 具名分隔行 + 图例三重编码、
名次断档提示、缺失值降级。

需要：`loadStandings(competitionId, seasonId, stageId?)`。

- 所有数值字段为 nullable。并列排名、扣分、「不适用」在服务端表示不同，
  不要统一压成 0（`PLAN.md` M6 退出条件）。
- `StandingZone` 到分区名称的映射需要来自服务端，不同赛事的分区规则不同，
  当前 enum 只覆盖常见五种，长尾赛制（小组赛、附加赛）需扩展。
- **射手榜 / 助攻榜 / 赛程分栏未实现**，当前显示所属 milestone 的说明页。

Milestone M6。

### 2.6 球队资料 · `:feature:entities`

球队主页的「资料」分栏 UI 已完成：基本资料头、近期战绩、本赛季数据、下一场。

需要：`loadTeamProfile(teamId)`、`loadTeamSeasonStats(teamId, seasonId)`、
`loadTeamNextMatch(teamId)`。

**关键约束（`PLAN.md` M7）**：Repository **不得按热门名单分支**。
本页必须能接收任意 `TeamId` —— 范围外的球队让各 section 分别降级，
而不是整页显示「不支持该球队」。这是 M10 主队入口能复用同一条链路的前提。

未实现：阵容 / 赛程 / 数据 / 资讯四个分栏；球员与赛事资料页的 Domain model 与页面。

Milestone M7。

### 2.7 搜索 · `:feature:search`

UI 已完成：焦点输入框、清空、类型筛选 chip、按实体类型分组的结果列表、
搜索历史。上一次搜索可取消，慢响应不会覆盖新查询。

需要：`search(query, filter, cursor)`。

- 输入防抖尚未实现，接入时加在 `SearchViewModel.updateQuery` 里。
- 搜索历史当前来自示例数据，真实实现需本机持久化（DataStore）。
- 只搜索第一阶段已支持的实体类型；未覆盖类型不应出现空分组。
- 球员结果的点击当前为空实现（球员资料页属于 M7）。

Milestone M8。

### 2.8 登录与会话 · `:feature:account`

「我的」页面的登录按钮当前 `enabled = false`，并显式标注「登录功能尚未实现（M9）」。

需要（M9）：
- `SessionManager` 与 `ARCHITECTURE.md §9` 的会话状态机
- Android Keystore 支持的加密存储
- 登录、验证、冷启动恢复、失效、退出

接入后「我的」页面需要按会话状态分支；主队入口属于 M10。

**注意**：账号能力不得反向影响公开内容 ——
退出登录后所有公开页面必须仍然完整可用。

### 2.9 本机功能 · `:feature:settings`

**已完成，不需要网络。**

- 设置：主题三档（跟随系统 / 浅色 / 深色），DataStore 持久化，已验证重启保持。
- 关于：版本、非官方声明、无广告无博彩声明、隐私说明。
- 开源许可：GPL-3.0-only 与主要第三方依赖。

唯一遗留项标记为 `TODO(release)`：开源许可页的第三方清单目前手写，
发布前应改为构建期生成，避免与实际依赖脱节（`PLAN.md` M17）。

## 3. 接入时必须保持的约束

这几条是 UI 设计的前提，改动会破坏已实现的行为：

1. **缺失即 null。** mapper 不得把服务端缺失的字段补成 0 或空字符串。
   UI 侧统一由 `MissingValue` / `ValueText` 渲染为 `—`。
   （`PRODUCT.md §2.4 完整但不伪造`）
2. **未知状态保留原值。** `MatchStatus.Unknown` 与 `MatchEventKind.Unknown` 是
   parser 的正常出口，不是错误。
3. **错误分类要准。** `UnsupportedContract` 与 `Network` 在 UI 上表现不同：
   前者不显示「重试」，因为重试不会成功。分类错了文案就会误导用户。
4. **section 粒度的失败。** 一个 endpoint 失效只能让它自己那个
   `SectionContainer` 进入 `Failed`，不能让整页崩掉。
5. **公开数据不带 Authorization。** 包括 M11 / M12 的完整公开数据。

## 4. 环境备忘

构建需要 **JDK 17**。**不能用 Android Studio 自带的 JBR** ——
较新版本的 JBR 是 JDK 25，AGP 8.13.2 不支持，
失败时只输出一个版本号（如 `25.0.2`）作为错误信息，极难定位。

把 `JAVA_HOME` 指向本机的 JDK 17 后再构建：

```bash
export JAVA_HOME="$(/usr/libexec/java_home -v 17)"
./gradlew :app:assembleDebug
```

`local.properties` 需指向 Android SDK，该文件不进版本库。

依赖版本上限受 `compileSdk = 36` 约束：
`lifecycle` 与 `navigation-compose` 的 2.10+ 要求编译到 API 37，
而 AGP 8.13.2 最高支持 36。升级 `compileSdk` 前不要提升这两项。

## 5. 完成后

当本文所有条目都已接入：

1. 删除 `:core:sampledata` 模块目录
2. 从 `settings.gradle.kts` 移除 `include(":core:sampledata")`
3. 从各 feature 的 `build.gradle.kts` 移除该依赖
4. 确认 `grep -rn "TODO(data)" --include=*.kt` 无残留
5. 删除本文件，并从 `docs/README.md` 移除其索引条目
