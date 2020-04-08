package presentation.view.configuring

import com.intellij.openapi.application.AppUIExecutor
import com.intellij.openapi.project.Project
import data.model.ProfilingError
import data.model.RequestVerdict
import data.repository_impl.ConfigurationRepositoryImpl
import tooling.ContentRouter
import presentation.view.configuring.dialog.ConfigWizardDialog
import presentation.viewmodel.ConfiguringViewModel
import presentation.viewmodel.ProfilingViewModel
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel

class ConfiguringContentView(
        private val project: Project,
        private val router: ContentRouter
) {

    // UI components
    lateinit var contentPanel: JPanel
    private lateinit var configureButton: JButton
    private lateinit var profileButton: JButton
    private lateinit var stopButton: JButton
    private lateinit var androidAppModuleField: JLabel
    private lateinit var instrumentedTestList: JList<String>
    private val profilingVM = ProfilingViewModel(project, ConfigurationRepositoryImpl)
    private val configuringVM = ConfiguringViewModel(ConfigurationRepositoryImpl)

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
            isEnabled = false
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
        configuringVM.profilingConfiguration
                .subscribe { config ->
                    AppUIExecutor.onUiThread().execute {
                        profileButton.isEnabled = true
                        androidAppModuleField.text = config.module.name
                        instrumentedTestList.setListData(config.instrumentedTestNames.toTypedArray())
                    }
                }
    }
}
