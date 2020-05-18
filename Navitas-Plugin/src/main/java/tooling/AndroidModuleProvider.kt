package tooling

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import extensions.findInstrumentedTestNames
import extensions.isAndroidModule

class AndroidModuleProvider(private val project: Project) {

    fun fetchAndroidModuleList(): List<Module> {
        return ModuleManager.getInstance(project)
                .modules
                .filter { module -> module.isAndroidModule() }
    }

    fun fetchInstrumentedTestNames(module: Module): Map<String, List<String>> {
        return module.findInstrumentedTestNames()
    }
}