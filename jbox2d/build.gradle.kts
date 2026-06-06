plugins {
    id("com.android.library")
}

android {
    namespace = "org.jbox2d"
    compileSdk = 36

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

buildscript {
    repositories {
        mavenCentral()
        google()
    }
}
