package presentation.viewmodel

import com.intellij.openapi.externalSystem.task.TaskCallback
import com.intellij.openapi.project.Project
import domain.model.PowerProfile
import data.model.ProfilingError
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
import tooling.RawProfilingResultAnalyzer
import tooling.RawProfilingResultParser

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
                val raw = RawProfilingResultParser.parse("${currentConfiguration!!.modulePath}/profileOutput", "logs.json")
                val result = RawProfilingResultAnalyzer.analyze(raw, powerProfile!!)
                profilingResultRepository.save(result)

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
                            "-Pgranularity=methods",
                            "-Ptest_paths=$tests"
                    ),
                    config.modulePath
            )
        }
    }

    fun stopProfiling() {
        // TODO: how to stop executing gradle task ???
    }
}