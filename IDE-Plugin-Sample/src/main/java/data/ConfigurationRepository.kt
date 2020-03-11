package data

import com.intellij.openapi.module.Module
import com.intellij.psi.PsiFile
import data.model.ProfilingConfiguration
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

object ConfigurationRepository {

    private val androidAppModuleSubject = BehaviorSubject.create<Module>()
    private val instrumentedTestsSubject = BehaviorSubject.create<List<PsiFile>>()

    var androidAppModule: Observable<Module> = androidAppModuleSubject
    var instrumentedTests: Observable<List<PsiFile>> = instrumentedTestsSubject

    var isEmpty = true
        private set

    fun saveProfilingConfiguration(config: ProfilingConfiguration) {
        isEmpty = false
        androidAppModuleSubject.onNext(config.module)
        instrumentedTestsSubject.onNext(config.tests)
    }
}