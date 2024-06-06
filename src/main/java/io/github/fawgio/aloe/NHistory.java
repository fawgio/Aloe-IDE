package io.github.fawgio.aloe;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoableEdit;
import java.util.Stack;

/**
 * Simple implementation of {@link UndoableEditListener} which doesn't react when edit name is "style changed"
 * @see UndoableEditListener
 */
public class NHistory implements UndoableEditListener {
    private final Stack<UndoableEdit> changesDone = new Stack<>();
    private final Stack<UndoableEdit> changesUndone = new Stack<>();

    @Override
    public void undoableEditHappened(UndoableEditEvent e) {
        if(!e.getEdit().getPresentationName().equals("style change")) {
            changesUndone.clear();
            changesDone.add(e.getEdit());
        }
    }
    public void redo(){
        if(changesUndone.size()>0) {
            changesDone.push(changesUndone.peek());
            changesUndone.pop().redo();
        }
    }
    public void undo(){
        if(changesDone.size()>0) {
            changesUndone.push(changesDone.peek());
            changesDone.pop().undo();
        }
    }
}
