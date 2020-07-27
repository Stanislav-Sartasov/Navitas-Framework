package extensions

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.module.Module
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.FilenameIndex
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.idea.refactoring.fqName.getKotlinFqName
import org.jetbrains.kotlin.idea.core.util.toPsiFile
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
import java.util.regex.Pattern

const val INSTRUMENTED_TEST_FOLDER_NAME = "androidTest"
const val ANDROID_MANIFEST_FILE_NAME = "AndroidManifest.xml"
const val TEST_ANNOTATION_TEXT = "@Test"

private fun Module.isAndroidModule(type: String?): Boolean {
    val manifest = FilenameIndex.getFilesByName(
            project,
            ANDROID_MANIFEST_FILE_NAME,
            moduleContentScope
    ).firstOrNull() ?: return false

    return type?.let {
        manifest.hasChildWithText(type)
    } ?: true
}

fun Module.isAndroidModule(): Boolean = isAndroidModule(null)

fun Module.findInstrumentedTestNames(): Map<String, List<String>> {
    val result = mutableMapOf<String, List<String>>()

    val manifest = FilenameIndex.getFilesByName(project, ANDROID_MANIFEST_FILE_NAME, moduleContentScope).firstOrNull()
    if (manifest != null) {
        val packagePattern: Pattern = Pattern.compile("package=\"([a-zA-Z0-9_.\\\\]+)\"")
        val matcher = packagePattern.matcher(manifest.text).apply { find() }
        val packageName = matcher.group(1)

        val testFiles =
                listOf(
                        FileTypeIndex.getFiles(JavaFileType.INSTANCE, moduleContentScope),
                        FileTypeIndex.getFiles(KotlinFileType.INSTANCE, moduleContentScope)
                )
                .flatten()
                .asSequence()
                .filter { file -> file.path.contains(INSTRUMENTED_TEST_FOLDER_NAME, ignoreCase = true) }
                .toList()

        for (file in testFiles) {
            when (val psiFile = file.toPsiFile(project)) {
                is KtFile -> {
                    for (declaration in psiFile.declarations) {
                        if (declaration is KtClass && declaration.hasChildWithText(TEST_ANNOTATION_TEXT)) {
                            val fullName = declaration.getKotlinFqName().toString()
                            val testClassName = fullName.substringAfter("$packageName.")
                            val testNames = mutableListOf<String>()

                            for (child in declaration.body!!.children) {
                                if (child is KtFunction) {
                                    val isTest = child.annotationEntries.find { annotation -> annotation.text == TEST_ANNOTATION_TEXT } != null
                                    if (isTest) testNames.add(child.name!!)
                                }
                            }
                            result[testClassName] = testNames
                        }
                    }
                }
                is PsiJavaFile -> {
                    for (clazz in psiFile.classes) {
                        if (clazz.hasChildWithText(TEST_ANNOTATION_TEXT)) {
                            val fullName = clazz.getKotlinFqName().toString()
                            val testClassName = fullName.substringAfter("$packageName.")
                            val testNames = mutableListOf<String>()

                            for (method in clazz.methods) {
                                val isTest = method.annotations.find { annotation -> annotation.text == TEST_ANNOTATION_TEXT } != null
                                if (isTest) testNames.add(method.name)
                            }
                            result[testClassName] = testNames
                        }
                    }
                }
            }
        }
    }

    return result
}