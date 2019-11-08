import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.util.regex.Pattern
import java.io.FileWriter

plugins {
    id("com.android.application")
    kotlin("android")
    id("profilingPlugin")
}

android {
    compileSdkVersion(29)
    defaultConfig {
        applicationId = "com.example.ui_testing_samples"
        minSdkVersion(19)
        targetSdkVersion(29)
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
    implementation("com.android.support.constraint:constraint-layout:1.1.3")
    implementation("androidx.appcompat:appcompat:1.0.2")
    implementation("androidx.core:core-ktx:1.0.2")

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

val adb: File = android.adbExecutable

val profileOutput = File("$projectDir/profileOutput")
if (!profileOutput.exists()) profileOutput.mkdirs()

tasks.register("profile") {
    dependsOn("assembleDebug", "assembleDebugAndroidTest")
    finalizedBy("clean")

    doLast {
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

            val testRunnerInfo = getTestRunnerInfo(testApkPath)

            clearLogs()

            for (path in testPaths) {
                runTest(path, testRunnerInfo)
            }

            printLogs()
            printStats()
            cleanBuild()

            val logsFile = File("$projectDir/profileOutput/logs.txt")
            parseToCsv(logsFile)
        } else {
            runCommand("echo", "Error: apk_path, test_apk_path and test_paths must be passed!")
        }
    }
}

val nameRegex = "([a-zA-Z0-9_\\.]+)"
val targetPackagePattern: Pattern = Pattern.compile("android:targetPackage.*=\"$nameRegex\"")
val testPackagePattern: Pattern = Pattern.compile("package=\"$nameRegex\"")
val runnerNamePattern: Pattern = Pattern.compile("android:name.*=\"$nameRegex\"")

class TestRunnerInfo(aaptOutput: String) {
    val targetPackage: String
    val testPackage: String
    val runnerName: String

    init {
        var matcher = testPackagePattern.matcher(aaptOutput).apply { find() }
        testPackage = matcher.group(1)

        val tail = aaptOutput.substringAfter("instrumentation")

        matcher = targetPackagePattern.matcher(tail).apply { find() }
        targetPackage = matcher.group(1)

        matcher = runnerNamePattern.matcher(tail).apply { find() }
        runnerName = matcher.group(1)
    }
}

val getTestRunnerInfo = { apkPath: String ->
    val out = ByteArrayOutputStream()
    exec {
        standardOutput = out
        workingDir("./..")
        commandLine("aapt", "dump", "xmltree", apkPath, "AndroidManifest.xml")
    }
    TestRunnerInfo(out.toString())
}

val runTest = { testPath: String, info: TestRunnerInfo ->
    runCommand("$adb", "shell", "am", "instrument", "-w", "-e", "class", "${info.targetPackage}.$testPath", "${info.testPackage}/${info.runnerName}")//androidx.test.runner.AndroidJUnitRunner")
}

val installApk = { apkPath: String ->
    runCommand("echo", "APK Installing: ", apkPath)
    runCommand("$adb", "install", "-r", apkPath)
}

val clearLogs = {
    exec{
        isIgnoreExitValue = true //needs to be fixed somehow
        commandLine("$adb", "logcat", "-c")
        commandLine("$adb", "logcat", "-c")
    }
}

val printStats = {
    exec {
        commandLine("$adb", "shell", "dumpsys", "batterystats")
        standardOutput = FileOutputStream("${profileOutput.absolutePath}/batterystats.txt")
    }
}

val printLogs = {
    exec{
        commandLine("$adb", "logcat", "-d", "-s", "TEST")
        standardOutput = FileOutputStream("${profileOutput.absolutePath}/logs.txt")
    }
}

val cleanBuild = {
    delete{
        delete(buildDir)
    }
}

fun runCommand(vararg args: String) {
    exec {
        workingDir("./..")
        commandLine("cmd", "/c", *args)
    }
}

fun parseToCsv(input: File) {
    val csvHeader = "time,thread_id,enter/exit,method_name"
    val fileWriter = FileWriter("$projectDir/profileOutput/parsedLogs.csv")
    fileWriter.append(csvHeader)
    fileWriter.append('\n')

    val data = input.readLines()
    for (line in data) {
        if (!line.startsWith('-')) {
            val dataList = line.split("\\s+".toRegex())
            val time = dataList[1]
            val threadId = dataList[3]
            val entryOrExit = dataList[7]
            val methodName = dataList[8]

            fileWriter.append("$time,$threadId,$entryOrExit,$methodName")
            fileWriter.append('\n')
        }
    }
    fileWriter.close()
}

tasks.register("Test") {

    val profileOutput = File("${projectDir}/profileOutput")

    doLast {
        exec {
            commandLine("$adb", "logcat", "-d")
            standardOutput = FileOutputStream("${profileOutput.absolutePath}/dumpstateTime.txt")
        }
    }
    doLast {
        //File("${profileOutput.absolutePath}/log.txt").writeText(/*"${profileOutput.absolutePath}/batterystats.txt", */(("${profileOutput.absolutePath}/dmesg.txt", "${profileOutput.absolutePath}/dumpstateTime.txt").{it.readText}.join("\n"))
        File("${profileOutput.absolutePath}/log.txt").writeBytes(File("${profileOutput.absolutePath}/dmesg.txt").readBytes())
        File("${profileOutput.absolutePath}/log.txt").appendText("\n")
        File("${profileOutput.absolutePath}/log.txt").appendBytes(File("${profileOutput.absolutePath}/dumpstateTime.txt").readBytes())

        val resFile = File("${profileOutput.absolutePath}/res.txt")

        for (line in File(("${profileOutput.absolutePath}/log.txt")).readLines()) {
            if (line.contains("currnt") || line.contains("volt") || line.contains("dumpstate")) {
                resFile.appendText(line + System.lineSeparator())
            }
        }

        val lines = resFile.readLines()
        val dumpTime = lines.findLast { i -> i.contains("dumpstate: done") }?.split(" ")?.get(1)
        val dumpSec =
            lines.findLast { i -> i.contains("Service 'dumpstate'") }?.replace("]", "")?.replace("[", " ")?.split("\\s+".toRegex())
                ?.get(1)
        var prevTime = " "
        var lineToWrite = ""
        val logsWithTime = File("${profileOutput.absolutePath}/logsWithTime.txt")

        for (l in lines.filter { i -> i.contains("get_") }) {
            val line = l.replace("[", " ").replace("]", "")

            val timeDifference =
                (line.split("\\s+".toRegex())[1]).toDouble() - (dumpSec!!.toDouble())

            var hrs = ((timeDifference.toInt() / (24 * 60)) + (dumpTime?.split(":")?.get(0)?.toInt()
                ?: 0)) % 24

            var min = ((timeDifference.toInt() / 60) % 60 + (dumpTime?.split(":")?.get(1)?.toInt()
                ?: 0))
            var sec = (timeDifference.toInt() % 60 + ((dumpTime?.split(":")?.get(2)?.substring(
                0,
                2
            ))?.toInt()
                ?: 0))
            var mcsec =
                ((timeDifference * 1000).toInt() % 1000 + (dumpTime?.split(":")?.get(2)?.substring(3)?.toInt()
                    ?: 0))

            if (mcsec < 0) {
                sec -= 1
                mcsec += 1000
            } else if (mcsec > 999) {
                sec += 1
                mcsec %= 1000
            }
            if (sec < 0) {
                min -= 1
                sec += 60
            } else if (sec > 59) {
                min += 1
                sec %= 60
            }
            if (min < 0) {
                hrs -= 1
                min += 60
            } else if (min > 59) {
                hrs += 1
                hrs %= 24
                min %= 60
            }

            val time = "$hrs:$min:$sec.$mcsec"

            if (time == prevTime) {
                if (line.contains("volt")) {
                    lineToWrite += " voltage =" + line.split("=")[1]
                } else if (line.contains("currnt")) {
                    lineToWrite += " amperage =" + line.split("=")[1]
                }
            } else {
                logsWithTime.appendText(lineToWrite + System.lineSeparator())
                lineToWrite = time
                if (line.contains("volt")) {
                    lineToWrite += " voltage =" + line.split("=")[1]
                } else if (line.contains("currnt")) {
                    lineToWrite += " amperage =" + line.split("=")[1]
                }
            }
            prevTime = time
        }
    }
}