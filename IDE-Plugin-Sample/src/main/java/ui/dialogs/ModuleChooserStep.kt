package ui.dialogs

import com.intellij.ui.components.JBList
import com.intellij.ui.wizard.WizardNavigationState
import com.intellij.ui.wizard.WizardStep
import javax.swing.JComponent
import javax.swing.ListSelectionModel

class ModuleChooserStep(private val configModel: ConfigurationModel) : WizardStep<ConfigurationModel>("Choose module for deployment") {

    private lateinit var moduleList: JBList<String>
    private var isNextButtonFrozen = true

    override fun prepare(wizardNavigationState: WizardNavigationState): JComponent? {
        moduleList = JBList(configModel.moduleNames).apply {
            selectionMode = ListSelectionModel.SINGLE_SELECTION
            addListSelectionListener {
                wizardNavigationState.NEXT.isEnabled = true
                isNextButtonFrozen = false
            }
        }
        if (isNextButtonFrozen) wizardNavigationState.NEXT.isEnabled = false
        return moduleList
    }

    override fun onNext(model: ConfigurationModel?): WizardStep<*> {
        println("ModuleChooserStep::onNext") // TODO: problem with returning back from TestChooserStep
        model?.selectModule(moduleList.selectedIndex)
        return super.onNext(model)
    }
}
