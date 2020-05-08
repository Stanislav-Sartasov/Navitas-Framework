package presentation.view.power_profile

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBLabel
import com.intellij.uiDesigner.core.AbstractLayout
import com.intellij.util.ui.GridBag
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import data.model.PowerProfile
import presentation.viewmodel.PowerProfileVM
import tooling.PowerProfileManager
import tooling.PowerProfileParser
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.filechooser.FileSystemView

class PowerProfileDialog(private val powerProfileVM: PowerProfileVM) : DialogWrapper(true) {

    private var powerProfile: PowerProfile? = null

    private val centerPanel: JPanel = JPanel(GridBagLayout())
    private val fileChooserPanel = object : JPanel(), ActionListener {

        private val fileChooser = JFileChooser(FileSystemView.getFileSystemView().homeDirectory)
        private val openButton: JButton = JButton("Open")

        init {
            this.add(openButton)
            openButton.addActionListener(this)
            fileChooser.fileFilter = FileNameExtensionFilter("Power Profile XML File", "xml")
        }

        override fun actionPerformed(e: ActionEvent?) {
            if (e?.source == openButton) {
                val dialog = fileChooser.showOpenDialog(this)

                if (dialog == JFileChooser.APPROVE_OPTION) {
                    val file = fileChooser.selectedFile
                    powerProfile = PowerProfileParser(file).parseXML()
                }
            }
        }
    }
    private val useDefaultPanel = object : JPanel(), ActionListener {

        private val defaultButton: JButton = JButton("Use default")

        init {
            this.add(defaultButton)
            defaultButton.addActionListener(this)
        }

        override fun actionPerformed(e: ActionEvent?) {
            if (e?.source == defaultButton) {
                powerProfile = PowerProfileManager.getDefaultProfile()
            }
        }
    }

    init {
        init()
        title = "Power Profile configuration"
    }

    override fun createCenterPanel(): JComponent? {
        val gridbag = GridBag()
                .setDefaultWeightX(1.0)
                .setDefaultFill(GridBagConstraints.HORIZONTAL)
                .setDefaultInsets(Insets(0, 0, AbstractLayout.DEFAULT_VGAP, AbstractLayout.DEFAULT_HGAP))

        centerPanel.preferredSize = Dimension(400, 50)
        centerPanel.add(getLabel("Choose \"power_profile.xml\" file: "), gridbag.nextLine().next().weightx(0.2))
        centerPanel.add(fileChooserPanel, gridbag.next().weightx(0.8))
        centerPanel.add(useDefaultPanel, gridbag.next().weightx(0.8))

        return centerPanel
    }

    private fun getLabel(text: String): JComponent {
        val label = JBLabel(text)

        label.componentStyle = UIUtil.ComponentStyle.LARGE
        label.fontColor = UIUtil.FontColor.BRIGHTER
        label.border = JBUI.Borders.empty(0, 5, 2, 0)

        return label
    }

    override fun doOKAction() {
        if (powerProfile == null) {
            Messages.showInfoMessage("You have to upload XML with power profile values!", "Error")
        } else {
            powerProfileVM.save(powerProfile!!)
            super.doOKAction()
        }
    }
}