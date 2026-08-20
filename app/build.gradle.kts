plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.HcmDz.ElecPilot"
    compileSdk = 37
    base.archivesName = "ElecPilot"
    buildToolsVersion = "36.1.0"

    defaultConfig {
        applicationId = "com.HcmDz.ElecPilot"
        minSdk = 29
        targetSdk = 36
        versionCode = 27
        versionName = "6.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
        }
    }

    signingConfigs {
        create("release") {
            val storeFilePath = providers.gradleProperty("RELEASE_STORE_FILE").getOrElse("")
            if (storeFilePath.isNotEmpty()) {
                storeFile = file(storeFilePath)
                storePassword = providers.gradleProperty("RELEASE_STORE_PASSWORD").get()
                keyAlias = "HcmDz"
                keyPassword = providers.gradleProperty("RELEASE_KEY_PASSWORD").get()
                enableV3Signing = true
            }
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
            ndk {
                abiFilters += listOf("arm64-v8a", "x86_64")
            }
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile != null) {
                signingConfig = releaseSigning
            }
            ndk {
                abiFilters += "arm64-v8a"
            }
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    sourceSets {
        getByName("androidTest") {
            assets.directories.add("$projectDir/schemas")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/INDEX.LIST",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1"
            )
        }
    }

    lint {
        checkDependencies = true
        disable += "RememberInComposition"
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // Compose BOM
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material.icons.extended)
    debugImplementation(libs.compose.ui.tooling)

    // Core
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.compose)

    // Room
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)

    // Apache POI (shadow jar from centic9/poi-on-android)
    implementation(files("libs/poishadow-all.jar"))

    // WorkManager
    implementation(libs.work.runtime)

    // DocumentFile (SAF)
    implementation(libs.documentfile)

    // Cloud Backup (rclone)
    implementation(libs.okhttp)

    implementation(libs.browser)

    // Tests
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.core.testing)

    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)

    debugImplementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.8.1") {
        version { strictly("1.8.1") }
    }
    debugImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1") {
        version { strictly("1.8.1") }
    }

    // Android security lints (CVE / security rules on dependencies)
    lintChecks("com.android.security.lint:lint:1.0.4")
}
