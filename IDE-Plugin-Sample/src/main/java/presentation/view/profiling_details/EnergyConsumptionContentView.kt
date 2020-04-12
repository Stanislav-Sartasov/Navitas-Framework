package presentation.view.profiling_details

import action.BackContentAction
import action.ShowDetailsContentAction
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.AppUIExecutor
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.table.JBTable
import data.repository_impl.ProfilingResultRepositoryImpl
import domain.model.FullEnergyConsumption
import domain.model.TestEnergyConsumption
import presentation.viewmodel.ProfilingResultViewModel
import tooling.ActionState
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
//    private var selectedItemPosition = -1

    private lateinit var showDetailsActionState: ActionState

    private val onBackClickCallback = object : OnBackClickCallback {
        override fun onBackClick(): Boolean {
            return if (currentViewMode == ViewMode.SPECIFIC_TEST_DETAILS) {
                currentViewMode = ViewMode.FULL_TEST_LIST
                profilingResultVM.fetch()
                scrollTableToTop()
//                tableView.clearSelection()
                showDetailsActionState.isVisible = true
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
                showDetailsActionState.isVisible = false
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
            actionManager.getAction("navitas.action.Back").also { templateAction ->
                val newAction = BackContentAction(router, onBackClickCallback)
                newAction.copyFrom(templateAction)
                add(newAction)
            }
            // add 'see details' button
            actionManager.getAction("navitas.action.ShowDetails").also { templateAction ->
                val newAction = ShowDetailsContentAction(onShowDetailsClickCallback)
                newAction.copyFrom(templateAction)
                add(newAction)
                showDetailsActionState = newAction
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
//        tableView.selectionModel.addListSelectionListener(onSelectRowListener)

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