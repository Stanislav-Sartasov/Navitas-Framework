package ui.tool_windows;

import com.intellij.openapi.wm.ToolWindow;
import ui.dialogs.ApkChooserDialog;
import ui.dialogs.ItemChooserDialog;

import javax.swing.*;

public class MainToolWindow {

    private JButton startButton;
    private JPanel contentPane;

    public MainToolWindow(ToolWindow toolWindow) {
        createUIComponents();
    }

    public JPanel getContentPane() {
        return contentPane;
    }

    private void createUIComponents() {
        startButton.addActionListener(actionEvent -> {
            new ApkChooserDialog().show();
        });
    }
}
