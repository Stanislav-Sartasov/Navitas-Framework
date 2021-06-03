package presentation.view.configuring.profiling_dialog.steps

import com.intellij.ui.CheckBoxList
import com.intellij.ui.wizard.WizardNavigationState
import com.intellij.ui.wizard.WizardStep
import presentation.view.configuring.profiling_dialog.ProfConfigModel
import javax.swing.JComponent

class InstrumentedTestChoosingStep(
        private val profConfigModel: ProfConfigModel
) : WizardStep<ProfConfigModel>("Choose tests for profiling") {

    private val testList: CheckBoxList<String>
    private lateinit var selectedTestArray: BooleanArray
    private var selectedTestAmount = 0

    init {
        // create UI components
        testList = CheckBoxList()
        testList.setCheckBoxListListener { position: Int, isChecked: Boolean ->
            selectedTestArray[position] = isChecked
            selectedTestAmount += if (isChecked) 1 else -1
            profConfigModel.currentNavigationState.FINISH.isEnabled = (selectedTestAmount != 0)
        }
    }

    override fun prepare(wizardNavigationState: WizardNavigationState): JComponent? {
        val items = profConfigModel.getTestNamesOfSelectedModule().entries.map { testClass -> testClass.value.map { testName -> "${testClass.key}.$testName"}}.flatten()
        selectedTestArray = BooleanArray(items.size)
        testList.setItems(items, String::toString)
        selectedTestAmount = 0

        wizardNavigationState.FINISH.isEnabled = (selectedTestAmount != 0)
        return testList
    }

    override fun onFinish(): Boolean {
        profConfigModel.selectTests(selectedTestArray)
        return super.onFinish()
    }
}
