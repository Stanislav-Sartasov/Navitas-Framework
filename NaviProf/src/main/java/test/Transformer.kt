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
            pool.importPackage("java.io.FileNotFoundException")
            pool.importPackage("java.io.File")
            pool.importPackage("java.util.Scanner")
            pool.importPackage("android.provider.Settings")
            transformClass(clazz)
            clazz.writeFile(output.absolutePath)
        }
    }
}

private fun transformClass(clazz: CtClass) {
    clazz.declaredMethods.forEach { method ->
        if (!method.isEmpty) {
            val getComponentsInfoCode = "StringBuilder timeInStateBuilder = new StringBuilder();\n" +
                    "int cores = Runtime.getRuntime().availableProcessors();\n" +
                    "for (int i = 0; i < cores; i++) {\n" +
                    "    timeInStateBuilder.append(\"cpu\");\n" +
                    "    timeInStateBuilder.append(i);\n" +
                    "    timeInStateBuilder.append(\" \");\n" +
                    "    String filePath = \"/sys/devices/system/cpu/cpu\" + i + \"/cpufreq/stats/time_in_state\";\n" +
                    "\n" +
                    "    try {\n" +
                    "        Scanner sc = new Scanner(new File(filePath));\n" +
                    "        while (sc.hasNextLine()) {\n" +
                    "            timeInStateBuilder.append(sc.nextLine());\n" +
                    "            timeInStateBuilder.append(\" \");\n" +
                    "        }\n" +
                    "\n" +
                    "        timeInStateBuilder.append(\"; \");\n" +
                    "    } catch (FileNotFoundException e) {\n" +
                    "        e.printStackTrace();\n" +
                    "    }\n" +
                    "}\n" +
                    "String timeInState = timeInStateBuilder.toString();\n" +
                    "\n" +
                    "int brightness = 0;\n"

            method.insertBefore("{" + getComponentsInfoCode +
                    "        StringBuilder logMessage = new StringBuilder(\"Entry ${clazz.name}.${method.name} TimeInStates \");\n" +
                    "        logMessage.append(timeInStateBuilder);\n" +
                    "        logMessage.append(\" EndOfData \");\n" +
                    "        logMessage.append(brightness);\n" +
                    "        Log.d(\"TEST\", logMessage.toString());" + "}")
            method.insertAfter("{" + getComponentsInfoCode +
                    "        StringBuilder logMessage = new StringBuilder(\"Exit ${clazz.name}.${method.name} TimeInStates \");\n" +
                    "        logMessage.append(timeInStateBuilder);\n" +
                    "        logMessage.append(\" EndOfData \");\n" +
                    "        logMessage.append(brightness);\n" +
                    "        Log.d(\"TEST\", logMessage.toString());" + "}")
        }
    }
}

private fun File.toClassname(): String =
    path.replace("/", ".")
        .replace("\\", ".")
        .replace(".class", "")

private fun File.isClassfile(): Boolean = isFile && path.endsWith(".class")

//private fun getTimeInState(): String {
//    var timeInState = ""
//    for (i in 0 until 8) {
//        timeInState += "cpu$i "
//        val filePath = "/sys/devices/system/cpu/cpu$i/cpufreq/stats/time_in_state"
//        val proc = Runtime.getRuntime().exec("adb shell cat \"$filePath\"")
//
//        Scanner(proc.inputStream).use {
//            while (it.hasNextLine())
//                timeInState += it.nextLine() + " "
//            timeInState += "; "
//        }
//    }
//    return timeInState
//}
//
//private fun getScreenBrightness(): Int {
//    val proc = Runtime.getRuntime().exec("adb shell settings get system screen_brightness")
//
//    Scanner(proc.inputStream).use {
//        return if (it.hasNextInt())
//            it.nextInt()
//        else
//            -1
//    }
//}