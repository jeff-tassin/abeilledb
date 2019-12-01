/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

/**
 * This panel contains a text field type widget and a spin button. It manages
 * the layout for both and responds to events from the spin button. The text
 * field is set to its preferred size during layouts.
 * 
 * [Component][spin btn]
 * 
 * @author Jeff Tassin
 */
public class TSSpinPanel extends JPanel implements ActionListener {
	JComponent m_field;
	JSpinButton m_spin;
	private static int HORIZONTAL_STRUT = 2;

	public TSSpinPanel() {

	}

	/**
	 * Action listener for spin button
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == m_spin) {
			if (e.getID() == JSpinButton.SPIN_UP_EVENT) {
				spinup();
				m_field.repaint();
			} else if (e.getID() == JSpinButton.SPIN_DOWN_EVENT) {
				spindown();
				m_field.repaint();
			}
		}
	}

	public Dimension getPreferredSize() {
		Dimension d = m_field.getPreferredSize();
		d.width = d.width + m_spin.getPreferredSize().width + HORIZONTAL_STRUT;
		return d;
	}

	/**
	 * Override to get spin down events
	 */
	public void spindown() {

	}

	/**
	 * Override to get spin up events
	 */
	public void spinup() {

	}

	/**
	 * Gets the underlying field for this panel
	 */
	public JComponent getField() {
		return m_field;
	}

	/**
	 * Derived classes should call initialize
	 */
	public void initialize(JComponent comp) {
		m_field = comp;
		m_spin = new JSpinButton();
		m_spin.addActionListener(this);
		setLayout(new SpinPanelLayoutManager());
		add(m_field);
		add(m_spin);
		m_field.requestFocus();
		m_field.requestDefaultFocus();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		// g.drawRect( 1, 1, getWidth() - 2, getHeight() - 2 );
	}

	class SpinPanelLayoutManager implements LayoutManager {

		public void addLayoutComponent(String name, Component comp) {
		}

		public void layoutContainer(Container parent) {
			m_field.setSize(m_field.getPreferredSize());
			m_spin.setLocation(m_field.getWidth() + HORIZONTAL_STRUT, 0);
			m_spin.setHeight(m_field.getHeight());
			m_field.setMinimumSize(m_field.getPreferredSize());
		}

		public Dimension minimumLayoutSize(Container parent) {
			return new Dimension(12, 12);
		}

		public Dimension preferredLayoutSize(Container parent) {
			return getPreferredSize();
		}

		public void removeLayoutComponent(Component comp) {
		}

	}

}
