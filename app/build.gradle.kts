plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "me.slarker.yunsou"
    compileSdk = 35

    defaultConfig {
        applicationId = "me.slarker.yunsou"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.composeBom))
    implementation(libs.ui)
    implementation(libs.uiGraphics)
    implementation(libs.uiToolingPreview)
    debugImplementation(libs.uiTooling)
    implementation(libs.material3)
    implementation(libs.materialIcons)
    implementation(libs.activityCompose)
    implementation(libs.lifecycleViewmodelCompose)
    implementation(libs.lifecycleRuntimeCompose)
    implementation(libs.coreKtx)
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
    implementation(libs.kotlinxSerialization)
    implementation(libs.retrofitKotlinxSerialization)
    implementation(libs.hiltAndroid)
    ksp(libs.hiltCompiler)
    implementation(libs.hiltNavigationCompose)
}
