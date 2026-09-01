plugins {
    alias(libs.plugins.kotlin.jvm)
}

// ┌──────────────────────────────────────────────────────────────────────────┐
// │  临时模块。接入真实数据后整个删除。                                        │
// │                                                                          │
// │  本模块存在的唯一目的：让 UI 层在 :core:network / :core:data 就绪之前      │
// │  可以独立开发与验收。所有假数据集中在这里，而不是散落在各 feature 里，      │
// │  这样「删除假数据」是一个删除模块的动作，不是一次全仓库搜索。              │
// │                                                                          │
// │  删除步骤见 docs/engineering/BACKEND-CONTRACT-TODO.md                     │
// └──────────────────────────────────────────────────────────────────────────┘
kotlin {
    jvmToolchain(17)
}

dependencies {
    api(project(":core:model"))
}
