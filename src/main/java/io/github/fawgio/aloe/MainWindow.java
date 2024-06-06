package io.github.fawgio.aloe;

import io.github.fawgio.aloe.highlight.SyntaxHighlighter;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

/**
 * The main window of Aloe IDE
 */
public class MainWindow extends JFrame implements WindowListener {
    public static final JLabel CONSOLE_LABEL = new JLabel("Console");
    public static String font;
    public static final Font NORMAL_FONT = new Font(font, Font.PLAIN, 15);
    private static String lastChanged;
    public static Color fore;
    public static int theme;
    static JTree projectTree = new JTree();
    static JPanel mainContent = new JPanel(new BorderLayout());
    public static JTabbedPane codeTabs = new JTabbedPane();
    public static JMenuBar menu = new JMenuBar();
    static JMenu fileMenu = new JMenu("File");
    static JMenuItem openFile = new JMenuItem("Open");
    static JMenuItem saveFile = new JMenuItem("Save");
    static JMenu newFile = new JMenu("New...");
    static JMenuItem newProjectFile = new JMenuItem("Project");
    static JMenuItem newFileFile = new JMenuItem("File");
    static JMenuItem newFolderFile = new JMenuItem("Folder");
    static JMenuItem settingsFile = new JMenuItem("Settings");
    static JMenuItem exitFile = new JMenuItem("Exit");
    static JMenu editMenu = new JMenu("Edit");
    static JMenuItem undoEdit = new JMenuItem("Undo");
    static JMenuItem redoEdit = new JMenuItem("Redo");
    static JMenuItem findEdit = new JMenuItem("Find next");
    static JMenuItem replaceEdit = new JMenuItem("Replace all");
    static JMenuItem commentEdit = new JMenuItem("Comment");
    static JMenu runMenu = new JMenu("Run");
    static JMenuItem buildRun = new JMenuItem("Build");
    static JMenuItem execRun = new JMenuItem("Execute");
    static JMenu helpMenu = new JMenu("Help");
    static JMenuItem aboutHelp = new JMenuItem("About");
    public static JTextPane console = new JTextPane();
    static JTextArea input = new JTextArea();
    static JPanel consolePanel = new JPanel(new BorderLayout());
    static JScrollPane consoleScrollPane = new JScrollPane(console);

    public static String q = "";

    File elevenL;
    static File current;
    static File project;
    public Color back;
    public DefaultMutableTreeNode root;

    static {
        console.setBackground(Color.BLACK);
        input.setBackground(Color.BLACK);
        input.setForeground(Color.green);
        input.setCaretColor(Color.green);
        console.setFont(NORMAL_FONT);
        input.setFont(NORMAL_FONT);
        console.setEditable(false);
        consolePanel.add(consoleScrollPane);
        consolePanel.add(input, BorderLayout.SOUTH);
        consolePanel.add(CONSOLE_LABEL, BorderLayout.NORTH);
        CONSOLE_LABEL.setFont(NORMAL_FONT);
        CONSOLE_LABEL.setForeground(Color.white);
        consolePanel.setBackground(Color.BLACK);
        consoleScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        consoleScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        consoleScrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
            if(!console.getText().equals(lastChanged)) {
                e.getAdjustable().setValue(e.getAdjustable().getMaximum());
                lastChanged = console.getText();
            }
        });
        codeTabs.addChangeListener(e -> {
            if (codeTabs.getTabCount() > 0) {
                for (Component codePane :
                        codeTabs.getComponents()) {
                    ((JCodePane) codePane).isSelected(codeTabs.indexOfComponent(codePane) == codeTabs.getSelectedIndex());
                }
                current = new File(codeTabs.getSelectedComponent().getName());
                ((JCodePane) codeTabs.getSelectedComponent()).isSelected(true).highlight();
            }
        });
        openFile.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser j = new JFileChooser();
                j.setDialogTitle("Choose directory of your project");
                j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int res = j.showOpenDialog(null);
                if (res == JFileChooser.APPROVE_OPTION) {
                    Aloe.mainWindow.setProject(j.getSelectedFile());
                }
            }
        });
        openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,InputEvent.CTRL_MASK));
        saveFile.addActionListener((e)-> save());
        saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_MASK));
        commentEdit.addActionListener((e)-> {
            var start = ((JCodePane) codeTabs.getSelectedComponent()).getCode().getSelectionStart();
            var end = ((JCodePane) codeTabs.getSelectedComponent()).getCode().getSelectionEnd();
            try {
                if(!Arrays.asList("\\‘","\\(","\\[","\\{").contains(((JCodePane) codeTabs.getSelectedComponent()).getCode().getDocument().getText(start,2))) {
                    ((JCodePane) codeTabs.getSelectedComponent()).getCode().getDocument().insertString(start, "\\‘", SyntaxHighlighter.commentSet);
                    ((JCodePane) codeTabs.getSelectedComponent()).getCode().getDocument().insertString(end+2, "’", SyntaxHighlighter.commentSet);
                    ((JCodePane) codeTabs.getSelectedComponent()).highlight();
                } else {
                    ((JCodePane) codeTabs.getSelectedComponent()).getCode().getDocument().remove(start,2);
                    if(Arrays.asList("’",")","]","}").contains(((JCodePane) codeTabs.getSelectedComponent()).getCode().getDocument().getText(end-3,1)))
                        ((JCodePane) codeTabs.getSelectedComponent()).getCode().getDocument().remove(end-3,1);
                }
            } catch (BadLocationException badLocationException) {
                badLocationException.printStackTrace();
            }
        });
        commentEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SLASH,InputEvent.CTRL_MASK));
        newFileFile.addActionListener((e) -> newFile());
        newFileFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,InputEvent.CTRL_MASK));
        newFolderFile.addActionListener((e) -> {
            try {
                String file = project.getPath() + System.getProperty("file.separator") + JOptionPane.showInputDialog("New Folder");
                Files.createDirectory(Path.of(file));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        newFolderFile.setAccelerator(KeyStroke.getKeyStroke("F7"));
        newProjectFile.addActionListener(e -> Aloe.mainWindow.createProject());
        newProjectFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,InputEvent.CTRL_MASK|InputEvent.SHIFT_MASK));
        settingsFile.addActionListener(e -> new SettingsWindow());
        buildRun.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Aloe.mainWindow.compilerStart("",false);
            }
        });
        buildRun.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9,InputEvent.SHIFT_MASK));
        execRun.addActionListener(e -> Aloe.mainWindow.exec(new File(current.getParent()+System.getProperty("file.separator")+
                current.getName().split("\\.")[0] + (System.getProperty("os.name").startsWith("Windows")?".exe":""))));
        execRun.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9,InputEvent.CTRL_MASK));
        aboutHelp.addActionListener(e -> {
            JDialog about = new JDialog();
            JPanel aloeP = new JPanel();
            aloeP.add(new JLabel("<html><h1><font color=green size=15>Aloe</font> IDE</h1></html>"));
            aloeP.add(new JLabel("Alexander Boolba"));
            aloeP.add(new JLabel("(ↄ) 2024 All lefts reversed"));
            aloeP.add(new JLabel("Version: 1.0.0-beta"));
            JPanel elevenLp = new JPanel();
            elevenLp.add(new JLabel("<html><h1><font face=\"Courier New\" size=15>11l</font></h1></html>"));
            elevenLp.add(new JLabel("© 2018-2020 Alexander Tretyak"));
            elevenLp.add(new JLabel("Version: 2024.4"));
            about.add(aloeP);
            about.add(elevenLp);
            about.setUndecorated(true);
            about.setResizable(false);
            about.setSize(new Dimension(450,175));
            about.setLocationRelativeTo(Aloe.mainWindow);
            about.setLayout(new GridLayout());
            ((JPanel)about.getContentPane()).setBorder(BorderFactory.createEmptyBorder(25,25,25,25));
            about.setVisible(true);
        });
        findEdit.addActionListener(e ->{
            q = JOptionPane.showInputDialog(null,null,"Search...",JOptionPane.QUESTION_MESSAGE,null,null, q).toString();
            Aloe.mainWindow.findNext();
        });
        findEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,InputEvent.CTRL_MASK));
        replaceEdit.addActionListener(e ->{
            var q = JOptionPane.showInputDialog(null,null,"Replace...",JOptionPane.QUESTION_MESSAGE,null,null, null).toString();
            String replacement = JOptionPane.showInputDialog(null, null, "... with ...", JOptionPane.QUESTION_MESSAGE, null, null, q).toString();
            ((JCodePane)codeTabs.getSelectedComponent()).getCode().setText(((JCodePane)codeTabs.getSelectedComponent()).getText().replaceAll(q,replacement));
            ((JCodePane)codeTabs.getSelectedComponent()).highlight();
        });
        replaceEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,InputEvent.CTRL_MASK|InputEvent.SHIFT_MASK));
        undoEdit.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //((JCodePane)codeTabs.getSelectedComponent()).getHistory().undo();
                ((JCodePane)codeTabs.getSelectedComponent()).getNHistory().undo();
            }
        });
        undoEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,InputEvent.CTRL_MASK));
        redoEdit.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //((JCodePane)codeTabs.getSelectedComponent()).getHistory().redo();
                ((JCodePane)codeTabs.getSelectedComponent()).getNHistory().redo();
            }
        });
        redoEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,InputEvent.CTRL_MASK));
        exitFile.addActionListener(e -> Aloe.mainWindow.close(e));
        exitFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,InputEvent.ALT_MASK));
    }

    MainWindow() {
        super("Aloe IDE");
        fileMenu.add(newFile);
        fileMenu.add(openFile);
        newFile.add(newProjectFile);
        newFile.addSeparator();
        newFile.add(newFileFile);
        newFile.add(newFolderFile);
        fileMenu.addSeparator();
        fileMenu.add(saveFile);
        fileMenu.addSeparator();
        fileMenu.add(settingsFile);
        fileMenu.add(exitFile);
        menu.add(fileMenu);
        editMenu.add(undoEdit);
        editMenu.add(redoEdit);
        editMenu.add(findEdit);
        editMenu.add(replaceEdit);
        editMenu.add(commentEdit);
        menu.add(editMenu);
        runMenu.add(buildRun);
        runMenu.add(execRun);
        menu.add(runMenu);
        helpMenu.add(aboutHelp);
        menu.add(helpMenu);
        mainContent.add(projectTree, BorderLayout.WEST);
        mainContent.add(codeTabs, BorderLayout.CENTER); // add codeArea with scroll panes
        mainContent.add(consolePanel, BorderLayout.SOUTH);
        consoleScrollPane.setPreferredSize(new Dimension(consolePanel.getWidth(),200));
        add(mainContent);
        add(menu, BorderLayout.NORTH);
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        addWindowListener(this);
        ImageIcon icon = new ImageIcon("icon.gif");
        setIconImage(icon.getImage());
    }

    Process p;

    /**
     * Read input from executing 11l program and print it to console
     */
    void readInputFromProcess() {
        try {
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder s = new StringBuilder(Character.toString((char) stdInput.read()));
            while (stdInput.ready()) {
                s.append((char) stdInput.read());
            }
            MutableAttributeSet attributeSet = new SimpleAttributeSet();
            StyleConstants.setForeground(attributeSet, Color.green);
            console.getStyledDocument().insertString(console.getStyledDocument().getLength(), s.toString(), attributeSet);
        } catch (IOException | BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Executes an 11l program, analyze input written by user and send it to program
     * @param executable an 11l program
     */
    public void exec(File executable) {
        try {
            p = Runtime.getRuntime().exec(String.valueOf(executable));
            OutputStreamWriter stdOutput = new OutputStreamWriter(p.getOutputStream());
            var self = this;
            new Thread(this::readInputFromProcess).start();
            input.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent ke) {
                    if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
                        ke.consume(); // Prevent the newline character from being added to input
                        if(!input.getText().startsWith("$aloe"))
                            sendInputToProcess();
                        else {
                            switch (input.getText().split("\\$aloe\\.")[1]) {
                                case "clear" -> console.setText("");
                            }
                        }
                        input.setText("");
                    }
                }

                private void sendInputToProcess() {
                    try {
                        String userInput = input.getText();
                        stdOutput.write(userInput + System.lineSeparator());
                        stdOutput.flush();
                        new Thread(self::readInputFromProcess).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    /**
     * Save current file
     */
    public static void save() {
        try {
            Files.writeString(current.toPath(), ((JCodePane)codeTabs.getSelectedComponent()).getText(), StandardCharsets.UTF_8);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    /**
     * Create a file and request its name
     */
    public static void newFile() {
        try {
            String file = project.getPath() + System.getProperty("file.separator") + JOptionPane.showInputDialog("New File");
            Files.createFile(Path.of(file));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    /**
     * Read input and errors from 11l compiler
     * @param highlightErrors are errors have to be highlighted in the code instead of printing
     */
    void readInputFromProcess(boolean highlightErrors) {
        try {
            if (new InputStreamReader(p.getInputStream(), "CP866").read()!=-1){
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), "CP866"));
                String line;
                while (true) {
                    line = r.readLine();
                    if (line == null) {
                        break;
                    }
                    MutableAttributeSet attributeSet = new SimpleAttributeSet();
                    StyleConstants.setForeground(attributeSet, Color.white);
                    console.getStyledDocument().insertString(console.getStyledDocument().getLength(), line + "\n", attributeSet);
                }
            }
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getErrorStream(), "CP866"));
            String line;
            StringBuilder errorMessage = new StringBuilder();
            while (true) {
                line = r.readLine();
                if (line == null) { break; }
                errorMessage.append(line).append("\n");
            }
            if (!errorMessage.isEmpty()) {
                if(!highlightErrors) {
                    MutableAttributeSet attributeSet = new SimpleAttributeSet();
                    StyleConstants.setForeground(attributeSet, Color.RED);
                    console.getStyledDocument().insertString(console.getStyledDocument().getLength(), errorMessage.toString(), attributeSet);
                } else {
                    parse11lError(errorMessage.toString());
                }
            }
        } catch (IOException | BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parse an 11l error
     * @param error the error
     * @return a parsed error
     */
    private String parse11lError(String error) {
        if (error.matches(".*:.*\n in file '.*', line \\d*\n.*\n.*\n")){
            String type = error.split(":",2)[0];
            String description = error.split(":",2)[1].split("\n",2)[0];
            String line = error.split(".*:.*\n in file '.*', line \\d*\n")[1].split("\n")[0];
            String filename = error.split(".*:.*\n in file '")[1].split("',")[0];
            int row = Integer.parseInt(error.split(".*:.*\n in file '.*', line ")[1].split("\n")[0]);
            int col = error.split(".*:.*\n in file '.*', line \\d*\n.*\n")[1].indexOf("^");
            int len = error.split(".*:.*\n in file '.*', line \\d*\n.*\n")[1].lastIndexOf("^") - col +1;
            MutableAttributeSet errorSet = new SimpleAttributeSet();
            StyleConstants.setBackground(errorSet, Color.RED);
            StyleConstants.setUnderline(errorSet, true);
            int start = 0;
            var lines = ((JCodePane)codeTabs.getSelectedComponent()).getText().split("\n");
            for (int i = 0; i < row-1; i++) {
                start += lines[i].length() + 1;
            }
            ((JCodePane)codeTabs.getSelectedComponent()).highlight(start + col, len, errorSet);
            return filename+"("+row+","+col+"):"+description+" on line "+line;
        }
        return error;
    }

    /**
     * Start compiling current code
     * @param params compiler params (see 11l -help)
     * @param highlightErrors are error have to be highlighted in the code instead of printing in the console
     */
    public void compilerStart(String params, boolean highlightErrors){
        compilerStart(current.getPath(),params,highlightErrors);
    }

    /**
     * Start compiling a file
     * @param file the file
     * @param params compiler params (see 11l -help)
     * @param highlightErrors are error have to be highlighted in the code instead of printing in the console
     */
    public void compilerStart(String file, String params, boolean highlightErrors) {
        if (elevenL == null) {
            JFileChooser j = new JFileChooser();
            j.setDialogTitle("Choose 11l compiler");
            int res = j.showOpenDialog(null);
            if (res == JFileChooser.APPROVE_OPTION) {
                elevenL = j.getSelectedFile();
            }
        }
        try {
            ProcessBuilder builder = new ProcessBuilder(elevenL.getPath(), file, params);
            p = builder.start();
            console.getStyledDocument().remove(0, console.getStyledDocument().getLength());
            MutableAttributeSet attributeSet = new SimpleAttributeSet();
            StyleConstants.setForeground(attributeSet, Color.white);
            console.getStyledDocument().insertString(console.getStyledDocument().getLength(), "[BUILD]\n", attributeSet);
            readInputFromProcess(highlightErrors);
            File to = new File(current.getParent()+System.getProperty("file.separator")+
                    current.getName().split("\\.")[0] + (System.getProperty("os.name").startsWith("Windows")?".exe":""));
            File from = new File(System.getProperty("user.dir")+System.getProperty("file.separator")+current.getName().split("\\.11l")[0] + (System.getProperty("os.name").startsWith("Windows")?".exe":""));
            if(to.exists())
                to.delete();
            if(from.exists())
                from.renameTo(to);
        } catch (IOException | BadLocationException ioException) {
            ioException.printStackTrace();
        }
    }

    /**
     * Find next phrase
     * @return array contains of start and end of found phrase or {-1, -1} if it wasn't found
     */
    public int[] findNext() {
        if(((JCodePane)codeTabs.getSelectedComponent()).getText()
                .substring(((JCodePane)codeTabs.getSelectedComponent()).getCode().getCaretPosition()).contains(q)) {
            var selectionStart = ((JCodePane) codeTabs.getSelectedComponent()).getText()
                    .substring(((JCodePane) codeTabs.getSelectedComponent()).getCode().getCaretPosition())
                    .indexOf(q) + ((JCodePane) codeTabs.getSelectedComponent()).getCode().getCaretPosition();
            ((JCodePane) codeTabs.getSelectedComponent()).getCode()
                    .select(
                            selectionStart
                            , q.length() + selectionStart
                    );
            return new int[]{selectionStart,q.length() + selectionStart};
        }
        return new int[]{-1,-1};
    }

    /**
     * Find previous phrase
     * @return array contains of start and end of found phrase or {-1, -1} if it wasn't found
     */
    public int[] findPrev() {
        if(((JCodePane)codeTabs.getSelectedComponent()).getText()
                .substring(0,((JCodePane)codeTabs.getSelectedComponent()).getCode().getCaretPosition()).contains(q)) {
            var selectionStart = ((JCodePane) codeTabs.getSelectedComponent()).getText()
                    .substring(0, ((JCodePane) codeTabs.getSelectedComponent()).getCode().getCaretPosition())
                    .indexOf(q) + ((JCodePane) codeTabs.getSelectedComponent()).getCode().getCaretPosition() - q.length();
            ((JCodePane) codeTabs.getSelectedComponent()).getCode().setCaretPosition(q.length() + selectionStart);
            ((JCodePane) codeTabs.getSelectedComponent()).getCode().moveCaretPosition(selectionStart);
            return new int[]{selectionStart,q.length() + selectionStart};
        }
        return new int[]{-1,-1};
    }


    @Deprecated
    private void close(ActionEvent actionEvent) {
        windowClosing(null);
    }

    @Override
    public void windowOpened(WindowEvent e) {
        SyntaxHighlighter.setTheme(theme);
        setFont(font);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        try {
            Files.write(Path.of("system.properties"), Collections.singleton(
                    "project=" + project.getPath() + "\n" +
                    "compiler=" + elevenL.getPath() + "\n" +
                    "font=" + font + "\n" +
                    "theme=" + theme), StandardCharsets.UTF_8);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        System.exit(0);
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }

    public void setProject(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            root = new DefaultMutableTreeNode(dir.getPath());
            var selected = projectTree.getSelectionPath();
            projectTree.setModel(new DefaultTreeModel(root, false));
            listFiles(dir,root);
            if (selected != null)
                projectTree.expandPath(selected);
            projectTree.setExpandsSelectedPaths(true);
            projectTree.addSelectionPath(selected);
            projectTree.scrollPathToVisible(selected);
            projectTree.repaint();
            projectTree.updateUI();
            project = dir;
        }
        if(isVisible())
            setVisible(true); // Update the window
    }

    private void listFiles(File dir, DefaultMutableTreeNode parent){
        for (File file :
                dir.listFiles()) {
            parent.add(file(file));
        }
    }

    private DefaultMutableTreeNode file(File file) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(file.getName());
        if(file.isFile()) {
            projectTree.addMouseListener(new MouseAdapter(){
                @Override
                public void mouseClicked(MouseEvent e){
                    try {
                        if(projectTree.getLastSelectedPathComponent() == node) {
                            current = file;
                            codeTabs.addTab(file.getPath(), new JCodePane(file.getName().endsWith(".11l"),Files.readString(file.toPath())).setCaption(file.getPath()).isSelected(true));
                            codeTabs.setSelectedIndex(codeTabs.getTabCount()-1);
                        }
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            });
        } else {
            listFiles(file,node);
        }
        return node;
    }

    void createProject() {
        MainWindow mw = new MainWindow();
        JFileChooser j = new JFileChooser();
        j.setDialogTitle("Set directory of your project");
        j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int res = j.showOpenDialog(null);
        try {
            if (res == JFileChooser.APPROVE_OPTION) {
                mw.setProject(j.getSelectedFile());
                Files.createFile(Path.of(project.getPath() + System.getProperty("file.separator") + JOptionPane.showInputDialog("The First 11l Module")));
                mw.elevenL = elevenL;
                mw.setVisible(true);
                Aloe.mainWindow = mw;
                this.dispose();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void setFont(String font) {
        MainWindow.font = font;
        console.setFont(new Font(font,Font.PLAIN,15));
        input.setFont(console.getFont());
        CONSOLE_LABEL.setFont(console.getFont());
        for (Component component : codeTabs.getComponents()) {
            ((JCodePane)component).getCode().setFont(console.getFont());
            ((JCodePane)component).getLines().setFont(console.getFont());
        }
    }

    public void setJCodePanesColor(Color fore, Color color) {
        MainWindow.fore = fore;
        this.back = color;
        for (Component component : codeTabs.getComponents()) {
            ((JCodePane)component).getCode().setForeground(fore);
            ((JCodePane)component).getCode().setCaretColor(fore);
            ((JCodePane)component).setBg(color);
            ((JCodePane)component).highlight();
        }
    }
}