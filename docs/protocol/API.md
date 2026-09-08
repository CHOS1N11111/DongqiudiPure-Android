# API 调研与 Contract

> 基线日期：2026-09-01。本文记录证据，不代表懂球帝官方公开或承诺支持这些接口。账号、凭据和写操作边界在对应章节内维护。
> 账号静态调研补充至 2026-09-08；以下有日期的结论仅对应当时取得的样本，不保证服务端当前仍采用同一实现。

## 1. 证据等级

| 等级 | 定义 | 可以用于什么 |
| --- | --- | --- |
| A：已验证 | 本项目直接发送 Request 并观察到符合预期的 Response | 建立初始 contract 与 smoke test |
| B：有旁证 | 官方页面、官方 APK 资源或公开客户端捕获支持该结论 | 指导验证，不单独作为完成标准 |
| C：推断 | 根据命名、旧实现或相邻接口推测 | 只能进入调研 backlog |
| U：未知 | 尚无足够证据 | 不得实现为可用功能 |

每个 endpoint 在进入生产代码前，至少需要：method、完整 path、query/form、必要 Header、成功 fixture、空 fixture、错误 fixture、分页规则和敏感字段说明。

静态材料须进一步区分：**调用代码**可以为所见的 method、地址、参数及响应读取方式提供 B 级证据；
**路径字符串**只能为该字符串存在于样本中提供 B 级证据，其用途分类仍是 C 级推断。
两者都不等于实际调用成功；未取得的 Host、参数、响应结构和成功语义继续标为 U。

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

当前客户端已接入受限的兼容路径：只从登录 Response 的 `Authorization` Header 或
明确命名的 Authorization/token 字段读取候选值，并立即调用 `/v2/user/is_login`；
验证失败或结构不兼容时不保存候选值。该实现不改变上述证据等级，真实成功 schema、
密码表示和 challenge 分支仍须由用户授权的专用账号完成端到端确认。

会话校验必须返回可识别的明确登录状态。只有普通成功码或用户资料、登录状态缺失或
无法识别时，返回 `UnsupportedContract`，不保存新令牌，也不显示已登录。

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

### 5.1 已实现的会话生命周期（2026-09-08）

- 官方 H5 [公共网络脚本](https://static1.dongqiudi.com/dqd-node/static/js/vendor.d971c5255659b1e448a4.js)
  在 HTTP 480 时调用原生 `userKickOut`。这是 B 级静态证据，不代表已观察到真实账号失效的响应，
  也不能据此推断具体失效原因。客户端只在专用账号请求中将 480 映射为 `SessionExpired`，
  不让匿名内容或媒体响应触发退出。
- 冷启动校验已保存的凭证；应用回到前台时，复用 `/v2/user/is_login` 复查现有会话，
  自动复查间隔至少 60 秒。没有凭证时不发起账号请求，已有登录或校验进行时不重复请求。
  此处的 `refresh()` 是会话复查，不是刷新令牌或自动重新登录。
- 校验明确未登录或返回 HTTP 401、403、480 时，清除本机凭证和账号摘要。
  普通业务错误不再一律视为会话过期；网络故障、429、5xx 不删除已保存的凭证，
  前台复查遇到这些暂时性故障时保留此前已确认的账号状态。
- 登录状态缺失、无法识别或响应不兼容时不显示已登录，但可保留原有加密凭证供后续重试。
  新登录的候选令牌仍须先校验，再持久化。复查同一凭证时，如已知账号 ID 发生变化，则清除会话，
  不静默换成另一个账号；同一进程中的校验失败不会抹掉此前确认的账号 ID。
- 退出或发起新的登录会取消旧账号请求；状态和凭证写入还校验请求是否仍属于当前操作，
  防止迟到的成功响应重新登录、旧账号的失效响应清除新账号。退出不等待网络完成。
  若本机凭证清理失败，则显示存储错误，并阻止本次进程自动恢复旧凭证。
- 本轮不增加远端退出、自动续签、短信登录或任何互动写接口，也不变更公开资讯、比赛、数据请求。
  当前没有已接入的私有列表缓存；主队和关注仍是本机偏好，不表示懂球帝账号同步。

新增测试使用内存会话仓库和本机 MockWebServer，覆盖取消、换号、过期、限频及错误分支；
测试响应只用于验证代码行为，不提升移动端成功登录 contract 的证据等级。

### 5.2 H5 会话传递的静态旁证（2026-09-08）

APK 普通缓存引用的[赛事历史页面](https://n.dongqiudi.com/webapp/matchHistory.html?competition_id=4&app=dqd&language=zh-cn&app_type=)
及其[页面脚本](https://static1.dongqiudi.com/dqd-node/static/js/matchHistory.8745769947816b710799.js)
显示，该页面的 Axios 实例使用 `API_MAP["sport-data"]`，从 `_globalParams` 获取 `UUID` 和 `Authorization`。
公共脚本的生产配置把该 Host 映射到 `sport-data.dongqiudi.com`；公开页面中的两个凭证值均为空字符串。
这是 B 级调用代码旁证，不证明匿名请求需要设备标识，也不授权向本项目的公开数据或媒体请求添加账号 Header。
第 5.1 节的 480 处理同时覆盖默认 Axios 和 `create` 创建的实例，但没有提供 Android 登录成功 schema。

## 6. 匿名读取能力

完整能力清单在 [FEATURES.md](../product/FEATURES.md) 维护。本节只记录当前接口证据，避免把产品要求写成已经验证的 API 事实。

| 能力组 | 当前匿名证据 | Contract 状态 | 计划 milestone |
| --- | --- | --- | --- |
| 首页/分类资讯流 | A，2026-09-01 匿名实测 | Contract 已归档并接入 | M3 |
| 文章详情 | A，2026-09-01 匿名实测 | Contract 已归档并接入 | M3 |
| 文章评论只读 | A，2026-09-02 匿名实测 | 一级评论与回复线程 Contract 已归档并接入 | M3 |
| 专题、话题、图集和公开视频 | U | 待验证 | M3 |
| 日期比赛列表 | A，2026-09-02 匿名实测 | 五大联赛和中超 Contract 已归档并接入 | M4 |
| 比赛基础详情/赛前信息 | 前期已观察可匿名读取 | 待归档 | M4 |
| 完整事件时间线 | U | 待验证 | M5 |
| 首发、替补、教练和阵型 | U | 待验证 | M5 |
| 技术统计 | U | 待验证 | M5 |
| 赛后信息、交锋和实时更新数据 | U | 待验证 | M5 |
| 五大联赛和中超积分榜 | A，2026-09-02 匿名实测 | 当前赛季 Contract 已归档并接入 | M6 |
| 主要/热门赛程赛果、射手榜 | U | 待验证 | M6 |
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

文章评论与回复：

```http
GET /v2/article/{articleId}/comment?size=20&platform=web
GET /v2/article/{articleId}/comment?sort=down&next={cursor}&pn={page}&platform=h5&version=0
GET /v2/comment/{commentId}?size=20&sort=up&platform=web
GET /v2/comment/{commentId}?size=20&sort=up&next={cursor}&pn={page}&platform=web
```

- 成功 envelope 为 `errCode=0` 且 `data` 非空；空评论由三个空列表和空 `next` 表示。
- “最热”在第一页先合并 `recommend_list`，再合并 `comment_list`；“最新”只使用 `comment_list`。后续页均使用普通评论列表，并按稳定 comment ID 去重。
- `user_list` 用于解析评论作者；评论 HTML 只保留纯文本，内联表情图片转为可见占位文本。允许正文为空但带有 `attachments` 的图片评论，附件仍只加载 `qunliao.info` HTTPS 媒体。
- `up` 是只读点赞数，缺失时保持 `null`；客户端不提供点赞写操作。
- 回复详情使用 `comment_info` 作为父评论、`reply_list` 作为分页回复列表，并校验返回的 `article_id` 与当前文章一致。没有回复时展示真实空状态，不构造内容。

### 6.2 已归档的比赛与积分榜 Contract

共同约束：匿名 `GET`，不发送 Authorization、Cookie 或设备标识。固定脱敏样本位于
`core/testing/src/main/resources/contracts/football/2026-09-02/`。

比赛列表：

```http
GET https://api.dongqiudi.com/data/tab/new/important?init=1
```

- 响应是服务端当前七日窗口；客户端按设备时区把 UTC 开球时间归入本地日期。
- 当前只保留赛事 ID `4` 英超、`3` 西甲、`9` 意甲、`5` 德甲、`12` 法甲、`43` 中超，其余赛事不会进入 UI。
- 比分或状态字段缺失时保持 `null` / `Unknown`；接口没有目标赛事时显示空状态，不补造比赛。
- 主客队 `logo` 只接受 `qunliao.info` 及其子域的 HTTPS URL。

赛季与积分榜：

```http
GET https://sport-data.dongqiudi.com/soccer/biz/data/seasons?competition_id={competitionId}&app=dqd&platform=miniprogram&version=830&lang=zh-cn
GET https://sport-data.dongqiudi.com/soccer/biz/data/standing?season_id={seasonId}&app=dqd&platform=miniprogram&version=830&lang=zh-cn
```

- 客户端先从赛季列表解析当前赛季 ID，再请求积分榜，不硬编码会过期的赛季 ID。
- 只向 UI 暴露上述六项赛事；名次、场次、胜平负、进失球和积分保留服务端的可空语义。
- 分区范围与名称来自响应的 `desc.from`、`desc.to` 和 `desc.text`，不按联赛猜测欧战或降级区。

M11/M12 只扩大覆盖范围，不改变鉴权方式；对应 Request 必须先按匿名 contract 验证，不能因为实现时已经有登录模块就默认携带 Authorization。

### 6.3 匿名主队、关注实体与球队圈子

2026-09-04 以不携带 Authorization、Cookie 或设备标识的最小 `GET` Request 验证：

```http
GET https://api.dongqiudi.com/search?keywords={query}
GET https://sport-data.dongqiudi.com/soccer/biz/dqd/team/sample/{shortTeamId}?app=dqd&lang=zh-cn
GET https://api.dongqiudi.com/groups/topic/all/{groupId}?order=reply&page={page}
```

- 搜索响应直接提供真实球队/球员 ID、名称、队徽或头像；名称中的高亮 HTML 只转为纯文本，不执行标签。
- 球队 sample 的 `tabs.list` 决定公开内容层级，并在 `tab=circle` 时提供 `group_id`。客户端不按球队名称猜圈子 ID。
- 圈子列表可匿名分页读取，展示作者、正文摘要、媒体、回复数和点赞数；分页、空数据和不兼容响应独立降级。
- 圈子话题 ID 虽带有 App 内部 article scheme，但已验证的 `/v2/article/detail/{articleId}` 不接受该 ID。话题详情 contract 未确认前，列表不错误跳转到资讯详情。
- 主队及关注列表当前只保存在本机 DataStore。未验证任何远端关注或账号同步写接口，也不会向用户宣称服务端关注成功。

结论：主队/关注中心的公开内容与本机个性化不需要登录；账号既有关注、跨设备同步和远端关注操作仍需要登录及独立 contract。

“前期已观察可匿名读取”仍不等于 contract 完成。M2-M12 对每个能力执行同一流程：

1. 从官方匿名入口确认实际可达页面和参数来源。
2. 低频发送最小 read Request，记录 method、path、Header、分页和错误分支。
3. 保存脱敏 fixture，完成 parser 与 mapper 验证。
4. 将 API 证据更新到本节，将能力状态更新到 [FEATURES.md](../product/FEATURES.md)。

固化 contract 时仍需验证 User-Agent、UUID、分页 cursor、默认语言和地区参数。遇到签名、CAPTCHA、设备证明或付费内容边界时停止验证并记录阻塞，不尝试绕过。

## 7. 登录后读取能力

除会话检查外，当前尚未取得账号私有读取能力的成功 schema。下表记录产品目标的成功 contract 状态；
第 7.1-7.3 节的静态候选不能将这些能力升级为已验证或已接入。

| 能力 | 成功 contract 证据 | 计划 milestone |
| --- | --- | --- |
| 当前账号基础摘要与主队字段 | U | M10 |
| 消息列表与详情 | U | M13 |
| 关注动态 | U | M13 |
| 收藏列表 | U | M13 |
| 关注/粉丝列表 | U | M13 |
| 账号已发布内容和评论记录 | U | M13 |

每项 authenticated read contract 必须额外记录 Authorization 使用方式、退出后的缓存清理、401 行为和响应中的私有字段。只读账号页不得隐式发送标记已读、统计上报或其他会改变远端状态的 Request。

### 7.1 APK 主队与关注候选（2026-09-07/08）

来源：[官方 Android APK](https://apk.dongqiudi.com/app/apk/channel/dongqiudi-DQD_PC.apk) 8.7.2 的
`lib/arm64-v8a/libapp.so`，样本指纹见第 12.1 节。以下全部是路径字符串，未找到对应完整调用代码。
HTTP 方法、Host、参数、必要 Header、响应和分页均为 U；右列只是 C 级查找方向，不是接口说明。
路径的大小写、前缀和末尾斜杠按样本保留，字节位置从 0 开始。

| 包内原始路径字符串 | 字节位置 | 待核对方向与边界 |
| --- | ---: | --- |
| `user/hometeam` | 1135023 | 主队相关，不能区分读取与设置 |
| `user/followed_channels_new` | 1601730 | 已关注频道或实体列表候选 |
| `team/followed/` | 758733 | 球队关注关系候选，后缀拼接未知 |
| `team/follow` | 206586 | 球队关注操作候选，不调用 |
| `team/unfollow` | 1668890 | 取消球队关注候选，不调用 |
| `person/unfollow` | 1755869 | 取消球员或人物关注候选；未找到对应 `person/follow` 字符串 |
| `v3/useract/app/webchannel/sortFollowedList` | 192483 | 关注排序候选，排序 Body 和成功语义未知 |
| `v2/favourites/addTeam` | 1654672 | 球队相关收藏或关注候选，不是文章收藏证据 |
| `v2/favourites/delTeam` | 643772 | 球队相关取消候选，不是删除文章收藏证据 |
| `channel/refresh_follow?type=1` | 1051951 | 关注更新候选，不能认定为无副作用读取 |

可用于继续定位的原始方法名：

- `_mergePendingFollowAfterLogin@815062298`、`_getRemoteFavouriteTeamId@815062298`。
- `_getRemoteFollowChannelIds@815062298`、`_doSortFollowedListRequestByIds@815062298`。
- `_doTeamHostRequest@815062298`、`_doFollowTeamsRequest@815062298`。

模型及字段字符串包括 `TeamFollowPageEntity.fromJson`、`TeamFollowPageFavouriteList.fromJson`、
`followed_list`、`favourite_list`、`favourite_team`、`team_id`、`person_id`、`channel_ids`、
`is_follow`、`is_follow_match`。没有方法体或响应证明它们与某条路径的对应关系，不能拼成成功 fixture。
登录后待合并关注的方法名也不能证明合并优先级、实体 ID 映射或排序规则。

9 月 8 日补查普通缓存 JSON、Flutter 资源清单和公开 H5 模块后，仍未补齐这些调用。
主队和关注继续只在本机保存，不把本地操作宣称为账号同步成功。

### 7.2 APK 其他账号与消息候选（2026-09-07）

与第 7.1 节使用同一 APK 样本和证据边界。本节集中保存候选，包括可能写入状态的路径；
不能因其列在账号读取章节就将它们作为 GET 试探。

| 包内原始路径字符串 | 字节位置 | 待核对方向与边界 |
| --- | ---: | --- |
| `v1/user/index` | 693695 | 用户相关，Host 与用途未知 |
| `v1/user/login` | 1670289 | 登录候选，不足以替换现有 `/v2/user/login` |
| `v3/useract/app/user/messages` | 1688787 | 消息候选 |
| `v3/useract/app/user/messageSenders` | 803455 | 消息发送者候选 |
| `v3/useract/app/user/messagesFlush` | 613497 | 可能改变消息状态，不作为只读请求调用 |
| `v3/useract/app/user/updateNotifySetting?action=` | 588816 | 通知设置操作候选，`action` 未知 |
| `v3/user/Usernotify/list` | 1703224 | 通知列表候选，保留原大小写 |
| `users/mix_messages` | 1690573 | 聚合消息候选 |
| `users/mentions` | 239016 | 提及或通知候选 |
| `users/quote_comments` | 734894 | 评论引用或回复记录候选，不是发表评论证据 |
| `users/up_comments` | 1730637 | 点赞相关记录或通知候选，不是执行点赞证据 |

### 7.3 H5 用户信息调用（2026-09-08）

来源：[H5 公共脚本](https://static1.dongqiudi.com/dqd-node/static/js/vendor.d971c5255659b1e448a4.js)，
`getUserByToken` 出现在 UTF-8 解码后的 UTF-16 字符下标 496156（从 0 开始）。
这是 B 级调用代码证据，没有实际账号请求或成功 Response。

| 项目 | 所见代码或缺口 |
| --- | --- |
| Method / Path | `GET /v3/sportsvideo/score/center/getUserByToken` |
| Header | 通过原生 `getUserInfo` 桥接取 `Authorization` 和 `uuid`，分别传入 `Authorization`、`UUID` |
| 响应读取 | 取 Axios 响应的 `data.data`，调用方使用 `phone_number` |
| 未确认项 | 外部传入的 `$axios` 完整 Host、账号范围、完整成功/错误 JSON 和匿名行为 |

不能把它认定为通用账号主页接口，也不因所见字段增加手机号收集。
同脚本的生产配置虽有 `api` 和 `sport-data` Host 映射，但不足以确定这个外部 `$axios` 的 Host，
更不能套用到 APK 的其他相对路径。

### 7.4 后续需要的最小材料

1. Android 正常登录及随后会话校验的脱敏请求与响应，用于补齐第 4.3 节，不能用网页登录样本替代。
2. 打开主队、查看已关注球队和球员时的真实读取调用：完整 Host、method、参数编码、必要 Header、
   成功/空/错误响应、分页规则，以及实体 ID、排序和本地与远端关注的合并规则。
3. 写功能另行取得完整调用代码或原版操作的脱敏记录；不能通过猜路径或重放真实账号写操作补证。

密码、短信验证码、有效 Authorization、Cookie 和账号私有内容不进入文档；
即使凭据出现在 URL query 中也必须脱敏。没有成功样本时保留未知，不伪造服务端响应。

## 8. 网页登录调研

### 8.1 二维码登录的既有结论

官方页面 `https://www.dongqiudi.com/user/login` 的二维码 payload 形如 `code:<32-hex>`，网页通过以下 path 轮询：

- `POST /user/scan/polling`
- `POST /user/confirm/polling`

普通流程获得的是网页侧 `laravel_session`、`dqduid` 等 Cookie，并依赖已经登录的官方 App 扫码确认。当前没有证据表明这些 Cookie 能稳定换取移动端 Authorization。

结论：二维码登录不作为 Android 客户端的主登录路线。未来只有观察到官方、稳定且权限明确的移动 token exchange 后才重新评估。

### 8.2 移动网页短信登录（2026-09-08）

来源：[官方移动文章页](https://m.dongqiudi.com/article/3717733.html) 引用的
[主脚本](https://static1.dongqiudi.com/m/dist/app.37b9dd434940bb08a2a1.js) 中，`/userLogin` 路由指向
[登录模块](https://static1.dongqiudi.com/m/dist/27.0133bf43fadc58600e9d.js)。以下是 B 级调用代码证据，未发送短信或登录请求。

| 项目 | 所见调用 |
| --- | --- |
| 短信请求 | `GET https://api.dongqiudi.com/v3/useract/app/user/postGottenlCode` |
| 短信 query | `phone` 为输入手机号，`region=CN`，`type=msite` |
| 登录请求 | `GET https://api.dongqiudi.com/v3/useract/app/user/pcLogin` |
| 登录 query | `applyPhone` 为输入手机号，`applyCode` 为验证码 |
| 响应读取 | HTTP JSON 中的 `data.auth` 保存到网页 `localStorage` 的 `mtoken`，随后提示成功并返回上一页 |

- `postGottenlCode` 是源码原始拼写；GET 也会发送短信或建立会话，不能作为无副作用读取验证。
- 两处调用未显式设置 `UUID` 或 `Authorization`，不等于已证明服务端不需要其他 Header。
- 页面没有展示完整的业务成功判断；`data.auth` 的格式、有效期、错误和 challenge 响应均为 U。
  固定 120 秒倒计时也不是短信 TTL 或服务器限流规则的证据。
- 没有证据证明 `mtoken` 等于 Android `Authorization`，不能把 `auth` 加入现有移动登录令牌解析。
  网页退出只删除 `mtoken`，不是服务端撤销会话的证据。
- 手机号和验证码位于 URL query，后续取样不能只对 Body 与 Header 脱敏。

### 8.3 懂球号网页短信登录（2026-09-07）

来源：[官方懂球号首页](https://hao.dongqiudi.com/home) 的内联脚本，归档 HTML 第 158-274 行。
以下同为 B 级调用代码证据，没有实际短信或登录响应。

| 项目 | 所见调用 |
| --- | --- |
| 短信请求 | `GET https://hao.dongqiudi.com/web/sns/sendMessage`，query 为 `phonenumber` |
| 短信响应读取 | `message`、`data` 中候选账号的 `id` / `name`、`ttl` |
| 登录请求 | `POST https://hao.dongqiudi.com/web/user/phonelogin` |
| 表单字段 | `phonenumber`、`code`、`useridcheck`、`oauth_redirect` |
| 前端成功判断 | `req.rs` 为真时跳转到 `req.msg`，否则使用 `req.msg` 提示错误 |

这是懂球号网页自身的流程，不是 Android 密码登录成功 schema；没有取得移动端 Authorization，
也未证明它的网页会话可转换为 Android 会话。发送短信同样是有外部影响的操作，本轮未调用。

### 8.4 已排查但不能作为可用登录方案的方向

- 2026-09-07 的懂球号 HTML 第 243-265 行保留旧 `POST /login` 处理器，字段是
  `name`、`password`、`remeber`、`redirect`，其中 `remeber` 按原拼写保留。
  但第 59-64 行的账号密码输入框已被注释，只能记为遗留代码，不能据此新增密码登录方案。
- 同日 [PC 主脚本](https://pc.dongqiudi.com/_nuxt/e353568729f73ca597eb.js) 将 `/login` 路由重定向到 `/`；
  [对应模块](https://pc.dongqiudi.com/_nuxt/b5ac5560e2f9da4e1dac.js) 只有占位页面，没有登录请求实现。
- PC 主脚本中的 `GET /api/v2/article/detail/{id}`、`GET /api/v2/article/{id}/comment`（`size=200`、
  `platform=web`）及[旧文章模块](https://pc.dongqiudi.com/_nuxt/15c842662a3dd9dda903.js) 中的
  `GET /api/v2/comment/{id}?platform=web` 只支持文章与评论读取的静态结论，不证明可提交评论或点赞。
  `/api` 是 PC 站调用路径，不能机械加到移动 API。
- 对 `https://www.dongqiudi.com/user/login` 的本机访问，9 月 7 日返回 HTTP 567；9 月 8 日观察到
  `www` 以 302 跳到 `m`，后者再以 302 跳到 `/guide`。这是各次请求的结果，不覆盖历史二维码记录，
  也不能推断所有用户或当前时刻都无法访问。

重新评估需要新的实际可达入口、完整调用代码或脱敏成功样本；找到路径或遗留表单本身不满足条件。

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
| 关注/取消关注 | APK 有路径字符串，完整调用为 U，见第 7.1 节 | 获取授权样本或静态 contract，不猜测 |
| 收藏/取消收藏 | 文章收藏仍为 U；球队相关字符串不能代替文章收藏证据 | 获取授权样本或静态 contract，不猜测 |
| 消息已读/删除/屏蔽 | APK 有可能改变状态的候选，语义为 U，见第 7.2 节 | 与消息只读 endpoint 分开验证 |
| 发表评论/回复 | U | 获取授权样本或静态 contract，不猜测 |
| 发布帖子 | U | 获取授权样本或静态 contract，不猜测 |
| 发送私信 | U | 获取授权样本或隔离环境，不猜测 |
| 删除/编辑帖子 | U | 只建立 Mock，等待隔离环境 |
| 编辑资料/主队 | 主队有路径字符串，但读写未分；编辑资料仍为 U | 只建立 Mock，等待隔离环境 |

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

### 12.1 账号静态调研样本与范围（2026-09-07/08）

上述模拟器观察是既有记录。9 月 7、8 日的补充只下载公开 HTML、JS、APK 并读取普通资源与字符串，
没有执行脚本、APK 或模拟器，没有登录、发送短信或调用账号读写接口，没有解密或脱壳。
本次入库仅归档这些已完成调研的脱敏结论，不重新请求账号接口。

APK 下载来源见第 7.1 节，归档样本大小为 186,460,376 字节，Manifest 可读版本为 8.7.2。
下载 URL 可能更新，路径偏移只对下列指纹对应的样本有效：

| 材料 | SHA-256 |
| --- | --- |
| 官方 APK 样本 | `620CF35595A2C8AECC1AEAAAAEF41A6F2E05F12DCAE63B376F85438D29D7598A` |
| `lib/arm64-v8a/libapp.so` | `028F899CC935C40BCFB603889C66F7AAAD763EF33DA01CE43865466413A9F984` |
| 懂球号首页 HTML 样本 | `FC26A89DFD828463E75B2FC69AAF5C93150548EB0596C5A049FF31BDF573FDD7` |

普通缓存 JSON、Flutter 资源清单和公开脚本未补出主队/关注的完整调用，所查脚本也未发现可用的
末尾 source-map 链接；资源名、方法名或打包器内的 source-map 字符串都不能当作业务源码。
原始 APK、脚本、临时报告、来源元数据和本机审计材料仍留在仓库外，不随本文入库。
