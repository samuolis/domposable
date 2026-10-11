import java.util.Properties
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}
val release = Properties().apply { rootProject.file("version.properties").inputStream().use(::load) }
android {
    namespace = "io.github.samuolis.domposable.demo"
    compileSdk { version = release(37) { minorApiLevel = 2 } }
    defaultConfig {
        applicationId = "io.github.samuolis.domposable.demo"
        minSdk = 24
        targetSdk = 37
        versionCode = maxOf(1, release.getProperty("BUILD_NUMBER").toInt())
        versionName = release.getProperty("VERSION")
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
