package ui.dialogs

import com.intellij.ui.CheckBoxList
import com.intellij.ui.wizard.WizardNavigationState
import com.intellij.ui.wizard.WizardStep

import javax.swing.*

class TestChooserStep(private val model: ConfigurationModel) : WizardStep<ConfigurationModel>("Choose tests for profiling") {

    private val list: CheckBoxList<String> = CheckBoxList()
    private var selectedArray: BooleanArray? = null

    init {
        list.setCheckBoxListListener { i: Int, b: Boolean -> selectedArray?.let{ it[i] = b } }
    }

    override fun prepare(wizardNavigationState: WizardNavigationState): JComponent? {
        return list.also {
            val items = model.getTestNamesOfSelectedModule()
            selectedArray = BooleanArray(items.size) {false}
            it.setItems(items, String::toString)
        }
    }

    override fun onFinish(): Boolean {
        val indices = selectedArray!!.toList()
                .asSequence()
                .withIndex()
                .filter { (_, value) -> value }
                .map { (index, _) -> index }
                .toList()
        model.selectTests(indices)
        return super.onFinish()
    }
}
