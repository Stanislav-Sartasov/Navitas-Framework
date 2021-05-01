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
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBLabel
import com.intellij.ui.treeStructure.Tree
import domain.model.CpuMethodEnergyConsumption
import extensions.copyTemplate
import presentation.view.common.ContentContainer
import presentation.viewmodel.DetailedTestEnergyConsumptionVM
import tooling.ContentRouter
import java.awt.event.ItemEvent
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel

class TestProfilingResultDetailsContentView(
        private val router: ContentRouter,
        private val profilingResultVM: DetailedTestEnergyConsumptionVM
) : ContentContainer() {

    override val panel: JPanel
    private lateinit var contentPanel: JPanel
    private lateinit var processThreadChooser: ComboBox<Pair<Int, Int>>
    private lateinit var treeView: Tree

    private lateinit var treeModel: DefaultTreeModel
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

    override fun setArgument(arg: Any) {
        if (arg is Int) {
            profilingResultVM.fetch(arg)
        }
    }

    private fun setupUI() {
        processThreadChooserModel = CollectionComboBoxModel()
        processThreadChooser.model = processThreadChooserModel

        treeModel = DefaultTreeModel(null)
        treeView.model = treeModel
        treeView.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION

        treeView.cellRenderer =
                object : ColoredTreeCellRenderer() {
                    override fun customizeCellRenderer(tree: JTree, value: Any, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean) {
                        val node = value as DefaultMutableTreeNode
                        val item = node.userObject as CpuMethodEnergyConsumption
                        append(item.methodName, SimpleTextAttributes.REGULAR_ATTRIBUTES)
                        append("   ")
                        append("${item.cpuEnergy} mJ", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                    }
                }

        processThreadChooser.renderer =
                object : SimpleListCellRenderer<Pair<Int, Int>>() {
                    override fun customize(list: JList<out Pair<Int, Int>>, value: Pair<Int, Int>, index: Int, selected: Boolean, hasFocus: Boolean) {
                        if (value != null) {
                            text = if (value == DetailedTestEnergyConsumptionVM.ALL_PROCESSES_AND_THREADS) "All" else "Process: ${value.first} | Thread: ${value.second}"
                        }
                    }
                }

        treeView.selectionModel.addTreeSelectionListener { event ->
            if (event.isAddedPath) {
                val node = event.path.lastPathComponent as DefaultMutableTreeNode
                val item = node.userObject as CpuMethodEnergyConsumption
                profilingResultVM.selectMethod(item)
            } else {
                profilingResultVM.selectMethod(null)
            }
        }

        @Suppress("UNCHECKED_CAST")
        processThreadChooser.addItemListener { event ->
            if (event.stateChange == ItemEvent.SELECTED) {
                val processThreadIDs = event.item as Pair<Int, Int>
                profilingResultVM.fetch(processThreadIDs)
                profilingResultVM.selectMethod(null)
            }
        }

        profilingResultVM.threadProcessIDs
                .subscribe { ids ->
                    AppUIExecutor.onUiThread().execute {
                        processThreadChooserModel.add(ids)
                        processThreadChooser.selectedIndex = 0
                    }
                }

        profilingResultVM.currentEnergyConsumption
                .subscribe { data ->
                    AppUIExecutor.onUiThread().execute {
                        // TODO: update chart (title and energy consumption list)
                    }
                }

        profilingResultVM.energyConsumptionTree
                .subscribe { root ->
                    AppUIExecutor.onUiThread().execute {
                        treeModel.setRoot(root)
                        treeModel.reload()
                    }
                }
    }
}