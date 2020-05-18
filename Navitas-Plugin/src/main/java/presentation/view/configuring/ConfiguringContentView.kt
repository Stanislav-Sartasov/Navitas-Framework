package presentation.view.configuring

import action.*
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.AppUIExecutor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.components.JBLabel
import data.model.ProfilingError
import data.model.RequestVerdict
import extensions.copyTemplate
import presentation.view.common.ContentContainer
import presentation.view.configuring.dialog.ConfigWizardDialog
import presentation.view.power_profile.PowerProfileDialog
import presentation.viewmodel.ConfiguringVM
import presentation.viewmodel.PowerProfileVM
import presentation.viewmodel.ProfilingVM
import tooling.ContentRouter
import tooling.OnActionClickCallback
import javax.swing.JPanel

class ConfiguringContentView(
        project: Project,
        private val router: ContentRouter,
        private val profilingVM: ProfilingVM,
        private val configuringVM: ConfiguringVM,
        private val powerProfileVM: PowerProfileVM
) : ContentContainer() {

    // UI components
    override val panel: JPanel
    private lateinit var contentPanel: JPanel
    private lateinit var androidModuleField: JBLabel
    private lateinit var testListField: JBLabel
    private lateinit var powerProfileField: JBLabel

    private val configureAction: CustomAction
    private val choosePowerProfileAction: CustomAction
    private val startProfilingAction: CustomAction
    private val stopProfilingAction: CustomAction

    private val onConfigureClickCallback = object : OnActionClickCallback {
        override fun onActionClick() {
            ConfigWizardDialog(project) { config ->
                configuringVM.save(config)
            }.show()
        }
    }

    private val onChoosePowerProfileClickCallback = object : OnActionClickCallback {
        override fun onActionClick() {
            PowerProfileDialog(powerProfileVM).show()
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
            // add 'choose power profile' button
            ChoosePowerProfileAction(onChoosePowerProfileClickCallback).also { newAction ->
                choosePowerProfileAction = newAction
                actionManager.copyTemplate("navitas.action.ChoosePowerProfile", newAction)
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
                        androidModuleField.text = config.moduleName
                        val tests = config.instrumentedTestNames.entries
                                .joinToString(separator="</li><li>", prefix = "<ul><li>", postfix = "</li></ul>") { clazz ->
                                    "${clazz.key}:${clazz.value.joinToString(separator="</li><li>", prefix = "<ul><li>", postfix = "</li></ul>") }"
                                }
                        testListField.text = "<html>$tests</html>"
                    }
                }

        powerProfileVM.powerProfile
                .subscribe { profile ->
                    AppUIExecutor.onUiThread().execute {
                        powerProfileField.text = profile.path
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
                                choosePowerProfileAction.isEnabled = true
                            }
                            ProfilingVM.ViewState.READY_FOR_PROFILING -> {
                                startProfilingAction.isEnabled = true
                                stopProfilingAction.isEnabled = false
                                configureAction.isEnabled = true
                                choosePowerProfileAction.isEnabled = true
                            }
                            ProfilingVM.ViewState.DURING_PROFILING -> {
                                startProfilingAction.isEnabled = false
                                stopProfilingAction.isEnabled = true
                                configureAction.isEnabled = false
                                choosePowerProfileAction.isEnabled = false
                            }
                        }
                    }
                }
    }
}
