package presentation.view.configuring

import action.ConfigureAction
import action.CustomAction
import action.StartProfilingAction
import action.StopProfilingAction
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.AppUIExecutor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.components.JBLabel
import data.model.ProfilingError
import data.model.RequestVerdict
import data.repository.ConfigurationRepositoryImpl
import data.repository.ProfilingResultRepositoryImpl
import extensions.copyTemplate
import presentation.view.common.ContentContainer
import presentation.view.configuring.dialog.ConfigWizardDialog
import presentation.viewmodel.ConfiguringVM
import presentation.viewmodel.ProfilingVM
import tooling.ContentRouter
import tooling.OnActionClickCallback
import javax.swing.JPanel

class ConfiguringContentView(
        private val project: Project,
        private val router: ContentRouter
) : ContentContainer() {

    // UI components
    override val panel: JPanel
    private lateinit var contentPanel: JPanel
    private lateinit var androidAppModuleField: JBLabel
    private lateinit var testListField: JBLabel

    private val profilingVM = ProfilingVM(project, ConfigurationRepositoryImpl, ProfilingResultRepositoryImpl)
    private val configuringVM = ConfiguringVM(ConfigurationRepositoryImpl)

    private val configureAction: CustomAction
    private val startProfilingAction: CustomAction
    private val stopProfilingAction: CustomAction

    private val onConfigureClickCallback = object : OnActionClickCallback {
        override fun onActionClick() {
            ConfigWizardDialog(project) { config ->
                configuringVM.save(config)
            }.show()
        }
    }

    private val onStartProfilingClickCallback = object : OnActionClickCallback {
        override fun onActionClick() {
            profilingVM.startProfiling()
        }
    }

    private val onStopProfilingClickCallback = object : OnActionClickCallback {
        override fun onActionClick() {
            profilingVM.stopProfiling()
        }
    }

    init {
        // create action toolbar
        val actionManager = ActionManager.getInstance()
        val actionGroup = DefaultActionGroup().apply {
            // add 'configure' button
            ConfigureAction(onConfigureClickCallback).also { newAction ->
                configureAction = newAction
                actionManager.copyTemplate("navitas.action.Configure", newAction)
                add(newAction)
            }
            // add 'profile' button
            StartProfilingAction(onStartProfilingClickCallback).also { newAction ->
                startProfilingAction = newAction
                actionManager.copyTemplate("navitas.action.Profile", newAction)
                add(newAction)
            }
            // add 'stop' button
            StopProfilingAction(onStopProfilingClickCallback).also { newAction ->
                stopProfilingAction = newAction
                actionManager.copyTemplate("navitas.action.Stop", newAction)
                add(newAction)
            }
        }
        val toolbar = actionManager.createActionToolbar(ActionPlaces.UNKNOWN, actionGroup, false)

        panel = SimpleToolWindowPanel(false, true).apply {
            setToolbar(toolbar.component)
            setContent(contentPanel)
        }

        setupUI()
    }

    private fun setupUI() {
        profilingVM.profilingVerdict
                .subscribe { verdict ->
                    AppUIExecutor.onUiThread().execute {
                        when (verdict) {
                            is RequestVerdict.Success -> router.toNextContent()
                            is ProfilingError -> {
                                // TODO: show error notification
                            }
                        }
                    }
                }

        configuringVM.profilingConfiguration
                .subscribe { config ->
                    AppUIExecutor.onUiThread().execute {
                        androidAppModuleField.text = config.module.name
                        testListField.text = "<html><ul>${config.instrumentedTestNames.joinToString(separator = "<li>", prefix = "<li>")}</ul></html>"
                    }
                }

        profilingVM.viewState
                .subscribe { state ->
                    AppUIExecutor.onUiThread().execute {
                        when (state) {
                            ProfilingVM.ViewState.INITIAL -> {
                                startProfilingAction.isEnabled = false
                                stopProfilingAction.isEnabled = false
                                configureAction.isEnabled = true
                            }
                            ProfilingVM.ViewState.READY_TO_PROFILING -> {
                                startProfilingAction.isEnabled = true
                                stopProfilingAction.isEnabled = false
                                configureAction.isEnabled = true
                            }
                            ProfilingVM.ViewState.DURING_PROFILING -> {
                                startProfilingAction.isEnabled = false
                                stopProfilingAction.isEnabled = true
                                configureAction.isEnabled = false
                            }
                        }
                    }
                }
    }
}
