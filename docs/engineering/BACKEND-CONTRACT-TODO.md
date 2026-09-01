# 待接入数据层清单

> **这是一份临时文档。** 当本文所有条目都已接入且校验通过时，
> 连同 `:core:sampledata` 模块一起删除本文件。
>
> 存在的原因：UI 层已经先于数据层实现。为了不让「哪些地方还是假的」
> 散落在代码注释里，全部集中记录在这里。
> 代码中所有相关位置都标记为 `TODO(data)`，可用
> `grep -rn "TODO(data)" --include=*.kt` 一次列出。

## 0. 当前状态

| 层 | 状态 |
| --- | --- |
| `:core:model` | 已完成。Domain model、`MatchStatus`、`AppError`、`SectionState` 均已定义，**不需要改动** |
| `:core:designsystem` | 已完成。主题、组件、状态视图均已实现 |
| `:core:sampledata` | **临时**。接入后整个模块删除 |
| `:core:network` | 未创建 |
| `:core:data` | 未创建 |
| `:feature:home` / `matches` / `account` | UI 已完成，数据源为假 |

UI 层的状态编排（loading / content / empty / error、分类切换、日期切换、重试）
已经按真实形态写好，接入时**只需替换数据来源，不需要改状态逻辑** ——
这正是把页面状态建模成 `SectionState<T>` 的目的。

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
对应的文案、诊断串与「重试」按钮（不可重试的错误不显示按钮）。**不需要在页面里写错误分支。**

## 2. 逐项清单

### 2.1 资讯流 · `:feature:home`

| 项 | 需要的能力 | 当前替身 |
| --- | --- | --- |
| 分类列表 | 官方公开分类（M2 归档，不写死名单） | `SampleFeed.categories` |
| 资讯流 | `loadHomeFeed(category, cursor)`，支持分页与去重 | `SampleFeed.articles` |
| 缩略图 / 封面图 | 图片加载库（Coil），`ImagePlaceholder` 作为 loading / error 回退 | 纯色占位块 |

补充说明：

- `ArticleSummary.commentCount` 为 `Int?`。服务端未提供时**必须保持 null**，
  UI 会渲染为 `—`。**不要在 mapper 里写 `?: 0`**。
- 分页与滚动位置恢复尚未实现（`LazyColumn` 已用稳定 key，接入分页后不会跳动）。
- 下拉刷新尚未实现。

对应 `FEATURES.md`：首页资讯流、分类资讯。Milestone M3。

### 2.2 文章详情 · 未实现

`ArticleDetail`、`ArticleBlock`、`Comment`、`EntityRef` 已在 `:core:model` 定义，
`SampleFeed.articleDetail` / `SampleFeed.comments` 已备好示例，**页面本身尚未编写**。
当前路由指向 `PendingScreen`。

需要：`loadArticle(id)`、`loadComments(id, sort, cursor)`。
正文富文本需按 `ARCHITECTURE.md §7` 先清理危险 scheme 与不可控 embed。

注意：第一阶段**不要**在此页添加点赞 / 收藏 / 发表评论入口 ——
它们属于 M14 / M15 的远端写操作，设计上刻意不画。

### 2.3 比赛列表 · `:feature:matches`

| 项 | 需要的能力 | 当前替身 |
| --- | --- | --- |
| 按日期取比赛 | `observeMatchesByDate(date)` | `SampleMatches.matches` |
| 日期条的「当日有进行中比赛」标记 | 服务端当日状态摘要 | `MatchesViewModel.buildDays()` 里写死 |
| 实时刷新 | 可取消、感知前后台、终场停止 | 未实现 |
| 队徽 | 图片加载 | `TeamCrest` 由 `TeamId` 派生占位色 |

实时刷新的判定逻辑已经有了：`MatchStatus.needsLiveRefresh`
在 `Live` 与 `HalfTime` 时为 `true`，其余为 `false`。轮询调度器接上即可。

`MatchStatus.Unknown(rawValue)` 分支已在 UI 中验证可用
（示例数据里的 `AWARD` 会原样显示为「AWARD / 未知状态」）。
**mapper 遇到不认识的状态时必须走这个分支，不能 fallback 到 `Finished`。**

对应 `FEATURES.md`：比赛日历、比赛状态与比分。Milestone M4 / M5。

### 2.4 比赛详情 · 未实现

`MatchEvent`、`MatchEventKind`、`StatItem` 已定义，
`SampleMatches.events` / `SampleMatches.stats` 已备好，**页面尚未编写**。

需要按 section 分别接入，每个 section 独立 `SectionState`：
基础信息 / 事件 / 阵容 / 技术统计 / 赛前信息 / 赛后信息。

- 技术统计必须用服务端驱动的开放模型（`StatItem` 的 `id` + `name` + `displayOrder`），
  **不要写死指标集合**。新增指标应自动出现。
- `StatItem.homeValue` 为 null 表示该赛事未提供此项，
  与值为 `"0"` 是两回事，UI 已分别处理。
- 阵容模型尚未定义（首发 / 替补 / 教练 / 阵型 / 位置 / 号码 / 缺阵）。

### 2.5 榜单 · 未实现

`StandingRow` / `StandingZone` / `StandingTable` 已定义，
`SampleStandings.premierLeague` 已备好，**页面尚未编写**。

需要：`loadStandings(competitionId, seasonId, stageId?)`。

- 所有数值字段为 nullable。并列排名、扣分、「不适用」在服务端表示不同，
  不要统一压成 0（`PLAN.md` M6 退出条件）。
- `StandingZone` 到分区名称的映射需要来自服务端，不同赛事的分区规则不同，
  当前 enum 只覆盖常见几种，长尾赛制（小组赛、附加赛）需扩展。

### 2.6 球队 / 球员 / 赛事资料 · 未实现

`TeamProfile` 已定义（仅基础字段 + 近期战绩），
`SampleMatches.teamProfile` 已备好，**页面尚未编写**。
球员与赛事的 Domain model 尚未定义。

关键约束（`PLAN.md` M7）：Repository **不得按热门名单分支**，
通用页面必须能接收范围外的实体 ID，未覆盖 section 明确降级，
不能显示「不支持该球队」。这是 M10 主队入口能复用同一条链路的前提。

### 2.7 搜索 · 未实现

路由 `search` 已在导航图中，指向 `PendingScreen`。
Domain model 与页面均未编写。

### 2.8 登录与会话 · `:feature:account`

「我的」页面的登录按钮当前 `enabled = false`，并显式标注「登录功能尚未实现（M9）」。

需要（M9）：
- `SessionManager` 与 `ARCHITECTURE.md §9` 的会话状态机
- Android Keystore 支持的加密存储
- 登录、验证、冷启动恢复、失效、退出

接入后「我的」页面需要按会话状态分支；主队入口属于 M10。

**注意**：账号能力不得反向影响公开内容 ——
退出登录后所有公开页面必须仍然完整可用。

### 2.9 设置 / 关于 / 开源许可 · 未实现

三个路由都指向 `PendingScreen`。这三项**不需要网络**，
属于纯本机功能，可以随时实现：

- 设置：主题三档切换（跟随系统 / 浅色 / 深色）需要 DataStore。
  `DqdTheme` 已接受 `darkTheme` 参数，传入覆盖值即可，主题本身不读偏好存储。
- 关于：应用版本、非官方声明。
- 开源许可：复用仓库根目录 `LICENSE`，另需第三方依赖声明。

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
