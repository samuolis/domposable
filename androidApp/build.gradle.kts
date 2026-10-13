import java.util.Properties
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}
val release = Properties().apply { rootProject.file("version.properties").inputStream().use(::load) }
val releaseVersion = release.getProperty("VERSION")
require(Regex("""(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)""").matches(releaseVersion)) {
    "VERSION must use MAJOR.MINOR.PATCH"
}
val versionParts = releaseVersion.split('.').map { it.toLong() }
require(versionParts[0] <= 21_000 && versionParts[1] < 1_000 && versionParts[2] < 100) {
    "Version exceeds the supported Android version-code range"
}
val releaseVersionCode = versionParts[0] * 100_000 + versionParts[1] * 100 + versionParts[2]
require(releaseVersionCode <= 2_100_000_000) { "Version exceeds Android's versionCode limit" }
android {
    namespace = "io.github.samuolis.domposable.demo"
    compileSdk { version = release(37) { minorApiLevel = 2 } }
    defaultConfig {
        applicationId = "io.github.samuolis.domposable.demo"
        minSdk = 24
        targetSdk = 37
        versionCode = maxOf(1, releaseVersionCode.toInt())
        versionName = releaseVersion
    }
    buildFeatures { compose = true }
    signingConfigs {
        create("release") {
            val key = providers.environmentVariable("ANDROID_KEYSTORE_PATH").orNull
            if (key != null) {
                storeFile = file(key)
                storePassword = providers.environmentVariable("ANDROID_KEYSTORE_PASSWORD").get()
                keyAlias = providers.environmentVariable("ANDROID_KEY_ALIAS").get()
                keyPassword = providers.environmentVariable("ANDROID_KEY_PASSWORD").get()
            }
        }
    }
    buildTypes { release { signingConfig = signingConfigs.getByName("release") } }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
dependencies {
    implementation(project(":demo"))
    implementation(libs.activity.compose)
}
