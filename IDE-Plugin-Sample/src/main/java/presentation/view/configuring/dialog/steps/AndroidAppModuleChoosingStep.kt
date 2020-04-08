package presentation.view.configuring.dialog.steps

import com.intellij.ui.components.JBList
import com.intellij.ui.wizard.WizardNavigationState
import com.intellij.ui.wizard.WizardStep
import presentation.view.configuring.dialog.ConfigModel
import javax.swing.JComponent
import javax.swing.ListSelectionModel

class AndroidAppModuleChoosingStep(private val configModel: ConfigModel) : WizardStep<ConfigModel>("Choose module for deployment") {

    private lateinit var moduleList: JBList<String>
    private var isNextButtonFrozen = true

    override fun prepare(wizardNavigationState: WizardNavigationState): JComponent? {
        moduleList = JBList(configModel.androidAppModuleNames).apply {
            selectionMode = ListSelectionModel.SINGLE_SELECTION
            addListSelectionListener {
                wizardNavigationState.NEXT.isEnabled = true
                isNextButtonFrozen = false
            }
        }
        if (isNextButtonFrozen) wizardNavigationState.NEXT.isEnabled = false
        return moduleList
    }

    // TODO: problem with returning back from TestChooserStep: 'Next' button isn't reacted
    override fun onNext(model: ConfigModel?): WizardStep<*> {
        model?.selectModule(moduleList.selectedIndex)
        return super.onNext(model)
    }
}
