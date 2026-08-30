plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.musicwave"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.musicwave"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        buildConfigField("String", "EXTRACTOR_BASE_URL", "\"https://moriextractor.koyeb.app\"")
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("standard") {
            dimension = "distribution"
            buildConfigField("String", "DISTRIBUTION", "\"standard\"")
        }
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
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/CONTRIBUTORS.md"
            excludes += "META-INF/LICENSE.md"
        }
        jniLibs {
            useLegacyPackaging = false
        }
    }

    lint {
        warningsAsErrors = false
        abortOnError = false
        checkDependencies = false
    }
}

dependencies {
    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.activity)
    implementation(libs.media)
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.navigation)

    // Media3
    implementation(libs.media3.common)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.animation)
    debugImplementation(platform(libs.compose.bom))
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.tooling.preview)

    // Hilt
    implementation(libs.hilt)
    implementation(libs.hilt.navigation)
    ksp(libs.hilt.compiler)

    // Kotlin
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.kotlin.serialization)

    // Ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.json)

    // OkHttp
    implementation(libs.okhttp)
    implementation(libs.okhttp.dnsoverhttps)

    // Image Loading
    implementation(libs.coil)

    // Testing
    testImplementation(libs.junit)

    // Desugaring
    coreLibraryDesugaring(libs.desugaring)
}