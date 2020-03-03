package ui.tool_window

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.content.ContentFactory

class ToolWindowFactory : com.intellij.openapi.wm.ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val mainToolWindow = MainToolWindow(project)
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(mainToolWindow.contentPane, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
