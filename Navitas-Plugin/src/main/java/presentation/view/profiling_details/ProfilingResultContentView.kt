package presentation.view.profiling_details

import action.BackAction
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.ui.SimpleToolWindowPanel
import extensions.copyTemplate
import presentation.view.common.ContentContainer
import tooling.ContentRouter
import javax.swing.JButton
import javax.swing.JPanel

class ProfilingResultContentView(
        private val router: ContentRouter
) : ContentContainer() {

    // UI components
    override val panel: JPanel
    private lateinit var contentPanel: JPanel
    private lateinit var showEnergyConsumptionViewButton: JButton

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

    private fun setupUI() {
        showEnergyConsumptionViewButton.apply {
            text = "Energy consumption"
            addActionListener {
                router.toNextContent()
            }
        }
    }
}