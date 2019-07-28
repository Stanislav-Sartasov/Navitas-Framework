package test

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

open class InstrPlugin : Plugin<Project>{
    override fun apply(target: Project){
        val extension = target.extensions.create("Instrum   entor", InstrExtension::class.java)

        val android = target.extensions.findByName("android") as BaseExtension
        android.registerTransform(Transformer(android, extension))
    }
}

open class InstrExtension {
    var applyFor: Array<String>? = null
}