package ui.dialogs

import com.intellij.ui.components.JBList
import com.intellij.ui.wizard.WizardNavigationState
import com.intellij.ui.wizard.WizardStep
import javax.swing.JComponent

class ModuleChooserStep(model: ConfigurationModel) : WizardStep<ConfigurationModel>("Choose module for deployment") {

    private val list: JBList<String> = JBList()

    init {
        list.setListData(model.moduleNames.toTypedArray())
    }

    override fun prepare(wizardNavigationState: WizardNavigationState): JComponent? {
        return list
    }

    override fun onNext(model: ConfigurationModel?): WizardStep<*> {
        model?.selectModule(list.selectedIndex) // TODO: what will happen if there is't selected item? --- while button 'next' is couldn't pressed
        return super.onNext(model)
    }
}
