package presentation.view

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import data.repository.ConfigurationRepositoryImpl
import data.repository.ConstantsResultRepositoryImpl
import data.repository.PowerProfileRepositoryImpl
import data.repository.ProfilingResultRepositoryImpl
import presentation.view.common.ContentContainer
import presentation.view.configuring.ConfiguringContentView
import presentation.view.profiling_details.TestProfilingResultDetailsContentView
import presentation.view.profiling_details.TestsProfilingResultContentView
import presentation.viewmodel.*
import tooling.ContentRouterImpl
import javax.inject.Provider

class ToolWindowFactory : com.intellij.openapi.wm.ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val router = ContentRouterImpl(toolWindow)

        val configRepository = ConfigurationRepositoryImpl()
        val powerProfileRepository = PowerProfileRepositoryImpl()
        val profilingResultRepository = ProfilingResultRepositoryImpl()
        val constantsResultRepository = ConstantsResultRepositoryImpl()

        val providers = listOf<Provider<ContentContainer>>(
                Provider {
                    ConfiguringContentView(
                        project,
                        router,
                        ProfilingAndConstantsVM(project, configRepository, profilingResultRepository, constantsResultRepository, powerProfileRepository),
                        ConfiguringVM(configRepository),
                        PowerProfileVM(powerProfileRepository)
                    )
                },
                Provider {
                    TestsProfilingResultContentView(router, TestEnergyConsumptionListVM(profilingResultRepository))
                },
                Provider {
                    TestProfilingResultDetailsContentView(router, DetailedTestEnergyConsumptionVM(profilingResultRepository))
                }
        )

        router.setupProviders(providers)
        router.toNextContent()
    }
}