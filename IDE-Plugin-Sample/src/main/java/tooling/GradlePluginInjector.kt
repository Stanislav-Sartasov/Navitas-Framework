package tooling

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import extensions.*
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory

class GradlePluginInjector(private val project: Project) {

    companion object {
        private const val BUILD_GRADLE_FILE_NAME = "build.gradle"
        private const val BUILD_SCRIPT_BLOCK_NAME = "buildscript"
        private const val DEPENDENCIES_BLOCK_NAME = "dependencies"
        private const val REPOSITORIES_BLOCK_NAME = "repositories"
        private const val MAVEN_BLOCK = "maven"

        private const val GRADLE_PROFILING_PLUGIN_FULL_NAME = "com.lanit:profilingPlugin:1.0-SNAPSHOT"
        private const val GRADLE_PROFILING_PLUGIN_SHORT_NAME = "profilingPlugin"
        private const val GRADLE_PROFILING_PLUGIN_REPOSITORY_NAME = "repo"
    }

    // TODO: build.gradle.kts injecting support
    // TODO: how to create method call expression with closable block?
    fun verifyAndInject() {

        val allBuildGradleFiles = FilenameIndex.getFilesByName(project, BUILD_GRADLE_FILE_NAME, GlobalSearchScope.projectScope(project))
        val rootBuildGradleFile = allBuildGradleFiles.first()
        val subBuildGradleFiles = allBuildGradleFiles.takeLast(allBuildGradleFiles.size - 1)

        val buildScriptBlock = rootBuildGradleFile.findChildWithPrefix(BUILD_SCRIPT_BLOCK_NAME)?.lastChild ?: return
        val dependenciesBlock = buildScriptBlock.findChildWithPrefix(DEPENDENCIES_BLOCK_NAME)?.lastChild ?: return
        val repositoriesBlock = buildScriptBlock.findChildWithPrefix(REPOSITORIES_BLOCK_NAME)?.lastChild ?: return

        ApplicationManager.getApplication().runWriteAction {
            WriteCommandAction.runWriteCommandAction(project) {
                val factory = GroovyPsiElementFactory.getInstance(rootBuildGradleFile.project)

                // adding plugin repository
                with(repositoriesBlock) {
                    val mavenBlock = findChildWithPrefix(MAVEN_BLOCK)?.lastChild

                    mavenBlock?.let {
                        with(it) {
                            if (!hasChildWithText(GRADLE_PROFILING_PLUGIN_REPOSITORY_NAME)) {
                                addBefore(factory.createUrlPathExpression(GRADLE_PROFILING_PLUGIN_REPOSITORY_NAME), lastChild)
                                addBefore(factory.createBreakLineElement(), lastChild)
                                CodeStyleManager.getInstance(project).reformat(this)
                            }
                        }
                    } ?: factory.createClosableExpression(MAVEN_BLOCK, factory.createUrlPathExpression(GRADLE_PROFILING_PLUGIN_REPOSITORY_NAME)).also {
                        addBefore(it, lastChild)
                        CodeStyleManager.getInstance(project).reformat(this)
                    }
                }

                // adding plugin classpath
                with(dependenciesBlock) {
                    if (!hasChildWithText(GRADLE_PROFILING_PLUGIN_FULL_NAME)) {
                        addBefore(factory.createClassPathExpression(GRADLE_PROFILING_PLUGIN_FULL_NAME), lastChild)
                        addBefore(factory.createBreakLineElement(), lastChild)
                        CodeStyleManager.getInstance(project).reformat(this)
                    }
                }

                // applying plugin
                // TODO: create build.gradle for module if it hasn't it yet
                for (file in subBuildGradleFiles) {
                    with(file) {
                        if (!hasChildWithText(GRADLE_PROFILING_PLUGIN_SHORT_NAME)) {
                            addAfter(factory.createBreakLineElement(), lastChild)
                            addAfter(factory.createApplyPluginExpression(GRADLE_PROFILING_PLUGIN_SHORT_NAME), lastChild)
                            CodeStyleManager.getInstance(project).reformat(this)
                        }
                    }
                }
            }
        }
    }
}