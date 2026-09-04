# 决策记录

> 本文记录会影响多个模块或后续成本的决策。任务状态不在这里维护，见 [PLAN.md](PLAN.md)。

## 1. 决策状态

| ID | 决策 | 状态 | 当前结论或建议 | 必须完成时间 |
| --- | --- | --- | --- | --- |
| D-001 | 首个平台 | 已确定 | Android 优先 | 已完成 |
| D-002 | 产品顺序 | 已确定 | 匿名核心 -> 登录与主队 -> 完整匿名公开数据 -> 账号能力 | 已完成 |
| D-003 | 登录主路径 | 待验证 | 用户名/手机号加密码，成功 contract 待专用账号确认 | M9 前 |
| D-004 | 写操作隔离 | 已确定 | 独立 client、独立 feature flag、Release 默认关闭 | M14 前 |
| D-005 | UI 技术 | 已确定 | Kotlin + Jetpack Compose + Material 3 | 已完成 |
| D-006 | Network 技术 | 已确定 | OkHttp + kotlinx.serialization，初期不引入 Retrofit | 已完成 |
| D-007 | DI 方式 | 已确定 | 使用 Hilt 统一 Application、Repository 与 ViewModel 对象图 | 已完成 |
| D-008 | `applicationId` | 已确定 | `io.github.chos1n11111.dongqiudipure` | 已完成 |
| D-009 | `minSdk` | 已确定 | API 26（Android 8.0） | 已完成 |
| D-010 | License | 已确定 | `GPL-3.0-only` | 已完成 |
| D-011 | 应用名称与图标 | 已确定 | 显示名 `DongqiudiPure`；图标使用原创中性方案 | 已完成 |
| D-012 | Analytics | 已确定 | 默认不集成第三方 analytics | 已完成 |
| D-013 | 模块粒度 | 已确定 | 按实际 vertical slice 逐步增加 module | 已完成 |
| D-014 | session import | 暂缓 | 先作为内部诊断能力，是否公开由登录稳定性决定 | M9 后 |
| D-015 | 发布渠道 | 待确认 | GitHub Releases 或其他渠道由用户决定 | M17 前 |
| D-016 | 根 Navigation | 已确定 | `资讯 / 比赛 / 主队 / 数据 / 我的`（第四次修订） | 已完成 |
| D-017 | 公开范围分批 | 已确定 | M6/M7 先做主要/热门范围，M11/M12 再完成全部公开覆盖 | 已完成 |
| D-018 | 账号能力分级 | 已确定 | 账号只读与远端状态变更分开实施和验收 | 已完成 |
| D-019 | 主题策略 | 已确定 | 深浅两套独立取值；设置提供跟随系统 / 浅色 / 深色三档，DataStore 持久化 | 已完成 |
| D-020 | 分页方式 | 已确定 | 资讯流与文章评论使用 Paging 3，搜索接入真实数据时沿用 | 已完成 |

## 2. 产品决策

### D-001：Android 优先

- 当前仓库明确命名为 `DongqiudiPure-Android`。
- 首期只建立 Android 架构、交互和验证基线，不为尚未开始的平台预建共享 UI 层。
- API contract 保持平台无关，未来其他平台可以复用研究结果。

### D-002：分阶段交付

- 登录前先完成匿名资讯、完整比赛数据以及主要/热门榜单和资料库，形成可用的匿名核心版。
- 登录和主队入口完成后，再继续扩展长尾赛事、历史赛季及完整公开资料库。
- 完整公开数据虽然排在登录之后实施，仍保持匿名访问，不进入 authenticated client。
- 完整公开数据收口后，再处理账号只读页和远端状态变更。
- 匿名能力是账号接口变化时的可靠降级路径，后续阶段不得反向依赖登录。

### D-017：公开范围分批

- M6/M7 的“主要/热门”以官方匿名默认入口和热门分类为基线，具体名单、来源和日期在 M2 归档。
- M11/M12 负责长尾赛事、历史赛季、非热门实体和完整公开入口审计。
- [FEATURES.md](../product/FEATURES.md) 是逐项覆盖状态的唯一记录；核心 Gate 和完整公开数据 Gate 分别验收。
- 排期在登录之后不等于需要登录；M11/M12 不得携带 Authorization。
- 广告、博彩/体育投注等明确排除项不计入完整性，也不得作为公开内容接入。

### D-018：账号能力分级

- 消息、关注动态、收藏列表等只读页面涉及凭据和私有数据，但本身不改变远端状态。
- 点赞、关注、收藏、发布、删除和资料修改会改变服务端状态，需要独立 contract、开关和结果语义。
- 账号只读能力完成不代表任何写能力获得授权，写能力之间也不互相继承验证结论。

### D-004：写操作独立隔离

- 非官方写接口会增加风控、误操作和账号限制风险。
- 非幂等 Request 不沿用 read client 的自动重试策略。
- 独立 feature flag 允许逐项关闭 UI 入口；如需远程开关，必须另行审查依赖和隐私影响。

## 3. 技术决策

### D-005：Kotlin + Compose

选择理由：

- 新项目没有 XML/View 兼容负担。
- Compose 适合状态驱动的 loading/content/empty/error 页面，并便于手机与平板适配。
- 可沿用 TiebaPure Android 的 Compose、Navigation 和工程经验，但不复制其业务协议。

复审条件：需要大量依赖传统 View 的组件，或目标最低系统与工具链出现明确冲突。

### D-006：OkHttp + kotlinx.serialization

选择理由：

- 非官方 API 需要精确控制 Header、form body、原始 Response 和兼容处理。
- 直接 transport 边界便于应对动态 endpoint 和非标准错误。
- kotlinx.serialization 可以显式定义 DTO，并容忍未知非关键字段。

复审条件：endpoint 数量显著增加且 contract 稳定，Retrofit 能减少真实重复而不隐藏必要控制。

### D-007：使用 Hilt 管理对象图

- 状态：已确定（替代 scaffold 阶段的手动 composition root）
- 日期：2026-09-01
- 背景：真实 Repository 落地后，多个 feature ViewModel 同时需要注入依赖，手写 Factory 会形成重复且分散的 composition root。
- 结论：由 `@HiltAndroidApp` Application 建立根对象图；Repository、Remote source 和共享 store 通过 module 绑定；导航入口统一使用 `hiltViewModel()`。
- 影响：所有现有 ViewModel 均由 Hilt 创建。测试继续在 Repository/Remote source 边界直接注入 fake，不要求启动完整 Android 对象图。
- 复审条件：Hilt 与未来工具链发生不可解决的兼容问题，或对象图需要迁移到跨平台共享层。

### D-013：按 vertical slice 增加 module

初始只有 `app` 和实际需要的 `core` module。资讯、比赛、榜单、实体资料、搜索、账号和 composer 在对应 milestone 开始时再创建 feature module，避免空 module 和无效 Gradle 开销。

## 4. 实施参数

### D-008：applicationId

使用 `io.github.chos1n11111.dongqiudipure`，初期 namespace 与 applicationId 保持一致。

- 全小写且发布后保持稳定。
- 不使用 `com.dongqiudi.*`，避免暗示官方归属。
- 如发布后必须迁移，按新应用身份处理，不静默改包名。

### D-009：minSdk

使用 API 26（Android 8.0）作为最低版本。`compileSdk` 和 `targetSdk` 在 scaffold 时选择受支持的稳定版本，不从 `minSdk` 推导。

### D-010：License

项目使用 `GPL-3.0-only`，完整条款位于仓库根目录 `LICENSE`。分发源码或二进制时遵守 GPL 对源码、License 和修改说明的要求；复用第三方代码时保留其 copyright 与 notice。

公开 API 事实和架构思想可以作为研究参考；复制具体实现前单独确认其 License 与 attribution 要求。

### D-011：名称与图标

应用显示名使用 `DongqiudiPure`。图标必须原创并保持中性，不复制懂球帝官方图标，也不以视觉方式暗示官方授权；正式图标完成前使用中性 placeholder。

### D-016：根 Navigation

- 状态：已确定（第四次修订）
- 修订历史：`资讯 / 比赛 / 我的` → `资讯 / 比赛 / 数据` → `资讯 / 比赛 / 数据 / 我的` → `资讯 / 比赛 / 主队 / 数据 / 我的`

compact width 固定使用 `资讯 / 比赛 / 主队 / 数据 / 我的` 五个 destination；medium/expanded width 使用语义相同的 NavigationRail。搜索与实体详情通过页面入口或 deep link 进入，不增加其他固定根 tab。

各 destination 的职责：

- `资讯` 承载公开内容入口，并通过搜索与关联链接进入资料库。
- `比赛` 是**日期视角** —— 按日期浏览赛程赛果与完整比赛详情。
- `主队` 承载本机选择的主队、关注球队和球员；设置主队后底栏使用真实队徽，未设置时使用添加图标。
- `数据` 是**赛事视角** —— 赛事切换器加当前赛事的积分榜、射手榜、助攻榜与赛程。
- `我的` 承载登录入口与本机设置，登录后承载账号能力。

为什么保留 `我的` 作为根 tab（而不是收进独立账号入口）：

1. **账号能力是一整个阶段。** M13 要做消息、收藏、关注动态、关注/粉丝列表。这些需要一个稳定的家；收进顶栏头像后面会明显降低可发现性，且要在每个页面回答「这个入口放哪」。
2. **五项仍在规范内。** Material 底栏上限五项；主队位于中间并以队徽形成稳定入口，NavigationRail 使用同一语义。
3. **`比赛` 与 `数据` 不重复。** 切分维度不同（日期视角 vs 赛事视角），不是同一批内容的两个入口。

已知代价：第一阶段的 `我的` 内容偏少（登录入口 + 设置 + 关于 + 开源许可）。这是阶段性状态而非结构问题 —— 设置与关于总需要落点，M13 之后该 tab 自然充实。为一时的空旷改结构、M13 再改回来是两次返工。

复审条件：需要第六个根入口，或主队与账号同步后的职责发生重叠时重新评估信息架构。

### D-019：主题策略

- 状态：已确定
- 背景：`DECISIONS.md` D-001–D-018 与 `PRODUCT.md` §8 均未记录深浅色策略，但实现必须做出选择。

深色与浅色是**两套独立取值的配色**，不是互相反相得到的。主色在深色下为 `#3DD6C0`、浅色下为 `#00786B`；足球语义色（live / 胜平负 / 黄牌 / 升降级区）同样各有两份取值，经 `LocalSportsColors` 下发。两套分别验证对比度。

设置提供三档：跟随系统（默认）/ 浅色 / 深色，用 DataStore 持久化。`DqdTheme` 只接受 `darkTheme` 参数，不读取偏好存储 —— 偏好由 `MainActivity` 读出后注入。

为什么主色避开红、绿、琥珀：足球界面里这三个色相已被语义占用（红牌与降级、胜场与进球、黄牌与警告）。品牌色若落在其中，会与 LIVE 状态和胜负标记争夺同一套视觉编码。这同时满足 D-011 对「不以视觉方式暗示官方授权」的要求。

复审条件：引入动态取色（Material You）或品牌色变更。

### D-020：资讯列表使用 Paging 3

- 状态：已确定
- 日期：2026-09-01
- 背景：资讯流和文章评论都有 cursor 分页、追加错误、去重与重试需求；自行维护这些状态会与 UI 生命周期产生重复逻辑。
- 结论：Repository 返回 `Flow<PagingData<T>>`，Remote source 只负责单次 Request，`PagingSource` 校验并解释服务端 `next` URL。首页和评论 UI 使用 `LazyPagingItems` 分别处理 refresh 与 append 状态。
- 影响：分页页面不再用单个 `SectionState<List<T>>` 表示整条列表；文章正文仍保留独立 `SectionState`，评论失败不会清空正文。搜索在接入真实分页 contract 时沿用同一方案。
- 复审条件：服务端改为稳定的本地同步模型且需要 Room 离线缓存时，评估 `RemoteMediator`，不回退到页面内手写 cursor。

## 5. 暂缓或待确认事项

- D-014 session import：M9 先作为内部诊断能力，是否放入正式 UI 取决于密码登录稳定性。
- D-015 发布渠道：M17 前由用户决定；完成 Release checklist 不等于自动发布。

## 6. 决策模板

新增重要决策时使用以下格式：

```markdown
### D-XXX：标题

- 状态：待确认 / 已确定 / 已废弃 / 已替代
- 日期：YYYY-MM-DD
- 背景：为什么现在需要决定
- 选项：实际可选方案及约束
- 结论：选择什么
- 理由：为什么选择
- 影响：需要修改哪些 module、文档或验证
- 复审条件：什么变化会触发重新评估
```

小范围、易撤销且只影响单个实现文件的选择不需要 ADR；跨 module、影响账号边界或一旦发布难以改变的选择必须记录。
