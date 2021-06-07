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
import java.util.concurrent.atomic.AtomicBoolean
import java.util.regex.Pattern

open class ProfPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create("profilingPlugin", InstrExtension::class.java)
        val android = target.extensions.findByName("android") as BaseExtension
        android.registerTransform(Transformer(android, extension))

        val adb: File = android.adbExecutable
        fun runCommand(vararg args: String) {
            try {
                target.exec {
                    it.workingDir("./..")
                    it.commandLine(*args)
                }
            }
            catch (exc : Exception) {
                throw GradleException(exc.message.toString())
            }
        }

        val testFinished = AtomicBoolean(false)

        fun cpuLogsOfClass(profilingOutput: File, path: String) {
            target.exec {
                it.commandLine("$adb", "logcat", "-d", "-s", "TEST", "-v", "threadtime")
                it.standardOutput = FileOutputStream("${profilingOutput.absolutePath}/$path.txt", true)
            }
        }

        fun cpuLogsOfMethod(profilingOutput: File, pathName: String, method: String) {
            target.exec {
                it.commandLine("$adb", "logcat", "-d", "-s", "TEST", "-v", "threadtime")
                it.standardOutput = FileOutputStream("${profilingOutput.absolutePath}/$pathName.$method.txt", true)
            }
        }

        fun componentsLogsOfClass(profilingOutput: File, path: String, frequencyInSec: Float) {
            target.exec {
                val date = SimpleDateFormat("MM-dd").format(Date())
                val time = SimpleDateFormat("HH:mm:ss.SSS").format(Date())

                it.commandLine("$adb", "shell", "dumpsys", "batterystats", "|", "grep", "-E", "\"Wifi: |Bluetooth: \"",
                    "-m", "2", "|", "tr", "-d", "'\\r\\n'", ";", "echo", "'   '", "$frequencyInSec", date, time)
                it.standardOutput = FileOutputStream("${profilingOutput.absolutePath}/$path.txt", true)
            }
        }

        fun componentsLoggingOfClassWithFrequency(milliseconds : Long, profilingOutput: File, path: String) {
            val frequencyInSec = milliseconds / 1000f

            while(!testFinished.get()) {
                componentsLogsOfClass(profilingOutput, path, frequencyInSec)

                Thread.sleep(milliseconds)
            }
        }

        fun componentsLogsOfMethod(profilingOutput: File, pathName: String, method: String, frequencyInSec: Float) {
            target.exec {
                val date = SimpleDateFormat("MM-dd").format(Date())
                val time = SimpleDateFormat("HH:mm:ss.SSS").format(Date())

                it.commandLine("$adb", "shell", "dumpsys", "batterystats", "|", "grep", "-E", "\"Wifi: |Bluetooth: \"",
                    "-m", "2", "|", "tr", "-d", "'\\r\\n'", ";", "echo", "'   '", "$frequencyInSec", date, time)
                it.standardOutput = FileOutputStream("${profilingOutput.absolutePath}/$pathName.$method.txt", true)
            }
        }

        fun componentsLoggingOfMethodWithFrequency(milliseconds : Long, profilingOutput: File, pathName: String, method: String) {
            val frequencyInSec = milliseconds / 1000f

            while(!testFinished.get()) {
                componentsLogsOfMethod(profilingOutput, pathName, method, frequencyInSec)

                Thread.sleep(milliseconds)
            }
        }

        val testConfiguration = {
            target.exec {
                it.commandLine("$adb", "shell", "dumpsys", "battery", "set", "ac", "0")
                it.commandLine("$adb", "shell", "dumpsys", "battery", "set", "usb", "0")
                it.commandLine("$adb", "shell", "dumpsys", "battery", "set", "wireless", "0")
                it.commandLine("$adb", "shell", "dumpsys", "battery", "set", "status", "0")

                it.commandLine("$adb", "shell", "dumpsys", "battery", "unplug")

                //it.commandLine("$adb", "shell", "svc", "wifi", "enable")
                //it.commandLine("$adb", "shell", "svc", "bluetooth", "enable")
            }
        }

        val defaultConfiguration = {
            target.exec{
                it.commandLine("$adb", "shell", "dumpsys", "battery", "set", "ac", "1")
                it.commandLine("$adb", "shell", "dumpsys", "battery", "set", "usb", "1")
                it.commandLine("$adb", "shell", "dumpsys", "battery", "set", "wireless", "1")
                it.commandLine("$adb", "shell", "dumpsys", "battery", "set", "status", "1")

                //it.commandLine("$adb", "shell", "svc", "wifi", "disable")
                //it.commandLine("$adb", "shell", "svc", "bluetooth", "disable")
            }
        }

        val prepareLogs = {
            target.exec{
                it.isIgnoreExitValue = true //needs to be fixed somehow
                it.commandLine("$adb", "logcat", "-c")
                it.commandLine("$adb", "logcat", "-c")

                it.commandLine("$adb", "shell", "dumpsys", "batterystats", "--reset")

                Thread.sleep(2000)
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
                "class", "${info.targetPackage}.$testPath", "${info.testPackage}/${info.runnerName}")
        }

        val runTestMethod = { testPath: String, info: TestRunnerInfo, methodName: String ->
            runCommand("$adb", "shell", "am", "instrument", "-w", "-e",
                "class", "${info.targetPackage}.$testPath#$methodName", "${info.testPackage}/${info.runnerName}")
        }

        fun executeTests(project: Project, testApkPath: String?, testPaths: List<String>?) {
            if (testPaths != null && testApkPath != null) {
                val testRunnerInfo = getTestRunnerInfo(testApkPath)

                val profilingOutput = File("${target.projectDir}/profilingOutput")
                if (!profilingOutput.exists()) profilingOutput.mkdirs() else profilingOutput.walk().forEach { it.delete() }
                val constantsOutput = File("${target.projectDir}/constantsOutput")
                if (!constantsOutput.exists()) profilingOutput.mkdirs() else constantsOutput.walk().forEach { it.delete() }

                fun profileClass(path : String, loggers : List<Thread>) {
                    testConfiguration()
                    prepareLogs()

                    loggers.forEach { logger -> logger.start() }

                    val testOutput = File("${profilingOutput.absolutePath}/$path.txt")
                    while(!testOutput.exists())
                    {
                        Thread.sleep(0)
                    }

                    runTestClass(path, testRunnerInfo)

                    testFinished.set(true)
                    loggers.forEach { logger -> logger.join() }
                    testFinished.set(false)

                    cpuLogsOfClass(profilingOutput, path)
                }

                fun profileMethod(pathName : String, method : String, loggers : List<Thread>) {
                    testConfiguration()
                    prepareLogs()

                    loggers.forEach { logger -> logger.start() }

                    val testOutput = File("${profilingOutput.absolutePath}/$pathName.$method.txt")
                    while(!testOutput.exists())
                    {
                        Thread.sleep(0)
                    }

                    runTestMethod(pathName, testRunnerInfo, method)

                    testFinished.set(true)
                    loggers.forEach { logger -> logger.join() }
                    testFinished.set(false)

                    cpuLogsOfMethod(profilingOutput, pathName, method)
                }

                if (project.hasProperty("granularity"))
                    when (project.property("granularity")) {
                        "class" -> {
                            if (project.hasProperty("mode"))
                                when (project.property("mode")) {
                                    "profiling" -> {
                                        for (path in testPaths) {
                                            val componentsLogger = Thread {
                                                componentsLoggingOfClassWithFrequency(
                                                    0L,
                                                    profilingOutput,
                                                    path
                                                )
                                            }

                                            profileClass(path, listOf(componentsLogger))
                                        }
                                    }
                                    "constants" -> {
                                        for (path in testPaths) {
                                            val componentsLoggers = listOf(
                                                Thread {
                                                    componentsLoggingOfClassWithFrequency(
                                                        100L,
                                                        profilingOutput,
                                                        path
                                                    )
                                                },
                                                Thread {
                                                    componentsLoggingOfClassWithFrequency(
                                                        200L,
                                                        profilingOutput,
                                                        path
                                                    )
                                                },
                                                Thread {
                                                    componentsLoggingOfClassWithFrequency(
                                                        500L,
                                                        profilingOutput,
                                                        path
                                                    )
                                                },
                                                Thread {
                                                    componentsLoggingOfClassWithFrequency(
                                                        1000L,
                                                        profilingOutput,
                                                        path
                                                    )
                                                },
                                                Thread {
                                                    componentsLoggingOfClassWithFrequency(
                                                        2000L,
                                                        profilingOutput,
                                                        path
                                                    )
                                                }
                                            )

                                            profileClass(path, componentsLoggers)
                                        }
                                    }
                                    else -> {
                                        runCommand("echo", "\n!Error at running tests: mode must be either \"profiling\" or \"constants\"\n")
                                        throw GradleException("Wrong mode")
                                    }
                                }
                            // supposes that absence of parameter means 'profiling' mode
                            else {
                                for (path in testPaths) {
                                    val componentsLogger = Thread {
                                        componentsLoggingOfClassWithFrequency(
                                            0L,
                                            profilingOutput,
                                            path
                                        )
                                    }

                                    profileClass(path, listOf(componentsLogger))
                                }
                            }
                        }
                        "methods" -> {
                            if (project.hasProperty("mode"))
                                when (project.property("mode")) {
                                    "profiling" -> {
                                        for (path in testPaths) {
                                            val pathName = path.substringBefore('#')
                                            val methods = path.substringAfter('#').split(':')

                                            for (method in methods) {
                                                val componentsLogger = Thread {
                                                    componentsLoggingOfMethodWithFrequency(
                                                        0L,
                                                        profilingOutput,
                                                        pathName,
                                                        method
                                                    )
                                                }

                                                profileMethod(pathName, method, listOf(componentsLogger))
                                            }
                                        }
                                    }
                                    "constants" -> {
                                        for (path in testPaths) {
                                            val pathName = path.substringBefore('#')
                                            val methods = path.substringAfter('#').split(':')

                                            for (method in methods) {
                                                val componentsLoggers = listOf(
                                                    Thread {
                                                        componentsLoggingOfMethodWithFrequency(
                                                            100L,
                                                            profilingOutput,
                                                            pathName,
                                                            method
                                                        )
                                                    },
                                                    Thread {
                                                        componentsLoggingOfMethodWithFrequency(
                                                            200L,
                                                            profilingOutput,
                                                            pathName,
                                                            method
                                                        )
                                                    },
                                                    Thread {
                                                        componentsLoggingOfMethodWithFrequency(
                                                            500L,
                                                            profilingOutput,
                                                            pathName,
                                                            method
                                                        )
                                                    },
                                                    Thread {
                                                        componentsLoggingOfMethodWithFrequency(
                                                            1000L,
                                                            profilingOutput,
                                                            pathName,
                                                            method
                                                        )
                                                    },
                                                    Thread {
                                                        componentsLoggingOfMethodWithFrequency(
                                                            2000L,
                                                            profilingOutput,
                                                            pathName,
                                                            method
                                                        )
                                                    }
                                                )

                                                profileMethod(pathName, method, componentsLoggers)
                                            }
                                        }
                                    }
                                    else -> {
                                        runCommand("echo", "\n!Error at running tests: mode must be either \"profiling\" or \"constants\"\n")
                                        throw GradleException("Wrong mode")
                                    }
                                }
                            // supposes that absence of parameter means 'profiling' mode
                            else {
                                for (path in testPaths) {
                                    val pathName = path.substringBefore('#')
                                    val methods = path.substringAfter('#').split(':')

                                    for (method in methods) {
                                        val componentsLogger = Thread {
                                            componentsLoggingOfMethodWithFrequency(
                                                0L,
                                                profilingOutput,
                                                pathName,
                                                method
                                            )
                                        }

                                        profileMethod(pathName, method, listOf(componentsLogger))
                                    }
                                }
                            }
                        }
                        else -> {
                            runCommand("echo", "\n!Error at running tests: granularity must be either \"methods\" or \"class\"\n")
                            throw GradleException("Wrong granularity")
                        }
                    }
                // supposes that absence of parameter means 'class' granularity
                else {
                    if (project.hasProperty("mode"))
                        when (project.property("mode")) {
                            "profiling" -> {
                                for (path in testPaths) {
                                    val componentsLogger = Thread {
                                        componentsLoggingOfClassWithFrequency(
                                            0L,
                                            profilingOutput,
                                            path
                                        )
                                    }

                                    profileClass(path, listOf(componentsLogger))
                                }
                            }
                            "constants" -> {
                                for (path in testPaths) {
                                    val componentsLoggers = listOf(
                                        Thread {
                                            componentsLoggingOfClassWithFrequency(
                                                100L,
                                                profilingOutput,
                                                path
                                            )
                                        },
                                        Thread {
                                            componentsLoggingOfClassWithFrequency(
                                                200L,
                                                profilingOutput,
                                                path
                                            )
                                        },
                                        Thread {
                                            componentsLoggingOfClassWithFrequency(
                                                500L,
                                                profilingOutput,
                                                path
                                            )
                                        },
                                        Thread {
                                            componentsLoggingOfClassWithFrequency(
                                                1000L,
                                                profilingOutput,
                                                path
                                            )
                                        },
                                        Thread {
                                            componentsLoggingOfClassWithFrequency(
                                                2000L,
                                                profilingOutput,
                                                path
                                            )
                                        }
                                    )

                                    profileClass(path, componentsLoggers)
                                }
                            }
                            else -> {
                                runCommand("echo", "\n!Error at running tests: mode must be either \"profiling\" or \"constants\"\n")
                                throw GradleException("Wrong mode")
                            }
                        }
                    // supposes that absence of parameter means 'profiling' mode
                    else {
                        for (path in testPaths) {
                            val componentsLogger = Thread {
                                componentsLoggingOfClassWithFrequency(
                                    0L,
                                    profilingOutput,
                                    path
                                )
                            }

                            profileClass(path, listOf(componentsLogger))
                        }
                    }
                }

                //defaultConfiguration()
            }
            else {
                runCommand("echo", "\n!Error at running tests: test_paths should be passed\n")
                throw GradleException("Arguments are not passed")
            }
        }

        target.tasks.register("customProfile") {
            it.doLast {
                JSONGenerator().generate("${target.projectDir}/profilingOutput/")
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

                executeTests(it.project, testApkPath, testPaths)
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
                JSONGenerator().generate("${target.projectDir}/profilingOutput/")
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

                executeTests(it.project, testApkPath, testPaths)
            }
        }

        target.tasks.named("runTests").configure { it.mustRunAfter("profileBuild") }

        val stopTasks = {
            runCommand("./gradlew", "--stop")
        }

        target.tasks.register("stopTests") {
            it.doFirst {
                stopTasks()
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
            if (it.isFile && it.name.endsWith(".txt")) {
                val testName = it.name.substringBefore(".txt")

                val testLogs = JSONObject()

                val cpuComponent = JSONArray()
                val wifiComponent = JSONArray()
                val bluetoothComponent = JSONArray()

                val data = it.readLines()
                for (line in data) {
                    if (!line.startsWith('-')) {
                        val entryLineList = line.trim().split("\\s+".toRegex())
                        try {
                            when(entryLineList[0]) {
                                "Wifi:", "Bluetooth:" -> {
                                    val header = JSONObject()
                                    val freqInSec = entryLineList[entryLineList.lastIndex - 2].toFloat()
                                    header["frequency"] = if (freqInSec != 0.0f) 1f / freqInSec else -1f
                                    header["timestamp"] = getTimestamp(entryLineList[entryLineList.lastIndex - 1],
                                        entryLineList.last())

                                    val componentsWithIndexes = listOf(
                                        Pair(wifiComponent, entryLineList.indexOf("Wifi:")),
                                        Pair(bluetoothComponent, entryLineList.indexOf("Bluetooth:")))

                                    for(componentWithIndex in componentsWithIndexes) {
                                        if(componentWithIndex.second != -1)
                                        {
                                            val body = JSONObject()
                                            body["common"] = entryLineList[componentWithIndex.second + 1].replace(",",".").toFloat()

                                            if(entryLineList.elementAt(componentWithIndex.second + 2) == "(") {
                                                var i = componentWithIndex.second + 3
                                                while(entryLineList[i] != ")") {
                                                    val details = entryLineList[i].split('=')

                                                    body[details[0]] = details[1].replace(",",".").toFloat()

                                                    i++
                                                }
                                            }

                                            val log = JSONObject()
                                            log["header"] = header
                                            log["body"] = body

                                            componentWithIndex.first.add(log)
                                        }
                                    }
                                }
                                else -> {
                                    var methodName: String
                                    var processId = 0
                                    var threadId = 0

                                    var startDate: String
                                    var startTime: String

                                    //timestamp from January 1, 1970, 00:00:00 GMT
                                    var timestamp: Long

                                    var isEntry: Boolean
                                    var parseIndex: Int

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
                                        startTime = SimpleDateFormat("HH:mm:ss.SSS").format(Date())

                                        timestamp = getTimestamp(startDate, startTime)

                                        isEntry = entryLineList[3] == "Entry"
                                        parseIndex = 6
                                    }
                                    else
                                    {
                                        methodName = entryLineList[3]
                                        startDate = SimpleDateFormat("MM-dd").format(Date())
                                        startTime = SimpleDateFormat("HH:mm:ss.SSS").format(Date())

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
                        catch (exc : Exception) {
                            //Just skip this line
                            print("JSON Generator:\n $line: was skipped due to a format mismatch\n")
                        }
                    }
                }
                if(cpuComponent.size != 0)
                {
                    testLogs["cpu"] = cpuComponent
                }
                if(wifiComponent.size != 0)
                {
                    testLogs["wifi"] = wifiComponent
                }
                if(bluetoothComponent.size != 0)
                {
                    testLogs["bluetooth"] = bluetoothComponent
                }

                val testObject = JSONObject()
                testObject["testName"] = testName
                testObject["logs"] = testLogs

                testList.add(testObject)
            }
        }

        val json = JSONObject()
        json["tests"] = testList

        val jsonFile = File("$directory/logs.json")
        if (!jsonFile.exists()) jsonFile.createNewFile() else {
            jsonFile.delete()
            jsonFile.createNewFile()
        }
        val file = FileWriter(jsonFile)
        file.write(json.toJSONString())
        file.flush()
        file.close()
    }

    private fun getTimestamp(date: String, time: String): Long {
        val sdf = SimpleDateFormat("MM-dd-yyyy HH:mm:ss.SSS")
        val dateString = date + "-" + Calendar.getInstance().get(Calendar.YEAR) + " " + time

        return sdf.parse(dateString).time
    }
}