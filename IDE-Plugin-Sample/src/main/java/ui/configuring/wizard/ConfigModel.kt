package ui.configuring.wizard

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.ui.wizard.WizardModel
import data.model.ProfilingConfiguration
import tooling.AndroidModuleProvider
import ui.configuring.wizard.steps.AndroidAppModuleChoosingStep
import ui.configuring.wizard.steps.InstrumentedTestChoosingStep

class ConfigModel(project: Project) : WizardModel("Navitas configuration") {

    private val provider = AndroidModuleProvider(project)
    private var selectedModule: Module? = null
    private var selectedTests: List<String> = emptyList()
    private val instrumentedTestNames: MutableList<String> = mutableListOf()

    private val androidAppModules: List<Module> = provider.fetchAndroidAppModuleList()
    val androidAppModuleNames: List<String> = androidAppModules.map { module -> module.name }

    init {
        add(AndroidAppModuleChoosingStep(this))
        add(InstrumentedTestChoosingStep(this))
    }

    fun selectModule(index: Int) {
        selectedModule = androidAppModules[index]
    }

    fun selectTests(indices: List<Int>) {
        selectedTests = instrumentedTestNames.asSequence()
                .withIndex()
                .filter { (index, _) -> indices.contains(index) }
                .map { (_, value) -> value }
                .toList()
    }

    fun getTestNamesOfSelectedModule(): List<String> {
        instrumentedTestNames.clear()
        selectedModule?.let { module ->
            instrumentedTestNames.addAll(provider.fetchInstrumentedTestNames(module))
        }
        return instrumentedTestNames
    }

    fun getProfilingConfiguration() = ProfilingConfiguration(selectedModule!!, selectedTests)
}
