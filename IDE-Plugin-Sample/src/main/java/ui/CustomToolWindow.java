package ui;

import com.intellij.openapi.wm.ToolWindow;

import javax.swing.*;

public class CustomToolWindow {
    private JButton start;
    private JCheckBox variant3CheckBox;
    private JCheckBox variant2CheckBox;
    private JCheckBox variant1CheckBox;
    private JList list;
    private JPanel content;
    private JPanel subPanel;

    public CustomToolWindow(ToolWindow toolWindow) {
        createUIComponents();
    }

    public JPanel getContent() {
        return content;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
