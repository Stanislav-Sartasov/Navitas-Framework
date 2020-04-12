package presentation.view.configuring

import action.ConfigureAction
import action.CustomAction
import action.StartProfilingAction
import action.StopProfilingAction
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.AppUIExecutor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.components.JBList
import data.model.ProfilingError
import data.model.RequestVerdict
import data.repository_impl.ConfigurationRepositoryImpl
import extensions.copyTemplate
import presentation.view.configuring.dialog.ConfigWizardDialog
import presentation.viewmodel.ConfiguringViewModel
import presentation.viewmodel.ProfilingViewModel
import tooling.ContentRouter
import tooling.OnActionClickCallback
import javax.swing.JLabel
import javax.swing.JPanel

class ConfiguringContentView(
        private val project: Project,
        private val router: ContentRouter
) {

    // UI components
    val panel: JPanel
    private lateinit var contentPanel: JPanel
    private lateinit var androidAppModuleField: JLabel
    private lateinit var instrumentedTestList: JBList<String>

    private val profilingVM = ProfilingViewModel(project, ConfigurationRepositoryImpl)
    private val configuringVM = ConfiguringViewModel(ConfigurationRepositoryImpl)

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
        val actionToolbar = actionManager.createActionToolbar(ActionPlaces.UNKNOWN, actionGroup, false)

        panel = SimpleToolWindowPanel(false, true).apply {
            toolbar = actionToolbar.component
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
                                // TODO: show error dialog or notification
                            }
                        }
                    }
                }
        configuringVM.profilingConfiguration
                .subscribe { config ->
                    AppUIExecutor.onUiThread().execute {
                        androidAppModuleField.text = config.module.name
                        instrumentedTestList.setListData(config.instrumentedTestNames.toTypedArray())
                    }
                }
        profilingVM.viewState
                .subscribe { state ->
                    AppUIExecutor.onUiThread().execute {
                        when (state) {
                            ProfilingViewModel.ViewState.INITIAL -> {
                                startProfilingAction.isEnabled = false
                                stopProfilingAction.isEnabled = false
                                configureAction.isEnabled = true
                            }
                            ProfilingViewModel.ViewState.READY_TO_PROFILING -> {
                                startProfilingAction.isEnabled = true
                                stopProfilingAction.isEnabled = false
                                configureAction.isEnabled = true
                            }
                            ProfilingViewModel.ViewState.PROFILING -> {
                                startProfilingAction.isEnabled = false
                                stopProfilingAction.isEnabled = true
                                configureAction.isEnabled = false
                            }
                        }
                    }
                }
    }
}
