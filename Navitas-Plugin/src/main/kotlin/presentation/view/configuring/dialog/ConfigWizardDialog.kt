package presentation.view.configuring.dialog

import com.intellij.openapi.project.Project
import com.intellij.ui.wizard.WizardDialog
import domain.model.ProfilingConfiguration

class ConfigWizardDialog(
        project: Project,
        private val callback: (ProfilingConfiguration) -> Unit
) : WizardDialog<ConfigModel>(true, true, ConfigModel(project)) {

    override fun onWizardGoalAchieved() {
        super.onWizardGoalAchieved()
        callback.invoke(myModel.getProfilingConfiguration())
    }
}
