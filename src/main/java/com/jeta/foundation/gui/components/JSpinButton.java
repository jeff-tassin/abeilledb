/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * This class provides a basic spin button.
 * 
 * @author Jeff Tassin
 */
public class JSpinButton extends JPanel implements ActionListener {
	private JSpinComponent m_top;
	private JSpinComponent m_bottom;
	private int m_height;

	public static final int SPIN_UP_EVENT = 1;
	public static final int SPIN_DOWN_EVENT = 3;
	private LinkedList m_listeners = new LinkedList();

	public JSpinButton() {
		m_height = 12;
		m_top = new JSpinComponent(true);
		m_bottom = new JSpinComponent(false);
		setLayout(new SpinPanelLayoutManager());
		add(m_top);
		add(m_bottom);
		m_top.addActionListener(this);
		m_bottom.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == m_top) {
			ActionEvent evt = new ActionEvent(this, SPIN_UP_EVENT, "spinevent");
			sendEvent(evt);
		} else if (e.getSource() == m_bottom) {
			ActionEvent evt = new ActionEvent(this, SPIN_DOWN_EVENT, "spinevent");
			sendEvent(evt);
		}
	}

	/**
	 * Receive events from this component
	 */
	public void addActionListener(ActionListener listener) {
		m_listeners.add(listener);
	}

	public void setHeight(int height) {
		this.setSize(height * 3 / 5, height);
		m_height = height;
	}

	/**
	 * Receive events from this component
	 */
	public void removeActionListener(ActionListener listener) {
		Iterator iter = m_listeners.iterator();
		while (iter.hasNext()) {
			if (listener == iter.next()) {
				iter.remove();
			}
		}
	}

	private void sendEvent(ActionEvent evt) {
		Iterator iter = m_listeners.iterator();
		while (iter.hasNext()) {
			ActionListener listener = (ActionListener) iter.next();
			listener.actionPerformed(evt);
		}

	}

	class SpinPanelLayoutManager implements LayoutManager {

		public void addLayoutComponent(String name, Component comp) {
		}

		public void layoutContainer(Container parent) {
			int height = JSpinButton.this.getHeight() / 2;
			m_top.setSize(JSpinButton.this.getWidth(), height);
			if (height * 2 < JSpinButton.this.getHeight()) {
				m_bottom.setSize(JSpinButton.this.getWidth(), height);
			} else
				m_bottom.setSize(JSpinButton.this.getWidth(), height - 1);

			m_bottom.setLocation(0, JSpinButton.this.getHeight() / 2 + 1);

		}

		public Dimension minimumLayoutSize(Container parent) {
			return new Dimension(12, 12);
		}

		public Dimension preferredLayoutSize(Container parent) {
			return new Dimension(m_height * 3 / 5, m_height);
		}

		public void removeLayoutComponent(Component comp) {
		}

	}

}
