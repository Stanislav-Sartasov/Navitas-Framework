package presentation.view

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.content.ContentFactory
import presentation.view.configuring.ConfiguringContentView
import presentation.view.profiling_details.EnergyConsumptionContentView
import presentation.view.profiling_details.ProfilingResultContentView
import tooling.ContentRouterImpl

class ToolWindowFactory : com.intellij.openapi.wm.ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val router = ContentRouterImpl(toolWindow)
        val contentFactory = ContentFactory.SERVICE.getInstance()

        val configuringContent = contentFactory.createContent(ConfiguringContentView(project, router).panel, "", false)
        val profilingContent = contentFactory.createContent(ProfilingResultContentView(router).contentPanel, "", false)
        val energyConsumptionContent = contentFactory.createContent(EnergyConsumptionContentView(router).panel, "", false)

        router.setupContents(listOf(configuringContent, profilingContent, energyConsumptionContent))
        router.toNextContent()
    }
}