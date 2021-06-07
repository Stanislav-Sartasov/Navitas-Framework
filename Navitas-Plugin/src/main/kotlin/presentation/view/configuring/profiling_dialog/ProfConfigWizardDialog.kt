package presentation.view.configuring.profiling_dialog

import com.intellij.openapi.project.Project
import com.intellij.ui.wizard.WizardDialog
import domain.model.ProfilingConfiguration

class ProfConfigWizardDialog(
        project: Project,
        private val callback: (ProfilingConfiguration) -> Unit
) : WizardDialog<ProfConfigModel>(true, true, ProfConfigModel(project)) {

    override fun onWizardGoalAchieved() {
        super.onWizardGoalAchieved()
        callback.invoke(myModel.getProfilingConfiguration())
    }
}
