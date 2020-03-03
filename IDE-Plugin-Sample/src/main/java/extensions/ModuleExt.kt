package extensions

import com.intellij.openapi.module.Module
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex

const val BUILD_GRADLE_FILE_NAME = "build.gradle"
const val PLUGIN_ANDROID_LIBRARY_NAME = "com.android.library"
const val PLUGIN_ANDROID_APPLICATION_NAME = "com.android.application"
const val INSTRUMENTED_TEST_FOLDER_NAME = "androidTest"
const val TEST_ANNOTATION = "@Test"
val FILE_EXTENSIONS = listOf("kt", "java")

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

// TODO: what if several classes in one file ???
fun Module.findInstrumentedTests(): List<PsiFile> {
    val result = mutableListOf<PsiFile>()
    for (fileExt in FILE_EXTENSIONS) {
        val fileNames = FilenameIndex.getAllFilesByExt(project, fileExt, moduleContentScope)
                .asSequence()
                .filter { file -> file.path.contains(INSTRUMENTED_TEST_FOLDER_NAME, ignoreCase = true) }
                .map { file -> file.name }
                .toList()

        for (fileName in fileNames) {
            result.addAll(
                    FilenameIndex.getFilesByName(project, fileName, moduleContentScope)
                            .filter { file -> file.hasChildWithText(TEST_ANNOTATION) }
            )
        }
    }
    return result
}