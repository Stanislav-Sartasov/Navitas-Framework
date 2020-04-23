package presentation.viewmodel

import com.intellij.openapi.externalSystem.task.TaskCallback
import com.intellij.openapi.project.Project
import data.model.ProfilingError
import data.model.RequestVerdict
import domain.model.ProfilingConfiguration
import domain.repository.ConfigurationRepository
import domain.repository.ProfilingResultRepository
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.jetbrains.kotlin.idea.configuration.externalProjectPath
import tooling.GradlePluginInjector
import tooling.GradleTaskExecutor
import tooling.RawProfilingResultAnalyzer
import tooling.RawProfilingResultParser

class ProfilingVM(
        private val project: Project,
        private val configurationRepository: ConfigurationRepository,
        private val profilingResultRepository: ProfilingResultRepository
) {

    enum class ViewState {
        INITIAL, READY_TO_PROFILING, DURING_PROFILING
    }

    private val profilingVerdictSubject = PublishSubject.create<RequestVerdict<Unit, ProfilingError>>()
    val profilingVerdict: Observable<RequestVerdict<Unit, ProfilingError>> = profilingVerdictSubject

    private val viewStateSubject = BehaviorSubject.create<ViewState>()
    val viewState: Observable<ViewState> = viewStateSubject

    private var currentConfiguration: ProfilingConfiguration? = null
    private val gradleTaskExecutor = GradleTaskExecutor(project)

    private val onExecuteTaskCallback = object : TaskCallback {
        override fun onSuccess() {
            // TODO: move to background thread
            val raw = RawProfilingResultParser.parse("${currentConfiguration!!.module.externalProjectPath!!}/profileOutput", "logs.json")
            val result = RawProfilingResultAnalyzer.analyze(raw)
            profilingResultRepository.save(result)

            profilingVerdictSubject.onNext(RequestVerdict.Success(Unit))
            viewStateSubject.onNext(ViewState.READY_TO_PROFILING)

            println("Profiling completed")
        }

        override fun onFailure() {
            profilingVerdictSubject.onNext(RequestVerdict.Failure(ProfilingError.FailedTaskExecutionError()))
            viewStateSubject.onNext(ViewState.READY_TO_PROFILING)

            println("Profiling failed")
        }
    }

    init {
        gradleTaskExecutor.callback = onExecuteTaskCallback

        viewStateSubject.onNext(ViewState.INITIAL)

        configurationRepository.fetch()
                .subscribe { config ->
                    currentConfiguration = config
                    viewStateSubject.onNext(ViewState.READY_TO_PROFILING)
                }
    }

    // TODO: ISSUE: task doesn't stop if device is unplugged
    // TODO: how to detect when task is failed ??? (onFailure doesn't invoke)
    fun startProfiling() {
        currentConfiguration?.let { config ->
            viewStateSubject.onNext(ViewState.DURING_PROFILING)
            GradlePluginInjector(project).verifyAndInject()
            gradleTaskExecutor.executeTask(
                    "defaultProfile",
                    arrayOf("-Ptest_paths=${config.instrumentedTestNames.joinToString(separator = ",")}"),
                    config.module
            )
        }
    }

    fun stopProfiling() {
        // TODO: how to stop executing gradle task ???
    }
}