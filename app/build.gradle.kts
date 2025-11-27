plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    // KAPT is still required for Data Binding only - Google has no plans to migrate Data Binding to KSP
    // Consider migrating to View Binding + Jetpack Compose to fully eliminate KAPT
    id("kotlin-kapt")
    alias(libs.plugins.ksp)
    alias(libs.plugins.navigation.safeargs)
    alias(libs.plugins.oss.licenses)
}

android {
    compileSdk = 36
    namespace = "goodtime.training.wod.timer"

    defaultConfig {
        applicationId = "goodtime.training.wod.timer"
        minSdk = 26
        targetSdk = 36
        versionCode = 34
        versionName = "0.9.13"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    // Core library desugaring for Java 8+ features (needed for java.time)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // AndroidX Core
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)

    // Lifecycle components (lifecycle-extensions deprecated - individual components used instead)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Preferences
    implementation(libs.androidx.preference.ktx)

    // Dependency Injection - Kodein
    implementation(libs.kodein.di.jvm)
    implementation(libs.kodein.di.framework.android)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // JSON Parsing
    implementation(libs.gson)

    // CSV Parsing for SmartWOD import
    implementation(libs.kotlin.csv.jvm)

    // Event Bus
    implementation(libs.eventbus)

    // UI Components
    implementation(libs.loopinglayout)
    implementation(libs.konfetti)

    // Charts for Statistics
    implementation(libs.mp.android.chart)

    // Google Play Services
    implementation(libs.play.review.ktx)
    implementation(libs.play.app.update.ktx)
    implementation(libs.play.services.oss.licenses)
    implementation(libs.billing.ktx)
}
