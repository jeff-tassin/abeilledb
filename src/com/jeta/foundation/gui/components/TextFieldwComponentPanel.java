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
 * This panel displays a text field with an arbitrary component to the right of
 * the field. This type of layout occurs frequently so we have a special class
 * for it. [text field][comp]
 * 
 * 
 * @author Jeff Tassin
 */
public class TextFieldwComponentPanel extends TSPanel {
	/** the main text field for this panel */
	private JTextField m_textfield;

	/** the button to the right of the text field */
	private Component m_comp;

	/**
	 * ctor
	 */
	public TextFieldwComponentPanel(JTextField txtField, Component comp) {
		initialize(txtField, comp);
	}

	/**
	 * @return the component
	 */
	public Component getComponent() {
		return m_comp;
	}

	/**
	 * @return the text field
	 */
	public JTextField getTextField() {
		return m_textfield;
	}

	/**
	 * Initializes the panel
	 */
	private void initialize(JTextField txtField, Component btn) {
		m_textfield = txtField;
		m_comp = btn;
		add(m_textfield);
		add(m_comp);
		setLayout(new PanelLayout(m_textfield, m_comp));
	}

	/**
	 * We need this layout manager for the [text-field][jbutton] layout. The
	 * reason is because the BorderLayout cause the JTextField to increase its
	 * height in some situations. This results in a very flaky looking GUI.
	 */
	public static class PanelLayout implements LayoutManager {
		private JTextField m_txtfield;
		private Component m_comp;

		public PanelLayout(JTextField txtfield, Component comp) {
			m_txtfield = txtfield;
			m_comp = comp;
		}

		public void addLayoutComponent(String name, Component comp) {
		}

		public void layoutContainer(Container parent) {
			Dimension comp_d = m_comp.getPreferredSize();
			m_txtfield.setLocation(0, 0);

			Dimension txt_d = m_txtfield.getPreferredSize();
			txt_d.width = parent.getWidth() - comp_d.width;

			comp_d.height = txt_d.height;

			m_comp.setLocation(txt_d.width, 0);
			m_comp.setSize(comp_d);
			m_txtfield.setSize(txt_d);
		}

		public Dimension minimumLayoutSize(Container parent) {
			Dimension d = m_txtfield.getPreferredSize();
			Dimension d2 = m_comp.getPreferredSize();
			d.width = d2.width * 5;
			return d;
		}

		public Dimension preferredLayoutSize(Container parent) {
			Dimension d = m_txtfield.getPreferredSize();
			Dimension d2 = m_comp.getPreferredSize();
			d.width += d2.width;
			return d;
		}

		public void removeLayoutComponent(Component comp) {
		}
	}
}
