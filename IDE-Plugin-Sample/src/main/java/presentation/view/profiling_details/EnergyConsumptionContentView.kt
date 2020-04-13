package presentation.view.profiling_details

import action.BackAction
import action.CustomAction
import action.ShowDetailsAction
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.AppUIExecutor
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.table.JBTable
import data.repository_impl.ProfilingResultRepositoryImpl
import domain.model.FullEnergyConsumption
import domain.model.TestEnergyConsumption
import extensions.copyTemplate
import presentation.viewmodel.ProfilingResultViewModel
import tooling.ContentRouter
import tooling.OnActionClickCallback
import tooling.OnBackClickCallback
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
    val panel: JPanel
    private lateinit var contentPanel: JPanel
    private lateinit var scrollPane: JScrollPane
    private lateinit var chartView: JPanel // TODO: add swing component to GUI form (use custom palette)
    private lateinit var tableView: JBTable

    private val profilingResultVM = ProfilingResultViewModel(ProfilingResultRepositoryImpl)
    private var currentViewMode = ViewMode.FULL_TEST_LIST
    private val energyTableModel = EnergyConsumptionTableModel()
//    private var selectedItemPosition = -1

    private lateinit var showDetailsAction: CustomAction

    private val onBackClickCallback = object : OnBackClickCallback {
        override fun onBackClick(): Boolean {
            return if (currentViewMode == ViewMode.SPECIFIC_TEST_DETAILS) {
                currentViewMode = ViewMode.FULL_TEST_LIST
                profilingResultVM.fetch()
                scrollTableToTop()
//                tableView.clearSelection()
                showDetailsAction.isVisible = true
//                showDetailsActionState.isEnabled = false
                true
            } else false
        }
    }

    private val onShowDetailsClickCallback = object : OnActionClickCallback {
        override fun onActionClick() {
            val position = tableView.selectedRow
            if (position != -1) {
                currentViewMode = ViewMode.SPECIFIC_TEST_DETAILS
                profilingResultVM.fetchTestDetails(position)
                showDetailsAction.isVisible = false
                scrollTableToTop()
            }
        }
    }

//    private val onSelectRowListener = ListSelectionListener { event: ListSelectionEvent ->
//        if (!event.valueIsAdjusting) {
//            println(event.toString())
//            var isSelected = false
//            if (selectedItemPosition == -1) isSelected = true
//            selectedItemPosition =
//                    if (event.firstIndex == event.lastIndex) {
//                        if (selectedItemPosition == -1) event.firstIndex else -1
//                    } else {
//                        isSelected = true
//                        if (selectedItemPosition == event.firstIndex) event.lastIndex else event.firstIndex
//                    }
//            if (currentViewMode == ViewMode.FULL_TEST_LIST) {
//                showDetailsActionState.isEnabled = isSelected
//            }
//        }
//    }

    init {
        // create action toolbar
        val actionManager = ActionManager.getInstance()
        val actionGroup = DefaultActionGroup().apply {
            // add 'back' button
            BackAction(router, onBackClickCallback).also { newAction ->
                actionManager.copyTemplate("navitas.action.Back", newAction)
                add(newAction)
            }
            // add 'see details' button
            ShowDetailsAction(onShowDetailsClickCallback).also { newAction ->
                showDetailsAction = newAction
                actionManager.copyTemplate("navitas.action.ShowDetails", newAction)
                add(newAction)
            }
        }
        val actionToolbar = actionManager.createActionToolbar(ActionPlaces.UNKNOWN, actionGroup, false)

        panel = SimpleToolWindowPanel(false, true).apply {
            toolbar = actionToolbar.component
            setContent(contentPanel)
        }

        setupUI()
    }

    private fun setupUI() {
//        showDetailsActionState.isEnabled = false
        tableView.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
        tableView.model = energyTableModel
//        tableView.selectionModel.addListSelectionListener(onSelectRowListener)

        profilingResultVM.energyDistribution
                .subscribe { data ->
                    AppUIExecutor.onUiThread().execute {
                        when (data) {
                            is FullEnergyConsumption -> {
                                // TODO: update chart
                                energyTableModel.consumerType = EnergyConsumptionTableModel.ConsumerType.TEST
                                energyTableModel.items = data.testDetails
                                tableView.invalidate()
                            }
                            is TestEnergyConsumption -> {
                                // TODO: update chart
                                energyTableModel.consumerType = EnergyConsumptionTableModel.ConsumerType.METHOD
                                energyTableModel.items = data.methodDetails
                                tableView.invalidate()
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