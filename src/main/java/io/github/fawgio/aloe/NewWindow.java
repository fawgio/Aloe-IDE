package io.github.fawgio.aloe;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The window user firstly see
 */
public class NewWindow extends JFrame {
    NewWindow(Component c){
        super();
        add(new JLabel("<html><h1>Welcome to <font color=green>Aloe</font> IDE</h1></html>"));
        JLabel q =new JLabel("First of all let me know some information about you");
        add(q);
        JButton ok = new JButton("OK");
        ok.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                q.setText("Do you have python and  "+(System.getProperty("os.name").startsWith("Windows")?"Visual Studio":"gcc 9+")+" installed?");
                remove(ok);
                repaint();
                JEditorPane a;
                a = new JEditorPane();
                JButton let = new JButton("Get Started!");

                let.setVisible(false);
                let.addActionListener(new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            Files.createFile(Path.of("system.properties"));
                            Aloe.mainWindow.elevenL = new File(System.getProperty("user.dir")+System.getProperty("file.separator")+"11l"+System.getProperty("file.separator")+"11l"+(System.getProperty("os.name").startsWith("Windows")?"" +
                                    ".cmd":"" +
                                    ""));
                            Aloe.mainWindow.createProject();
                            dispose();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }
                });
                JToggleButton toggleButton = new JToggleButton("Of course");
                JEditorPane finalA = a;
                a.setContentType("text/html");
                toggleButton.addItemListener(e1 -> {
                    if(toggleButton.isSelected()){
                        toggleButton.setText("No");
                        finalA.setText("<font size=\"3\" face=\"Tahoma\">Very good! Let's create our first 11l project!</font>");
                        let.setVisible(true);
                    } else {
                        toggleButton.setText("Of course");
                        finalA.setText("<html><font size=\"3\" face=\"Tahoma\">Then install them from this links, please<br>"+
                                "<a href=\"https://www.python.org/downloads/\">python</a><br>"+
                                "<a href=\"https://visualstudio.microsoft.com/downloads/\"> Visual Studio(Windows)</a><br>" +
                                "Then click the button again</font></html>");
                        finalA.setEditable(false);
                        finalA.setOpaque(false);
                        finalA.addHyperlinkListener(e11 -> {
                            if(e11.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)){
                                try {
                                    Desktop.getDesktop().browse(e11.getURL().toURI());
                                } catch (IOException | URISyntaxException ioException) {
                                    ioException.printStackTrace();
                                }
                            }
                        });

                        let.setVisible(false);
                    }
                });
                add(toggleButton);
                add(a);
                add(let);
                setVisible(true);
            }
        });
        add(ok);
        setLayout(new FlowLayout());
        setResizable(false);
        setSize(new Dimension(500,200));
        setUndecorated(true);
        getContentPane().setBackground(Color.WHITE);
        setVisible(true);
        setLocationRelativeTo(c);
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(25,25,25,25));
    }
}
