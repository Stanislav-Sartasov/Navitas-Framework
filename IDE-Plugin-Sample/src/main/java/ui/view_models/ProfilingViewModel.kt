package ui.view_models

import com.intellij.openapi.externalSystem.task.TaskCallback
import com.intellij.openapi.project.Project
import data.ConfigurationRepository
import data.model.ProfilingConfiguration
import data.model.ProfilingError
import data.model.RequestVerdict
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import tooling.GradlePluginInjector
import tooling.GradleTaskExecutor

class ProfilingViewModel(private val project: Project) {

    private val _profilingResult = PublishSubject.create<RequestVerdict<Unit, ProfilingError>>()
    val profilingResult: Observable<RequestVerdict<Unit, ProfilingError>> = _profilingResult

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
    }

    fun startProfiling() {
        if (ConfigurationRepository.isEmpty) {
            _profilingResult.onNext(RequestVerdict.Failure(ProfilingError.EmptyConfigurationError()))
        } else {
            GradlePluginInjector(project).verifyAndInject()
            gradleTaskExecutor.executeTask("rawProfile", arrayOf("-Ptest_apk_path=app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk", "-Papk_path=app/build/outputs/apk/debug/app-debug.apk", "-Ptest_paths=NavigationTest,AnotherTest"))
            // TODO: replace hardcoded args to selected app module and instrumented tests
            // TODO: ISSUE: task doesn't stop if device is unplugged
            // TODO: how to detect when task is failed ??? (onFailure doesn't invoke)
        }
    }

    fun stopProfiling() {
        // TODO: how to stop executing gradle task ???
    }
}