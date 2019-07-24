plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdkVersion(28)
    defaultConfig {
        applicationId = "com.example.ui_testing_samples"
        minSdkVersion(19)
        targetSdkVersion(28)
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
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    //Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.41")

    //Support
    implementation("com.android.support:appcompat-v7:28.0.0")
    implementation("com.android.support.constraint:constraint-layout:1.1.3")

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

tasks.register<Exec>("monkeyRunner") {
    val apkPath: String? =
        if (project.hasProperty("apk_path")) project.property("apk_path") as String else null

    val scriptPath: String? =
        if (project.hasProperty("script_path")) project.property("script_path") as String else null

    workingDir("./..")

    if (apkPath != null && scriptPath != null) {
        commandLine("monkeyrunner", scriptPath)
    } else {
        commandLine("echo", "Error: apk_path and script_path must be passed!")
    }
}

tasks.register<Exec>("espressoRunner") {
    val appPackage: String? =
        if (project.hasProperty("app_package")) project.property("app_package") as String else null

    val apkPath: String? =
        if (project.hasProperty("apk_path")) project.property("apk_path") as String else null

    val testApkPath: String? =
        if (project.hasProperty("test_apk_path")) project.property("test_apk_path") as String else null

    val testPath: String? =
        if (project.hasProperty("test_path")) project.property("test_path") as String else null

    workingDir("./..")

    if (appPackage != null && apkPath != null && testApkPath != null && testPath != null) {
        dependsOn("installApk")
        tasks["installApk"].inputs.properties["apk_path"] = apkPath
        tasks["installApk"].inputs.properties["test_apk_path"] = testApkPath

        //TODO: how to extract app package from apk???
        //TODO: how to run several tests???

        commandLine("adb", "shell", "am", "instrument", "-w", "-e", "class", "$appPackage.$testPath", "$appPackage.test/androidx.test.runner.AndroidJUnitRunner")
    } else {
        commandLine("echo", "Error: app_package, apk_path, test_apk_path, test_path must be passed!")
    }
}

tasks.register<Exec>("installApk") {
    val apkPath: String? = if (project.hasProperty("apk_path")) project.property("apk_path") as String else null
    val testApkPath: String? = if (project.hasProperty("test_apk_path")) project.property("test_apk_path") as String else null

    workingDir("./..")

    if (apkPath != null) {
        commandLine("adb", "install", "-r", apkPath)
    } else {
        commandLine("echo", "Error: apk path must be passed!")
    }

    if (testApkPath != null) {
        commandLine("adb", "install", "-r", testApkPath)
    } else {
        commandLine("echo", "Error: test apk path must be passed!")
    }
}