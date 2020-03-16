package extensions

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.module.Module
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.FilenameIndex
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.idea.refactoring.fqName.getKotlinFqName
import org.jetbrains.kotlin.idea.refactoring.toPsiFile
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import java.util.regex.Pattern

const val BUILD_GRADLE_FILE_NAME = "build.gradle"
const val PLUGIN_ANDROID_LIBRARY_NAME = "com.android.library"
const val PLUGIN_ANDROID_APPLICATION_NAME = "com.android.application"
const val INSTRUMENTED_TEST_FOLDER_NAME = "androidTest"
const val ANDROID_MANIFEST_FILE_NAME = "AndroidManifest.xml"
const val TEST_ANNOTATION = "@Test"

// TODO: checking for the presence of AndroidManifest file because module may not have build.gradle file
private fun Module.isAndroidModule(type: String): Boolean {
    return FilenameIndex.getFilesByName(
            project,
            BUILD_GRADLE_FILE_NAME,
            moduleContentScope
    ).firstOrNull()?.hasChildWithText(type) ?: false
}

fun Module.isAndroidLibraryModule(): Boolean = isAndroidModule(PLUGIN_ANDROID_LIBRARY_NAME)

fun Module.isAndroidApplicationModule(): Boolean = isAndroidModule(PLUGIN_ANDROID_APPLICATION_NAME)

fun Module.isAndroidModule(): Boolean = isAndroidLibraryModule() || isAndroidApplicationModule()

fun Module.findInstrumentedTestNames(): List<String> {
    val result = mutableListOf<String>()

    val manifest = FilenameIndex.getFilesByName(project, ANDROID_MANIFEST_FILE_NAME, moduleContentScope).first()!!
    val packagePattern: Pattern = Pattern.compile("package=\"([a-zA-Z0-9_.\\\\]+)\"")
    val matcher = packagePattern.matcher(manifest.text).apply { find() }
    val packageName = matcher.group(1)

    val testFiles = listOf(FileTypeIndex.getFiles(JavaFileType.INSTANCE, moduleContentScope), FileTypeIndex.getFiles(KotlinFileType.INSTANCE, moduleContentScope))
            .flatten()
            .asSequence()
            .filter { file -> file.path.contains(INSTRUMENTED_TEST_FOLDER_NAME, ignoreCase = true) }
            .toList()

    for (file in testFiles) {
        when (val psiFile = file.toPsiFile(project)) {
            is KtFile -> {
                for (declaration in psiFile.declarations) {
                    if (declaration is KtClass && declaration.hasChildWithText(TEST_ANNOTATION)) {
                        val fullName = declaration.getKotlinFqName().toString()
                        val shortName = fullName.substringAfter("$packageName.")
                        result.add(shortName)
                    }
                }
            }
            is PsiJavaFile -> {
                for (clazz in psiFile.classes) {
                    if (clazz.hasChildWithText(TEST_ANNOTATION)) {
                        val fullName = clazz.getKotlinFqName().toString()
                        val shortName = fullName.substringAfter("$packageName.")
                        result.add(shortName)
                    }
                }
            }
        }
    }

    return result
}