package ui.dialogs

import com.intellij.ui.wizard.WizardModel

class ConfigurationModel : WizardModel("Configuration Wizard Model") {

    val tests: List<String> = listOf("1", "2", "3")

    init {
        add(ApkChooserStep())
        add(TestChooserStep(this))
    }

    fun onFinish() {
        println("Finish")
    }
}
