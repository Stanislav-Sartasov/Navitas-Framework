package presentation.viewmodel

import com.intellij.openapi.externalSystem.task.TaskCallback
import com.intellij.openapi.project.Project
import data.model.ProfilingError
import data.model.RequestVerdict
import domain.model.ProfilingConfiguration
import domain.repository.ConfigurationRepository
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import tooling.GradlePluginInjector
import tooling.GradleTaskExecutor

class ProfilingViewModel(
        private val project: Project,
        private val configurationRepository: ConfigurationRepository
) {

    enum class ViewState {
        INITIAL, READY_TO_PROFILING, PROFILING
    }

    private val profilingVerdictSubject = PublishSubject.create<RequestVerdict<Unit, ProfilingError>>()
    val profilingVerdict: Observable<RequestVerdict<Unit, ProfilingError>> = profilingVerdictSubject

    private val viewStateSubject = BehaviorSubject.create<ViewState>()
    val viewState: Observable<ViewState> = viewStateSubject

    private var currentConfiguration: ProfilingConfiguration? = null

    private val onExecuteTaskCallback = object : TaskCallback {
        override fun onSuccess() {
            profilingVerdictSubject.onNext(RequestVerdict.Success(Unit))
            viewStateSubject.onNext(ViewState.READY_TO_PROFILING)
            println("ConfigAndProfilingViewModel: TaskCallback::onSuccess")
        }

        override fun onFailure() {
            profilingVerdictSubject.onNext(RequestVerdict.Failure(ProfilingError.FailedTaskExecutionError()))
            viewStateSubject.onNext(ViewState.READY_TO_PROFILING)
            println("ConfigAndProfilingViewModel: TaskCallback::onFailure")
        }
    }

    private val gradleTaskExecutor = GradleTaskExecutor(project)

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
            viewStateSubject.onNext(ViewState.PROFILING)
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