package ui.dialogs

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.ui.wizard.WizardModel
import components.AndroidModuleRepository

class ConfigurationModel(project: Project) : WizardModel("Navitas configuration") {

    private val repository = project.getComponent(AndroidModuleRepository::class.java)
    private var selectedModule: Module? = null
    private var selectedTests: List<PsiFile>? = null
    private val modules: List<Module> = repository.androidModules
    private val currentTests: MutableList<PsiFile> = mutableListOf()

    val moduleNames: List<String> = modules.map { it.name }

    init {
        add(ModuleChooserStep(this))
        add(TestChooserStep(this))
    }

    fun selectModule(index: Int) {
        selectedModule = modules[index]
    }

    fun selectTests(indices: List<Int>) {
        selectedTests = currentTests.asSequence()
                .withIndex()
                .filter { (index, _) -> indices.contains(index) }
                .map { (_, value) -> value }
                .toList()
    }

    fun getTestNamesOfSelectedModule(): List<String> {
        currentTests.clear()
        return selectedModule?.let {
            currentTests.addAll(repository.getTestsByModule(it))
            currentTests.map { test -> test.name }
        } ?: emptyList()
    }

    fun onFinish() {
        println(selectedTests?.map { it.name })
        //TODO: send harvested data to somebody (may be Presenter)
    }
}
