package ui.tool_windows

import com.intellij.openapi.project.Project
import ui.dialogs.ConfigurationDialog
import javax.swing.JButton
import javax.swing.JPanel

class MainToolWindow(private val project: Project) {

    lateinit var contentPane: JPanel
    private var startButton: JButton? = null

    init{
        createUIComponents()
    }

    private fun createUIComponents() {
        startButton!!.addActionListener {
            ConfigurationDialog(project) { model -> model.onFinish() }.apply { show() }
        }
    }
}
