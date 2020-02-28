package ui.tool_windows

import com.intellij.openapi.project.Project
import executeTask
import ui.dialogs.ConfigurationDialog
import ui.profiling.ProfilingDialog
import javax.swing.JButton
import javax.swing.JPanel

class MainToolWindow(private val project: Project) {

    lateinit var contentPane: JPanel
    private var configureButton: JButton? = null
    private var profileButton: JButton? = null

    init{
        setUpUIComponents()
    }

    private fun setUpUIComponents() {
        configureButton!!.apply {
            text = "Configure"
            addActionListener {
                ConfigurationDialog(project) { model ->
                    model.onFinish()
                }.show()
            }
        }
        profileButton!!.apply {
            text = "Profile"
            addActionListener {
                executeTask(project, "rawProfile", arrayOf("-Ptest_apk_path=app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk", "-Papk_path=app/build/outputs/apk/debug/app-debug.apk", "-Ptest_paths=NavigationTest,AnotherTest"))
                // TODO: check ui.configuration and show error message or profiling dialog
            }
        }
    }
}
