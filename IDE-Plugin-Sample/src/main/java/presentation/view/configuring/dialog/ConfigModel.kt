package presentation.view.configuring.dialog

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.ui.wizard.WizardModel
import domain.model.ProfilingConfiguration
import presentation.view.configuring.dialog.steps.AndroidAppModuleChoosingStep
import presentation.view.configuring.dialog.steps.InstrumentedTestChoosingStep
import tooling.AndroidModuleProvider

class ConfigModel(
        project: Project
) : WizardModel("Navitas configuration") {

    private val provider = AndroidModuleProvider(project)
    private var selectedModule: Module? = null
    private var selectedTests: List<String> = emptyList()
    private val currentTestNames: MutableList<String> = mutableListOf()

    private val androidAppModules: List<Module> = provider.fetchAndroidAppModuleList()
    val androidAppModuleNames: List<String> = androidAppModules.map { module -> module.name }

    init {
        add(AndroidAppModuleChoosingStep(this))
        add(InstrumentedTestChoosingStep(this))
    }

    fun selectModule(position: Int) {
        selectedModule = androidAppModules[position]
    }

    fun selectTests(selectedTestArray: BooleanArray) {
        val result = mutableListOf<String>()
        for ((i, isSelected) in selectedTestArray.withIndex()) {
            if (isSelected) result.add(currentTestNames[i])
        }
        selectedTests = result
    }

    fun getTestNamesOfSelectedModule(): List<String> {
        currentTestNames.clear()
        selectedModule?.let { module ->
            currentTestNames.addAll(provider.fetchInstrumentedTestNames(module))
        }
        return currentTestNames
    }

    fun getProfilingConfiguration() = ProfilingConfiguration(selectedModule!!, selectedTests)
}
