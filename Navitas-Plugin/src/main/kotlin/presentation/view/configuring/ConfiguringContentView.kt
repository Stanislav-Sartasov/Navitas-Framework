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
import presentation.view.configuring.profiling_dialog.ProfConfigWizardDialog
import presentation.view.configuring.constants_dialog.ConstConfigWizardDialog
import presentation.view.power_profile.PowerProfileDialog
import presentation.viewmodel.ConfiguringVM
import presentation.viewmodel.PowerProfileVM
import presentation.viewmodel.ProfilingAndConstantsVM
import tooling.ContentRouter
import tooling.OnActionClickCallback
import javax.swing.JPanel

class ConfiguringContentView(
    project: Project,
    private val router: ContentRouter,
    private val profilingAndConstantsVM: ProfilingAndConstantsVM,
    private val configuringVM: ConfiguringVM,
    private val powerProfileVM: PowerProfileVM
) : ContentContainer() {

    // UI components
    override val panel: JPanel
    private lateinit var contentPanel: JPanel
    private lateinit var androidModuleField: JBLabel
    private lateinit var testListField: JBLabel
    private lateinit var powerProfileField: JBLabel

    private val configureProfilingAction: CustomAction
    private val configureConstantsAction: CustomAction
    private val choosePowerProfileAction: CustomAction
    private val startProfilingAction: CustomAction
    private val stopProfilingAction: CustomAction

    private val onConfigureProfilingClickCallback = object : OnActionClickCallback {
        override fun onActionClick() {
            ProfConfigWizardDialog(project) { config ->
                configuringVM.save(config)
            }.show()
        }
    }

    private val onConfigureConstantsClickCallback = object : OnActionClickCallback {
        override fun onActionClick() {
            ConstConfigWizardDialog(project) { config ->
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
            profilingAndConstantsVM.start()
        }
    }

    private val onStopProfilingClickCallback = object : OnActionClickCallback {
        override fun onActionClick() {
            profilingAndConstantsVM.stop()
        }
    }

    init {
        // create action toolbar
        val actionManager = ActionManager.getInstance()
        val actionGroup = DefaultActionGroup().apply {
            // add 'configure profiling' button
            ConfigureProfilingAction(onConfigureProfilingClickCallback).also { newAction ->
                configureProfilingAction = newAction
                actionManager.copyTemplate("navitas.action.ProfilingConfigure", newAction)
                add(newAction)
            }
            // add 'configure constants' button
            ConfigureConstantsAction(onConfigureConstantsClickCallback).also { newAction ->
                configureConstantsAction = newAction
                actionManager.copyTemplate("navitas.action.ConstantsConfigure", newAction)
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
        profilingAndConstantsVM.profilingVerdict
                .subscribe { verdict ->
                    AppUIExecutor.onUiThread().execute {
                        when (verdict) {
                            is RequestVerdict.Success -> router.toNextContent()
                            is ProfilingError -> {
                                // TODO: show error notification (currently it's not reachable)
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

        profilingAndConstantsVM.viewState
                .subscribe { state ->
                    AppUIExecutor.onUiThread().execute {
                        when (state) {
                            ProfilingAndConstantsVM.ViewState.INITIAL -> {
                                startProfilingAction.isEnabled = false
                                stopProfilingAction.isEnabled = false
                                configureProfilingAction.isEnabled = true
                                configureConstantsAction.isEnabled = true
                                choosePowerProfileAction.isEnabled = true
                            }
                            ProfilingAndConstantsVM.ViewState.READY_FOR_PROFILING -> {
                                startProfilingAction.isEnabled = true
                                stopProfilingAction.isEnabled = false
                                configureProfilingAction.isEnabled = true
                                configureConstantsAction.isEnabled = true
                                choosePowerProfileAction.isEnabled = true
                            }
                            ProfilingAndConstantsVM.ViewState.READY_FOR_CONSTANTS -> {
                                startProfilingAction.isEnabled = true
                                stopProfilingAction.isEnabled = false
                                configureProfilingAction.isEnabled = true
                                configureConstantsAction.isEnabled = true
                                choosePowerProfileAction.isEnabled = false
                            }
                            ProfilingAndConstantsVM.ViewState.DURING -> {
                                startProfilingAction.isEnabled = false
                                stopProfilingAction.isEnabled = true
                                configureProfilingAction.isEnabled = false
                                configureConstantsAction.isEnabled = false
                                choosePowerProfileAction.isEnabled = false
                            }
                        }
                    }
                }
    }
}
