package presentation.view

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import data.repository.ConfigurationRepositoryImpl
import data.repository.ProfilingResultRepositoryImpl
import presentation.view.common.ContentContainer
import presentation.view.configuring.ConfiguringContentView
import presentation.view.profiling_details.ProfilingResultContentView
import presentation.view.profiling_details.TestProfilingResultDetailsContentView
import presentation.view.profiling_details.TestsProfilingResultContentView
import presentation.viewmodel.ConfiguringVM
import presentation.viewmodel.DetailedTestEnergyConsumptionVM
import presentation.viewmodel.ProfilingVM
import presentation.viewmodel.TestEnergyConsumptionListVM
import tooling.ContentRouterImpl
import tooling.RawProfilingResultAnalyzer
import tooling.RawProfilingResultParser
import javax.inject.Provider

class ToolWindowFactory : com.intellij.openapi.wm.ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val router = ContentRouterImpl(toolWindow)

        val providers = listOf<Provider<ContentContainer>>(
                Provider {
                    ConfiguringContentView(
                            project,
                            router,
                            ProfilingVM(project, ConfigurationRepositoryImpl, ProfilingResultRepositoryImpl),
                            ConfiguringVM(ConfigurationRepositoryImpl)
                    )
                },
                Provider {
                    ProfilingResultContentView(router)
                },
                Provider {
                    TestsProfilingResultContentView(router, TestEnergyConsumptionListVM(ProfilingResultRepositoryImpl))
                },
                Provider {
                    TestProfilingResultDetailsContentView(router, DetailedTestEnergyConsumptionVM(ProfilingResultRepositoryImpl))
                }
        )

//        val raw = RawProfilingResultParser.parse("${project.basePath!!}/app/profileOutput", "logs.json")
//        val result = RawProfilingResultAnalyzer.analyze(raw)
//        ProfilingResultRepositoryImpl.save(result)

        router.setupProviders(providers)
        router.toNextContent()
    }
}

//        Disposer.register(project, component)