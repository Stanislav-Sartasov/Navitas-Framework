package ui.dialogs;

import android.annotation.Nullable;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.CheckBoxList;
import com.intellij.ui.CheckBoxListListener;

import javax.swing.*;
import java.util.List;

public class ItemChooserDialog<T> extends DialogWrapper {

    private CheckBoxList<T> list;
    private boolean[] selectedArray;

    public ItemChooserDialog(String title, List<T> items) {
        super(true);
        setTitle(title);
        list = new CheckBoxList<>();
        list.setItems(items, T::toString);
        list.setCheckBoxListListener((int i, boolean b) -> selectedArray[i] = b);
        selectedArray = new boolean[items.size()];
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return list;
    }

    @Override
    protected void doOKAction() {
        System.out.print("OK is pressed. Selected item status: ");
        for (boolean isSelected : selectedArray) System.out.print(isSelected + " ");
        System.out.println();
        close(0);
        //TODO: pass selectedArray to somebody (may be to Presenter?)
    }
}
