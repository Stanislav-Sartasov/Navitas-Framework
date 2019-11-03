package ui.dialogs;

import android.annotation.Nullable;
import com.intellij.openapi.ui.DialogWrapper;

import javax.swing.*;
import java.awt.*;

public class SimpleDialogWrapper extends DialogWrapper {

    public SimpleDialogWrapper() {
        super(true);
        init();
        setTitle("Simple Dialog");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());

        JLabel label = new JLabel("Attention! Emergency situation!..");
        label.setPreferredSize(new Dimension(100, 100));
        dialogPanel.add(label, BorderLayout.CENTER);

        return dialogPanel;
    }
}
