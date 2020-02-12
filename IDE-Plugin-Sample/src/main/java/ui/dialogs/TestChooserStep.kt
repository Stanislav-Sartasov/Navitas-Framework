package ui.dialogs

import com.intellij.ui.CheckBoxList
import com.intellij.ui.wizard.WizardNavigationState
import com.intellij.ui.wizard.WizardStep

import javax.swing.*

class TestChooserStep(private val configModel: ConfigurationModel) : WizardStep<ConfigurationModel>("Choose tests for profiling") {

    private lateinit var testList: CheckBoxList<String>
    private lateinit var selectedArray: BooleanArray

    override fun prepare(wizardNavigationState: WizardNavigationState): JComponent? {
        testList = CheckBoxList<String>().apply {
            val items = configModel.getTestNamesOfSelectedModule()
            selectedArray = BooleanArray(items.size)
            setItems(items, String::toString)
            setCheckBoxListListener { i: Int, b: Boolean ->
                selectedArray.let { it[i] = b }
            }
        }
        return testList
    }

    override fun onFinish(): Boolean {
        val indices = selectedArray.toList()
                .asSequence()
                .withIndex()
                .filter { (_, value) -> value }
                .map { (index, _) -> index }
                .toList()
        configModel.selectTests(indices)
        return super.onFinish()
    }
}
