package components

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory

class GradlePluginInjector(private val project: Project) : ProjectComponent {

    companion object {
        private const val BUILD_GRADLE_FILE_NAME = "build.gradle"
        private const val BUILD_SCRIPT_BLOCK_NAME = "buildscript"
        private const val DEPENDENCIES_BLOCK_NAME = "dependencies"
        private const val REPOSITORIES_BLOCK_NAME = "repositories"

        private const val MAVEN_REPOSITORY_BLOCK_NAME = "maven"
        private const val CLASSPATH_DEPENDENCY_NAME = "classpath"
        private const val APPLY_PLUGIN_STATEMENT = "apply plugin:"
        private const val BREAK_LINE_SEPARATOR = "\n"
        private const val GRADLE_PROFILING_PLUGIN_FULL_NAME = "com.lanit:profilingPlugin:1.0-SNAPSHOT"
        private const val GRADLE_PROFILING_PLUGIN_SHORT_NAME = "profilingPlugin"
        private const val GRADLE_PROFILING_PLUGIN_REPOSITORY_NAME = "repo"
    }

    // TODO: checking and injecting on 'Profile' button click
    // TODO: build.gradle.kts injecting support
    // TODO: add extension functions
    override fun projectOpened() {
        super.projectOpened()

        val allBuildGradleFiles = FilenameIndex.getFilesByName(project, BUILD_GRADLE_FILE_NAME, GlobalSearchScope.projectScope(project))
        val rootBuildGradleFile = allBuildGradleFiles.first()
        val subBuildGradleFiles = allBuildGradleFiles.takeLast(allBuildGradleFiles.size - 1)

        val buildScriptBlock = rootBuildGradleFile.children.firstOrNull { child -> child.text.startsWith(BUILD_SCRIPT_BLOCK_NAME) }?.lastChild ?: return
        val dependenciesBlock = buildScriptBlock.children.firstOrNull { child -> child.text.startsWith(DEPENDENCIES_BLOCK_NAME) }?.lastChild ?: return
        val repositoriesBlock = buildScriptBlock.children.firstOrNull { child -> child.text.startsWith(REPOSITORIES_BLOCK_NAME) }?.lastChild ?: return

        ApplicationManager.getApplication().runWriteAction {
            WriteCommandAction.runWriteCommandAction(project) {
                val factory = GroovyPsiElementFactory.getInstance(rootBuildGradleFile.project)

                // adding plugin repository
                with(repositoriesBlock) {
                    val mavenRepoBlock = children.firstOrNull { child -> child.text.startsWith(MAVEN_REPOSITORY_BLOCK_NAME) }?.lastChild

                    if (mavenRepoBlock != null) {
                        with(mavenRepoBlock) {
                            if (!children.any { child -> child.text.contains(GRADLE_PROFILING_PLUGIN_REPOSITORY_NAME) }) {
                                addBefore(factory.createExpressionFromText("url = uri(\"$GRADLE_PROFILING_PLUGIN_REPOSITORY_NAME\")"), lastChild)
                                addBefore(factory.createLineTerminator(BREAK_LINE_SEPARATOR), lastChild)
                                CodeStyleManager.getInstance(project).reformat(this)
                            }
                        }
                    } else {
                        // TODO: how to create maven closable block?
                        addBefore(factory.createExpressionFromText("$MAVEN_REPOSITORY_BLOCK_NAME {\nurl = uri(\"$GRADLE_PROFILING_PLUGIN_REPOSITORY_NAME\")\n}"), lastChild)
                        addBefore(factory.createLineTerminator(BREAK_LINE_SEPARATOR), lastChild)
                        CodeStyleManager.getInstance(project).reformat(this)
                    }
                }

                // adding plugin classpath
                with(dependenciesBlock) {
                    if (!children.any { child -> child.text.contains(GRADLE_PROFILING_PLUGIN_FULL_NAME) }) {
                        addBefore(factory.createExpressionFromText("$CLASSPATH_DEPENDENCY_NAME \"$GRADLE_PROFILING_PLUGIN_FULL_NAME\""), lastChild)
                        addBefore(factory.createLineTerminator(BREAK_LINE_SEPARATOR), lastChild)
                        CodeStyleManager.getInstance(project).reformat(this)
                    }
                }

                // applying plugin
                // TODO: create build.gradle for module if it hasn't it yet
                for (file in subBuildGradleFiles) {
                    with(file) {
                        if (!children.any { child -> child.text.contains(GRADLE_PROFILING_PLUGIN_SHORT_NAME) }) {
                            addAfter(factory.createLineTerminator(BREAK_LINE_SEPARATOR), lastChild)
                            addAfter(factory.createExpressionFromText("$APPLY_PLUGIN_STATEMENT \"$GRADLE_PROFILING_PLUGIN_SHORT_NAME\""), lastChild)
                            CodeStyleManager.getInstance(project).reformat(this)
                        }
                    }
                }
            }
        }
    }
}