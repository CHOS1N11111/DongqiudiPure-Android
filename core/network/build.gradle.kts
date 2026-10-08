plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

val authVersionName = providers.gradleProperty("dqd.auth.versionName").get()
val authVersionCode = providers.gradleProperty("dqd.auth.versionCode").get().toInt()
require(authVersionName.matches(Regex("[0-9]+(\\.[0-9]+)*")))
require(authVersionCode > 0)

android {
    namespace = "io.github.chos1n11111.dongqiudipure.core.network"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        buildConfigField("String", "DQD_AUTH_VERSION_NAME", "\"$authVersionName\"")
        buildConfigField("int", "DQD_AUTH_VERSION_CODE", authVersionCode.toString())
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures { buildConfig = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    api(project(":core:model"))

    implementation(libs.hilt.android)
    implementation(libs.okhttp)
    api(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)

    ksp(libs.hilt.compiler)

    testImplementation(project(":core:testing"))
    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mockwebserver)
}
