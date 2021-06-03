package presentation.view.configuring.constants_dialog

import com.intellij.openapi.project.Project
import com.intellij.ui.wizard.WizardDialog
import domain.model.ProfilingConfiguration

class ConstConfigWizardDialog(
        project: Project,
        private val callback: (ProfilingConfiguration) -> Unit
) : WizardDialog<ConstConfigModel>(true, true, ConstConfigModel(project)) {

    override fun onWizardGoalAchieved() {
        super.onWizardGoalAchieved()
        callback.invoke(myModel.getProfilingConfiguration())
    }
}
