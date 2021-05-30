package presentation.viewmodel

import com.intellij.openapi.externalSystem.task.TaskCallback
import com.intellij.openapi.project.Project
import domain.model.PowerProfile
import data.model.ProfilingError
import data.model.ProfilingResult
import data.model.RequestVerdict
import domain.model.ProfilingConfiguration
import domain.repository.ConfigurationRepository
import domain.repository.PowerProfileRepository
import domain.repository.ProfilingResultRepository
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import tooling.GradlePluginInjector
import tooling.GradleTaskExecutor
import tooling.ProfilingResultAnalyzer
import tooling.ProfilingResultParser
import java.io.File

class ProfilingVM(
        private val project: Project,
        private val configurationRepository: ConfigurationRepository,
        private val profilingResultRepository: ProfilingResultRepository,
        private val powerProfileRepository: PowerProfileRepository
) {

    enum class ViewState {
        INITIAL, READY_FOR_PROFILING, DURING_PROFILING
    }

    private val profilingVerdictSubject = PublishSubject.create<RequestVerdict<Unit, ProfilingError>>()
    val profilingVerdict: Observable<RequestVerdict<Unit, ProfilingError>> = profilingVerdictSubject

    private val viewStateSubject = BehaviorSubject.create<ViewState>()
    val viewState: Observable<ViewState> = viewStateSubject

    private var currentConfiguration: ProfilingConfiguration? = null
    private val gradleTaskExecutor = GradleTaskExecutor(project)

    private var powerProfile: PowerProfile? = null

    private val onExecuteTaskCallback = object : TaskCallback {
        override fun onSuccess() {
            Thread {
                val profilingResult = ProfilingResultParser.parse("${currentConfiguration!!.modulePath}/profileOutput", "logs.json")
                val analysisResult = ProfilingResultAnalyzer.analyze(profilingResult, powerProfile!!)
                profilingResultRepository.save(analysisResult)

                profilingVerdictSubject.onNext(RequestVerdict.Success(Unit))
                viewStateSubject.onNext(ViewState.READY_FOR_PROFILING)
            }.start()
        }

        override fun onFailure() {
            profilingVerdictSubject.onNext(RequestVerdict.Failure(ProfilingError.FailedTaskExecutionError()))
            viewStateSubject.onNext(ViewState.READY_FOR_PROFILING)
        }
    }

    init {
        gradleTaskExecutor.callback = onExecuteTaskCallback

        viewStateSubject.onNext(ViewState.INITIAL)

        configurationRepository.fetch()
                .subscribe { config ->
                    currentConfiguration = config
                    if (powerProfile != null) {
                        viewStateSubject.onNext(ViewState.READY_FOR_PROFILING)
                    }
                }

        powerProfileRepository.fetch()
                .subscribe { profile ->
                    powerProfile = profile
                    if (currentConfiguration != null) {
                        viewStateSubject.onNext(ViewState.READY_FOR_PROFILING)
                    }
                }
    }

    // TODO: ISSUE: task doesn't stop if device is unplugged
    // TODO: how to detect when task is failed ??? (onFailure doesn't invoke --- Android Studio bug)
    fun startProfiling() {
        currentConfiguration?.let { config ->
            viewStateSubject.onNext(ViewState.DURING_PROFILING)
            GradlePluginInjector(project).verifyAndInject()

            val tests = config.instrumentedTestNames.entries.joinToString(separator = ",") { clazz -> "${clazz.key}#${clazz.value.joinToString(separator = ":")}" }

            gradleTaskExecutor.executeTask(
                    "defaultProfile",
                    arrayOf(
                            "-Pmode=constants",
                            "-Pgranularity=methods",
                            "-Ptest_paths=$tests",
                            "--full-stacktrace"
                    ),
                    config.modulePath
            )
        }
    }

    // TODO: how to stop executing gradle task?
    // Suggested approach don't work, because tasks get in queue
    // But it's OK, when queue is empty
    fun stopProfiling() {
        currentConfiguration?.let { config ->
            gradleTaskExecutor.executeTask(
                "stopTests", emptyArray(), config.modulePath)
        }
    }

    // Only for debug JSON parsing
//    private fun printJSONParseResult(config : ProfilingConfiguration, parseResult : ProfilingResult) {
//        val writer = File("${config.modulePath}/profileOutput/parseResult.txt").bufferedWriter()
//        val parsingResult = parseResult.getTestResults()
//
//        for(i in parsingResult)
//        {
//            writer.write(i.first)
//            writer.newLine()
//
//            val wifi = i.second.wifiInfo?.random()!!
//            writer.write("Wifi component:")
//            writer.newLine()
//            writer.write("frequency: " + wifi.testInfo.frequency)
//            writer.newLine()
//            writer.write("timestamp: " + wifi.testInfo.timestamp)
//            writer.newLine()
//            writer.write("common: " + wifi.wifiEnergyConsumption.common.toString())
//            writer.newLine()
//            writer.write("wifi: " + wifi.wifiEnergyConsumption.wifi.toString())
//            writer.newLine()
//            wifi.wifiEnergyConsumption.external?.forEach {
//                    x -> writer.write(x.component + ": ")
//                    writer.write(x.energy.toString())
//                    writer.newLine()
//            }
//
//            val bluetooth = i.second.bluetoothInfo?.random()!!
//            writer.write("Bluetooth component:")
//            writer.newLine()
//            writer.write("frequency: " + bluetooth.testInfo.frequency)
//            writer.newLine()
//            writer.write("timestamp: " + bluetooth.testInfo.timestamp)
//            writer.newLine()
//            writer.write("common: " + bluetooth.bluetoothEnergyConsumption.common.toString())
//            writer.newLine()
//            writer.write("bluetooth: " + bluetooth.bluetoothEnergyConsumption.bluetooth.toString())
//            writer.newLine()
//            bluetooth.bluetoothEnergyConsumption?.external?.forEach {
//                    x -> writer.write(x.component + ": ")
//                    writer.write(x.energy.toString())
//                    writer.newLine()
//            }
//
//            val cpu = i.second.cpuInfo
//            writer.write("Cpu component:")
//            writer.newLine()
//            for(k in cpu)
//            {
//                writer.write("isEntry: " + k.methodInfo.isEntry.toString())
//                writer.newLine()
//                writer.write("methodName: " + k.methodInfo.methodName)
//                writer.newLine()
//                writer.write("processID: " + k.methodInfo.processID)
//                writer.newLine()
//                writer.write("threadID: " + k.methodInfo.threadID)
//                writer.newLine()
//                writer.write("timestamp: " + k.methodInfo.timestamp)
//                writer.newLine()
//                writer.write("brightnessLevel: " + k.brightnessLevel)
//                writer.newLine()
//            }
//
//            writer.newLine()
//        }
//
//        writer.close()
//    }

    // Only for debug power_profile.xml parsing
//    private fun printPowerProfileParseResult(config : ProfilingConfiguration, powerProfile : PowerProfile) {
//        val writer = File("${config.modulePath}/profileOutput/powerProfile.txt").bufferedWriter()
//
//        writer.write("wifi.on: " + powerProfile.wifiOn.toString())
//        writer.newLine()
//
//        writer.write("wifi.scan: " + powerProfile.wifiScan.toString())
//        writer.newLine()
//
//        writer.write("wifi.active: " + powerProfile.wifiActive.toString())
//        writer.newLine()
//
//        writer.write("bluetooth.on: " + powerProfile.bluetoothOn.toString())
//        writer.newLine()
//
//        writer.write("bluetooth.active: " + powerProfile.bluetoothActive.toString())
//        writer.newLine()
//
//        writer.write("cpu.active: " + powerProfile.getPowerAtSpeed(0, 400000).toString())
//        writer.newLine()
//
//        writer.close()
//    }
}