package presentation.viewmodel

import com.intellij.openapi.externalSystem.task.TaskCallback
import com.intellij.openapi.project.Project
import data.model.ProfilingError
import data.model.RequestVerdict
import domain.model.ProfilingConfiguration
import domain.repository.ConfigurationRepository
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import tooling.GradlePluginInjector
import tooling.GradleTaskExecutor

class ProfilingViewModel(
        private val project: Project,
        private val configurationRepository: ConfigurationRepository
) {

    private val _profilingResult = PublishSubject.create<RequestVerdict<Unit, ProfilingError>>()
    val profilingResult: Observable<RequestVerdict<Unit, ProfilingError>> = _profilingResult

    private var currentConfiguration: ProfilingConfiguration? = null

    private val onExecuteTaskCallback = object : TaskCallback {
        override fun onSuccess() {
            _profilingResult.onNext(RequestVerdict.Success(Unit))
            println("ConfigAndProfilingViewModel: TaskCallback::onSuccess")
        }

        override fun onFailure() {
            _profilingResult.onNext(RequestVerdict.Failure(ProfilingError.FailedTaskExecutionError()))
            println("ConfigAndProfilingViewModel: TaskCallback::onFailure")
        }
    }

    private val gradleTaskExecutor = GradleTaskExecutor(project)

    init {
        gradleTaskExecutor.callback = onExecuteTaskCallback

        configurationRepository.fetch()
                .subscribe { config ->
                    currentConfiguration = config
                }
    }

    fun startProfiling() {
        currentConfiguration?.let { config ->
            GradlePluginInjector(project).verifyAndInject()
            val moduleName = config.module.name
            gradleTaskExecutor.executeTask(
                    "rawProfile",
                    arrayOf(
                            "-Ptest_apk_path=$moduleName/build/outputs/apk/androidTest/debug/$moduleName-debug-androidTest.apk",
                            "-Papk_path=$moduleName/build/outputs/apk/debug/$moduleName-debug.apk",
                            "-Ptest_paths=${config.instrumentedTestNames.joinToString(separator = ",")}"
                    ),
                    config.module
            )
            // TODO: replace hardcoded args to selected app module
            // TODO: ISSUE: task doesn't stop if device is unplugged
            // TODO: how to detect when task is failed ??? (onFailure doesn't invoke)
        }
    }

    fun stopProfiling() {
        // TODO: how to stop executing gradle task ???
    }
}