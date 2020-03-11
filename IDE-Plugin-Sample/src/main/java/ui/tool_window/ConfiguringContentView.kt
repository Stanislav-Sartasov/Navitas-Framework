package ui.tool_window

import com.intellij.openapi.application.AppUIExecutor
import com.intellij.openapi.project.Project
import data.model.ProfilingError
import data.model.RequestVerdict
import interfaces.ContentRouter
import ui.configuring.wizard.ConfigWizardDialog
import ui.view_models.ConfiguringViewModel
import ui.view_models.ProfilingViewModel
import javax.swing.JButton
import javax.swing.JPanel

class ConfiguringContentView(private val project: Project, private val router: ContentRouter) {

    // UI components
    lateinit var contentPanel: JPanel
    private lateinit var configureButton: JButton
    private lateinit var profileButton: JButton
    private lateinit var stopButton: JButton
    private val profilingVM = ProfilingViewModel(project)
    private val configuringVM = ConfiguringViewModel()

    init{
        setupUI()
    }

    private fun setupUI() {
        configureButton.apply {
            text = "Configure"
            addActionListener {
                ConfigWizardDialog(project) { config ->
                    configuringVM.save(config)
                }.show()
            }
        }
        profileButton.apply {
            text = "Profile"
            addActionListener {
                isEnabled = false
                configureButton.isEnabled = false
                stopButton.isEnabled = true
                profilingVM.startProfiling()
            }
        }
        stopButton.apply {
            text = "Stop"
            isEnabled = false
            addActionListener {
                isEnabled = false
                profilingVM.stopProfiling()
            }
        }
        profilingVM.profilingResult
                .subscribe { verdict ->
                    AppUIExecutor.onUiThread().execute {
                        configureButton.isEnabled = true
                        profileButton.isEnabled = true
                        stopButton.isEnabled = false
                        when (verdict) {
                            is RequestVerdict.Success -> router.toNextContent()
                            is ProfilingError -> {
                                // TODO: show error dialog or notification
                            }
                        }
                    }
        }
    }
}
