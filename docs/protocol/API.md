# API 调研与 Contract

> 基线日期：2026-09-01。本文记录证据，不代表懂球帝官方公开或承诺支持这些接口。账号、凭据和写操作边界在对应章节内维护。

## 1. 证据等级

| 等级 | 定义 | 可以用于什么 |
| --- | --- | --- |
| A：已验证 | 本项目直接发送 Request 并观察到符合预期的 Response | 建立初始 contract 与 smoke test |
| B：有旁证 | 官方页面、官方 APK 资源或公开客户端捕获支持该结论 | 指导验证，不单独作为完成标准 |
| C：推断 | 根据命名、旧实现或相邻接口推测 | 只能进入调研 backlog |
| U：未知 | 尚无足够证据 | 不得实现为可用功能 |

每个 endpoint 在进入生产代码前，至少需要：method、完整 path、query/form、必要 Header、成功 fixture、空 fixture、错误 fixture、分页规则和敏感字段说明。

## 2. 已知 Host 与信任边界

| Host | 用途 | 当前策略 |
| --- | --- | --- |
| `api.dongqiudi.com` | 移动 API | 首期 API allowlist |
| `www.dongqiudi.com` | 官方网页与二维码登录 | 只作研究和外链，不作为主登录会话来源 |
| 文章/图片/视频 CDN | 媒体内容 | 从已验证 Response 提取后逐个加入媒体 allowlist |

- API Request 不应自动跟随到非 allowlist Host 并继续携带 Authorization。
- media client 不携带账号 Header，除非某项媒体 contract 明确要求且完成安全审查。
- 生产代码不得使用调研代理、证书 bypass 或任意 Host 配置。

## 3. Client Profile

移动 API 会检查客户端版本。2026-08-31 的调研样本使用官方 Android 8.7.2（versionCode 20441）特征后，请求才进入账号密码校验。

观察到的 User-Agent 尾部示例：

```text
News/20441 Android/13 NewsApp/20441 SDK/33 VERSION/8.7.2 dproClientApp
```

这只是证据样本，不是永久常量。实现要求：

- `ClientProfile` 集中保存 app version name/code、Android version 和 SDK 信息。
- profile 更新不修改页面或 Repository。
- UUID 使用随机生成的稳定 UUID；调研中的占位 UUID 不得进入正式实现。
- 匿名 Request 默认不携带 Authorization。
- 不伪造与功能无关的设备标识，也不收集 IMEI、广告 ID 或硬件序列号。

## 4. 密码登录

### 4.1 当前 Request contract

| 项目 | 值 | 证据 |
| --- | --- | --- |
| Method | `POST` | A |
| URL | `https://api.dongqiudi.com/v2/user/login` | A |
| Content-Type | `application/x-www-form-urlencoded` | A |
| Form | `username`、`password` | A |
| Header | `UUID`、兼容版本的 `User-Agent` | A |
| 成功 Response | 尚未取得 | U |

安全示例：

```http
POST /v2/user/login HTTP/1.1
Host: api.dongqiudi.com
Content-Type: application/x-www-form-urlencoded
UUID: <persistent-random-uuid>
User-Agent: <compatible-client-profile>

username=<user-input>&password=<unverified-representation>
```

`password` 的字段名已经确认，但成功登录前不能确定服务端要求明文、hash 或其他表示。不要根据失败 Response 推断其表示方式。

### 4.2 已观察错误

| 条件 | HTTP/业务结果 | 结论 |
| --- | --- | --- |
| 缺少 username/password | `errCode=40002`，“请填写用户名和密码” | path 与字段校验存在 |
| 使用过期客户端标识 | `errCode=40026`，“您使用的版本过低” | profile 必须可更新 |
| 当前版本标识 + 明显虚构账号 | `errCode=40003`，“用户名或者密码错误，请重试” | Request 已进入凭据校验阶段 |

失败 Response 没有返回可用 Authorization，也没有观察到可作为移动会话的 Cookie。

### 4.3 成功登录必须补齐

- password 的准确表示方式。
- Authorization 位于 Response Header、JSON data 还是其他字段。
- Authorization 是否包含 scheme 前缀。
- 用户资料字段、账号 ID 与过期信息。
- 是否存在 CAPTCHA、设备 challenge、二次验证或限流分支。
- session 有效期、并发设备行为和退出语义。
- 是否存在真实且可验证的 refresh contract；在此之前按“无 refresh”处理。

验证只允许使用用户明确授权的专用懂球帝测试账号，完成登录和只读检查后停止。

## 5. 会话验证

| 项目 | 当前结论 | 证据 |
| --- | --- | --- |
| URL | `https://api.dongqiudi.com/v2/user/is_login` | A/B |
| 匿名调用 | 返回 HTTP 401 | A |
| 移动会话 Header | `UUID` + `Authorization` | B，公开捕获脚本 |
| 成功 schema | 待专用账号归档 | U |

目标流程：

```text
login Response
  -> 提取候选 Authorization
  -> 使用相同 UUID 调用 /v2/user/is_login
  -> 验证成功后持久化
```

公开脚本旁证：

- `https://github.com/chavyleung/scripts/blob/master/dongqiudi/dongqiudi.cookie.js`
- `https://github.com/chavyleung/scripts/blob/master/dongqiudi/dongqiudi.js`

这些脚本只能证明捕获的移动会话可以重放，不能代替本项目对登录成功 Response 的验证。

## 6. 匿名读取能力

完整能力清单在 [FEATURES.md](../product/FEATURES.md) 维护。本节只记录当前接口证据，避免把产品要求写成已经验证的 API 事实。

| 能力组 | 当前匿名证据 | Contract 状态 | 计划 milestone |
| --- | --- | --- | --- |
| 首页/分类资讯流 | A，2026-09-01 匿名实测 | Contract 已归档并接入 | M3 |
| 文章详情 | A，2026-09-01 匿名实测 | Contract 已归档并接入 | M3 |
| 文章评论只读 | A，2026-09-01 匿名实测 | 一级评论已接入；回复线程待验证 | M3 |
| 专题、话题、图集和公开视频 | U | 待验证 | M3 |
| 日期比赛列表 | 前期已观察可匿名读取 | 待归档 | M4 |
| 比赛基础详情/赛前信息 | 前期已观察可匿名读取 | 待归档 | M4 |
| 完整事件时间线 | U | 待验证 | M5 |
| 首发、替补、教练和阵型 | U | 待验证 | M5 |
| 技术统计 | U | 待验证 | M5 |
| 赛后信息、交锋和实时更新数据 | U | 待验证 | M5 |
| 主要/热门积分榜、赛程赛果、射手榜 | 前期已观察可匿名读取 | 待归档 | M6 |
| 主要/热门助攻榜、球队和球员统计榜 | U | 待验证 | M6 |
| 热门球队相关资讯流 | 前期已观察可匿名读取 | 待归档 | M7 |
| 主要/热门球队、球员和赛事当前资料 | U | 待验证 | M7 |
| 核心搜索和已支持实体解析 | U | 待验证 | M8 |
| 长尾/历史榜单与统计 | U | 待盘点 | M11 |
| 完整球队、球员、赛事和赛季资料 | U | 待盘点 | M12 |
| 完整搜索、公开用户页、帖子和动态 | U | 待盘点 | M12 |

### 6.1 已归档的匿名资讯 Contract

共同约束：

- Host 为 `api.dongqiudi.com`，Method 均为 `GET`。
- 必须发送 `Accept: application/json`；本项目使用可识别的 `User-Agent: DongqiudiPure-Android/0.1`。
- 不发送 `Authorization`、Cookie、UUID、账号标识或设备标识。
- JSON 忽略新增的非关键字段；关键字段缺失、字段类型不兼容或 `next` 跳出 API Host 时返回 `UnsupportedContract`。
- 固定脱敏样本位于 `core/testing/src/main/resources/contracts/news/2026-09-01/`，其中所有内容、账号、ID、cursor 与媒体路径均为虚构值。

资讯流：

```http
GET /app/tabs/web/{tabId}.json
```

- 已验证分类：`1` 头条、`3` 英超、`4` 意甲、`5` 西甲、`6` 德甲、`56` 中超、`114` 世界杯。
- 首页没有 query；响应的 `articles` 可以为空，`next` 为空表示结束。
- 下一页从服务端 `next` 提取 `after` 与 `page`，Request 同时携带 `child_tab_id=0` 和空的 `user_pay_type`。
- 条目展示时间使用 `created_at`；置顶条目的 `published_at` 可能被调整到未来以参与排序。`channel` 是内容类型，只有非空的 `showcontent` 才作为展示标签。
- 页面内按稳定 article ID 去重；刷新、追加加载和追加失败由 Paging 3 独立处理。

文章详情：

```http
GET /v2/article/detail/{articleId}
```

- 成功 envelope 为 `code=0` 且 `data` 非空；非零业务 code 或空 data 作为服务端错误。
- 正文 `body` 是 HTML。当前只转为段落、标题/引用文本和图片块，不执行脚本、embed 或 WebView 内容。
- 只加载 HTTPS 且 Host 为 `qunliao.info` 或其子域的媒体 URL。
- `infos.channels` 中已验证的 `dongqiudi:///team/{id}`、`player/{id}` 和 `competition/{id}` 转为应用内关联实体。

文章评论：

```http
GET /v2/article/{articleId}/comment?size=20&platform=web
GET /v2/article/{articleId}/comment?sort=down&next={cursor}&pn={page}&platform=h5&version=0
```

- 成功 envelope 为 `errCode=0` 且 `data` 非空；空评论由三个空列表和空 `next` 表示。
- “最热”在第一页先合并 `recommend_list`，再合并 `comment_list`；“最新”只使用 `comment_list`。后续页均使用普通评论列表，并按稳定 comment ID 去重。
- `user_list` 用于解析评论作者；评论 HTML 只保留纯文本，内联表情图片转为可见占位文本。允许正文为空但带有 `attachments` 的图片评论，附件仍只加载 `qunliao.info` HTTPS 媒体。当前只展示 `reply_total`，回复线程的读取 contract 尚未验证，因此不伪造回复内容。

M11/M12 只扩大覆盖范围，不改变鉴权方式；对应 Request 必须先按匿名 contract 验证，不能因为实现时已经有登录模块就默认携带 Authorization。

“前期已观察可匿名读取”仍不等于 contract 完成。M2-M12 对每个能力执行同一流程：

1. 从官方匿名入口确认实际可达页面和参数来源。
2. 低频发送最小 read Request，记录 method、path、Header、分页和错误分支。
3. 保存脱敏 fixture，完成 parser 与 mapper 验证。
4. 将 API 证据更新到本节，将能力状态更新到 [FEATURES.md](../product/FEATURES.md)。

固化 contract 时仍需验证 User-Agent、UUID、分页 cursor、默认语言和地区参数。遇到签名、CAPTCHA、设备证明或付费内容边界时停止验证并记录阻塞，不尝试绕过。

## 7. 登录后读取能力

除会话检查外，当前尚未取得账号私有读取能力的成功 schema。以下均是已确认的产品目标，不是已发现 endpoint：

| 能力 | 当前证据 | 计划 milestone |
| --- | --- | --- |
| 当前账号基础摘要与主队字段 | U | M10 |
| 消息列表与详情 | U | M13 |
| 关注动态 | U | M13 |
| 收藏列表 | U | M13 |
| 关注/粉丝列表 | U | M13 |
| 账号已发布内容和评论记录 | U | M13 |

每项 authenticated read contract 必须额外记录 Authorization 使用方式、退出后的缓存清理、401 行为和响应中的私有字段。只读账号页不得隐式发送标记已读、统计上报或其他会改变远端状态的 Request。

## 8. 网页二维码登录结论

官方页面 `https://www.dongqiudi.com/user/login` 的二维码 payload 形如 `code:<32-hex>`，网页通过以下 path 轮询：

- `POST /user/scan/polling`
- `POST /user/confirm/polling`

普通流程获得的是网页侧 `laravel_session`、`dqduid` 等 Cookie，并依赖已经登录的官方 App 扫码确认。当前没有证据表明这些 Cookie 能稳定换取移动端 Authorization。

结论：二维码登录不作为 Android 客户端的主登录路线。未来只有观察到官方、稳定且权限明确的移动 token exchange 后才重新评估。

## 9. 写接口现状

官方 H5 资源中观察到以下互动 path：

- `/comments/up/`
- `/v2/article/up_new/`
- `/v2/article/cancel_up/`
- `/v2/api/comment/appdel/`
- `/v3/comment/app/comment/del`

这些 path 主要指向点赞、取消点赞或删除，并不能证明发帖或发表评论 contract。已观察到 `dongqiudi://post_comments/{article}/{comment}` deep link，只能证明官方 App 有回复入口，不能推出实际 network Request。

当前状态：

| 能力 | 状态 | 允许的下一步 |
| --- | --- | --- |
| 点赞/取消点赞 | 有 path 旁证，参数未知 | 静态分析、脱敏 contract 研究、Mock |
| 删除评论 | 有 path 旁证，参数未知 | 只记录，不对真实账号调用 |
| 关注/取消关注 | U | 获取授权样本或静态 contract，不猜测 |
| 收藏/取消收藏 | U | 获取授权样本或静态 contract，不猜测 |
| 消息已读/删除/屏蔽 | U | 与消息只读 endpoint 分开验证 |
| 发表评论/回复 | U | 获取授权样本或静态 contract，不猜测 |
| 发布帖子 | U | 获取授权样本或静态 contract，不猜测 |
| 发送私信 | U | 获取授权样本或隔离环境，不猜测 |
| 删除/编辑帖子 | U | 只建立 Mock，等待隔离环境 |
| 编辑资料/主队 | U | 只建立 Mock，等待隔离环境 |

任何 write endpoint 在 method、body、幂等性、风控 Response 和服务端成功语义确认前，都不得连接 UI 的真实提交按钮。

## 10. Contract 归档格式

每个 endpoint 建议使用以下结构：

```text
core/testing/src/main/resources/contracts/<capability>/<date>/
  request.md
  success.json
  empty.json
  server-error.json
  malformed.json
  NOTES.md
```

`request.md` 至少记录：

- 验证日期和证据等级。
- method、Host、path、query、content type。
- 必须 Header 与明确不应发送的 Header。
- body 字段、类型、可空性和编码。
- HTTP status 与业务 code 的组合。
- 分页起点、下一页 cursor 和终止条件。
- cache/ETag 行为以及 rate limit 观察。
- 已删除或替换的敏感字段列表。

fixture 必须删除 password、Authorization、Cookie、手机号、账号标识和设备标识；只保留验证 contract 所需的最小结构。

## 11. Request Policy

- GET 等幂等 read Request 仅对连接中断和有限的 5xx 做有上限重试，并使用退避。
- 401 不自动重试；交给 SessionManager 处理。
- 429 尊重 `Retry-After`；没有 Header 时停止自动请求并提示稍后重试。
- parser error 不重试同一 Response。
- POST write Request 默认不重试，除非 contract 证明幂等并提供幂等 key。
- 页面离开或 ViewModel 清除时取消不再需要的 Request。
- 比分刷新在后台暂停，不建立永久轮询。

## 12. 参考来源

- 官方下载页：`https://www.dongqiudi.com/downloadApp`
- 官方 Android APK：`https://apk.dongqiudi.com/app/apk/channel/dongqiudi-DQD_PC.apk`
- 官方扫码登录页：`https://www.dongqiudi.com/user/login`
- 官方隐私政策：`https://topic.dongqiudi.com/webapp/privacy/privacy.html?channgle=xiaomi`
- 小米应用商店：`https://app.mi.com/details?id=com.dongqiudi.news`
- 移动会话公开捕获脚本：见“会话验证”章节。

官方 APK 8.7.2 使用保护方案并在调研模拟器中主动退出，因此“能从 APK 看到字符串”不等于已验证 runtime contract。
