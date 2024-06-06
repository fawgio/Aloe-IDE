package io.github.fawgio.aloe;

import io.github.fawgio.aloe.api.AloePlugin;
import io.github.fawgio.aloe.api.AloeTheme;
import io.github.fawgio.aloe.highlight.SyntaxHighlighter;

import java.io.File;
import java.util.*;
import java.util.List;

/**
 * Class that gets and runs plugins
 */
public class PluginLoader {

    /**
     * Where plugins are located
     */
    final String pluginDir = System.getProperty("user.dir") + File.separator + "plugins";
    /**
     * AloePlugin implementations which PluginLoader has got
     */
    List<AloePlugin> plugins = new ArrayList<>();
    /**
     * plugins which implement AloeTheme also
     */
    List<AloeTheme> themes = new ArrayList<>();

    void getPlugins() {
        File dir = new File(pluginDir);
        AloeClassLoader cl = new AloeClassLoader(dir);
        if (dir.exists() && dir.isDirectory()) {
            String[] files = dir.list();
            assert files != null;
            for (String file : files) {
                try {
                    if (!file.endsWith(".class"))
                        continue;

                    Class c = cl.loadClass(file.substring(0, file.indexOf(".")));
                    Class[] interfaces = c.getInterfaces();
                    boolean isPlugin = false;
                    for (Class aClass : interfaces) {
                        if (aClass.getName().equals("io.github.fawgio.aloe.api.AloePlugin")) {
                            AloePlugin plugin = (AloePlugin) c.getDeclaredConstructor().newInstance();
                            plugins.add(plugin);
                            isPlugin = true;
                        } else if (aClass.getName().equals("io.github.fawgio.aloe.api.AloeTheme")) {
                            AloeTheme theme = (AloeTheme) c.getDeclaredConstructor().newInstance();
                            themes.add(theme);
                        }
                    }
                    if(!isPlugin) throw new ClassCastException(c.getName()+" doesn't implement AloePlugin");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * @return is there plugins loaded
     */
    public boolean hasPlugins() {
        return plugins.size() > 0;
    }

    /**
     * Run plugins and add themes
     */
    public void runPlugins() {
        for (AloePlugin plugin : plugins) {
            plugin.start();
        }
        for (AloeTheme theme : themes) {
            SettingsWindow.themes.add(theme.getThemeName());
            SyntaxHighlighter.themeSets.add(new Runnable[]{
                    theme.applyTheme(),
                    ()->{
                Aloe.mainWindow.setJCodePanesColor(theme.getForeground(),theme.getBackground());
            }});
        }
    }
}

