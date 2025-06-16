plugins {
    id("com.android.application")
    kotlin("android") version "1.8.10"
}

android {
    namespace = "cn.itcast.hilink"
    compileSdk = 34

    defaultConfig {
        applicationId = "cn.itcast.hilink"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    configurations.all {
        resolutionStrategy {
            force(
                "org.jetbrains.kotlin:kotlin-stdlib:1.8.10",
                "org.jetbrains.kotlin:kotlin-stdlib-common:1.8.10",
                "org.jetbrains.kotlin:kotlin-reflect:1.8.10"
            )
            exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk7")
            exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
        }
    }
}

dependencies {
    // 基础库
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.7.2")
    implementation(libs.jsoup)
    // 网络库 (NanoHTTPD)
    implementation("org.nanohttpd:nanohttpd:2.3.1") {
        exclude(module = "nanohttpd-webserver")
    }

    // ZXing 二维码
    implementation("com.journeyapps:zxing-android-embedded:4.3.0") {
        exclude(group = "com.android.support")
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation("com.google.zxing:core:3.5.2")

    // 图片加载库 Glide
    implementation("com.github.bumptech.glide:glide:4.15.1") {
        exclude(group = "com.android.support")
    }
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    // 单元测试
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.google.android.gms:play-services-location:21.0.1")
}
