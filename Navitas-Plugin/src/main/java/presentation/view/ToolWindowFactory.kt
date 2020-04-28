package presentation.view

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import data.repository.ProfilingResultRepositoryImpl
import presentation.view.common.ContentContainer
import presentation.view.configuring.ConfiguringContentView
import presentation.view.profiling_details.ProfilingResultContentView
import presentation.view.profiling_details.TestProfilingResultDetailsContentView
import presentation.view.profiling_details.TestsProfilingResultContentView
import tooling.ContentRouterImpl
import tooling.RawProfilingResultAnalyzer
import tooling.RawProfilingResultParser
import javax.inject.Provider

class ToolWindowFactory : com.intellij.openapi.wm.ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val router = ContentRouterImpl(toolWindow)

        val providers = listOf<Provider<ContentContainer>>(
                Provider { ConfiguringContentView(project, router) },
                Provider { ProfilingResultContentView(router) },
                Provider { TestsProfilingResultContentView(router) },
                Provider { TestProfilingResultDetailsContentView(router) }
        )

//        val raw = RawProfilingResultParser.parse("${project.basePath!!}/app/profileOutput", "logs.json")
//        val result = RawProfilingResultAnalyzer.analyze(raw)
//        ProfilingResultRepositoryImpl.save(result)

        router.setupProviders(providers)
        router.toNextContent()
    }
}

//        Disposer.register(project, component)