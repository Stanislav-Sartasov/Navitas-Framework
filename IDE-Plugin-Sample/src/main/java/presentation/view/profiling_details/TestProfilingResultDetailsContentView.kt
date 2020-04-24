package presentation.view.profiling_details

import action.BackAction
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.AppUIExecutor
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.dualView.TreeTableView
import com.intellij.ui.treeStructure.treetable.ListTreeTableModelOnColumns
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo
import com.intellij.util.ui.ColumnInfo
import data.model.MethodDetails
import data.repository.ProfilingResultRepositoryImpl
import extensions.copyTemplate
import extensions.toTreeNode
import presentation.view.common.ContentContainer
import presentation.viewmodel.DetailedTestEnergyConsumptionVM
import tooling.ContentRouter
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.ListSelectionModel
import javax.swing.tree.DefaultMutableTreeNode

class TestProfilingResultDetailsContentView(
        private val router: ContentRouter
) : ContentContainer() {

    override val panel: JPanel
    private lateinit var contentPanel: JPanel
    private lateinit var treeTableView: TreeTableView

    private val profilingResultVM = DetailedTestEnergyConsumptionVM(ProfilingResultRepositoryImpl)
    private lateinit var treeTableModel: ListTreeTableModelOnColumns

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

    fun createUIComponents() {
        val columns = arrayOf(
                TreeColumnInfo("Method"),
                object : ColumnInfo<DefaultMutableTreeNode, Float>("Energy (mJ)") {
                    override fun valueOf(item: DefaultMutableTreeNode): Float {
                        return (item.userObject as MethodDetails).cpuEnergy
                    }
                }
        )

        treeTableModel = ListTreeTableModelOnColumns(null, columns)

        treeTableView = TreeTableView(treeTableModel).apply {
            setTreeCellRenderer(
                    object : ColoredTreeCellRenderer() {
                        override fun customizeCellRenderer(tree: JTree, value: Any?, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean) {
                            val node = value as DefaultMutableTreeNode
                            val model = node.userObject as MethodDetails
                            append(model.methodName)
                        }
                    }
            )
        }

    }

    override fun setArgument(arg: Any) {
        if (arg is Int) {
            profilingResultVM.fetch(arg)
        }
    }

    private fun setupUI() {
        treeTableView.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION

        profilingResultVM.energyConsumption
                .subscribe { result ->
                    AppUIExecutor.onUiThread().execute {
                        // TODO: update chart

                        // create tree from received data
                        val items = result.testDetails.entries.map { it.value }.flatten()
                        val root = DefaultMutableTreeNode(MethodDetails("", 0, 0, 0F, emptyList()))
                        for (item in items)
                            root.add(item.toTreeNode())

                        treeTableModel.setRoot(root)
                        treeTableModel.reload()
                    }
                }
    }
}