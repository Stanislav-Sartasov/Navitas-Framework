package data.model

import com.intellij.openapi.module.Module
import com.intellij.psi.PsiFile

class ProfilingConfiguration(val module: Module, val tests: List<PsiFile>)