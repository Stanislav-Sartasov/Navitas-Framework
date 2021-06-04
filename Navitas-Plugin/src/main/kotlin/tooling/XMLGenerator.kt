package tooling

import domain.model.EnergyConstant
import org.redundent.kotlin.xml.*
import java.io.File

object XMLGenerator {
    fun powerProfile(constantsList : List<EnergyConstant>, directory : String) {
        val constantsOutput = File("$directory/power_profile.xml")

        val powerProfile = xml("device", "utf-8", XmlVersion.V10) {
            attribute("name", "Android")

            constantsList.forEach {
                "item" {
                    attribute("name", it.component)

                    -it.constant.toString()
                }
            }
        }.toString(PrintOptions(
            pretty = true,
            singleLineTextElements = true,
            useSelfClosingTags = false,
            useCharacterReference = false,
            indent = "\t"
        ))

        constantsOutput.writeText(powerProfile)
    }
}