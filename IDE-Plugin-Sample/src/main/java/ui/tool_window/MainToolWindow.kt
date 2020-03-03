package ui.tool_window

import com.intellij.openapi.externalSystem.task.TaskCallback
import com.intellij.openapi.project.Project
import tooling.GradlePluginInjector
import tooling.GradleTaskExecutor
import ui.configuring.wizard.ConfigWizardDialog
import javax.swing.JButton
import javax.swing.JPanel

class MainToolWindow(private val project: Project) {

    lateinit var contentPane: JPanel
    private var configureButton: JButton? = null
    private var profileButton: JButton? = null
    private val gradleTaskExecutor = GradleTaskExecutor(project)

    // TODO: why is the 'onSuccess' method always invoked ???
    private val onExecuteTaskCallback = object : TaskCallback {
        override fun onSuccess() {
            println("TaskCallback.onSuccess")
        }

        override fun onFailure() {
            println("TaskCallback.onFailure")
        }
    }

    init{
        setUpUIComponents()
        gradleTaskExecutor.callback = onExecuteTaskCallback
    }

    private fun setUpUIComponents() {
        configureButton!!.apply {
            text = "Configure"
            addActionListener {
                ConfigWizardDialog(project) { model ->
                    model.onFinish()
                }.show()
            }
        }
        profileButton!!.apply {
            text = "Profile"
            addActionListener {
                GradlePluginInjector(project).verifyAndInject()
                gradleTaskExecutor.executeTask("rawProfile", arrayOf("-Ptest_apk_path=app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk", "-Papk_path=app/build/outputs/apk/debug/app-debug.apk", "-Ptest_paths=NavigationTest,AnotherTest"))
                // TODO: check configuration and show error message or profiling dialog
                // TODO: replace hardcoded args to selected app module and instrumented tests
            }
        }
    }
}
