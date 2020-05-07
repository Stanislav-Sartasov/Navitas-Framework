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
    private var selectedTests: Map<String, List<String>>? = null
    private var currentTestNames: Map<String, List<String>>? = null

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
        val result = mutableMapOf<String, MutableList<String>>()
        val items = currentTestNames!!.entries.toList()
        var classIndex = 0
        var methodIndex = 0

        for (isSelected in selectedTestArray) {
            if (isSelected) {
                val className = items[classIndex].key
                val methodName = items[classIndex].value[methodIndex]
                if (result[className] == null) result[className] = mutableListOf()
                result[className]!!.add(methodName)
            }
            methodIndex++
            if (methodIndex == items[classIndex].value.size) {
                methodIndex = 0
                classIndex++
            }
        }
        selectedTests = result
    }

    fun getTestNamesOfSelectedModule(): Map<String, List<String>> = provider.fetchInstrumentedTestNames(selectedModule!!).also { currentTestNames = it }

    fun getProfilingConfiguration() = ProfilingConfiguration(selectedModule!!, selectedTests!!)
}
