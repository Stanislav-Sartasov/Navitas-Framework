package tooling

import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode
import com.intellij.openapi.externalSystem.task.TaskCallback
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.idea.configuration.externalProjectPath
import org.jetbrains.plugins.gradle.util.GradleConstants

class GradleTaskExecutor(private val project: Project) {

    var callback: TaskCallback? = null

    fun executeTask(name: String, args: Array<String>, module: Module) {
        val settings = ExternalSystemTaskExecutionSettings()
        settings.taskNames = listOf(name)
        settings.scriptParameters = args.joinToString(separator = " ")
        settings.externalSystemIdString = GradleConstants.SYSTEM_ID.id
        settings.externalProjectPath = module.externalProjectPath

        ExternalSystemUtil.runTask(
                settings, DefaultRunExecutor.EXECUTOR_ID,
                project, GradleConstants.SYSTEM_ID,
                callback, ProgressExecutionMode.IN_BACKGROUND_ASYNC, false
        )
    }
}
