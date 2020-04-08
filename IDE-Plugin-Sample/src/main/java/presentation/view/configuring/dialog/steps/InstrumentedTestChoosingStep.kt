package presentation.view.configuring.dialog.steps

import com.intellij.ui.CheckBoxList
import com.intellij.ui.wizard.WizardNavigationState
import com.intellij.ui.wizard.WizardStep
import presentation.view.configuring.dialog.ConfigModel
import javax.swing.JComponent

class InstrumentedTestChoosingStep(private val configModel: ConfigModel) : WizardStep<ConfigModel>("Choose tests for profiling") {

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
