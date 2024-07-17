package io.github.fawgio.aloe;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.io.File;

public class JFileTree extends JTree {
    Icon folderIcon = new ImageIcon("folder.png");
    Icon folderOpenIcon = new ImageIcon("folderOpen.png");
    Icon fileIcon = new ImageIcon("file.png");
    Icon file11lIcon = new ImageIcon("file11l.png");
    Icon fileCppIcon = new ImageIcon("filecpp.png");
    Icon folderOpenEmptyIcon = new ImageIcon("folderOpenEmpty.png");

    public JFileTree() {
        addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                    getLastSelectedPathComponent();
            if (node == null) return;
            File nodeInfo = ((FileNode) node.getUserObject()).getFile();
            Aloe.mainWindow.showFile(nodeInfo);
        });

        //Icons

        //DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) getCellRenderer();

        //renderer.setClosedIcon(folderIcon);
        //renderer.setLeafIcon(fileIcon);
        setCellRenderer(new Renderer());
    }

    private class Renderer extends DefaultTreeCellRenderer{

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component comp = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf,
                    row, hasFocus);
            TreeNode current = (TreeNode)value;

            if (leaf) {
                if (((FileNode)((DefaultMutableTreeNode)value).getUserObject()).file.getName().endsWith(".11l")){
                    setIcon(file11lIcon);
                } else if (((FileNode)((DefaultMutableTreeNode)value).getUserObject()).file.getName().endsWith(".cpp")){
                    setIcon(fileCppIcon);
                } else {
                    if (((FileNode)((DefaultMutableTreeNode)value).getUserObject()).file.isDirectory()){
                        if (selected)
                            setIcon(folderOpenEmptyIcon);
                        else
                            setIcon(folderIcon);
                    } else {
                        setIcon(fileIcon);
                    }
                }
            } else if (expanded) {
                setIcon(folderOpenIcon);
            } else {
                setIcon(folderIcon);
            }

            return comp;
        }
    }



    private static MutableTreeNode getFiles(File file, boolean isRoot) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new FileNode(file, isRoot));
        if (file.isDirectory())
            for (File child: file.listFiles())
                node.add(getFiles(child, false));
        return node;
    }

    public void setModelFromFile(File file){
        setModel(new DefaultTreeModel(getFiles(file, true)));
    }
}
