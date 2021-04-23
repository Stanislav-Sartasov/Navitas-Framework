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
import java.util.*
import java.util.regex.Pattern

open class ProfPlugin : Plugin<Project> {
    override fun apply(target: Project) {
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

        val testConfiguration = {
            target.exec {
                it.commandLine("$adb", "shell", "dumpsys", "battery", "unplug")
                it.commandLine("$adb", "shell", "dumpsys", "battery", "set", "ac", "0")
                it.commandLine("$adb", "shell", "dumpsys", "battery", "set", "usb", "0")
                it.commandLine("$adb", "shell", "dumpsys", "battery", "set", "wireless", "0")
                it.commandLine("$adb", "shell", "dumpsys", "battery", "set", "status", "0")

                //it.commandLine("$adb", "shell", "svc", "wifi", "enable")
                //it.commandLine("$adb", "shell", "svc", "bluetooth", "enable")
            }
        }

        val prepareLogs = {
            target.exec{
                it.isIgnoreExitValue = true //needs to be fixed somehow
                it.commandLine("$adb", "logcat", "-c")
                it.commandLine("$adb", "logcat", "-c")

                //it.commandLine("$adb", "shell", "dumpsys", "batterystats", "--reset")
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

        val runTestClass = { testPath: String, info: TestRunnerInfo ->
            runCommand("$adb", "shell", "am", "instrument", "-w", "-e",
                "class", "${info.targetPackage}.$testPath", "${info.testPackage}/${info.runnerName}")//androidx.test.runner.AndroidJUnitRunner")
        }

        val runTestMethod = { testPath: String, info: TestRunnerInfo, methodName: String ->
            runCommand("$adb", "shell", "am", "instrument", "-w", "-e",
                "class", "${info.targetPackage}.$testPath#$methodName", "${info.testPackage}/${info.runnerName}")
        }

        val cpuLogsOfMethod = { profileOutput: File, pathName: String, method: String ->
            target.exec{
                it.commandLine("$adb", "logcat", "-d", "-s", "TEST")
                it.standardOutput = FileOutputStream("${profileOutput.absolutePath}/$pathName.$method.txt")
            }
        }

        val wifiLogsOfMethod = { profileOutput: File, pathName: String, method: String ->
            target.exec{
                it.commandLine("$adb", "shell", "dumpsys", "batterystats", "|", "grep", "-m", "1", "\"Wifi:\"")
                it.standardOutput = FileOutputStream("${profileOutput.absolutePath}/$pathName.$method.txt", true)
            }
        }

        val bluetoothLogsOfMethod = { profileOutput: File, pathName: String, method: String ->
            target.exec{
                it.commandLine("$adb", "shell", "dumpsys", "batterystats", "|", "grep", "-m", "1", "\"Bluetooth:\"")
                it.standardOutput = FileOutputStream("${profileOutput.absolutePath}/$pathName.$method.txt", true)
            }
        }

        val cpuLogs = { profileOutput: File, path: String ->
            target.exec {
                it.commandLine("$adb", "logcat", "-d", "-s", "TEST")
                it.standardOutput = FileOutputStream("${profileOutput.absolutePath}/${path}.txt")
            }
        }

        val wifiLogs = { profileOutput: File, path: String ->
            target.exec{
                it.commandLine("$adb", "shell", "dumpsys", "batterystats", "|", "grep", "-m", "1", "\"Wifi:\"")
                it.standardOutput = FileOutputStream("${profileOutput.absolutePath}/${path}.txt", true)
            }
        }

        val bluetoothLogs = { profileOutput: File, path: String ->
            target.exec{
                it.commandLine("$adb", "shell", "dumpsys", "batterystats", "|", "grep", "-m", "1", "\"Bluetooth:\"")
                it.standardOutput = FileOutputStream("${profileOutput.absolutePath}/${path}.txt", true)
            }
        }

        val defaultConfiguration = {
            target.exec{
                it.commandLine("$adb", "shell", "dumpsys", "battery", "set", "ac", "1")
                it.commandLine("$adb", "shell", "dumpsys", "battery", "set", "usb", "1")
                it.commandLine("$adb", "shell", "dumpsys", "battery", "set", "wireless", "1")
                it.commandLine("$adb", "shell", "dumpsys", "battery", "set", "status", "1")
            }
        }

        val execTests = { project: Project, testApkPath: String?, testPaths: List<String>? ->
            if (testPaths != null && testApkPath != null) {
                val testRunnerInfo = getTestRunnerInfo(testApkPath)

                val profileOutput = File("${target.projectDir}/profileOutput")
                if (!profileOutput.exists()) profileOutput.mkdirs()

                val profileClass = {path : String ->
                    testConfiguration()
                    prepareLogs()

                    runTestClass(path, testRunnerInfo)

                    cpuLogs(profileOutput, path)
                    wifiLogs(profileOutput, path)
                    bluetoothLogs(profileOutput, path)
                }

                val profileMethod = {pathName : String, method : String ->
                    testConfiguration()
                    prepareLogs()

                    runTestMethod(pathName, testRunnerInfo, method)

                    cpuLogsOfMethod(profileOutput, pathName, method)
                    wifiLogsOfMethod(profileOutput, pathName, method)
                    bluetoothLogsOfMethod(profileOutput, pathName, method)
                }

                if (project.hasProperty("granularity"))
                    when (project.property("granularity")) {
                        "class" -> {
                            for (path in testPaths) {
                                profileClass(path)
                            }
                        }
                        "methods" -> {
                            for (path in testPaths) {
                                val pathName = path.substringBefore('#')
                                val methods = path.substringAfter('#').split(':')

                                for (method in methods) {
                                    profileMethod(pathName, method)
                                }
                            }
                        }
                        else -> {
                            runCommand("echo", "\n!Error at running tests: granularity must be either \"methods\" or \"class\"\n")
                            throw GradleException("Wrong argument")
                        }
                    }
                else { // supposes that absence of parameter means 'class' granularity
                    for (path in testPaths) {
                        profileClass(path)
                    }
                }

                defaultConfiguration()
            }
            else {
                runCommand("echo", "\n!Error at running tests: test_paths should be passed\n")
                throw GradleException("Arguments are not passed")
            }
        }

        target.tasks.register("customProfile") {
            it.doLast {
                JSONGenerator().generate("${target.projectDir}/profileOutput/")
            }
        }

        target.tasks.named("customProfile").configure { it.dependsOn("runCustomTests") }
        //target.tasks.named("customProfile").configure { it.finalizedBy("clean") }

        target.tasks.register("runCustomTests") { it ->
            it.doLast {
                val testApkPath: String? =
                    if (it.project.hasProperty("test_apk_path"))
                        it.project.property("test_apk_path") as String
                    else null

                val testPaths: List<String>? =
                    if (it.project.hasProperty("test_paths")) {
                        val paths = it.project.property("test_paths") as String
                        paths.split(",")
                    } else null

                execTests(it.project, testApkPath, testPaths)
            }
        }

        target.tasks.register("profileBuild") {
            it.dependsOn("assembleDebug", "assembleDebugAndroidTest")

            val installApk = { apkPath: String ->
                runCommand("echo", "APK Installing: ", apkPath)
                runCommand("$adb", "install", "-r", apkPath)
            }

            it.doLast {
                val projectName = target.project.name
                val apkPath = "$projectName/build/outputs/apk/debug/$projectName-debug.apk"
                val testApkPath = "$projectName/build/outputs/apk/androidTest/debug/$projectName-debug-androidTest.apk"

                installApk(apkPath)
                installApk(testApkPath)
            }
        }

        target.tasks.register("defaultProfile") {
            it.doLast {
                JSONGenerator().generate("${target.projectDir}/profileOutput/")
            }
        }

        target.tasks.named("defaultProfile").configure { it.dependsOn("profileBuild") }
        target.tasks.named("defaultProfile").configure { it.dependsOn("runTests") }
        //target.tasks.named("customProfile").configure { it.finalizedBy("clean") }

        target.tasks.register("runTests") { it ->
            it.doLast {
                val projectName = target.project.name
                val testApkPath = "$projectName/build/outputs/apk/androidTest/debug/$projectName-debug-androidTest.apk"
                val testPaths: List<String>? =
                    if (it.project.hasProperty("test_paths")) {
                        val paths = it.project.property("test_paths") as String
                        paths.split(",")
                    } else null

                execTests(it.project, testApkPath, testPaths)
            }
        }

        target.tasks.named("runTests").configure { it.mustRunAfter("profileBuild") }
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
                if (it.isFile && it.name.endsWith(".txt")) {
                    val testName = it.name.substringBefore(".txt")

                    val testLogs = JSONObject()

                    val cpuComponent = JSONArray()

                    val data = it.readLines()
                    for (line in data) {
                        if (!line.startsWith('-')) {
                            val entryLineList = line.trim().split("\\s+".toRegex())

                            when {
                                entryLineList[0] == "Wifi:" -> {
                                    var wifiComponent = JSONObject()

                                    wifiComponent["common"] = entryLineList[1].replace(",",".").toFloat()

                                    if(entryLineList.size >= 3) {
                                        if(entryLineList[2] == "(") {
                                            var i = 3
                                            while(entryLineList[i] != ")") {
                                                val componentDetails = entryLineList[i].split('=')

                                                wifiComponent[componentDetails[0]] = componentDetails[1].replace(",",".").toFloat()

                                                i++
                                            }
                                        }
                                    }
                                    testLogs["wifi"] = wifiComponent
                                }
                                entryLineList[0] == "Bluetooth:" -> {
                                    var bluetoothComponent = JSONObject()

                                    bluetoothComponent["common"] = entryLineList[1].replace(",",".").toFloat()

                                    if(entryLineList.size >= 3) {
                                        if(entryLineList[2] == "(") {
                                            var i = 3
                                            while (entryLineList[i] != ")") {
                                                val componentDetails = entryLineList[i].split('=')

                                                bluetoothComponent[componentDetails[0]] = componentDetails[1].replace(",",".").toFloat()

                                                i++
                                            }
                                        }
                                    }

                                    testLogs["bluetooth"] = bluetoothComponent
                                }
                                else -> {
                                    var methodName: String = ""
                                    var processId: Int = 0
                                    var threadId: Int = 0

                                    var startDate: String = ""
                                    var startTime: String = ""

                                    //timestamp from January 1, 1970, 00:00:00 GMT
                                    var timestamp: Long = 0

                                    var isEntry: Boolean = false
                                    var parseIndex: Int = 0

                                    if(entryLineList[4] == "D" && entryLineList[5] == "TEST") {
                                        methodName = entryLineList[8]
                                        processId = entryLineList[2].toInt()
                                        threadId = entryLineList[3].toInt()

                                        startDate = entryLineList[0]
                                        startTime = entryLineList[1]

                                        //timestamp from January 1, 1970, 00:00:00 GMT
                                        timestamp = getTimestamp(startDate, startTime)

                                        isEntry = entryLineList[7] == "Entry"
                                        parseIndex = 10
                                    }
                                    else if (entryLineList[1] == "(")
                                    {
                                        methodName = entryLineList[4]
                                        startDate = SimpleDateFormat("MM-dd").format(Date())
                                        startTime = SimpleDateFormat("hh:mm:ss.SSS").format(Date())

                                        timestamp = getTimestamp(startDate, startTime)

                                        isEntry = entryLineList[3] == "Entry"
                                        parseIndex = 6
                                    }
                                    else
                                    {
                                        methodName = entryLineList[3]
                                        startDate = SimpleDateFormat("MM-dd").format(Date())
                                        startTime = SimpleDateFormat("hh:mm:ss.SSS").format(Date())

                                        timestamp = getTimestamp(startDate, startTime)

                                        isEntry = entryLineList[2] == "Entry"
                                        parseIndex = 5
                                    }

                                    val cpuDetails = JSONArray()
                                    var kernelDetails = JSONObject()
                                    var valuesDetails = JSONArray()

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

                                    val cpuTimeInStates = JSONObject()
                                    cpuTimeInStates["component"] = "cpuTimeInStates"
                                    cpuTimeInStates["details"] = cpuDetails

                                    val brightnessComponent = JSONObject()
                                    brightnessComponent["component"] = "brightness"
                                    brightnessComponent["details"] = brightness

                                    val bodyArray = JSONArray()
                                    bodyArray.add(cpuTimeInStates)
                                    bodyArray.add(brightnessComponent)

                                    val logObject = JSONObject()
                                    logObject["header"] = headerDetails
                                    logObject["body"] = bodyArray

                                    cpuComponent.add(logObject)
                                }
                            }
                        }
                    }
                    testLogs["cpu"] = cpuComponent

                    val testObject = JSONObject()
                    testObject["testName"] = testName
                    testObject["logs"] = testLogs

                    testList.add(testObject)

                    //it.delete()
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
            val sdf = SimpleDateFormat("MM-dd-yyyy hh:mm:ss.SSS")
            val dateString = date + "-" + Calendar.getInstance().get(Calendar.YEAR) + " " + time
            return sdf.parse(dateString).time
        }
    }