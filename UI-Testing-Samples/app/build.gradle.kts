import java.io.ByteArrayOutputStream
import java.util.regex.Pattern

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

tasks.register("monkeyRunner") {
    val apkPath: String? =
        if (project.hasProperty("apk_path")) project.property("apk_path") as String else null

    val scriptPath: String? =
        if (project.hasProperty("script_path")) project.property("script_path") as String else null

    if (apkPath != null && scriptPath != null) {
        runCommand("monkeyrunner", scriptPath)
    } else {
        runCommand("echo", "Error: apk_path and script_path must be passed!")
    }
}

tasks.register("espressoRunner") {
    val apkPath: String? =
        if (project.hasProperty("apk_path")) project.property("apk_path") as String else null

    val testApkPath: String? =
        if (project.hasProperty("test_apk_path")) project.property("test_apk_path") as String else null

    val testPaths: List<String>? =
        if (project.hasProperty("test_paths")) {
            val paths = project.property("test_paths") as String
            paths.split(",")
        } else null

    if (apkPath != null && testApkPath != null && testPaths != null) {
        installApk(apkPath)
        installApk(testApkPath)

        val apkInfo = getApkInfo(apkPath)
        val testApkInfo = getApkInfo(testApkPath)

        for (path in testPaths) {
            runTest(path, apkInfo, testApkInfo)
        }
    } else {
        runCommand("echo", "Error: apk_path, test_apk_path and test_paths must be passed!")
    }
}

//TODO: how to put it in a separate script???
class ApkInfo(_aaptOutput: String) {
    val appPackage: String

    init {
        val matcher = appPackagePattern.matcher(_aaptOutput)
        matcher.find()
        appPackage = matcher.group(1)
    }
}

val appPackagePattern = Pattern.compile("name='([a-zA-Z0-9_.]+)'", Pattern.MULTILINE)

val getApkInfo = { apkPath: String ->
    val out = ByteArrayOutputStream()
    exec {
        standardOutput = out
        workingDir("./..")
        commandLine("aapt", "dump", "badging", apkPath)
    }
    ApkInfo(out.toString())
}

val runTest = { testPath: String, apkInfo: ApkInfo, testApkInfo: ApkInfo ->
    runCommand("adb", "shell", "am", "instrument", "-w", "-e", "class", "${apkInfo.appPackage}.$testPath", "${testApkInfo.appPackage}/androidx.test.runner.AndroidJUnitRunner")
}

val installApk = { apkPath: String ->
    runCommand("echo", "APK Install: ", apkPath)
    runCommand("adb", "install", "-r", apkPath)
}

fun runCommand(vararg args: String) {
    exec {
        workingDir("./..")
        commandLine(*args)
    }
}