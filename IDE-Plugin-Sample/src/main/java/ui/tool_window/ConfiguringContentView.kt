package ui.tool_window

import com.intellij.openapi.externalSystem.task.TaskCallback
import com.intellij.openapi.project.Project
import data.ConfigRepository
import interfaces.ContentRouter
import tooling.GradlePluginInjector
import tooling.GradleTaskExecutor
import ui.configuring.wizard.ConfigWizardDialog
import javax.swing.JButton
import javax.swing.JPanel

class ConfiguringContentView(private val project: Project, private val router: ContentRouter) {

    // UI components
    lateinit var contentPanel: JPanel
    private lateinit var configureButton: JButton
    private lateinit var profileButton: JButton

    // other
    private val gradleTaskExecutor = GradleTaskExecutor(project)

    // TODO: why is the 'onSuccess' method always invoked ???
    // TODO: move this callback into ViewModel/Presenter class
    private val onExecuteTaskCallback = object : TaskCallback {
        override fun onSuccess() {
            println("TaskCallback.onSuccess")
        }

        override fun onFailure() {
            println("TaskCallback.onFailure")
        }
    }

    init{
        setupUI()
        gradleTaskExecutor.callback = onExecuteTaskCallback
    }

    private fun setupUI() {
        configureButton.apply {
            text = "Configure"
            addActionListener {
                ConfigWizardDialog(project) { model ->
                    model.onFinish()
                }.show()
            }
        }
        profileButton.apply {
            text = "Profile"
            addActionListener {
                if (ConfigRepository.isEmpty()) {
                    println("ERROR: Data is not selected")
                    // TODO: show error dialog or notification
                } else {
                    // TODO: add ViewModel/Presenter class and move this logic into it
                    router.toNextContent()
                    GradlePluginInjector(project).verifyAndInject()
                    gradleTaskExecutor.executeTask("rawProfile", arrayOf("-Ptest_apk_path=app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk", "-Papk_path=app/build/outputs/apk/debug/app-debug.apk", "-Ptest_paths=NavigationTest,AnotherTest"))
                    // TODO: replace hardcoded args to selected app module and instrumented tests
                }
            }
        }
    }
}
