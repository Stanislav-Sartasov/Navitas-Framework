package test

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.util.regex.Pattern

open class InstrPlugin : Plugin<Project>{
    override fun apply(target: Project){
        val extension = target.extensions.create("instrumentor", InstrExtension::class.java)
        val android = target.extensions.findByName("android") as BaseExtension
        android.registerTransform(Transformer(android, extension))

        val adb: File = android.adbExecutable
        fun runCommand(vararg args: String) {
            target.exec {
                it.workingDir("./..")
                it.commandLine("cmd", "/c", *args)
            }
        }

        target.tasks.register("Profile") { it ->
            it.dependsOn("assembleDebug", "assembleDebugAndroidTest")
            it.finalizedBy("clean")

            val profileOutput = File("${target.projectDir}/profileOutput")
            if (!profileOutput.exists()) profileOutput.mkdirs()

            val installApk = { apkPath: String ->
                runCommand("echo", "APK Installing: ", apkPath)
                runCommand("$adb", "install", "-r", apkPath)
            }

            val clearLogs = {
                target.exec{
                    it.isIgnoreExitValue = true //needs to be fixed somehow
                    it.commandLine("$adb", "logcat", "-c")
                    it.commandLine("$adb", "logcat", "-c")
                }
            }
            val getTestRunnerInfo = { apkPath: String ->
                val out = ByteArrayOutputStream()
                target.exec {
                    it.standardOutput = out
                    it.workingDir("./..")
                    it.commandLine("aapt", "dump", "xmltree", apkPath, "AndroidManifest.xml")
                }
                TestRunnerInfo(out.toString())
            }

            val runTest = { testPath: String, info: TestRunnerInfo ->
                runCommand("$adb", "shell", "am", "instrument", "-w", "-e", "class", "${info.targetPackage}.$testPath", "${info.testPackage}/${info.runnerName}")//androidx.test.runner.AndroidJUnitRunner")
            }

            val printStats = {
                target.exec {
                    it.commandLine("$adb", "shell", "dumpsys", "batterystats")
                    it.standardOutput = FileOutputStream("${profileOutput.absolutePath}/batterystats.txt")
                }
            }

            val printLogs = {
                target.exec{
                    it.commandLine("$adb", "logcat", "-d", "-s", "TEST")
                    it.standardOutput = FileOutputStream("${profileOutput.absolutePath}/logs.txt")
                }
            }

            val cleanBuild = {
                target.delete{
                    it.delete(target.buildDir)
                }
            }

            it.doLast {
                val apkPath: String? =
                    if (it.project.hasProperty("apk_path")) it.project.property("apk_path") as String else null

                val testApkPath: String? =
                    if (it.project.hasProperty("test_apk_path")) it.project.property("test_apk_path") as String else null

                val testPaths: List<String>? =
                    if (it.project.hasProperty("test_paths")) {
                        val paths = it.project.property("test_paths") as String
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
                    val logsFile = File("${target.projectDir}/profileOutput/logs.txt")
                    parseToCsv(logsFile, target)
                } else {
                    runCommand("echo", "Error: apk_path, test_apk_path and test_paths must be passed!")
                }
            }

        }

    }
}

open class InstrExtension {
    var applyFor: Array<String>? = null
}

val nameRegex = "([a-zA-Z0-9_\\.]+)"
val targetPackagePattern: Pattern = Pattern.compile("android:targetPackage.*=\"$nameRegex\"")
val testPackagePattern: Pattern = Pattern.compile("package=\"$nameRegex\"")
val runnerNamePattern: Pattern = Pattern.compile("android:name.*=\"$nameRegex\"")

open class TestRunnerInfo(aaptOutput: String) {
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

fun parseToCsv(input: File, target: Project) {
    val csvHeader = "time,thread_id,enter/exit,method_name"
    val fileWriter = FileWriter("${target.projectDir}/profileOutput/parsedLogs.csv")
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