plugins {
    alias(libs.plugins.kotlin.jvm)
}

// :core:model 只依赖 Kotlin 标准库。做成纯 JVM module 而非 Android library，
// 是为了让「不包含 endpoint path、序列化 annotation 或 Android Context」
// 这条规则由编译器强制，而不是靠 review 自觉（ARCHITECTURE.md §4）。
kotlin {
    jvmToolchain(17)
}
