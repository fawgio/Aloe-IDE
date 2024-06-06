package io.github.fawgio.aloe;

import io.github.fawgio.aloe.highlight.SyntaxHighlighter;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class SettingsWindow extends JFrame {
    public static JTree navi;
    public static JPanel editor = new JPanel();
    static JPanel compiler = new JPanel();
    public static JPanel cur = editor;
    public static SettingsWindow self;
    public static ArrayList<String> themes = new ArrayList<>(Arrays.asList("Light", "Dark","Vera"));
    static JTextField comp = new JTextField();
    public static DefaultMutableTreeNode settings = new DefaultMutableTreeNode("Settings");

    static {
        settings.add(new DefaultMutableTreeNode("Editor"));
        editor.add(new JLabel("Font"));
        editor.add(new JFontComboBox());
        editor.add(new JLabel("Theme"));
        settings.add(new DefaultMutableTreeNode("Compiler"));
        compiler.add(new JLabel("Path to 11l:"));
        compiler.add(comp);
        navi = new JTree(settings);
    }

    SettingsWindow(){
        self = this;
        compiler.add(new JFileButton(comp).addApprovedListener(str -> {Aloe.mainWindow.elevenL = new File(str); this.setVisible(true);}));
        editor.add(new JThemeComboBox());
        navi.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                remove(cur);
                switch (((DefaultMutableTreeNode)navi.getLastSelectedPathComponent()).getUserObject().toString()){
                    case "Editor" -> cur = editor;
                    case "Compiler" -> cur = compiler;
                }
                add(cur);
                repaint();
            }
        });
        add(navi,BorderLayout.WEST);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(500,500));
        setLocationRelativeTo(Aloe.mainWindow);
        setVisible(true);
        ImageIcon icon = new ImageIcon("icon.gif");
        setIconImage(icon.getImage());
    }

    private static class JFontComboBox extends JComboBox {
        public JFontComboBox() {
            super(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
            setSelectedItem(Aloe.mainWindow.font);
            addItemListener(e -> {
                Aloe.mainWindow.setFont(Objects.requireNonNull(getSelectedItem()).toString());
                Aloe.mainWindow.setVisible(true); // update main window
                self.setVisible(true);          //\\ but keep main window behind settings window
            });
        }
    }

    private static class JThemeComboBox extends JComboBox<Object> {
        public JThemeComboBox() {
            super(themes.toArray());
            setSelectedIndex(MainWindow.theme);
            addItemListener(e -> {
                SyntaxHighlighter.setTheme(getSelectedIndex());
                MainWindow.theme = getSelectedIndex();
            });
        }
    }
}
