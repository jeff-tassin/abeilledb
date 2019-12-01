/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.quickedit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * This method shows a text editor in panel with a button. The button invokes a
 * full text editor dialog in case the entered text is more extensive than a
 * single line.
 * 
 * @author Jeff Tassin
 */
public class QuickEditView extends TSPanel {
	/** the cell editor */
	private JTextComponent m_editor;

	/** button command ids */
	public static final String ID_EDITOR_BTN = "editor.btn";
	public static final String ID_CLOSE_BTN = "close.btn";

	/**
	 * ctor
	 */
	public QuickEditView(JTextComponent editor) {
		initialize(editor);
	}

	/**
	 * @return the preferred size for this panel
	 */
	public Dimension getPreferredSize() {
		Font f = m_editor.getFont();
		FontMetrics metrics = m_editor.getFontMetrics(f);
		Border border = m_editor.getBorder();
		if (border != null) {
			Insets insets = border.getBorderInsets(m_editor);
			return new Dimension(100, metrics.getHeight() + insets.top + insets.bottom);
		} else
			return new Dimension(100, metrics.getHeight());
	}

	/**
	 * @return the text in the editor
	 */
	public String getText() {
		return m_editor.getText();
	}

	/**
	 * creates the components on the panel
	 */
	private void initialize(JTextComponent editor) {
		m_editor = editor;

		setOpaque(false);

		setLayout(new BorderLayout());
		add(m_editor, BorderLayout.CENTER);

		JButton editorbtn = new JButton(TSGuiToolbox.loadImage("ellipsis16.gif"));
		editorbtn.setBorderPainted(true);
		editorbtn.setFocusPainted(false);

		Dimension d = new Dimension(32, 16);
		editorbtn.setSize(d);
		editorbtn.setMaximumSize(d);
		editorbtn.setPreferredSize(d);
		editorbtn.setName(ID_EDITOR_BTN);

		JButton btn = new JButton(TSGuiToolbox.loadImage("close16.gif"));
		btn.setBorderPainted(true);
		btn.setFocusPainted(false);
		d = new Dimension(16, 16);
		btn.setSize(d);
		btn.setMaximumSize(d);
		btn.setPreferredSize(d);
		btn.setName(ID_CLOSE_BTN);

		JPanel btnpanel = new JPanel(new BorderLayout());
		btnpanel.add(editorbtn, BorderLayout.CENTER);
		btnpanel.add(btn, BorderLayout.EAST);

		add(btnpanel, BorderLayout.EAST);

	}

	/**
	 * creates the components on the panel
	 */
	private void initialize2(JTextComponent editor) {
		m_editor = editor;

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.insets = new Insets(2, 2, 2, 2);
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		add(m_editor, c);

		c.gridx = 2;
		c.gridy = 0;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridwidth = 1;

		JButton editorbtn = new JButton(TSGuiToolbox.loadImage("ellipsis16.gif"));
		editorbtn.setBorderPainted(false);

		Dimension d = new Dimension(16, 16);
		editorbtn.setSize(d);
		editorbtn.setMaximumSize(d);
		editorbtn.setPreferredSize(d);
		editorbtn.setName(ID_EDITOR_BTN);
		add(editorbtn, c);

		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 1.0;
		c.weighty = 1.0;
		add(Box.createVerticalStrut(1));
	}

	/**
	 * Sets the text in the editor
	 */
	public void setText(String txt) {
		m_editor.setText(txt);
	}
}
