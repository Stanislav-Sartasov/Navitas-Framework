package ui.profiling;

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBList
import data.ConfigurationRepository
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.ListSelectionModel

// TODO: delete later if it's not needed
class ProfilingDialog(private val project: Project) : DialogWrapper(project) {

    private lateinit var testList: JBList<String>
    private val configRepository = project.getComponent(ConfigurationRepository::class.java)

    init {
        init()
        title = "Profiling"
    }

    override fun createCenterPanel(): JComponent? {
        testList = JBList(configRepository.testClasses.map { test -> test.name }).apply {
            selectionMode = ListSelectionModel.SINGLE_SELECTION
        }
        val button = JButton("GO").apply {
            addActionListener {}
        }
        return JPanel(BorderLayout()).apply {
            add(testList, BorderLayout.NORTH)
            add(button, BorderLayout.SOUTH)
            preferredSize = Dimension(100, 100)
        }
    }
}