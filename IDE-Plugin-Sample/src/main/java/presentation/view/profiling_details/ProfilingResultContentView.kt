package presentation.view.profiling_details

import tooling.ContentRouter
import javax.swing.JButton
import javax.swing.JPanel

// ATTENTION: this content view is temporarily unavailable
class ProfilingResultContentView(
        private val router: ContentRouter
) {

    // UI components
    lateinit var contentPanel: JPanel
    private lateinit var chartView: JPanel
    private lateinit var energyConsumptionView: JPanel
    private lateinit var finishButton: JButton
    private lateinit var saveAndFinishButton: JButton
    private lateinit var compareButton: JButton

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
                router.toPreviousContent()
            }
        }
        compareButton.apply {
            text = "Compare"
            addActionListener {
                // TODO: update chart and data views
            }
        }
    }
}