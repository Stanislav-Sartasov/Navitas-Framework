package presentation.view.configuring.profiling_dialog.steps

import com.intellij.ui.components.JBList
import com.intellij.ui.wizard.WizardNavigationState
import com.intellij.ui.wizard.WizardStep
import presentation.view.configuring.profiling_dialog.ProfConfigModel
import javax.swing.JComponent
import javax.swing.ListSelectionModel

class AndroidModuleChoosingStep(
    profConfigModel: ProfConfigModel
) : WizardStep<ProfConfigModel>("Choose Android module for profiling") {

    private val moduleList: JBList<String>
    private var selectedModulePosition = -1

    init {
        // create UI components
        moduleList = JBList(profConfigModel.androidModuleNames)
        moduleList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        moduleList.addListSelectionListener { event ->
            if (!event.valueIsAdjusting) {
                selectedModulePosition = moduleList.selectedIndex
                profConfigModel.currentNavigationState.NEXT.isEnabled = (selectedModulePosition != -1)
            }
        }
    }

    override fun prepare(wizardNavigationState: WizardNavigationState): JComponent? {
        wizardNavigationState.NEXT.isEnabled = (selectedModulePosition != -1)
        return moduleList
    }

    override fun onNext(modelProf: ProfConfigModel): WizardStep<*> {
        modelProf.selectModule(selectedModulePosition)
        return super.onNext(modelProf)
    }
}
