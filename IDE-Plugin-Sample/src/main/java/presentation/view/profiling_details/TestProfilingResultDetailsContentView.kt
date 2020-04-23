package presentation.view.profiling_details

import action.BackAction
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.AppUIExecutor
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.table.JBTable
import data.repository.ProfilingResultRepositoryImpl
import extensions.copyTemplate
import presentation.view.common.ContentContainer
import presentation.viewmodel.DetailedTestEnergyConsumptionVM
import tooling.ContentRouter
import javax.swing.JPanel
import javax.swing.ListSelectionModel

class TestProfilingResultDetailsContentView(
        private val router: ContentRouter
) : ContentContainer() {

    override val panel: JPanel
    private lateinit var contentPanel: JPanel
    private lateinit var tableView: JBTable

    private val profilingResultVM = DetailedTestEnergyConsumptionVM(ProfilingResultRepositoryImpl)

    init {
        // create action toolbar
        val actionManager = ActionManager.getInstance()
        val actionGroup = DefaultActionGroup().apply {
            // add 'back' button
            BackAction(router).also { newAction ->
                actionManager.copyTemplate("navitas.action.Back", newAction)
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

    override fun setArgument(arg: Any) {
        if (arg is Int) {
            profilingResultVM.fetch(arg)
        }
    }

    private fun setupUI() {
        tableView.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION

        profilingResultVM.energyConsumption
                .subscribe { result ->
                    AppUIExecutor.onUiThread().execute {
                        // TODO: update chart
                        val items = result.testDetails.entries  // TODO: flatten all trees
                                .map { it.value }
                                .flatten()
                        tableView.model = DetailedTestEnergyConsumptionTableModel(items)
                    }
                }
    }
}