import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    kotlin("native.cocoapods")
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvmToolchain(17)

    cocoapods {
        summary = "EcoGarden Game"
        homepage = "https://github.com/rafarg/EcoGardenGame"
        version = "1.0"
        ios.deploymentTarget = "14.1"
        pod("GoogleSignIn")
        framework {
            baseName = "composeApp"
            isStatic = true
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    )

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.google.auth)
            implementation(libs.play.services.location)
            implementation(libs.ktor.client.okhttp)

            // BOM de Firebase
            implementation(project.dependencies.platform("com.google.firebase:firebase-bom:33.2.0"))

            implementation(libs.androidx.appcompat)
            implementation("androidx.core:core-splashscreen:1.0.1")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.datastore.preferences.core)
            implementation(libs.kotlinx.datetime)
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

            // Firebase GitLive
            implementation(libs.firebase.auth)
            implementation(libs.firebase.firestore)

            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            // Extended icons if needed
            implementation(compose.materialIconsExtended)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "com.rafarg.ecogardengame"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.rafarg.ecogardengame"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

// The Compose resource generator adds generated accessor/collector code directly into
// each target's Kotlin source set, which makes ktlint's KMP source-set discovery pick
// it up too. Pin every per-source-set task to its real `src/<name>/kotlin` directory
// instead of whatever Compose appended to the source set at configuration time.
val ktlintSourceSetTaskName = Regex("""run(?:KtlintFormat|KtlintCheck)Over(.+)SourceSet""")
tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask>().configureEach {
    ktlintSourceSetTaskName.find(name)?.let { match ->
        val sourceSetName = match.groupValues[1].replaceFirstChar { it.lowercase() }
        setSource(project.fileTree("src/$sourceSetName/kotlin") { include("**/*.kt") })
    }
}

detekt {
    source.setFrom(
        files(
            "src/commonMain/kotlin",
            "src/androidMain/kotlin",
            "src/commonTest/kotlin",
        ),
    )
    buildUponDefaultConfig = true
    baseline = file("detekt-baseline.xml")
}
