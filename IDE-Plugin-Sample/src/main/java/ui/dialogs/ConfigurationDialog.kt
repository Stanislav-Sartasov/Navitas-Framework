package ui.dialogs

import com.intellij.openapi.project.Project
import com.intellij.ui.wizard.WizardDialog

class ConfigurationDialog(
        project: Project,
        private val callback: (ConfigurationModel) -> Unit
) : WizardDialog<ConfigurationModel>(true, true, ConfigurationModel(project)) {

    override fun onWizardGoalAchieved() {
        super.onWizardGoalAchieved()
        callback.invoke(myModel)
    }
}
