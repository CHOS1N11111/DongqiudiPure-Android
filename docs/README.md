# DongqiudiPure Android 开发文档

> 项目阶段：M3 资讯与公开内容进行中
> 文档基线：2026-09-01

本目录是项目规划与工程约束的单一入口。文档按产品、计划、工程和协议四个方向组织。当前文档用于明确范围、验证路径和质量门槛。

## 文档地图

| 方向 | 文档                                          | 负责回答                                       | 主要维护时机                    |
| ---- | --------------------------------------------- | ---------------------------------------------- | ------------------------------- |
| 产品 | [PRODUCT.md](product/PRODUCT.md)               | 产品目标、阶段边界和阶段验收条件               | 产品范围变化时                  |
| 产品 | [FEATURES.md](product/FEATURES.md)             | 逐项功能覆盖、证据状态和所属 milestone         | 发现、验证或实现功能时          |
| 计划 | [PLAN.md](planning/PLAN.md)                    | 先做什么、依赖什么、每阶段如何退出             | 每个 milestone 开始和结束时     |
| 计划 | [DECISIONS.md](planning/DECISIONS.md)          | 已确定、待确认和待验证的关键决策               | 作出或推翻技术决策时            |
| 工程 | [ARCHITECTURE.md](engineering/ARCHITECTURE.md) | 代码如何分层、模块如何依赖、状态如何流转       | 架构边界变化时                  |
| 工程 | [BACKEND-CONTRACT-TODO.md](engineering/BACKEND-CONTRACT-TODO.md) | UI 层已完成但数据层尚未接入的位置（**临时文档，接入完成后删除**） | 接入某项数据源时 |
| 协议 | [API.md](protocol/API.md)                      | 哪些 API 已验证、哪些仍未知、如何记录 contract | 每次 API 调研或 contract 变化时 |

## 推荐阅读顺序

1. 先读 [PRODUCT.md](product/PRODUCT.md)，确认阶段边界。
2. 在 [FEATURES.md](product/FEATURES.md) 找到具体能力及当前状态。
3. 再读 [PLAN.md](planning/PLAN.md)，选择当前 milestone 中最靠前且未被阻塞的任务。
4. 实现前读对应的 [ARCHITECTURE.md](engineering/ARCHITECTURE.md) 或 [API.md](protocol/API.md)。
5. 遇到未决问题时，在 [DECISIONS.md](planning/DECISIONS.md) 记录选项和结论，不在多个文档重复讨论。

## 文档维护规则

- 每条需求、决策和 contract 只在一个权威文档中完整描述，其他位置使用链接引用。
- 已观察到的事实、合理推断和计划目标必须明确区分，不把推断写成已验证事实。
- API 记录必须注明验证日期、证据级别和是否需要账号。
- 阶段边界只在 `PRODUCT.md` 维护，逐项功能状态只在 `FEATURES.md` 维护，milestone 状态只在 `PLAN.md` 更新。
- 文档中的示例不得包含真实密码、`Authorization`、Cookie、手机号或可恢复账号身份的数据。
- 实现与文档冲突时，先确认哪一方是当前要求，再在同一个改动中修正过期的一方。

## 状态词约定

| 状态   | 含义                                             |
| ------ | ------------------------------------------------ |
| 已确定 | 用户已经确认，或已成为项目约束                   |
| 已验证 | 已通过实际 Request/Response、构建或测试得到证据  |
| 有旁证 | 有官方资源或公开实现支持，但本项目尚未端到端验证 |
| 待确认 | 需要用户选择，选择不同会改变实现                 |
| 待验证 | 路线基本明确，但仍缺少成功样本或测试证据         |
| 暂缓   | 不在当前 milestone 中处理                        |
