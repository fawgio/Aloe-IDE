package io.github.fawgio.aloe;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JButton that can open JFileChooser dialog
 */
public class JFileButton extends JButton {
    private final List<ApprovedListener> approvedListeners = new ArrayList<>();

    /**
     * @param textField a JTextField where the file path will be printed
     */
    JFileButton(JTextField textField){
        super("...");
        addActionListener(e -> {
            var j = new JFileChooser();
            int res = j.showOpenDialog(null);
            if (res == JFileChooser.APPROVE_OPTION) {
                textField.setText(j.getSelectedFile().getPath());
                approvedListeners.forEach(approvedListener -> approvedListener.approved(j.getSelectedFile().getPath()));
            }
        });
    }

    /**
     * @see ApprovedListener
     * @return this JFileButton
     */
    public JFileButton addApprovedListener(ApprovedListener approvedListener){
        approvedListeners.add(approvedListener);
        return this;
    }

    /**
     * @see ApprovedListener
     * @return this JFileButton
     */
    public JFileButton removeApprovedListener(ApprovedListener approvedListener){
        approvedListeners.remove(approvedListener);
        return this;
    }


    /**
     * Listen when user clicked JFileButton and has selected a file
     */
    public interface ApprovedListener {
        /**
         * @param path the path of selected file
         */
        void approved(String path);
    }
}
