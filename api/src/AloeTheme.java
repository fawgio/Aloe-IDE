package io.github.fawgio.aloe.api;

import java.awt.*;

/**
 * Implement it to create themes
 */
public interface AloeTheme {
    /**
     * @return theme name for SettingsWindow
     */
    String getThemeName();

    /**
     * @return Runnable which starts when this theme is selected
     */
    Runnable applyTheme();

    /**
     * @return foreground Color of this themes
     */
    Color getForeground();

    /**
     * @return background Color of this themes
     */
    Color getBackground();
}
