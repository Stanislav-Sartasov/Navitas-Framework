package test

import com.android.build.gradle.BaseExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Calendar
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

        target.tasks.register("rawProfile") {
            it.doLast {
                JSONGenerator().generate("${target.projectDir}/profileOutput/")
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
                        clearLogs()
                        runTest(path, testRunnerInfo)

                        val profileOutput = File("${target.projectDir}/profileOutput")
                        if (!profileOutput.exists()) profileOutput.mkdirs()

                        val printLogs = {
                            target.exec{
                                it.commandLine("$adb", "logcat", "-d", "-s", "TEST")
                                it.standardOutput = FileOutputStream("${profileOutput.absolutePath}/${path}.txt")
                            }
                        }

                        printLogs()
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

private class JSONGenerator {
    fun generate(directory: String) {
        val testList = JSONArray()

        File(directory).walk().forEach {
            if (it.isFile) {
                val testName = it.name.substringBefore(".txt")

                val testLogs = JSONArray()

                val data = it.readLines()
                for (i in 0 until data.size) {
                    val line = data[i]

                    if (!line.startsWith('-')) {
                        val entryLineList = line.split("\\s+".toRegex())

                        val methodName = entryLineList[8]
                        val processId = entryLineList[2].toInt()
                        val threadId = entryLineList[3].toInt()

                        val startDate = entryLineList[0]
                        val startTime = entryLineList[1]

                        //timestamp from January 1, 1970, 00:00:00 GMT
                        val timestamp = getTimestamp(startDate, startTime)

                        val isEntry = entryLineList[7] == "Entry"

                        val cpuDetails = JSONArray()
                        var kernelDetails = JSONObject()
                        var valuesDetails = JSONArray()

                        var parseIndex = 10
                        while (entryLineList[parseIndex] != "EndOfData") {
                            val item = entryLineList[parseIndex]

                            when {
                                item == ";" -> {
                                    kernelDetails["details"] = valuesDetails
                                    cpuDetails.add(kernelDetails)

                                    parseIndex += 1
                                }
                                item.startsWith("cpu") -> {
                                    kernelDetails = JSONObject()
                                    valuesDetails = JSONArray()

                                    val kernelIndex = item.substringAfter("cpu").toInt()
                                    kernelDetails["kernel"] = kernelIndex

                                    parseIndex += 1
                                }
                                else -> {
                                    val freq = entryLineList[parseIndex].toInt()
                                    val timeInState = entryLineList[parseIndex + 1].toInt()

                                    val valuesPair = JSONObject()
                                    valuesPair["frequency"] = freq
                                    valuesPair["timestamp"] = timeInState

                                    valuesDetails.add(valuesPair)

                                    parseIndex += 2
                                }
                            }
                        }

                        val brightness = entryLineList[parseIndex + 1].toInt()

                        val headerDetails = JSONObject()
                        headerDetails["timestamp"] = timestamp
                        headerDetails["processID"] = processId
                        headerDetails["threadID"] = threadId
                        headerDetails["methodName"] = methodName
                        headerDetails["isEntry"] = isEntry

                        val cpuComponent = JSONObject()
                        cpuComponent["component"] = "cpuTimeInStates"
                        cpuComponent["details"] = cpuDetails

                        val brightnessComponent = JSONObject()
                        brightnessComponent["component"] = "brightness"
                        brightnessComponent["details"] = brightness

                        val bodyArray = JSONArray()
                        bodyArray.add(cpuComponent)
                        bodyArray.add(brightnessComponent)

                        val logObject = JSONObject()
                        logObject["header"] = headerDetails
                        logObject["body"] = bodyArray

                        testLogs.add(logObject)
                    }
                }

                val testObject = JSONObject()
                testObject["testName"] = testName
                testObject["logs"] = testLogs

                testList.add(testObject)
            }
        }

        val json = JSONObject()
        json["tests"] = testList

        val file = FileWriter("$directory/logs.json")
        file.write(json.toJSONString())
        file.flush()
        file.close()
    }

    private fun getTimestamp(date: String, time: String): Long {
        val sdf = SimpleDateFormat("dd-MM-yyyy hh:mm:ss.SSS")
        val dateString = date + "-" + Calendar.getInstance().get(Calendar.YEAR) + " " + time
        return sdf.parse(dateString).time
    }
}