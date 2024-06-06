import io.github.fawgio.aloe.api.*;

import javax.swing.*;

public class EnchancedPlugin implements AloePlugin {
    public void start() {
        System.out.println("[EnchancedPlugin] Plugin started");

        JMenu pluginMenu = new JMenu("Enchanced Plugin");
        JMenuItem item = new JMenuItem("Click!");
        item.addActionListener(e -> JOptionPane.showMessageDialog(null,"Hello, Aloe!"));
        pluginMenu.add(item);
        API.addJMenu(pluginMenu);

        System.out.println("[EnchancedPlugin] Added JMenu in JMenuBar of MainWindow");

        API.addSettingsWindowTab("Enchanced Plugin", new JPanel());

        System.out.println("[EnchancedPlugin] Added empty tab in SettingsWindow");
    }
}