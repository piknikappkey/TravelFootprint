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

    android {
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
            isCoreLibraryDesugaringEnabled = true
        }

        kotlinOptions {
            jvmTarget = "17"
        }
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
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.0")

    // ================== Compose ==================
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // ================== Hilt ==================
    implementation("com.google.dagger:hilt-android:2.48")
    implementation("androidx.compose.foundation:foundation")
    implementation(libs.androidx.ui)
    implementation(libs.play.services.maps)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.constraintlayout)
    // ❌ 删除这行: implementation(libs.androidx.compose.remote.creation.core)
    ksp("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // ================== Room ==================
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // ================== 图片加载 ==================
    implementation("io.coil-kt:coil-compose:2.5.0")

    // ================== 协程 ==================
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ================== 序列化 ==================
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // ================== 测试 ==================
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // 定位服务需要
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // 核心库去糖化（支持Java 8+特性在旧设备上运行）
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // 文件提供器（用于分享）
    implementation("androidx.core:core-ktx:1.12.0")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Compose BOM (Bill of Materials)
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))

    // Compose 核心库
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Compose 生命周期相关
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // 用于 collectAsStateWithLifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    debugImplementation("androidx.compose.ui:ui-tooling")

    // Material Icons 扩展（包含更多图标）- 使用 BOM 管理版本
    implementation("androidx.compose.material:material-icons-extended")

    // 高德地图 SDK
    implementation("com.amap.api:3dmap:10.0.600")
//    // 3D地图
//    implementation("com.amap.api:3dmap:9.7.0"){
//        // 排除 location 相关重复类
//        exclude(group = "com.amap.api", module = "location")
//    }
//    // 定位
//    implementation("com.amap.api:location:6.4.3")
//    // 搜索
//    implementation("com.amap.api:search:9.7.0")

    // 权限库（用于运行时权限申请）
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    // 图片加载库（用于显示）
    implementation("io.coil-kt:coil-compose:2.5.0")

    // 权限请求库（可选，简化权限处理）
    implementation("com.google.accompanist:accompanist-permissions:0.35.0-alpha")

}