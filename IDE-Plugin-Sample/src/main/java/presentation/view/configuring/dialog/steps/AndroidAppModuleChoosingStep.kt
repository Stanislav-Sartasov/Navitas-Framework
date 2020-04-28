package presentation.view.configuring.dialog.steps

import com.intellij.ui.components.JBList
import com.intellij.ui.wizard.WizardNavigationState
import com.intellij.ui.wizard.WizardStep
import presentation.view.configuring.dialog.ConfigModel
import javax.swing.JComponent
import javax.swing.ListSelectionModel

class AndroidAppModuleChoosingStep(
        configModel: ConfigModel
) : WizardStep<ConfigModel>("Choose module for profiling") {

    private val moduleList: JBList<String>
    private var selectedModulePosition = -1

    init {
        // create UI components
        moduleList = JBList(configModel.androidAppModuleNames)
        moduleList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        moduleList.addListSelectionListener { event ->
            if (!event.valueIsAdjusting) {
                selectedModulePosition = moduleList.selectedIndex
                configModel.currentNavigationState.NEXT.isEnabled = (selectedModulePosition != -1)
            }
        }
    }

    override fun prepare(wizardNavigationState: WizardNavigationState): JComponent? {
        wizardNavigationState.NEXT.isEnabled = (selectedModulePosition != -1)
        return moduleList
    }

    override fun onNext(model: ConfigModel): WizardStep<*> {
        model.selectModule(selectedModulePosition)
        return super.onNext(model)
    }
}
