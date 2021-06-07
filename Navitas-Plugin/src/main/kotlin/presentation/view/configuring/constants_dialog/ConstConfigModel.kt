package presentation.view.configuring.constants_dialog

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.ui.wizard.WizardModel
import domain.model.ProfilingConfiguration
import org.jetbrains.kotlin.idea.configuration.externalProjectPath
import presentation.view.configuring.constants_dialog.steps.InstrumentedTestChoosingStep
import tooling.AndroidModuleProvider

class ConstConfigModel(
        project: Project
) : WizardModel("Constants configuration") {

    private val provider = AndroidModuleProvider(project)
    private var selectedTests: Map<String, List<String>>? = null
    private var currentTestNames: Map<String, List<String>>? = null

    private var selectedModule: Module? = provider.fetchAndroidModuleList().
    find{ module -> module.name.split('.').contains("navi_constants")}

    init {
        add(InstrumentedTestChoosingStep(this))
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

    fun getProfilingConfiguration() = ProfilingConfiguration(selectedModule!!.name, selectedModule!!.externalProjectPath!!, selectedTests!!)
}
