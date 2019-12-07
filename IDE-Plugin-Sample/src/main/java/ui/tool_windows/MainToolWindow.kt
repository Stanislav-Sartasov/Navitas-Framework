package ui.tool_windows

import com.intellij.openapi.wm.ToolWindow
import ui.dialogs.ConfigurationDialog
import javax.swing.JButton
import javax.swing.JPanel

class MainToolWindow(toolWindow: ToolWindow) {

    lateinit var contentPane: JPanel
    private var startButton: JButton? = null

    init{
        createUIComponents()
    }

    private fun createUIComponents() {
        startButton!!.addActionListener {
            ConfigurationDialog { model -> model.onFinish() }.apply { show() }
        }
    }
}
