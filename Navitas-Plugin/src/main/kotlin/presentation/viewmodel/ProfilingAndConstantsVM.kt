package presentation.viewmodel

import com.intellij.openapi.externalSystem.task.TaskCallback
import com.intellij.openapi.project.Project
import domain.model.PowerProfile
import data.model.ProfilingError
import data.model.RequestVerdict
import domain.model.ProfilingConfiguration
import domain.repository.ConfigurationRepository
import domain.repository.ConstantsResultRepository
import domain.repository.PowerProfileRepository
import domain.repository.ProfilingResultRepository
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import tooling.*

class ProfilingAndConstantsVM(
        private val project: Project,
        private val configurationRepository: ConfigurationRepository,
        private val profilingResultRepository: ProfilingResultRepository,
        private val constantsResultRepository: ConstantsResultRepository,
        private val powerProfileRepository: PowerProfileRepository
) {

    enum class ViewState {
        INITIAL, POWER_PROFILE_REQUIRED, READY_FOR_PROFILING, READY_FOR_CONSTANTS, DURING
    }

    private val profilingVerdictSubject = PublishSubject.create<RequestVerdict<Unit, ProfilingError>>()
    val profilingVerdict: Observable<RequestVerdict<Unit, ProfilingError>> = profilingVerdictSubject

    private val viewStateSubject = BehaviorSubject.create<ViewState>()
    val viewState: Observable<ViewState> = viewStateSubject

    private var currentConfiguration: ProfilingConfiguration? = null
    private var powerProfile: PowerProfile? = null
    private var mode: String? = null

    private val gradleTaskExecutor = GradleTaskExecutor(project)

    private val onExecuteTaskCallback = object : TaskCallback {
        override fun onSuccess() {
            Thread {
                val profilerResult = ProfilerResultParser.parse("${currentConfiguration!!.modulePath}/profilingOutput", "logs.json")

                when(mode)
                {
                    "profiling" -> {
                        val profilingAnalysisResult = ProfilingResultAnalyzer.analyze(profilerResult, powerProfile!!)
                        profilingResultRepository.save(profilingAnalysisResult)
                    }
                    "constants" -> {
                        val constantsAnalysisResult = ConstantsResultAnalyzer.analyze(profilerResult)
                        constantsResultRepository.save(constantsAnalysisResult)
                        XMLGenerator.powerProfile(constantsAnalysisResult, "${currentConfiguration!!.modulePath}/constantsOutput")
                    }
                }

                profilingVerdictSubject.onNext(RequestVerdict.Success(Unit))
                viewStateSubject.onNext(ViewState.INITIAL)
            }.start()
        }

        override fun onFailure() {
            profilingVerdictSubject.onNext(RequestVerdict.Failure(ProfilingError.FailedTaskExecutionError()))
            viewStateSubject.onNext(ViewState.INITIAL)
        }
    }

    init {
        gradleTaskExecutor.callback = onExecuteTaskCallback

        viewStateSubject.onNext(ViewState.INITIAL)

        configurationRepository.fetch()
                .subscribe { config ->
                    currentConfiguration = config

                    mode = if (currentConfiguration!!.moduleName.split('.').contains("navi_constants"))
                        "constants" else "profiling"
                    configurationRepository.switchMode(mode!!)

                    if (powerProfile == null && mode == "profiling") {
                        viewStateSubject.onNext(ViewState.POWER_PROFILE_REQUIRED)
                    }
                    if (powerProfile != null && mode == "profiling") {
                        viewStateSubject.onNext(ViewState.READY_FOR_PROFILING)
                    }
                    if (powerProfile == null && mode == "constants") {
                        viewStateSubject.onNext(ViewState.READY_FOR_CONSTANTS)
                    }
                    if (powerProfile != null && mode == "constants") {
                        powerProfile = null
                        viewStateSubject.onNext(ViewState.READY_FOR_CONSTANTS)
                    }
                }

        powerProfileRepository.fetch()
            .subscribe { profile ->
                powerProfile = profile
                viewStateSubject.onNext(ViewState.READY_FOR_PROFILING)
            }
    }

    // TODO: ISSUE: task doesn't stop if device is unplugged
    // TODO: how to detect when task is failed ??? (onFailure doesn't invoke --- Android Studio bug)
    fun start() {
        currentConfiguration?.let { config ->
            viewStateSubject.onNext(ViewState.DURING)
            GradlePluginInjector(project).verifyAndInject()

            val tests = config.instrumentedTestNames.entries.joinToString(separator = ",") { clazz -> "${clazz.key}#${clazz.value.joinToString(separator = ":")}" }

            gradleTaskExecutor.executeTask(
                    "defaultProfile",
                    arrayOf(
                            "-Pmode=$mode",
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
    fun stop() {
        currentConfiguration?.let { config ->
            gradleTaskExecutor.executeTask(
                "stopTests", emptyArray(), config.modulePath)
        }
    }

    // Only for debug JSON parsing
//    private fun printJSONParseResult(config : ProfilingConfiguration, parseResult : ProfilingResult) {
//        val writer = File("${config.modulePath}/profilingOutput/parseResult.txt").bufferedWriter()
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
//        val writer = File("${config.modulePath}/profilingOutput/powerProfile.txt").bufferedWriter()
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