package presentation.view.profiling_details

import com.intellij.openapi.application.AppUIExecutor
import com.intellij.ui.table.JBTable
import data.model.ProfilingError
import data.model.RequestVerdict
import data.repository_impl.ProfilingResultRepositoryImpl
import domain.model.FullEnergyConsumption
import domain.model.TestEnergyConsumption
import javafx.scene.control.SelectionMode
import presentation.viewmodel.ProfilingResultViewModel
import tooling.ContentRouter
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.ListSelectionModel

class EnergyConsumptionContentView(
        private val router: ContentRouter
) {

    private enum class ViewMode {
        FULL_TEST_LIST, SPECIFIC_TEST_DETAILS
    }

    // UI components
    lateinit var contentPanel: JPanel
    private lateinit var scrollPane: JScrollPane
    private lateinit var chartView: JPanel // TODO: add swing component to GUI form (use custom palette)
    private lateinit var tableView: JBTable
    private lateinit var backButton: JButton
    private lateinit var detailsButton: JButton
    private val profilingResultVM = ProfilingResultViewModel(ProfilingResultRepositoryImpl)

    private var currentViewMode = ViewMode.FULL_TEST_LIST

    init {
        setupUI()
    }

    private fun setupUI() {
        backButton.apply {
            text = "Back"
            addActionListener {
                when (currentViewMode) {
                    ViewMode.FULL_TEST_LIST -> {
                        router.toPreviousContent()
                    }
                    ViewMode.SPECIFIC_TEST_DETAILS -> {
                        currentViewMode = ViewMode.FULL_TEST_LIST
                        profilingResultVM.fetch()
                        detailsButton.isVisible = true
                        scrollTableToTop()
                    }
                }
            }
        }

        detailsButton.apply {
            text = "Details"
            addActionListener {
                val position = tableView.selectedRow
                if (position != -1) {
                    currentViewMode = ViewMode.SPECIFIC_TEST_DETAILS
                    profilingResultVM.fetchTestDetails(position)
                    detailsButton.isVisible = false
                    scrollTableToTop()
                }
            }
        }

        tableView.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION

        profilingResultVM.energyDistribution
                .subscribe { data ->
                    AppUIExecutor.onUiThread().execute {
                        when (data) {
                            is FullEnergyConsumption -> {
                                // TODO: update chart
                                tableView.model = TestEnergyTableModel(data.testDetails)
                            }
                            is TestEnergyConsumption -> {
                                // TODO: update chart
                                tableView.model = MethodEnergyTableModel(data.methodDetails)
                            }
                        }
                    }
                }

        profilingResultVM.fetch()
    }

    private fun scrollTableToTop() {
        val verticalBar = scrollPane.verticalScrollBar
        verticalBar.value = verticalBar.minimum
    }
}