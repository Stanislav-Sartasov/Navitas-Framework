package ui.dialogs

import com.intellij.ui.wizard.WizardNavigationState
import com.intellij.ui.wizard.WizardStep
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField

class ApkChooserStep : WizardStep<ConfigurationModel>() {

    private var contentPane: JPanel? = null
    private var app_apk_field: JTextField? = null
    private var test_apk_field: JTextField? = null

    override fun prepare(wizardNavigationState: WizardNavigationState): JComponent? {
        return contentPane
    }

    /*

    @NotNull
    @Override
    protected Action[] createActions() {
        setOKButtonText("Next");
        return new Action[] {getCancelAction(), getOKAction()};
    }

    @Override
    protected void doOKAction() {
        System.out.println("NEXT is pressed");
        ArrayList<String> list = new ArrayList<>(Arrays.asList("test1", "test2", "test3", "test4", "test5"));
        close(0);
        new ItemChooserDialog<>("Test chooser", list).show();
    }

     */
}
