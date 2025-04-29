import com.android.build.api.dsl.Packaging

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.pdr_locator"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.pdr_locator"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
//            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")) // 同时支持32和64位
            // 或仅支持64位（推荐上架Google Play）：
             abiFilters.addAll(listOf("arm64-v8a", "x86_64"))
        }

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
//    packaging {
//        resources.excludes.add("META-INF/native-image/**/jnijavacpp/jni-config.json")
//        jniLibs.excludes.add("META-INF/native-image/**/jnijavacpp/jni-config.json")
//    }
}

dependencies {

//    implementation("org.deeplearning4j:deeplearning4j-core:1.0.0-beta6")

//    implementation("org.ejml:ejml-all:0.43") // 替换为最新版本
    implementation("org.rajawali3d:rajawali:1.1.970") // 替换为最新版本号

    implementation("org.pytorch:pytorch_android_lite:1.13.0")
    implementation("org.pytorch:pytorch_android_torchvision_lite:1.13.0")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.32")

    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.slf4j:slf4j-simple:2.0.9")

//    implementation ("ai.onnxruntime:onnxruntime-android:1.15.1")
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.16.0-rc1")
    // PyTorch Android
//    implementation("org.pytorch:pytorch_android:1.12.2")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.rajawali)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}


