/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTextField;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * This panel displays a text field with an icon button to the right of the
 * field. This type of layout occurs frequently so we have a special class for
 * it. [text field][button]
 * 
 * Note: Make sure you use a small icon (16x16) for the button.
 * 
 * @author Jeff Tassin
 */
public class TextFieldwButtonPanel extends TSPanel {
	/** the main text field for this panel */
	private JTextField m_textfield;

	/** the button to the right of the text field */
	private JButton m_button;

	/**
	 * ctor
	 */
	public TextFieldwButtonPanel(JTextField txtField, JButton btn) {
		initialize(txtField, btn);
	}

	/**
	 * ctor
	 */
	public TextFieldwButtonPanel(ImageIcon icon) {
		initialize(new JTextField(), new JButton(icon));
	}

	/**
	 * @return the button
	 */
	public JButton getButton() {
		return m_button;
	}

	/**
	 * @return the text field
	 */
	public JTextField getTextField() {
		return m_textfield;
	}

	public static Dimension getButtonDimension() {
		return new Dimension(32, 16);
	}

	/**
	 * Initializes the panel
	 */
	private void initialize(JTextField txtField, JButton btn) {
		m_textfield = txtField;
		m_button = btn;

		Dimension d = new Dimension(32, 16);
		btn.setSize(d);
		btn.setMaximumSize(d);
		btn.setPreferredSize(d);
		add(m_textfield);
		add(m_button);
		setLayout(new PanelLayout(m_textfield, m_button));
	}

	/**
	 * Enables the button and text field
	 */
	public void setEnabled(boolean benable) {
		super.setEnabled(benable);
		m_button.setEnabled(benable);
		m_textfield.setEnabled(benable);
	}

	/**
	 * We need this layout manager for the [text-field][jbutton] layout. The
	 * reason is because the BorderLayout cause the JTextField to increase its
	 * height in some situations. This results in a very flaky looking GUI.
	 */
	public static class PanelLayout implements LayoutManager {
		private JTextField m_txtfield;
		private JButton m_btn;

		public PanelLayout(JTextField txtfield, JButton btn) {
			m_txtfield = txtfield;
			m_btn = btn;
		}

		public void addLayoutComponent(String name, Component comp) {
		}

		public void layoutContainer(Container parent) {
			Dimension btn_d = m_btn.getPreferredSize();
			m_txtfield.setLocation(0, 0);

			Dimension txt_d = m_txtfield.getPreferredSize();
			txt_d.width = parent.getWidth() - btn_d.width;

			btn_d.height = txt_d.height;

			m_btn.setLocation(txt_d.width, 0);
			m_btn.setSize(btn_d);
			m_txtfield.setSize(txt_d);
		}

		public Dimension minimumLayoutSize(Container parent) {
			Dimension d = m_txtfield.getPreferredSize();
			Dimension d2 = m_btn.getPreferredSize();
			d.width = d2.width * 5;
			return d;
		}

		public Dimension preferredLayoutSize(Container parent) {
			Dimension d = m_txtfield.getPreferredSize();
			Dimension d2 = m_btn.getPreferredSize();
			d.width += d2.width;
			return d;
		}

		public void removeLayoutComponent(Component comp) {
		}
	}
}
