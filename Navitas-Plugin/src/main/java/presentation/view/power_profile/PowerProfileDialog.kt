package presentation.view.power_profile

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import presentation.viewmodel.PowerProfileVM
import javax.swing.*
import java.io.File
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.filechooser.FileSystemView

class PowerProfileDialog(private val powerProfileVM: PowerProfileVM) : DialogWrapper(true) {

    private lateinit var contentPane: JPanel
    private lateinit var chooseButton: JButton
    private lateinit var defaultButton: JButton
    private lateinit var chosenProfileLabel: JLabel

    private var profileFile: File? = null
    private var isChosen: Boolean = false

    init {
        init()
        title = "Power Profile configuration"
        chooseButton.addActionListener { onChoose() }
        defaultButton.addActionListener { onUseDefault() }
    }

    private fun onChoose() {
        val fileChooser = JFileChooser(FileSystemView.getFileSystemView().homeDirectory)
        fileChooser.fileFilter = FileNameExtensionFilter("Power Profile XML File", "xml")

        val dialog = fileChooser.showOpenDialog(contentPane)

        if (dialog == JFileChooser.APPROVE_OPTION) {
            profileFile = fileChooser.selectedFile
            isChosen = true
            chosenProfileLabel.text = profileFile!!.absolutePath
        }
    }

    private fun onUseDefault() {
        profileFile = null
        isChosen = true
        chosenProfileLabel.text = "Default profile"
    }

    override fun createCenterPanel(): JComponent? {
        return contentPane
    }

    override fun doOKAction() {
        if (!isChosen) {
            Messages.showInfoMessage("You have to choose Power Profile!", "Error")
            return
        }

        if (profileFile != null) {
            powerProfileVM.save(profileFile!!)
        } else {
            powerProfileVM.saveDefault()
        }

        super.doOKAction()
    }
}
