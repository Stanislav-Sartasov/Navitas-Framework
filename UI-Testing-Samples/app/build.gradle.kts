plugins {
    id("com.android.application")
    kotlin("android")
    `maven-publish`
    id("NaviProf")
}

android {
    compileSdkVersion(29)
    defaultConfig {
        applicationId = "com.example.ui_testing_samples"
        minSdkVersion(19)
        targetSdkVersion(29)
        multiDexEnabled = true
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    //Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.41")

    //Support
    implementation("com.android.support.constraint:constraint-layout:1.1.3")
    implementation("androidx.appcompat:appcompat:1.0.2")
    implementation("androidx.core:core-ktx:1.0.2")
    implementation("com.android.support:multidex:1.0.3")

    //Local unit tests
    testImplementation("junit:junit:4.12")

    //Android unit tests
    androidTestImplementation("junit:junit:4.12")

    //AndroidX - instrumented testing
    androidTestImplementation("androidx.test:core:1.2.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test:rules:1.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.2.0")
    androidTestImplementation("androidx.test.espresso.idling:idling-concurrent:3.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-idling-resource:3.2.0")
}