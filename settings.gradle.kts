pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "DongqiudiPure"

// Module 按实际 vertical slice 增加（DECISIONS.md D-013）。
include(":app")

include(":core:model")
include(":core:designsystem")

// 临时模块，接入真实数据后删除。见 docs/engineering/BACKEND-CONTRACT-TODO.md
include(":core:sampledata")

include(":feature:home")
include(":feature:article")
include(":feature:matches")
include(":feature:rankings")
include(":feature:entities")
include(":feature:search")
include(":feature:account")
include(":feature:settings")
