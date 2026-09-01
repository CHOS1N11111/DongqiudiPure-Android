# Android 架构方案

> 状态：已确认，可直接用于 scaffold。具体决策与复审条件见 [DECISIONS.md](../planning/DECISIONS.md)，API 事实以 [API.md](../protocol/API.md) 为准。

## 1. 架构目标

架构需要优先解决以下问题：

- 非官方 API 可能改变 path、Header、字段类型和 Response 结构。
- 匿名与登录态必须同时存在，登录失败不能破坏匿名功能。
- read 与 write 的重试、安全和测试策略不同。
- 资讯、完整比赛数据、榜单和球员/球队/赛事资料包含多种异构 section，需要稳定 Domain model 和独立降级边界。
- 项目从零开始，应保留清晰边界，但不提前创建没有业务的抽象和 module。

## 2. 推荐技术栈

具体版本在 scaffold 时通过 Version Catalog 固定，本文只定义技术选择。

| 领域 | 选择 | 使用理由 |
| --- | --- | --- |
| 语言与构建 | Kotlin、Gradle Kotlin DSL、JDK 17 | Android 主流工具链，配置可类型检查 |
| UI | Jetpack Compose、Material 3 | 状态驱动、自适应和 UI test 成本较低 |
| Navigation | Navigation Compose | 单 Activity、多 feature destination |
| 并发 | Coroutines、Flow | Request 取消、状态流和生命周期集成 |
| HTTP | OkHttp | 精确控制 Header、form body、redirect 和原始 Response |
| JSON | kotlinx.serialization | DTO 明确、支持忽略未知字段和自定义兼容 parser |
| 图片 | Coil | Compose 集成、缓存和 Request 控制 |
| 设置 | DataStore | 小型、类型明确的本机设置 |
| 结构化本地数据 | Room，按需引入 | 只在历史、离线或关系查询出现后使用 |
| Secret | Android Keystore 支持的加密存储 | 隔离 Authorization，避免明文落盘 |
| 测试 | JUnit、coroutines-test、MockWebServer、Compose UI test | 覆盖 contract、状态与 UI |
| DI | 初期手动 composition root | 对象图较小时更透明，避免过早引入框架 |

不在首期引入 Retrofit、Hilt、WorkManager 或复杂 MVI 框架。它们不是永久禁止；只有在出现可量化的重复、生命周期问题或后台任务需求时再通过决策记录引入。

## 3. 系统边界

```text
Android UI
  -> Feature ViewModel
    -> Repository interface
      -> Local source / Remote source
        -> Dongqiudi public or authenticated API

Android Keystore-backed session store
  -> Session manager
    -> Authenticated HTTP client

Experimental write UI
  -> Write Repository
    -> Dedicated write client
```

应用不提供中转 Backend。账号密码和 Authorization 不应离开用户设备直接访问懂球帝所需的最小链路。

## 4. Module 规划

module 按实际 milestone 创建，不在 scaffold 时一次性生成全部空目录。

| Module | 职责 | 允许依赖 |
| --- | --- | --- |
| `:app` | Application、Activity、根 Navigation、composition root、build config | 所有实际启用的 core/feature |
| `:core:model` | 稳定 Domain model、ID、枚举和跨 feature contract | Kotlin 标准库 |
| `:core:network` | OkHttp client、endpoint registry、DTO、parser、Remote source | `:core:model` |
| `:core:data` | Repository 实现、缓存协调、DataStore/Room、session manager | `:core:model`、`:core:network` |
| `:core:designsystem` | Theme、通用控件、图标和 layout token | Compose/Material 3 |
| `:core:testing` | fixture loader、fake、test dispatcher、builder | 需要测试的 core contract |
| `:feature:home` | 资讯流与分类 | model、data、designsystem |
| `:feature:article` | 文章、媒体和评论只读 | model、data、designsystem |
| `:feature:matches` | M4/M5 的比赛列表、基础详情、事件、阵容和统计 | model、data、designsystem |
| `:feature:rankings` | M6 的主要/热门榜单及 M11 的完整公开扩展 | model、data、designsystem |
| `:feature:entities` | M7 的主要/热门资料及 M12 的完整公开资料库 | model、data、designsystem |
| `:feature:search` | M8 的核心搜索及 M12 的完整覆盖与 deep link | model、data、designsystem |
| `:feature:account` | M9/M10 的登录与主队、M13 的账号只读页面 | model、data、designsystem |
| `:feature:settings` | 本机设置、关于和实验功能总开关 | data、designsystem |
| `:feature:composer` | M15 的 draft、评论/回复、发帖和媒体提交 | model、data、designsystem |

依赖规则：

- feature module 之间不得直接依赖，通过 `:app` Navigation contract 或 `:core:model` 中的稳定 ID 协作。
- `:core:network` 不引用 Compose、Android 页面或 feature 类型。
- `:core:data` 不向上层返回 DTO、OkHttp Response 或 JSON 节点。
- `:core:model` 不包含 endpoint path、序列化 annotation 或 Android Context。
- write 实现不得复用带自动重试的 read Request pipeline。

## 5. 分层与数据流

标准读取流程：

```text
UI event
  -> ViewModel intent
  -> Repository
  -> Remote/Local source
  -> DTO parser
  -> DTO-to-Domain mapper
  -> Domain result
  -> immutable UI state
  -> Compose render
```

### 5.1 UI 层

- Composable 接收不可变 UI state 和 callback，不直接调用 network 或 session store。
- ViewModel 负责事件编排、取消 Request、分页状态和一次性 UI effect。
- 页面状态使用明确 sealed model，而不是多个可能矛盾的 Boolean。
- 导航参数只传稳定 ID；大对象通过 Repository 重新加载或使用受控缓存。

### 5.2 Domain model

- ID 使用明确 value type 或语义化类型，至少区分 ArticleId、MatchId、TeamId、PlayerId、CompetitionId 和 SeasonId。
- 服务端 nullable 不直接决定 Domain nullable；mapper 根据产品语义决定缺失时降级还是失败。
- 时间统一转换为 `Instant`，只有 UI 层按用户时区格式化。
- 比赛状态保留 `Unknown(rawValue)`，避免服务端增加状态时 parser 崩溃。
- 比赛事件保留未知事件类型；技术统计使用标识、名称、值和显示顺序组成的开放模型，不写死固定指标集合。
- 资料页和比赛详情由独立 section state 组成，单项缺失或失败不能清空整个页面。

### 5.3 Data 层

- Repository 定义用户行为能力，如 `observeHomeFeed()`、`loadMatch(id)`、`loadEntity(id)`，不暴露 URL。
- Remote source 负责 Request 和 DTO；Local source 负责 cache、设置和 session。
- Repository 决定 cache policy、刷新和数据合并，不由页面自行拼接。
- 写操作使用结果明确的 command model，不把失败伪装为本地成功。

## 6. Network 边界

建议创建三类逻辑 client，共享底层 connection pool，但使用不同 interceptor 与 policy：

M11/M12 的完整榜单和资料库虽然在登录功能之后开发，仍必须走 `Anonymous read`；milestone 顺序不能成为携带 Authorization 的理由。

| Client | Authorization | 自动重试 | 用途 |
| --- | --- | --- | --- |
| Anonymous read | 不携带 | 仅安全、幂等且有限次 | 资讯、比赛、榜单、搜索和公开资料库 |
| Authenticated read | 会话有效时携带 | 仅安全、幂等且有限次 | 登录状态和账号只读数据 |
| Experimental write | 必须携带且功能已开启 | 默认不自动重试 | 点赞、关注、收藏、发布、删除和资料修改等 |

所有 Request 通过以下集中组件：

- `EndpointRegistry`：Host、path、method 和能力标识。
- `ClientProfileProvider`：可更新的客户端版本 User-Agent 与静态 Header。
- `DeviceIdStore`：生成并保存一个 UUID。
- `SessionProvider`：只向 authenticated/write client 暴露当前 Authorization。
- `RedactingLogger`：仅 Debug 可用，并按 key、Header 和 query allowlist 输出。

## 7. 兼容性 parser

- JSON 默认忽略未知字段，但关键字段缺失必须生成 `UnsupportedContract`，不能静默伪造内容。
- 数字可能以 number 或 string 返回时，使用局部 serializer 兼容，不全局宽松转换。
- HTML 正文、富文本 block 与普通 JSON 分开解析，先清理危险 scheme 和不可控 embed。
- 每个 endpoint 至少保留 success、empty、server error 和 malformed fixture。
- parser 版本变化需要更新 fixture 来源日期和 [API.md](../protocol/API.md) 状态。

## 8. 错误模型

```kotlin
sealed interface AppError {
    data class Network(val kind: NetworkKind) : AppError
    data class Http(val status: Int) : AppError
    data class Server(val code: String?, val message: String?) : AppError
    data class Parse(val endpoint: EndpointId) : AppError
    data object AuthenticationRequired : AppError
    data object SessionExpired : AppError
    data class RateLimited(val retryAfter: Duration?) : AppError
    data class UnsupportedContract(val endpoint: EndpointId) : AppError
}
```

UI 不直接展示 exception message。用户文案描述可采取的动作；Debug detail 通过脱敏诊断信息提供。

## 9. 会话状态机

```text
Anonymous
  -> SubmittingCredentials
  -> ValidatingSession
  -> Authenticated
  -> Expired
  -> Anonymous
```

规则：

- 只有 `/v2/user/is_login` 验证通过后才进入 `Authenticated`。
- 冷启动可以短暂进入 `Restoring`，但匿名内容不需要等待恢复完成。
- 单个 authenticated Request 返回鉴权失败时，由 SessionManager 串行执行失效处理，避免多个页面重复弹出登录。
- 退出是本地确定操作：先清除 Secret，再更新状态；远端 logout 只有 contract 验证后才作为附加操作。

## 10. 本地存储

| 数据 | 建议存储 | 生命周期 |
| --- | --- | --- |
| UUID | DataStore 或专用 device store | 安装生命周期，除非用户清除数据 |
| Authorization | Keystore 支持的加密存储 | 会话生命周期 |
| 密码 | 不落盘 | 单次登录 Request |
| UI 设置 | DataStore | 安装生命周期 |
| 首页短期 cache | memory，必要时 Room | 可清理 |
| 阅读历史/离线 | Room + 文件目录，后续引入 | 用户可管理 |
| 图片 | Coil cache | 系统与用户可清理 |

不得将 Secret 放入 Room、普通 SharedPreferences、SavedStateHandle、Bundle 或 Compose state saver。

## 11. Build 与配置

- `debug`：允许脱敏 HTTP metadata、fixture capture 辅助和实验入口，但仍不允许 TLS bypass。
- `release`：关闭详细 network log，实验写功能默认关闭，开启 R8/resource shrink。
- endpoint 与 profile 通过类型化 BuildConfig 或资源生成，不从 UI 字符串读取。
- Secret 只从本机安全输入或 CI Secret 注入，不写入 `gradle.properties` 的仓库版本。
- 版本升级先跑 contract tests，再升级 Android/Kotlin dependency，避免同时混合多类风险。

## 12. 架构审查触发条件

以下情况需要更新本文和 [DECISIONS.md](../planning/DECISIONS.md)：

- 引入 Backend proxy、WebView 登录、远程 feature flag 或第三方 analytics。
- 引入新的 DI、network、database 或跨平台框架。
- feature module 开始相互依赖。
- write 与 read 无法继续共享安全边界。
- API 开始要求签名、设备证明、CAPTCHA 或 refresh token。
