package ui.dialogs

import com.intellij.ui.wizard.WizardDialog

class ConfigurationDialog(private val callback: (ConfigurationModel) -> Unit)
    : WizardDialog<ConfigurationModel>(true, true, ConfigurationModel()) {

    override fun onWizardGoalAchieved() {
        super.onWizardGoalAchieved()
        callback.invoke(myModel)
    }
}
