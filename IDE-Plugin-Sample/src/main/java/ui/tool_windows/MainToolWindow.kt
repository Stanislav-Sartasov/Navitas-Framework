package ui.tool_windows

import com.intellij.openapi.project.Project
import ui.dialogs.ConfigurationDialog
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
            isEnabled = false
            addActionListener {
                // TBD: check configuration and show error message or profiling dialog
            }
        }
    }
}
