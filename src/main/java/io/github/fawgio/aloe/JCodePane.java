package io.github.fawgio.aloe;

import io.github.fawgio.aloe.highlight.SyntaxHighlighter;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Special Component for code editing.
 * Contains of 2 JTextPanes (for text and for line numbers) wrapped by JScrollPane
 */
public class JCodePane extends JScrollPane implements KeyListener {
    private JTextPane code = new JTextPane();
    private JTextPane lines = new JTextPane();
    private boolean selected;
    private final boolean toHighlight;
    private final JPopupMenu autoComplete = new JPopupMenu();

    //autocomplete words
    static private final String[] modules = new String[]{"f","os","fs","time","re","random","minheap","maxheap","bits","csv"};
    static private final String[] keyLetters = new String[]{"C","E","F","I","L","N","R","S","T","V","X"};
    static private final String[] keyWords = new String[]{"in","else","fn","if","loop","null","return","switch","type","var","exception",
            "exception.catch","exception.try","exception.try_end","F.args","F.destructor","F.virtual","fn.args","fn.destructor","fn.virtual","fn.virtual.abstract","fn.virtual.assign","fn.virtual.final","fn.virtual.new","fn.virtual.override","I.likely","I.unlikely","if.likely","if.unlikely","L.break","L.continue","L.index","L.last_iteration","L.next","L.on_break","L.on_continue","L.prev","L.remove_current_element_and_break","L.remove_current_element_and_continue","L.was_no_break","loop.break","loop.continue","loop.index","loop.last_iteration","loop.next","loop.on_break","loop.on_continue","loop.prev","loop.remove_current_element_and_break","loop.remove_current_element_and_continue","loop.was_no_break","S.break","S.fallthrough","switch.break","switch.fallthrough","T.base","T.enum","T.interface","type","type.base","type.enum","type.interface","X.catch","X.try","X.try_end",
            "F.virtual.abstract","F.virtual.assign","F.virtual.final","F.virtual.new","F.virtual.override"};
    static private final String[] functions = new String[]{"print(object = ‘’, end = \"\\n\")","input([prompt])","assert(expression, message = ‘’)","exit(message=N)",
            "sleep(secs)","swap(&a, &b)","zip(iterable1, iterable2 new String[]{,iterable3])","all(iterable)","any(iterable)",
            "cart_product(iterable1, iterable2 new String[]{,iterable3])","multiloop((iterable1, iterable2 new String[]{,iterable3], function))",
            "multiloop_filtered(iterable1, iterable2 new String[]{,iterable3], filter_function, function)","sum(iterable)",
            "sorted(iterable, key = N, reverse = 0B)","product(iterable)","min(iterable)","min(arg1,arg2)","max(iterable)","max(arg1,arg2)",
            "hex(x)","bin(x)","rotl(value, shift)","rotr(value, shift)"};
    private static final List<String> auto = new ArrayList<>();
    private final NHistory nHistory = new NHistory();
    static {
        Collections.addAll(auto,modules);
        Collections.addAll(auto,keyWords);
        Collections.addAll(auto,keyLetters);
        Collections.addAll(auto,functions);
    }

    /**
     * @param toHighlight if true then JCodePane text has to be highlighted every time key released
     */
    JCodePane(boolean toHighlight){
        this.toHighlight = toHighlight;
        code.getDocument().addUndoableEditListener(nHistory);
        code.setFont(new Font(MainWindow.font, Font.PLAIN, 15));
        code.setForeground(MainWindow.fore);
        code.setCaretColor(MainWindow.fore);
        setViewportView(code);
        setBg(Aloe.mainWindow.back);
        lines.setFont(new Font(MainWindow.font, Font.ITALIC, 15));
        lines.setForeground(Color.GRAY);
        lines.setEditable(false);
        try {
            lines.getStyledDocument().insertString(lines.getStyledDocument().getLength(),"0001\n",new SimpleAttributeSet());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        setRowHeaderView(lines);
        var self = this;
        JPopupMenu jPopupMenu = new JPopupMenu();
        JMenuItem close = new JMenuItem("Close");
        close.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((JTabbedPane)self.getParent()).removeTabAt(((JTabbedPane)self.getParent()).indexOfComponent(self));
            }
        });
        code.addKeyListener(this);
        jPopupMenu.add(close);
        code.setComponentPopupMenu(jPopupMenu);
        setComponentPopupMenu(autoComplete);
        JMenuItem delete = new JMenuItem("Delete");
        JMenuItem rename = new JMenuItem("Rename");
        delete.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(null, "Are you sure?", "Confirm deleting", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == 0){
                    try {
                        Files.delete(MainWindow.current.toPath());
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
                MainWindow.projectTree.setModelFromFile(MainWindow.projectTree.getRoot());
            }
        });
        rename.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                var name = JOptionPane.showInputDialog("Rename to");
                if (name!=null)
                    MainWindow.current.renameTo(new File(MainWindow.current.getPath().replace(MainWindow.current.getName(), name)));
                MainWindow.projectTree.setModelFromFile(MainWindow.projectTree.getRoot());
            }
        });
        jPopupMenu.addSeparator();
        jPopupMenu.add(delete);
        jPopupMenu.add(rename);
    }

    /**
     * @param toHighlight if true then JCodePane text has to be highlighted every time key released
     * @param text standard text
     */
    public JCodePane(boolean toHighlight, String text) {
        this(toHighlight);
        setText(text);
    }

    /**
     * @return is tab when this JCodePane selected
     */
    @Deprecated
    public boolean isSelected(){
        return selected;
    }

    /**
     * @param b is tab when this JCodePane selected
     * @return this JCodePane
     */
    @Deprecated
    public JCodePane isSelected(boolean b){
        selected = b;
        return this;
    }

    /**
     * @return text of JCodePane
     */
    public String getText() {
        return code.getText().replace("\r\n","\n");
    }

    /**
     * Sets JTextPane where code is written
     * @param code a JTextPane where code is written
     */
    public void setCode(JTextPane code) {
        this.code = code;
    }

    /**
     * Gets a JTextPane where line numbers have to be printed
     * @return a JTextPane where line numbers have to be printed
     */
    public JTextPane getLines() {
        return lines;
    }

    /**
     * Gets JTextPane where code is written
     * @return a JTextPane where code is written
     */
    public JTextPane getCode() {
        return code;
    }

    /**
     * Sets a JTextPane where line numbers have to be printed
     * @param lines a JTextPane where line numbers have to be printed
     */
    public void setLines(JTextPane lines) {
        this.lines = lines;
    }

    /**
     * Sets text of JCodePane
     * @param text
     * @return this JCodePane
     */
    public JCodePane setText(String text) {
        code.setText(text);
        return this;
    }

    /**
     * Sets background color of JCodePane
     * @param bg background color
     * @return this JCodePane
     */
    public JCodePane setBg(Color bg) {
        code.setBackground(bg);
        lines.setBackground(bg);
        return this;
    }

    /**
     * Highlight text and calculate numbers of JCodePane
     * @return this JCodePane
     */
    public JCodePane highlight() {
        new SyntaxHighlighter(this,toHighlight).doHighlight();
        return this;
    }

    @Deprecated
    public JCodePane setCaption(String name){
        super.setName(name);
        return this;
    }

    /**
     * Is Control button pressed now
     */
    boolean ctrl_click = false;
    /*
     * KeyListener methods
     */
    @Override
    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode()!=KeyEvent.VK_SPACE)
//          history.act(new Change(getLastWord(getText().substring(0,code.getCaretPosition())),code.getCaretPosition()-getLastWord(getText()).length()));
        if(e.getKeyCode()==KeyEvent.VK_CONTROL)
            ctrl_click = false;
        new Thread(this::highlight).start();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_CONTROL){
            ctrl_click = true;
        } else if(e.getKeyCode()==KeyEvent.VK_SPACE&&ctrl_click){
            addAutoComplete();
            e.consume();
        } else if(e.getKeyCode()==KeyEvent.VK_F6) {
            Aloe.mainWindow.findNext();
            e.consume();
        } else if(e.getKeyCode()==KeyEvent.VK_F5) {
            Aloe.mainWindow.findPrev();
            e.consume();
        }
    }

    /**
     * @see NHistory
     * @return NHistory of this JCodePane
     */
    public NHistory getNHistory(){
        return nHistory;
    }


    /**
     * Adds autocomplete
     */
    private void addAutoComplete() {
        autoComplete.removeAll();
        final Pattern pattern = Pattern.compile("(.*[\n\r\s])*([^\n\r\s]*)$", Pattern.MULTILINE);
        var str = getText().substring(0, code.getCaretPosition());
        final Matcher matcher = pattern.matcher(str);
        var temp = matcher.replaceAll("$2");
        for (String elem:
             auto) {
            if(elem.startsWith(temp)){
                if(Arrays.stream(functions).toList().contains(elem))
                    elem = "fn    "+elem;
                else if(Arrays.stream(modules).toList().contains(elem))
                    elem = "module    "+elem;
                JMenuItem hint = new JMenuItem(elem);
                String finalElem = elem;
                hint.addActionListener(new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        autoComplete.setVisible(false);
                        var start = code.getCaretPosition();
                        var valueToPaste = Pattern.compile("(?<=\\().*(?=\\))|fn {4}|module| {4}",Pattern.MULTILINE).matcher(finalElem).replaceAll("");
                        code.setText(code.getText().substring(0, code.getCaretPosition() - temp.length()) +
                                valueToPaste + code.getText().substring(code.getCaretPosition()));
                        code.setCaretPosition( start + valueToPaste.length()-temp.length());
                        if(finalElem.startsWith("fn    ")){ //print(*cursor here*)
                            code.setCaretPosition(code.getCaretPosition()-1);
                        }
                        highlight();
                    }
                });
                autoComplete.add(hint);
            }
        }
        autoComplete.setVisible(true);
        autoComplete.show(this, 0, 0);

        try {
            var rect = code.modelToView2D(code.getCaretPosition());
            autoComplete.setLocation(Math.toIntExact(Math.round(rect.getX()))+(Toolkit.getDefaultToolkit().getScreenSize().width-getWidth()-lines.getWidth()),Math.toIntExact(Math.round(rect.getY()))+(Toolkit.getDefaultToolkit().getScreenSize().height-getHeight()- MainWindow.menu.getHeight()));
        } catch (BadLocationException badLocationException) {
            badLocationException.printStackTrace();
        }

        InputMap inputMap = autoComplete.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = autoComplete.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");

        actionMap.put("up", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                Component selected = KeyboardFocusManager.getCurrentKeyboardFocusManager().
                        getFocusOwner();
                if (selected instanceof JMenuItem) {
                    int index = autoComplete.getComponentIndex(selected);
                    if (index > 0) {
                        autoComplete.getComponent(index - 1).requestFocus();
                    }
                }
            }
        });

        actionMap.put("down", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                Component selected = KeyboardFocusManager.getCurrentKeyboardFocusManager().
                        getFocusOwner();
                if (selected instanceof JMenuItem) {
                    int index = autoComplete.getComponentIndex(selected);
                    if (index < autoComplete.getComponentCount() - 1) {
                        autoComplete.getComponent(index + 1).requestFocus();
                    }
                }
            }
        });
        ctrl_click = false;
    }

    /**
     * Highlight area of the JCodePane text
     * @param start the position of start symbol of area
     * @param len the length of area
     * @param set the MutableAttributeSet
     * @return this JCodePane
     */
    public JCodePane highlight(int start, int len, MutableAttributeSet set) {
        new SyntaxHighlighter(this,toHighlight).doHighlight(start, len, set);
        return this;
    }

}
