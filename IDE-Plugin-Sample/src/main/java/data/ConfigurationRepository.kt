package data

import com.intellij.openapi.components.ProjectComponent
import com.intellij.psi.PsiFile
import com.intellij.openapi.module.Module

class ConfigurationRepository : ProjectComponent {

    var appModule: Module? = null
    var testClasses: List<PsiFile> = emptyList()

}