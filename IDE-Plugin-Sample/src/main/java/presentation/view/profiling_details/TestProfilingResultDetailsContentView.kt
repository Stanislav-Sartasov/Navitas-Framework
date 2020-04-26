package presentation.view.profiling_details

import action.BackAction
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.AppUIExecutor
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.ListCellRendererWrapper
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dualView.TreeTableView
import com.intellij.ui.treeStructure.treetable.ListTreeTableModelOnColumns
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo
import com.intellij.ui.treeStructure.treetable.TreeTableCellRenderer
import com.intellij.util.ui.ColumnInfo
import data.model.MethodDetails
import data.repository.ProfilingResultRepositoryImpl
import extensions.copyTemplate
import extensions.toTreeNode
import org.jetbrains.kotlin.diagnostics.Errors.UNCHECKED_CAST
import presentation.view.common.ContentContainer
import presentation.viewmodel.DetailedTestEnergyConsumptionVM
import tooling.ContentRouter
import java.awt.Component
import java.awt.event.ItemEvent
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeCellRenderer

class TestProfilingResultDetailsContentView(
        private val router: ContentRouter
) : ContentContainer() {

    override val panel: JPanel
    private lateinit var contentPanel: JPanel
    private lateinit var treeTableView: TreeTableView
    private lateinit var processThreadChooser: ComboBox<Pair<Int, Int>>

    private val profilingResultVM = DetailedTestEnergyConsumptionVM(ProfilingResultRepositoryImpl)
    private lateinit var treeTableModel: ListTreeTableModelOnColumns
    private lateinit var processThreadChooserModel: CollectionComboBoxModel<Pair<Int, Int>>

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
        treeTableView = TreeTableView(treeTableModel)
    }

    override fun setArgument(arg: Any) {
        if (arg is Int) {
            profilingResultVM.fetch(arg)
        }
    }

    private fun setupUI() {
        processThreadChooserModel = CollectionComboBoxModel()
        processThreadChooser.model = processThreadChooserModel

        treeTableView.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION

        treeTableView.setTreeCellRenderer(
                object : ColoredTreeCellRenderer() {
                    override fun customizeCellRenderer(tree: JTree, value: Any, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean) {
                        val node = value as DefaultMutableTreeNode
                        val data = node.userObject as MethodDetails
                        append(data.methodName)
                    }
                }
        )

        processThreadChooser.renderer =
                object : SimpleListCellRenderer<Pair<Int, Int>>() {
                    override fun customize(list: JList<out Pair<Int, Int>>?, value: Pair<Int, Int>?, index: Int, selected: Boolean, hasFocus: Boolean) {
                        if (value != null) {
                            text = "Process: ${value.first} | Thread: ${value.second}"
                        }
                    }
                }

        @Suppress("UNCHECKED_CAST")
        processThreadChooser.addItemListener { event ->
            if (event.stateChange == ItemEvent.SELECTED) {
                val processThreadIDs = event.item as Pair<Int, Int>
                profilingResultVM.fetch(processThreadIDs)
            }
        }

        profilingResultVM.testInfo
                .subscribe { info ->
                    AppUIExecutor.onUiThread().execute {
                        // TODO: update chart (set test name and energy)

                        processThreadChooserModel.add(info.processThreadIDs)
                        processThreadChooser.selectedIndex = 0

                        profilingResultVM.fetch(info.processThreadIDs[0])
                    }
                }

        profilingResultVM.energyConsumption
                .subscribe { result ->
                    AppUIExecutor.onUiThread().execute {
                        // TODO: update chart (set methods name and energy)

                        // create tree from received data
                        val root = DefaultMutableTreeNode(MethodDetails("", 0, 0, 0F, emptyList()))
                        for (item in result) root.add(item.toTreeNode())

                        treeTableModel.setRoot(root)
                        treeTableModel.reload()
                    }
                }
    }
}