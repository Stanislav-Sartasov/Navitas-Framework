package ui.tool_windows;

import com.intellij.openapi.wm.ToolWindow;
import ui.dialogs.SimpleDialogWrapper;

import javax.swing.*;

public class MainToolWindow {
    private JButton start;
    private JCheckBox variant3CheckBox;
    private JCheckBox variant2CheckBox;
    private JCheckBox variant1CheckBox;
    private JList list;
    private JPanel content;
    private JPanel subPanel;

    public MainToolWindow(ToolWindow toolWindow) {
        createUIComponents();
    }

    public JPanel getContent() {
        return content;
    }

    private void createUIComponents() {
        start.addActionListener(actionEvent -> {
            new SimpleDialogWrapper().show();
        });
    }
}
