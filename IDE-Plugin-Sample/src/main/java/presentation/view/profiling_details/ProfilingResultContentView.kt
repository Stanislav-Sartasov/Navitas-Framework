package presentation.view.profiling_details

import tooling.ContentRouter
import javax.swing.JButton
import javax.swing.JPanel

class ProfilingResultContentView(
        private val router: ContentRouter
) {

    // UI components
    lateinit var contentPanel: JPanel
    private lateinit var finishButton: JButton
    private lateinit var saveAndFinishButton: JButton
    private lateinit var compareButton: JButton
    private lateinit var showEnergyConsumptionViewButton: JButton

    init {
        setupUI()
    }

    private fun setupUI() {
        finishButton.apply {
            text = "Finish"
            addActionListener {
                router.toPreviousContent()
            }
        }
        saveAndFinishButton.apply {
            text = "Save"
            addActionListener {
                // TODO: save measurements
            }
        }
        compareButton.apply {
            text = "Compare"
            addActionListener {
                // TODO: update chart and data views
            }
        }
        showEnergyConsumptionViewButton.apply {
            text = "Energy consumption"
            addActionListener {
                router.toNextContent()
            }
        }
    }
}