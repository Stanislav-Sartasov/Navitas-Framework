package ui.dialogs

import com.intellij.ui.CheckBoxList
import com.intellij.ui.wizard.WizardNavigationState
import com.intellij.ui.wizard.WizardStep

import javax.swing.*

class TestChooserStep(private val model: ConfigurationModel) : WizardStep<ConfigurationModel>("Test chooser") {

    private val list: CheckBoxList<String> = CheckBoxList()
    private val selectedArray: BooleanArray

    init {
        val items = model.tests;
        selectedArray = BooleanArray(items.size)
        list.setItems(items, String::toString)
        list.setCheckBoxListListener { i: Int, b: Boolean -> selectedArray[i] = b }
    }

    override fun prepare(wizardNavigationState: WizardNavigationState): JComponent? {
        return list
    }

    /*

     @Override
    protected void doOKAction() {
        System.out.print("OK is pressed. Selected item status: ");
        for (boolean isSelected : selectedArray) System.out.print(isSelected + " ");
        System.out.println();
        close(0);
        //TODO: pass selectedArray to somebody (may be to Presenter?)
    }

     */
}
