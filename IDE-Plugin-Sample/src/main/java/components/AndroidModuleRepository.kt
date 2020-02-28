package components

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import extensions.findInstrumentedTests
import extensions.isAndroidModule

class AndroidModuleRepository(private val project: Project) : ProjectComponent {

    private val _androidModules: MutableList<Module> = mutableListOf()
    val androidModules: List<Module> = _androidModules

    private val moduleTests: HashMap<String, List<PsiFile>> = HashMap()

    // TODO: fetching all android modules on 'Configure' button click
    override fun projectOpened() {
        super.projectOpened()
        _androidModules.addAll(
                ModuleManager.getInstance(project).modules.toList().filter { it.isAndroidModule() }
        )

        for (module in _androidModules) {
            moduleTests[module.moduleFilePath] = module.findInstrumentedTests()
        }
    }

    fun getTestsByModule(module: Module): List<PsiFile> {
        return moduleTests[module.moduleFilePath] ?: emptyList()
    }
}