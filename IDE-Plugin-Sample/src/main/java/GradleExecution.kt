import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode
import com.intellij.openapi.externalSystem.task.TaskCallback
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.gradle.util.GradleConstants

fun executeTask(project: Project, name: String, args: Array<String>) {
    val taskCallback = object : TaskCallback {
        override fun onSuccess() {
            println("TaskCallback.onSuccess")
        }

        override fun onFailure() {
            println("TaskCallback.onFailure")
        }
    }

    val settings = ExternalSystemTaskExecutionSettings()
    settings.externalProjectPath = project.basePath
    settings.taskNames = listOf(name)
    settings.scriptParameters = args.joinToString(separator = " ")
    settings.externalSystemIdString = GradleConstants.SYSTEM_ID.id

    ExternalSystemUtil.runTask(
            settings, DefaultRunExecutor.EXECUTOR_ID,
            project, GradleConstants.SYSTEM_ID,
            taskCallback, ProgressExecutionMode.IN_BACKGROUND_ASYNC, false
    )
}
