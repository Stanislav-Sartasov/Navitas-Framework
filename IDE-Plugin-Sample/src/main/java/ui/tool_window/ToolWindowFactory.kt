package ui.tool_window

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.content.ContentFactory
import tooling.ContentRouterImpl

class ToolWindowFactory : com.intellij.openapi.wm.ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val router = ContentRouterImpl(toolWindow)

        val contentFactory = ContentFactory.SERVICE.getInstance()
        val configuringContent = contentFactory.createContent(ConfiguringContentView(project, router).contentPanel, "", false)
        val profilingContent = contentFactory.createContent(ProfilingContentView(router).contentPanel, "", false)

        router.setupContents(listOf(configuringContent, profilingContent))
        router.toNextContent()
    }
}