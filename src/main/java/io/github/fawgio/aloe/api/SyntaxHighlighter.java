package io.github.fawgio.aloe.api;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;

public interface SyntaxHighlighter { //constants
    MutableAttributeSet stringSet = new SimpleAttributeSet();
    MutableAttributeSet numberSet = new SimpleAttributeSet();
    MutableAttributeSet boolSet = new SimpleAttributeSet();
    MutableAttributeSet keywordSet = new SimpleAttributeSet();
    MutableAttributeSet otherSet = new SimpleAttributeSet();
    MutableAttributeSet nothingSet = new SimpleAttributeSet();
    MutableAttributeSet commentSet = new SimpleAttributeSet();
}
