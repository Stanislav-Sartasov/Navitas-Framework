package ui.configuring.wizard

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.ui.wizard.WizardModel
import tooling.AndroidModuleProvider
import ui.configuring.wizard.steps.AndroidAppModuleChoosingStep
import ui.configuring.wizard.steps.InstrumentedTestChoosingStep

class ConfigModel(private val project: Project) : WizardModel("Navitas configuration") {

    private val repository = AndroidModuleProvider(project)
    private var selectedModule: Module? = null
    private var selectedTests: List<PsiFile> = emptyList()
    private val currentTests: MutableList<PsiFile> = mutableListOf()

    private val androidAppModules: List<Module> = repository.fetchAndroidAppModuleList()
    val androidAppModuleNames: List<String> = androidAppModules.map { module -> module.moduleFilePath }

    init {
        add(AndroidAppModuleChoosingStep(this))
        add(InstrumentedTestChoosingStep(this))
    }

    fun selectModule(index: Int) {
        selectedModule = androidAppModules[index]
    }

    fun selectTests(indices: List<Int>) {
        selectedTests = currentTests.asSequence()
                .withIndex()
                .filter { (index, _) -> indices.contains(index) }
                .map { (_, value) -> value }
                .toList()
    }

    // TODO: how to get package name of PsiFile ???
    fun getTestNamesOfSelectedModule(): List<String> {
        currentTests.clear()
        return selectedModule?.let {
            currentTests.addAll(repository.fetchTestList(it))
            currentTests.map { test -> test.name }
        } ?: emptyList()
    }

    fun onFinish() {
        // TODO: send harvested data to somebody (may be Presenter)
    }
}
