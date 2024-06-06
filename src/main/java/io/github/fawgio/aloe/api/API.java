package io.github.fawgio.aloe.api;

import io.github.fawgio.aloe.Aloe;
import io.github.fawgio.aloe.JCodePane;
import io.github.fawgio.aloe.MainWindow;
import io.github.fawgio.aloe.SettingsWindow;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static io.github.fawgio.aloe.SettingsWindow.navi;

public class API {
    public static void addJMenu(JMenu menu) {
        MainWindow.menu.add(menu);
    }

    public static void addSettingsWindowTab(String name, JPanel tab) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(name);
        SettingsWindow.settings.add(node);
        SettingsWindow.navi.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                SettingsWindow.self.remove(SettingsWindow.cur);
                if (node.getUserObject().toString().equals(((DefaultMutableTreeNode) navi.getLastSelectedPathComponent()).getUserObject().toString())) {
                    SettingsWindow.cur = tab;
                }
                SettingsWindow.self.add(SettingsWindow.cur);
                SettingsWindow.self.repaint();
            }
        });
        SettingsWindow.navi.setModel(new DefaultTreeModel(SettingsWindow.settings,false));
    }

    public static JTextPane getCurrentTabComponent(){
        return ((JCodePane) MainWindow.codeTabs.getSelectedComponent()).getCode();
    }
}
