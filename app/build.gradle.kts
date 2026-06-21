import org.gradle.kotlin.dsl.implementation

// app/build.gradle.kts
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp") version "1.9.24-1.0.20"
}

android {
    namespace = "com.example.travel_footprint_android"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.travel_footprint_android"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

// ksp 配置
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // ================== 核心依赖 ==================
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.0")

    // ================== Compose (统一版本) ==================
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")
    
    // Material Icons 扩展（包含更多图标）
    implementation("androidx.compose.material:material-icons-extended")

    // Compose 运行时
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")

    // ================== Lifecycle ==================
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // ================== Hilt ==================
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // ================== Room ==================
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // ================== 图片加载 ==================
    implementation("io.coil-kt:coil-compose:2.5.0")

    // ================== DataStore ==================
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // ================== 协程 ==================
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ================== 序列化 ==================
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // ================== 网络请求 ==================
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // ================== Google Play Services ==================
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    // ================== 核心库去糖化 ==================
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // ================== Navigation Compose ==================
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // ================== Gson ==================
    implementation("com.google.code.gson:gson:2.10.1")

    // ================== 高德地图 SDK ==================
    implementation("com.amap.api:3dmap:10.0.600")
    implementation("com.amap.api:search:9.7.0")

    // ================== 权限库 ==================
    implementation("com.google.accompanist:accompanist-permissions:0.35.0-alpha")

    // ================== PAG 动效 ==================
    implementation("com.tencent.tav:libpag-enterprise:latest.release")

    // ================== ExifInterface ==================
    implementation("androidx.exifinterface:exifinterface:1.3.3")

    // ================== 测试 ==================
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    
    // Debug 依赖
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
