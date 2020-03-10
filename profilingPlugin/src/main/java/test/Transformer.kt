package test

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.BaseExtension
import javassist.ClassPool
import javassist.CtClass
import java.io.File
import java.util.*

class Transformer(
    private val android : BaseExtension,
    private val extension: InstrExtension
) : Transform() {
    override fun getName() : String = "profilingPlugin"

    override fun getInputTypes(): Set<QualifiedContent.ContentType> = setOf(QualifiedContent.DefaultContentType.CLASSES)

    override fun isIncremental(): Boolean = false

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> =
        mutableSetOf(QualifiedContent.Scope.PROJECT)

    override fun getReferencedScopes(): MutableSet<in QualifiedContent.Scope> =
        mutableSetOf(
            QualifiedContent.Scope.EXTERNAL_LIBRARIES,
            QualifiedContent.Scope.SUB_PROJECTS
        )

    override fun transform(transformInvocation: TransformInvocation){
        val variant = transformInvocation.context.variantName
        val applyTransform = variant.endsWith("Debug", true)

        val androidJar = "${android.sdkDirectory.absolutePath}/platforms/${android.compileSdkVersion}/android.jar"

        val externalJars = mutableListOf<File>()
        val externalDirs = mutableListOf<File>()

        transformInvocation.referencedInputs.forEach { input ->
            externalJars += input.jarInputs.map { it.file }
            externalDirs += input.directoryInputs.map { it.file }
        }

        transformInvocation.outputProvider.deleteAll()
        val outputDir = transformInvocation.outputProvider.getContentLocation(
            "classes",
            outputTypes,
            scopes,
            Format.DIRECTORY
        )

        transformInvocation.inputs.forEach {input ->
            input.directoryInputs.forEach {inputDirectory ->

                val baseDir = inputDirectory.file
                val pool = ClassPool()
                pool.appendSystemPath()
                pool.insertClassPath(baseDir.absolutePath)
                pool.insertClassPath(androidJar)
                externalJars.forEach { pool.insertClassPath(it.absolutePath) }
                externalDirs.forEach { pool.insertClassPath(it.absolutePath) }

                if (applyTransform) {
                    transformInput(inputDirectory, outputDir, pool)
                } else {
                    inputDirectory.file.copyRecursively(outputDir, true)
                }
            }
        }
    }
}

private fun transformInput(
    input: DirectoryInput,
    output: File,
    pool: ClassPool
) {
    input.file.walkTopDown().forEach { originalClassFile ->
        if (originalClassFile.isClassfile()) {
            val classname = originalClassFile.relativeTo(input.file).toClassname()
            val clazz = pool.get(classname)
            pool.importPackage("android.util.Log")
            transformClass(clazz)
            clazz.writeFile(output.absolutePath)
        }
    }
}

private fun transformClass(clazz: CtClass) {
    clazz.declaredMethods.forEach { method ->
        if (!method.isEmpty) {
            method.insertBefore(String.format("Log.d(\"TEST\", \"Entry ${method.name} " +
                    "TimeInStates ${getTimeInState()} EndOfData\");"))
            method.insertAfter(String.format("Log.d(\"TEST\", \"Exit ${method.name} " +
                    "TimeInStates ${getTimeInState()} EndOfData\");"))
        }
    }
}

private fun File.toClassname(): String =
    path.replace("/", ".")
        .replace("\\", ".")
        .replace(".class", "")

private fun File.isClassfile(): Boolean = isFile && path.endsWith(".class")

private fun getTimeInState(): String {
    var timeInState = ""
    for (i in 0 until 8) {
        val filePath = "/sys/devices/system/cpu/cpu$i/cpufreq/stats/time_in_state"
        val proc = Runtime.getRuntime().exec("adb shell cat \"$filePath\"")

        Scanner(proc.inputStream).use {
            while (it.hasNextLine())
                timeInState += it.nextLine() + " "
        }
    }
    return timeInState
}