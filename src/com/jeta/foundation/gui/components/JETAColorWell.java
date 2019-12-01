/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JComponent;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * This class is used to display a small rectangular button on a panel. The
 * button is used to select a color for those dialogs that support color
 * selection/preferences.
 * 
 * @author Jeff Tassin
 */
public class JETAColorWell extends JComponent {
	/**
	 * The inkwell component
	 */
	private JETAInkWellEx m_inkwell;

	/**
	 * A list of ActionListener objects that get notified when the color
	 * changes.
	 */
	private LinkedList m_listeners;

	/**
	 * ctor
	 */
	public JETAColorWell() {
		initialize(null);
	}

	/**
	 * ctor
	 */
	public JETAColorWell(Color color) {
		initialize(color);
	}

	/**
	 * Adds an listener that wants to be notified when the color is changed.
	 */
	public void addActionListener(ActionListener listener) {
		if (m_listeners == null)
			m_listeners = new LinkedList();
		m_listeners.add(listener);
	}

	/**
	 * @return the color for this ink well
	 */
	public Color getColor() {
		return m_inkwell.getColor();
	}

	/**
	 * Initializes the color well
	 */
	private void initialize(Color color) {
		m_inkwell = new JETAInkWellEx(color);
		setLayout(new BorderLayout());
		add(m_inkwell, BorderLayout.CENTER);
		m_inkwell.addPropertyChangeListener(new ColorChangedAction());
	}

	/**
	 * Notifies any listeners that the color has changed.
	 */
	protected void notifyListeners(ActionEvent evt) {
		// System.out.println( "JETAColorWell.notifyListeners: " + m_listeners
		// );
		if (m_listeners != null) {
			Iterator iter = m_listeners.iterator();
			while (iter.hasNext()) {
				ActionListener listener = (ActionListener) iter.next();
				listener.actionPerformed(evt);
			}
		}
	}

	/**
	 * Sets the color for this ink well
	 */
	public void setColor(Color color) {
		m_inkwell.setColor(color);
	}

	/**
	 * Handler for changing color
	 */
	public class ColorChangedAction implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent evt) {
			if ("color".equals(evt.getPropertyName())) {
				notifyListeners(new ActionEvent(JETAColorWell.this, ActionEvent.ACTION_PERFORMED, getName()));
			}
		}
	}

}
