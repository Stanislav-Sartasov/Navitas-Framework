package data

import com.intellij.openapi.module.Module
import com.intellij.psi.PsiFile

object ConfigRepository {
    var androidAppModule: Module? = null
    var instrumentedTests: List<PsiFile> = emptyList()

    fun isEmpty(): Boolean = androidAppModule == null || instrumentedTests.isEmpty()
}