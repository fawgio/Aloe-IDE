package io.github.fawgio.aloe.highlight;

import io.github.fawgio.aloe.Aloe;
import io.github.fawgio.aloe.JCodePane;
import io.github.fawgio.aloe.MainWindow;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SyntaxHighlighter {
    public static List<Runnable[]> themeSets = new ArrayList<Runnable[]>();
    private final JTextPane textPane;
    private final JTextPane linePane;
    private final boolean toHighlight;

    public SyntaxHighlighter( JCodePane codePane, boolean toHighlight) {
        textPane = codePane.getCode();
        linePane = codePane.getLines();
        this.toHighlight = toHighlight;
    }

    public static void setTheme(int theme) {
        for (int i = 0; i < themeSets.get(theme).length; i++) {
            themeSets.get(theme)[i].run();
        }
    }

    public ArrayList<Token> tokenize(String elevenLString){
        ArrayList<Token> tokens = new ArrayList<>();
        for (int i = 0; i < elevenLString.length(); i++) {
            char c = elevenLString.charAt(i);
            Token cur;
            int from = i;
            StringBuilder s;
            if (c == '"') {
                s = new StringBuilder(Character.toString(c));
                c = elevenLString.charAt(++i);
                var j = 0;
                for (j = i; c!='"' && c != '\n' ; ){
                    if((c == '\\')&&(j+1 < elevenLString.length())){
                        s.append(c);
                        c = elevenLString.charAt(++j);
                    }
                    s.append(c);
                    c = elevenLString.charAt(++j);
                }
                s.append("\"");
                i = j;
                cur = new Token(Type.STRING, s.toString());
            } else if (1+i < elevenLString.length()&&(elevenLString.charAt(i) == '\\'&&(elevenLString.charAt(i+1) == '\\'))||(elevenLString.charAt(i) == '/'&&(elevenLString.charAt(i+1) == '/'))){
                s = new StringBuilder(Character.toString(elevenLString.charAt(i))+elevenLString.charAt(++i));
                while (1+i < elevenLString.length()&&(elevenLString.charAt(i)) != '\n')
                    s.append(elevenLString.charAt(++i));
                cur = new Token(Type.COMMENT, s.toString());
            } else if (1+i < elevenLString.length()&&elevenLString.charAt(i) == '\\'&& List.of('[','(','{','‘').contains(elevenLString.charAt(i + 1))){
                var cStart = elevenLString.charAt(i + 1);
                var cTerminate = List.of(']',')','}','’').get(List.of('[','(','{','‘').indexOf(cStart));
                var nestingLevel = 1;
                s = new StringBuilder(Character.toString(elevenLString.charAt(i))+elevenLString.charAt(++i));
                while (1+i < elevenLString.length()&&((((elevenLString.charAt(i)) != cTerminate)||(nestingLevel>0)))) {
                    s.append(elevenLString.charAt(++i));
                    if(elevenLString.charAt(i)==cStart)
                        nestingLevel++;
                    if(elevenLString.charAt(i)==cTerminate)
                        nestingLevel--;
                }
                cur = new Token(Type.COMMENT, s.toString());
            } else if (elevenLString.charAt(i) == '‘') {
                s = new StringBuilder("‘");
                while (1+i < elevenLString.length()&&(elevenLString.charAt(i)) != '’'&&(elevenLString.charAt(i)) != '\n')
                    s.append(elevenLString.charAt(++i));
                cur = new Token(Type.STRING, s.toString());
            } else if (i + 1 < elevenLString.length()&&(Character.toString(c)+elevenLString.charAt(i+1)).matches("[0-1]B")) {
                i++;
                cur = new Token(Type.BOOL, c+"B");
            } else if (Character.toString(c).matches("[0-9]")) {
                s = new StringBuilder(Character.toString(c));
                while (++i < elevenLString.length() && Character.toString((c = elevenLString.charAt(i))).matches("[0-9]"))
                    s.append(c);
                i--;
                cur = new Token(Type.NUMBER, s.toString());
            } else if (Character.toString(c).matches("[a-zA-Z]")) {
                s = new StringBuilder(Character.toString(c));
                while (++i < elevenLString.length() && Character.toString((c = elevenLString.charAt(i))).matches("[a-zA-Z.]"))
                    s.append(c);
                if (Arrays.asList(new String[]{
                        "in", "else", "fn", "if", "loop", "null", "return", "switch", "type", "var", "exception", "exception.catch", "exception.try", "exception.try_end", "F.args", "F.destructor", "F.virtual", "fn.args", "fn.destructor", "fn.virtual", "fn.virtual.abstract", "fn.virtual.assign", "fn.virtual.final", "fn.virtual.new", "fn.virtual.override", "I.likely", "I.unlikely", "if.likely", "if.unlikely", "L.break", "L.continue", "L.index", "L.last_iteration", "L.next", "L.on_break", "L.on_continue", "L.prev", "L.remove_current_element_and_break", "L.remove_current_element_and_continue", "L.was_no_break", "loop.break", "loop.continue", "loop.index", "loop.last_iteration", "loop.next", "loop.on_break", "loop.on_continue", "loop.prev", "loop.remove_current_element_and_break", "loop.remove_current_element_and_continue", "loop.was_no_break", "S.break", "S.fallthrough", "switch.break", "switch.fallthrough", "T.base", "T.enum", "T.interface", "type", "type.base", "type.enum", "type.interface", "X.catch", "X.try", "X.try_end", "F.virtual.abstract", "F.virtual.assign", "F.virtual.final", "F.virtual.new", "F.virtual.override", "C", "E", "F", "I", "L", "N", "R", "S", "T", "V", "X"
                }).contains(s.toString())) {
                    cur = new Token(Type.KEYWORD, s.toString());
                } else {
                    cur = new Token(Type.NOTHING, s.toString());
                }
                i--;
            } else {
                cur = new Token(Type.OTHER, Character.toString(c));
            }
            tokens.add(cur.from(from).to(i));
        }
        return tokens;
    }

    public static final MutableAttributeSet stringSet = new SimpleAttributeSet();
    public static final MutableAttributeSet numberSet = new SimpleAttributeSet();
    public static final MutableAttributeSet boolSet = new SimpleAttributeSet();
    public static final MutableAttributeSet keywordSet = new SimpleAttributeSet();
    public static final MutableAttributeSet otherSet = new SimpleAttributeSet();
    public static final MutableAttributeSet nothingSet = new SimpleAttributeSet();
    public static final MutableAttributeSet commentSet = new SimpleAttributeSet();

    // standard theme sets
    static {
        themeSets.add(new Runnable[]{() -> {
            StyleConstants.setItalic(stringSet, false);
            StyleConstants.setBold(stringSet, false);
            StyleConstants.setForeground(stringSet, Color.GREEN);

            StyleConstants.setItalic(numberSet, false);
            StyleConstants.setBold(numberSet, false);
            StyleConstants.setForeground(numberSet, Color.BLUE);

            StyleConstants.setItalic(boolSet, false);
            StyleConstants.setBold(boolSet, true);
            StyleConstants.setForeground(boolSet, Color.BLUE);

            StyleConstants.setItalic(keywordSet, false);
            StyleConstants.setBold(keywordSet, true);
            StyleConstants.setForeground(keywordSet, new Color(200, 100, 100));

            StyleConstants.setItalic(otherSet, false);
            StyleConstants.setBold(otherSet, false);
            StyleConstants.setForeground(otherSet, new Color(200, 0, 0));

            StyleConstants.setItalic(nothingSet, false);
            StyleConstants.setBold(nothingSet, false);
            StyleConstants.setForeground(nothingSet, Color.BLACK);

            StyleConstants.setBold(commentSet, false);
            StyleConstants.setItalic(commentSet, true);
            StyleConstants.setForeground(commentSet, Color.GRAY);

            Aloe.mainWindow.setJCodePanesColor(Color.BLACK,Color.WHITE);
        }});

        themeSets.add(new Runnable[]{() -> {
            StyleConstants.setItalic(stringSet, false);
            StyleConstants.setBold(stringSet, false);
            StyleConstants.setForeground(stringSet, Color.BLUE);

            StyleConstants.setItalic(numberSet, false);
            StyleConstants.setBold(numberSet, false);
            StyleConstants.setForeground(numberSet, Color.MAGENTA);

            StyleConstants.setItalic(boolSet, false);
            StyleConstants.setBold(boolSet, true);
            StyleConstants.setForeground(boolSet, Color.MAGENTA);

            StyleConstants.setItalic(keywordSet, false);
            StyleConstants.setBold(keywordSet, true);
            StyleConstants.setForeground(keywordSet, Color.ORANGE);

            StyleConstants.setItalic(otherSet, false);
            StyleConstants.setBold(otherSet, false);
            StyleConstants.setForeground(otherSet, Color.RED);

            StyleConstants.setItalic(nothingSet, false);
            StyleConstants.setBold(nothingSet, false);
            StyleConstants.setForeground(nothingSet, Color.WHITE);

            StyleConstants.setBold(commentSet, false);
            StyleConstants.setItalic(commentSet, true);
            StyleConstants.setForeground(commentSet, Color.LIGHT_GRAY);

            Aloe.mainWindow.setJCodePanesColor(Color.WHITE,Color.DARK_GRAY);
        }});

        themeSets.add(new Runnable[]{() -> {
            StyleConstants.setItalic(stringSet, false);
            StyleConstants.setBold(stringSet, false);
            StyleConstants.setForeground(stringSet, new Color(150,250,0));

            StyleConstants.setItalic(numberSet, false);
            StyleConstants.setBold(numberSet, false);
            StyleConstants.setForeground(numberSet, new Color(0,250,250));

            StyleConstants.setItalic(boolSet, false);
            StyleConstants.setBold(boolSet, true);
            StyleConstants.setForeground(boolSet, new Color(0,250,250));

            StyleConstants.setItalic(keywordSet, false);
            StyleConstants.setBold(keywordSet, true);
            StyleConstants.setForeground(keywordSet, Color.MAGENTA);

            StyleConstants.setItalic(otherSet, false);
            StyleConstants.setBold(otherSet, false);
            StyleConstants.setForeground(otherSet, Color.YELLOW);

            StyleConstants.setItalic(nothingSet, false);
            StyleConstants.setBold(nothingSet, false);
            StyleConstants.setForeground(nothingSet, Color.WHITE);

            StyleConstants.setBold(commentSet, false);
            StyleConstants.setItalic(commentSet, true);
            StyleConstants.setForeground(commentSet, new Color(180,200,180));

            Aloe.mainWindow.setJCodePanesColor(Color.WHITE,Color.DARK_GRAY);
        }});
    }

    public void doHighlight(){
        ArrayList<Token> tokens = null;
        try {
            if(toHighlight)
                tokens = tokenize(textPane.getStyledDocument().getText(0,textPane.getStyledDocument().getLength()));
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        StyledDocument doc = textPane.getStyledDocument();

        if (tokens != null) {
            for (Token t :
                    tokens) {
                doc.setCharacterAttributes(t.from(),t.length(),switch (t.type()) {
                    case STRING -> stringSet;
                    case NUMBER -> numberSet;
                    case BOOL -> boolSet;
                    case KEYWORD -> keywordSet;
                    case NOTHING -> nothingSet;
                    case OTHER -> otherSet;
                    case COMMENT -> commentSet;
                },false);
            }
        }

        StringBuilder builder = new StringBuilder().append("0002\n");

        for (int i = textPane.getText().indexOf("\n")+1; i < textPane.getText().length(); i++) {
            if (textPane.getText().charAt(i) == '\n') {
                builder.append("0".repeat(4 - String.valueOf((builder.toString().split("\n").length + 2)).length()))
                        .append(builder.toString().split("\n").length + 1)
                        .append("\n");
            }
        }
        try {
            linePane.getStyledDocument().remove(5, linePane.getStyledDocument().getLength() - 5);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        try {
            linePane.getStyledDocument().insertString(5,builder.toString(),new SimpleAttributeSet());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        MutableAttributeSet removeErrorSet = new SimpleAttributeSet();
        StyleConstants.setBackground(removeErrorSet, new Color(0,0,0,0));
        StyleConstants.setUnderline(removeErrorSet,false);
        doHighlight(0, textPane.getText().length(), removeErrorSet);
        MainWindow.save();
        Aloe.mainWindow.compilerStart("-t",true);
    }

    public void doHighlight(int start, int len, MutableAttributeSet set) {
        textPane.getStyledDocument().setCharacterAttributes(start, len,set,false);
    }
}