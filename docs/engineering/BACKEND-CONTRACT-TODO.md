# 待接入数据层清单

> **这是一份临时文档。** 当本文所有条目都已接入且校验通过时，
> 连同 `:core:sampledata` 模块一起删除本文件。
>
> 存在的原因：UI 层已经先于数据层实现。为了不让「哪些地方还是假的」
> 散落在代码注释里，全部集中记录在这里。
> 代码中所有相关位置都标记为 `TODO(data)`，可用
> `grep -rn "TODO(data)" --include=*.kt` 一次列出。

## 0. 当前状态

**前端已完成，数据层按 vertical slice 接入中。** 资讯流、文章详情、评论回复、
五大联赛和中超的比赛列表与积分榜已经使用真实匿名数据。未验证的比赛详情、
球队/球员资料明确显示为空；搜索入口暂时移除，运行时不展示样例数据。

| 层 | 状态 |
| --- | --- |
| `:core:model` | 已完成。Domain model、`MatchStatus`、`AppError`、`SectionState` 均已定义，**不需要改动** |
| `:core:designsystem` | 已完成。深浅两套主题、语义色、组件、状态视图、18 个图标 |
| `:core:sampledata` | **临时**。仅供当前不可达的搜索模块使用；应用运行路径不读取样例数据 |
| `:core:network` | 匿名资讯、评论回复、比赛、赛季与积分榜 Request、DTO、错误映射及 contract test 已完成 |
| `:core:data` | 资讯与评论 Paging、比赛和积分榜 Repository、mapper 已完成 |
| `:feature:home` | 已接入真实分类、资讯流、图片、下拉刷新和分页 |
| `:feature:article` | 已接入真实正文、图片、关联实体、系统分享、一级评论、点赞数和回复详情分页 |
| `:feature:matches` | 五大联赛和中超比赛列表已接入；详情头使用真实列表数据，未验证的事件 / 阵容 / 统计为空 |
| `:feature:rankings` | 「数据」根 tab 仅展示五大联赛和中超的真实当前赛季积分榜 |
| `:feature:entities` | 球队/球员资料 contract 未接入，当前 section 为空而非样例数据 |
| `:feature:search` | 模块保留供后续开发，但应用依赖和导航入口已移除 |
| `:feature:account` | 已接入登录界面、会话验证、加密存储、冷启动恢复与退出；真实成功 contract 仍待专用账号确认 |
| `:feature:settings` | 已完成，且**不需要网络**（主题偏好走 DataStore，见 D-019） |
| 界面文案 | 全部走 string 资源，各模块自带 `strings.xml`；服务端驱动的赛事、球队与分区名称直接使用 contract 字段 |

UI 层继续保持 loading / content / empty / error、分类切换、日期切换、分栏切换
和重试的独立状态。资讯与评论已改为 Paging 3 的 refresh/append 状态；其余
非分页 section 仍使用 `SectionState<T>`。

## 已关闭的数据层前置决策

- DI：使用 Hilt，八个现有 ViewModel 已统一由 `hiltViewModel()` 创建，见 D-007。
- 分页：资讯流和评论（含回复）使用 Paging 3；搜索后续接入 cursor contract 时沿用，见 D-020。
- 主题：深浅色与 DataStore 策略已归档，见 D-019。

这些决策不再阻塞 `:core:data`。后续 slice 直接沿用，不再建立手写 ViewModel Factory
或页面内 cursor 状态机。

### 一个不算债但要知道的事实

`:core:model` 里的 Domain model 是**按 UI 需要和足球数据的一般形态定的，
不是按真实 API 定的**。真接上会遇到三类情况：

- 字段不存在 → UI 自动显示 `—`，不崩（这是设计好的）
- 字段存在但没建模 → 往 model 加
- 结构根本不同 → 例如事件按半场嵌套、统计返回 map、阵容主客队分两个 endpoint

这些摩擦集中在 mapper 层。资讯 mapper 已验证该边界；后续比赛和资料 contract
仍需逐项确认。model 保持在无 Android 依赖的模块中。

## 1. 接入方式

除已接入的资讯 ViewModel 外，其余 ViewModel 仍有私有 `loadXxx()`，当前形如：

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

### 2.1 资讯流 · `:feature:home`（已接入）

| 项 | 当前实现 |
| --- | --- |
| 分类列表 | 使用 2026-09-01 已验证的七个公开 tab ID |
| 资讯流 | `NewsRepository.pagedFeed(category)` + Paging 3，支持刷新、分页与页内去重 |
| 缩略图 / 封面图 | Coil；只允许已观察到的 HTTPS 媒体域名，失败时保留稳定占位 |

`ArticleSummary.commentCount` 在服务端缺失时保持 null。Contract、fixture 与测试见
`API.md §6.1` 和 `core/testing/src/main/resources/contracts/news/2026-09-01/`。

### 2.2 文章详情 · `:feature:article`（主体已接入）

- `ArticleRepository.loadArticle(id)` 独立加载正文；评论 Paging 失败不影响正文。
- HTML 正文只解析文本、标题/引用和受控 HTTPS 图片，不执行 embed 或脚本。
- 关联球队、球员和赛事使用稳定 ID 导航；顶栏已接入系统 `ACTION_SEND`。
- 一级评论支持最热/最新切换、分页、去重、公开作者名、`replyCount` 和只读点赞数。
- 点击一级评论进入详情，使用真实 `comment_info` 和 `reply_list` 分页展示楼中楼；空回复保持空状态。

**注意**：本页**刻意没有**点赞 / 收藏 / 发表评论入口 ——
它们属于 M14 / M15 的远端写操作。接入数据时不要顺手加上。

Milestone M3。

### 2.3 比赛列表 · `:feature:matches`（已接入限定范围）

| 项 | 当前实现 |
| --- | --- |
| 按日期取比赛 | `MatchRepository.loadMatches(date)` 读取真实重要比赛窗口并按本地日期筛选 |
| 赛事范围 | 仅英超、西甲、意甲、德甲、法甲和中超；其他赛事在 mapper 前过滤 |
| 日期条的进行中标记 | 根据当日已加载比赛的真实状态计算 |
| 实时刷新 | 尚未实现自动轮询，用户可手动重试 |
| 队徽 | Coil 加载受控 HTTPS `logo`，短暂失败有限重试，最终保留可读占位 |

实时刷新的判定逻辑已经有了：`MatchStatus.needsLiveRefresh`
在 `Live` 与 `HalfTime` 时为 `true`，其余为 `false`。轮询调度器接上即可。

`MatchStatus.Unknown(rawValue)` 保留接口未知原值，
**mapper 遇到不认识的状态时必须走这个分支，不能 fallback 到 `Finished`。**

Milestone M4。

### 2.4 比赛详情 · `:feature:matches`

UI 已完成：比分头（含 LIVE 状态）、事件时间线、阵型图 / 阵容名单、技术统计对比条。
每个 section 一个独立 `SectionState`，互不等待、互不影响。

比分头已经复用比赛列表中的真实数据。以下 section 尚未完成 contract，当前显示空状态：

| Section | 状态 |
| --- | --- |
| 比分头 | 已接入列表缓存与必要的窗口重取 |
| 事件 | UI 完成，需 `loadEvents(id)` |
| 阵容 | UI 完成，需 `loadLineup(id)` |
| 技术统计 | UI 完成，需 `loadStats(id)` |
| 赛前 / 赛后信息 | 未实现（需先确认服务端提供哪些字段） |

**阵容的关键约定**：`LineupPlayer.gridRow` / `gridColumn` 是阵型图坐标，
服务端未提供时**必须为 null**。此时 UI 自动降级为按位置分组的名单，
并显示「该场比赛未提供球员站位」。**mapper 不得按位置推断站位** ——
`TeamLineup.hasFormationGrid` 要求所有首发都有坐标才绘制阵型图，
半张编造的阵型图比一份诚实的名单更容易误导。

`formation`、`coach`、`Absentee.reason`、`shirtNumber` 缺失时 UI 均显示「—」。

- 技术统计必须用服务端驱动的开放模型（`StatItem` 的 `id` + `name` + `displayOrder`），
  **不要写死指标集合**。新增指标应自动出现。
- `StatItem.homeValue` 为 null 表示该赛事未提供此项，与值为 `"0"` 是两回事。
  UI 已分别处理：前者是虚线破折号 + 虚线空槽，后者是数字 + 实心零长条。
- 事件的 `MatchEventKind.Unknown` 会原样显示服务端返回值，不丢弃。

Milestone M5。

### 2.5 榜单 · `:feature:rankings`

积分榜 UI 已完成：分区色条 + 具名分隔行 + 图例三重编码、
名次断档提示、缺失值降级。

五大联赛和中超的当前赛季积分榜已接入：先请求赛季列表动态解析当前赛季 ID，
再请求积分表；没有数据时显示空状态，不填充样例行。

- 所有数值字段为 nullable。并列排名、扣分、「不适用」在服务端表示不同，
  不要统一压成 0（`PLAN.md` M6 退出条件）。
- `StandingZone` 到分区名称的映射需要来自服务端，不同赛事的分区规则不同，
  当前 enum 只覆盖常见五种，长尾赛制（小组赛、附加赛）需扩展。

射手榜、助攻榜和赛程尚未验证，当前不在「数据」页展示。
`PlayerRankingRow.team` 为 null（转会期未确定）时显示「—」，不要猜一个球队。

赛程读取 contract 尚未验证，当前不在「数据」页展示；后续需要
`loadFixtures(competitionId, seasonId, round?)`，并支持按轮次分组。

Milestone M6。

### 2.6 球队资料 · `:feature:entities`

球队主页的「资料」分栏 UI 已完成：基本资料头、近期战绩、本赛季数据、下一场。

需要：`loadTeamProfile(teamId)`、`loadTeamSeasonStats(teamId, seasonId)`、
`loadTeamNextMatch(teamId)`。

**关键约束（`PLAN.md` M7）**：Repository **不得按热门名单分支**。
本页必须能接收任意 `TeamId` —— 范围外的球队让各 section 分别降级，
而不是整页显示「不支持该球队」。这是 M10 主队入口能复用同一条链路的前提。

五个分栏 UI 均已完成，各自独立 `SectionState`：
`loadTeamProfile` / `loadSquad` / `loadTeamFixtures` / `loadTeamStats` / `loadTeamNews`。

**球员资料页 UI 也已完成**（`PlayerProfileScreen`）：资料、本赛季数据、履历。
需要 `loadPlayerProfile(id)`、`loadPlayerSeasonStats(id, seasonId)`、`loadPlayerCareer(id)`。
履历的历史赛季常缺数据，缺失行显示「—」而不是 0 —— 补 0 会被读成「那个赛季一场没打」。

当前球队和球员资料页均显示真实空状态；接入 Repository 后必须按传入 ID 查询，
不得按热门名单或样例对象分支。

赛事资料本身由榜单页（`StandingsScreen`）承担，四个分栏即赛事主页的内容。
若后续需要独立的「赛事资料」分栏（参赛队、阶段、历史冠军），再另加。

Milestone M7。

### 2.7 搜索 · `:feature:search`

按当前产品要求，应用已移除搜索依赖、导航与可见入口。模块源码暂时保留，
后续确认 contract 后再重新接入。

需要：`search(query, filter, cursor)`。

- 输入防抖尚未实现，接入时加在 `SearchViewModel.updateQuery` 里。
- 重新接入时不得展示样例历史；真实历史需本机持久化（DataStore）。
- 只搜索第一阶段已支持的实体类型；未覆盖类型不应出现空分组。
- 球队 / 球员 / 赛事 / 资讯四类结果均已接通对应详情页。

Milestone M8。

### 2.8 登录与会话 · `:feature:account`

「我的」页面已开放用户名/手机号加密码入口，并接入 `ARCHITECTURE.md §9` 的统一
会话状态机。Authorization 使用 Android Keystore 的 AES-GCM 密钥加密后落盘；密码
不保存，稳定随机 UUID 只由 authenticated client 使用。登录候选会话只有通过
`/v2/user/is_login` 验证后才进入已登录状态，退出只执行确定的本地清除操作。

M9 尚余：使用用户明确授权的专用懂球帝账号确认成功 Response、密码表示、未知
challenge 分支，以及完成登录 -> 重启恢复 -> 退出的端到端验证。在此之前，兼容
parser 无法识别的成功 Response 会明确显示协议不兼容，不会误报登录成功。

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
