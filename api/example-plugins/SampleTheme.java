import io.github.fawgio.aloe.api.*;
import static io.github.fawgio.aloe.highlight.SyntaxHighlighter.*;

import java.awt.*;
import javax.swing.text.*;

public class SampleTheme implements AloePlugin, AloeTheme{
    @Override
    public void start(){
        System.out.println("[SampleTheme] Plugin started");
    }
    @Override
    public String getThemeName(){
        return "Sample";
    }
    @Override
    public Runnable applyTheme(){
        System.out.println("[SampleTheme] Added theme \"Sample\"!");
        return () -> {
            StyleConstants.setItalic(stringSet, false);
            StyleConstants.setBold(stringSet, false);
            StyleConstants.setForeground(stringSet, Color.MAGENTA);

            StyleConstants.setItalic(numberSet, false);
            StyleConstants.setBold(numberSet, false);
            StyleConstants.setForeground(numberSet, Color.GREEN);

            StyleConstants.setItalic(boolSet, false);
            StyleConstants.setBold(boolSet, true);
            StyleConstants.setForeground(boolSet, Color.GREEN);

            StyleConstants.setItalic(keywordSet, false);
            StyleConstants.setBold(keywordSet, true);
            StyleConstants.setForeground(keywordSet, Color.MAGENTA);

            StyleConstants.setItalic(otherSet, false);
            StyleConstants.setBold(otherSet, false);
            StyleConstants.setForeground(otherSet, Color.RED);

            StyleConstants.setItalic(nothingSet, false);
            StyleConstants.setBold(nothingSet, false);
            StyleConstants.setForeground(nothingSet, Color.GREEN);

            StyleConstants.setBold(commentSet, false);
            StyleConstants.setItalic(commentSet, true);
            StyleConstants.setForeground(commentSet, Color.GRAY);
        };
    }
    @Override
    public Color getForeground(){
        return Color.GREEN;
    }
    @Override
    public Color getBackground(){
        return Color.BLACK;
    }
}