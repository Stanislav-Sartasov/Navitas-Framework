package tooling

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import extensions.findInstrumentedTests
import extensions.isAndroidModule

class AndroidModuleProvider(private val project: Project) {

    fun fetchAndroidAppModuleList(): List<Module> {
        return ModuleManager.getInstance(project)
                .modules
                .toList()
                .filter { it.isAndroidModule() }
    }

    fun fetchTestList(module: Module): List<PsiFile> {
        return module.findInstrumentedTests()
    }
}