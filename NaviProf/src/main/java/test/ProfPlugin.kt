package test

import com.android.build.gradle.BaseExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.util.regex.Pattern

open class ProfPlugin : Plugin<Project>{
    override fun apply(target: Project){
        val extension = target.extensions.create("profilingPlugin", InstrExtension::class.java)
        val android = target.extensions.findByName("android") as BaseExtension
        android.registerTransform(Transformer(android, extension))

        val adb: File = android.adbExecutable
        fun runCommand(vararg args: String) {
            target.exec {
                it.workingDir("./..")
                it.commandLine(*args)
            }
        }

        target.tasks.register("rawProfile") { it ->
            val profileOutput = File("${target.projectDir}/profileOutput")
            if (!profileOutput.exists()) profileOutput.mkdirs()

            val printLogs = {
                target.exec{
                    it.commandLine("$adb", "logcat", "-d", "-s", "TEST")
                    it.standardOutput = FileOutputStream("${profileOutput.absolutePath}/logs.txt")
                }
            }

            it.doLast {
                printLogs()
                val logsFile = File("${target.projectDir}/profileOutput/logs.txt")
                parseToCsv(logsFile, target)
            }
        }

        target.tasks.named("rawProfile").configure { it.dependsOn("profileBuild") }
        target.tasks.named("rawProfile").configure { it.dependsOn("runTests") }
        target.tasks.named("rawProfile").configure { it.finalizedBy("clean") }

        target.tasks.register("runTests") { it ->
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

            it.doLast {
                clearLogs()

                val testApkPath: String? =
                    if (it.project.hasProperty("test_apk_path")) it.project.property("test_apk_path") as String else null

                val testPaths: List<String>? =
                    if (it.project.hasProperty("test_paths")) {
                        val paths = it.project.property("test_paths") as String
                        paths.split(",")
                    } else null

                if (testPaths != null && testApkPath != null) {
                    val testRunnerInfo = getTestRunnerInfo(testApkPath)

                    for (path in testPaths) {
                        runTest(path, testRunnerInfo)
                    }
                }
                else {
                    runCommand("echo", "\n!Error at task [runTests]: test_apk_path and test_paths should be passed\n")
                    throw GradleException("Arguments are not passed")
                }
            }
        }

        target.tasks.register("profileBuild") { it ->
            it.dependsOn("assembleDebug", "assembleDebugAndroidTest")

            val installApk = { apkPath: String ->
                runCommand("echo", "APK Installing: ", apkPath)
                runCommand("$adb", "install", "-r", apkPath)
            }

            it.doLast {
                val apkPath: String? =
                    if (it.project.hasProperty("apk_path"))
                        it.project.property("apk_path") as String
                    else null

                val testApkPath: String? =
                    if (it.project.hasProperty("test_apk_path"))
                        it.project.property("test_apk_path") as String
                    else null

                if (apkPath != null && testApkPath != null) {
                    installApk(apkPath)
                    installApk(testApkPath)
                }
                else {
                    runCommand("echo", "\n!Error at task [profileBuild]: apk_path and test_apk_path should be passed\n")
                    throw GradleException("Arguments are not passed")
                }
            }
        }
    }
}

open class InstrExtension {
    var applyFor: Array<String>? = null
}

const val nameRegex = "([a-zA-Z0-9_\\.]+)"
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
    val fileWriter = FileWriter("parsedLogs.csv")
    val header = "MethodName,StartTime,EndTime,ThreadID,Energy"
    fileWriter.append(header)
    fileWriter.append('\n')

    val data = input.readLines()
    for (i in 0 until data.size) {
        val line = data[i]

        if (!line.startsWith('-')) {
            val entryLineList = line.split("\\s+".toRegex())
            var dataString = ""

            if (entryLineList[7] != "Entry") {
                continue
            } else {
                val methodName = entryLineList[8]
                val threadId = entryLineList[3]

                val exitLine = findExitLine(data, i, methodName)
                val exitLineList = exitLine!!.split("\\s+".toRegex())

                val startTime = entryLineList[1]
                val endTime = exitLineList[1]

                var energy = 0

                var parseIndex = 10
                while (entryLineList[parseIndex] != "EndOfData") {
                    val freq = entryLineList[parseIndex].toInt()
                    val timeAtEntry = entryLineList[parseIndex + 1].toInt()
                    val timeAtExit = exitLineList[parseIndex + 1].toInt()

                    energy += freq * (timeAtExit - timeAtEntry)

                    parseIndex += 2
                }

                dataString += "$methodName,$startTime,$endTime,$threadId,$energy"
            }

            fileWriter.append(dataString)
            fileWriter.append('\n')
        }
    }
    fileWriter.close()
}

fun findExitLine(data: List<String>, startingIndex: Int, methodName: String): String? {
    for (i in (startingIndex + 1) until data.size) {
        val line = data[i]

        if (!line.startsWith('-')) {
            val dataList = line.split("\\s+".toRegex())
            if (dataList[7] == "Exit" && dataList[8] == methodName)
                return line
        }
    }
    return null
}