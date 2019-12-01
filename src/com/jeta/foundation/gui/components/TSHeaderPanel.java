/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

/**
 * This is a JPanel that contains a component with a table-like heading.
 * 
 * [heading] [component]
 * 
 * @author Jeff Tassin
 */
public class TSHeaderPanel extends JPanel {
	/** the heading */
	private JLabel m_heading;

	/** the component */
	private JComponent m_comp;

	/**
	 * ctor
	 */
	public TSHeaderPanel(JComponent comp) {
		initialize(comp);
	}

	/**
	 * @return the underlying component
	 */
	public JComponent getComponent() {
		return m_comp;
	}

	/**
	 * @return the font for the component in this panel.
	 */
	public Font getFont() {
		if (m_comp == null)
			return super.getFont();
		else
			return m_comp.getFont();
	}

	/**
	 * @return the fontmetrics for the component in this panel.
	 */
	public FontMetrics getFontMetrics(Font f) {
		if (m_comp == null)
			return super.getFontMetrics(f);
		else
			return m_comp.getFontMetrics(f);
	}

	/**
	 * Initializes the panel
	 */
	void initialize(JComponent comp) {
		m_comp = comp;

		setLayout(new BorderLayout(1, 1));

		// this is the list heading
		m_heading = new JLabel();
		m_heading.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		m_heading.setFont(m_comp.getFont());
		m_heading.setForeground(Color.black);
		m_heading.setHorizontalAlignment(JLabel.CENTER);

		add(m_heading, BorderLayout.NORTH);
		add(m_comp, BorderLayout.CENTER);
	}

	/**
	 * Sets the heading for the list box
	 * 
	 * @param txt
	 *            the heading text to set
	 */
	public void setHeadingText(String txt) {
		m_heading.setText(txt);
	}

}
