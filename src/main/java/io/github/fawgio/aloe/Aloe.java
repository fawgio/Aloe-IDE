package io.github.fawgio.aloe;

import io.github.fawgio.aloe.highlight.SyntaxHighlighter;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The main class of Aloe IDE
 */
public class Aloe {
    public static MainWindow mainWindow;
    public static void main(String[] args){
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            mainWindow = new MainWindow();

            var plugins = new PluginLoader();
            plugins.getPlugins();
            if(plugins.hasPlugins())
                plugins.runPlugins();
            try {
                if(new File("system.properties").exists()) {
                    String[] properties = Files.readString(Path.of("system.properties")).split("\n|\r|\r\n");
                    for (String property :
                            properties) {
                        switch (property.split("=")[0]) {
                            case "project" -> mainWindow.setProject(new File(property.split("=")[1]));
                            case "compiler" -> mainWindow.elevenL = new File(property.split("=")[1]);
                            case "font" -> MainWindow.font = property.split("=")[1];
                            case "theme" -> {
                                MainWindow.theme = Integer.parseInt(property.split("=")[1]);
                                SyntaxHighlighter.setTheme(Integer.parseInt(property.split("=")[1]));
                            }
                        }
                    }
                    mainWindow.setVisible(true);
                } else {
                    new NewWindow(mainWindow);
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
