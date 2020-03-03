package ui.configuring.wizard

import com.intellij.openapi.project.Project
import com.intellij.ui.wizard.WizardDialog

class ConfigWizardDialog(
        project: Project,
        private val callback: (ConfigModel) -> Unit
) : WizardDialog<ConfigModel>(true, true, ConfigModel(project)) {

    override fun onWizardGoalAchieved() {
        super.onWizardGoalAchieved()
        callback.invoke(myModel)
    }
}
