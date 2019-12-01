package com.jeta.foundation.gui.editor.options;

import java.awt.Dimension;
import java.awt.event.KeyEvent;

import java.util.Vector;

import javax.swing.KeyStroke;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * Panel that allows the user to type key strokes
 * 
 */
public class KeySequenceInputPanel extends TSPanel {
	public static String PROP_KEYSEQUENCE = "keySequence";

	private Vector m_strokes = new Vector();
	private StringBuffer m_text = new StringBuffer();

	private JLabel keySequenceLabel;
	private JTextArea collisionLabel;
	private JTextField keySequenceInputField;

	/** Creates new form KeySequenceInputPanel with empty sequence */
	public KeySequenceInputPanel() {
		initComponents();
	}

	/**
	 * Clears actual sequence of KeyStrokes
	 */
	public void clear() {
		m_strokes.clear();
		m_text.setLength(0);
		keySequenceInputField.setText(m_text.toString());
		firePropertyChange(PROP_KEYSEQUENCE, null, null);
	}

	/*
	 * Sets the text of JLabel locaten on the bottom of this panel
	 */
	public void setInfoText(String s) {
		collisionLabel.setText(s + ' '); // NOI18N
	}

	/**
	 * Returns sequence of completed KeyStrokes as KeyStroke[]
	 */
	public KeyStroke[] getKeySequence() {
		return (KeyStroke[]) m_strokes.toArray(new KeyStroke[0]);
	}

	/**
	 * Makes it trying to be bigger
	 */
	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(6, 5);
	}

	/**
	 * We're redirecting our focus to proper component.
	 */
	public void requestFocus() {
		keySequenceInputField.requestFocus();
	}

	/**
	 * Visual part and event handling:
	 */
	private void initComponents() {
		// GEN-BEGIN:initComponents
		java.awt.GridBagConstraints gridBagConstraints;

		keySequenceLabel = new javax.swing.JLabel();
		keySequenceInputField = new javax.swing.JTextField();
		collisionLabel = new javax.swing.JTextArea();

		setLayout(new java.awt.GridBagLayout());

		setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 11, 11)));
		keySequenceLabel.setText("Key_Sequence");
		keySequenceLabel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 8)));
		keySequenceLabel.setLabelFor(keySequenceInputField);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
		add(keySequenceLabel, gridBagConstraints);

		keySequenceInputField.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyTyped(java.awt.event.KeyEvent evt) {
				keySequenceInputFieldKeyTyped(evt);
			}

			public void keyPressed(java.awt.event.KeyEvent evt) {
				keySequenceInputFieldKeyPressed(evt);
			}

			public void keyReleased(java.awt.event.KeyEvent evt) {
				keySequenceInputFieldKeyReleased(evt);
			}
		});

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 1.0;
		add(keySequenceInputField, gridBagConstraints);

		collisionLabel.setLineWrap(true);
		collisionLabel.setEditable(false);
		collisionLabel.setRows(2);
		collisionLabel.setForeground(java.awt.Color.red);
		collisionLabel.setBackground(getBackground());
		collisionLabel.setDisabledTextColor(java.awt.Color.red);
		collisionLabel.setEnabled(false);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
		add(collisionLabel, gridBagConstraints);
	}

	private void keySequenceInputFieldKeyTyped(java.awt.event.KeyEvent evt) {
		evt.consume();
	}

	private void keySequenceInputFieldKeyReleased(java.awt.event.KeyEvent evt) {
		evt.consume();
		keySequenceInputField.setText(m_text.toString());
	}

	private void keySequenceInputFieldKeyPressed(java.awt.event.KeyEvent evt) {
		evt.consume();
		String modif = KeyEvent.getKeyModifiersText(evt.getModifiers());
		if (isModifier(evt.getKeyCode())) {
			keySequenceInputField.setText(m_text.toString() + modif + '+'); // NOI18N
		} else {
			KeyStroke stroke = KeyStroke.getKeyStrokeForEvent(evt);
			m_strokes.add(stroke);
			m_text.append(org.netbeans.editor.Utilities.keyStrokeToString(stroke));
			m_text.append(' ');
			keySequenceInputField.setText(m_text.toString());
			firePropertyChange(PROP_KEYSEQUENCE, null, null);
		}
	}

	private boolean isModifier(int keyCode) {
		return (keyCode == KeyEvent.VK_ALT) || (keyCode == KeyEvent.VK_ALT_GRAPH) || (keyCode == KeyEvent.VK_CONTROL)
				|| (keyCode == KeyEvent.VK_SHIFT) || (keyCode == KeyEvent.VK_META);
	}

	/**
	 * This method simply sets the text in the sequence input field. It does not
	 * parse the strokes here. This is mainly for initialization.
	 */
	public void setKeyStrokesText(String txt) {
		keySequenceInputField.setText(txt);
		keySequenceInputField.selectAll();
	}

}
