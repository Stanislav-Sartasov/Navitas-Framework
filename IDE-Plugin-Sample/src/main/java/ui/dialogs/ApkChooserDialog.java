package ui.dialogs;

import android.annotation.Nullable;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;

public class ApkChooserDialog extends DialogWrapper {

    private JPanel contentPane;
    private JTextField app_apk_field;
    private JTextField test_apk_field;

    public ApkChooserDialog() {
        super(true);
        init();
        setTitle("Choose APKs");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

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
}
