package presentation.view.profiling_details

import action.BackAction
import action.ShowDetailsAction
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.AppUIExecutor
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.table.JBTable
import extensions.copyTemplate
import presentation.view.common.ContentContainer
import presentation.viewmodel.ConstantsEnergyListVM
import presentation.viewmodel.TestEnergyConsumptionListVM
import tooling.ContentRouter
import tooling.OnActionClickCallback
import javax.swing.JPanel
import javax.swing.ListSelectionModel

class TestsProfilingResultContentView(
        private val router: ContentRouter,
        private val isConstantMode: Boolean,
        private val profilingResultVM: TestEnergyConsumptionListVM,
        private val constantsResultVM: ConstantsEnergyListVM
) : ContentContainer() {

    // UI components
    override val panel: JPanel
    private lateinit var contentPanel: JPanel
    private lateinit var chartView: JPanel // TODO: add swing component to GUI form (use custom palette)
    private lateinit var tableView: JBTable

    private val onShowDetailsClickCallback = object : OnActionClickCallback {
        override fun onActionClick() {
            val position = tableView.selectedRow
            if (position != -1) {
                router.toNextContent(position)
            }
        }
    }

    init {
        // create action toolbar
        val actionManager = ActionManager.getInstance()
        val actionGroup = DefaultActionGroup().apply {
            // add 'back' button
            BackAction(router).also { newAction ->
                actionManager.copyTemplate("navitas.action.Back", newAction)
                add(newAction)
            }
            if (!isConstantMode) {
                // add 'see details' button
                ShowDetailsAction(onShowDetailsClickCallback).also { newAction ->
                    actionManager.copyTemplate("navitas.action.ShowDetails", newAction)
                    add(newAction)
                }
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
        tableView.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION

        if (!isConstantMode) {
            profilingResultVM.energyConsumption
                .subscribe { result ->
                    AppUIExecutor.onUiThread().execute {
                        // TODO: update chart
                        tableView.model = TestEnergyConsumptionTableModel(result)
                    }
                }

            profilingResultVM.fetch()
        }
        else {
            constantsResultVM.energyConstant
                .subscribe { result ->
                    AppUIExecutor.onUiThread().execute {
                        tableView.model = ConstantsEnergyTableModel(result)
                    }
                }
            constantsResultVM.fetch()
        }
    }
}