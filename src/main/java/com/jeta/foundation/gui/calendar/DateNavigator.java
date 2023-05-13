/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.calendar;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.util.*;

import com.jeta.foundation.gui.utils.*;

/**
 * DateNavigator is a component of a CalendarPanel. It is composed of two 'spin'
 * buttons and a text field.
 * 
 * [spin left] [text field] [spin right]
 * 
 * The text field contains either a month or year display. The user clicks on
 * the spin buttons to change to the next year or month.
 * 
 * 
 * @author Jeff Tassin
 */
public class DateNavigator extends JPanel implements ActionListener {
	private JButton m_leftbtn; // left spin button
	private JButton m_rightbtn; // right spin button
	private DateNavigatorTextField m_textfield; // the text field that holds
												// date or month
	private static final int SPIN_WIDTH = 16; // the width of each spin button
	private LinkedList m_listeners = new LinkedList(); // listeners that want
														// date navigation
														// events

	public DateNavigator(boolean bType) {
		initialize(bType);
	}

	/**
	 * Adds the listener to the list of listeners that receive date navigation
	 * events (ActionEvent)
	 */
	public void addActionListener(ActionListener listener) {
		m_listeners.add(listener);
	}

	/**
	 * Called when user clicks left or right navigation buttons. All date change
	 * listeners (ActionListeners) are notified of the change
	 */
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == m_leftbtn) {
			m_textfield.decrement();
			ActionEvent e = new ActionEvent(this, 0, "datechange.decrement");
			notifyListeners(e);
		} else if (evt.getSource() == m_rightbtn) {
			m_textfield.increment();
			ActionEvent e = new ActionEvent(this, 0, "datechange.increment");
			notifyListeners(e);
		} else if (evt.getSource() == m_textfield) {
			ActionEvent e = new ActionEvent(this, 0, evt.getActionCommand());
			notifyListeners(e);
		}

	}

	/**
	 * @return the month or year value. If this navigator represents a month, we
	 *         return 0-11. If this navigator represents a year, we return 0 -
	 *         9999 (the actual year )
	 */
	public int getValue() {
		return m_textfield.getValue();
	}

	/**
	 * Instantiates and initalizes the child components
	 */
	private void initialize(boolean bType) {
		m_leftbtn = new DNButton(TSGuiToolbox.loadImage("spinleft12.gif"));
		m_leftbtn.addActionListener(this);
		m_rightbtn = new DNButton(TSGuiToolbox.loadImage("spinright12.gif"));
		m_rightbtn.addActionListener(this);
		if (bType) {
			m_textfield = new DateNavigatorTextField(Calendar.JANUARY, false);
		} else {
			m_textfield = new DateNavigatorTextField(2001, true);
		}
		setLayout(new DateNavigatorLayoutManager());
		add(m_leftbtn);
		add(m_rightbtn);
		add(m_textfield);

		m_textfield.addActionListener(this); // to get year change events when
												// year is changed with keyboard
	}

	/**
	 * @return the preferred size for this panel
	 */
	public Dimension getPreferredSize() {
		Dimension d = getTextFieldPreferredSize();
		// we add 2*d.height to accommodate the spin buttons
		d.setSize(2 * SPIN_WIDTH + d.width, d.height);
		return d;
	}

	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	/**
	 * The text field preferred size is used to govern the size of the overall
	 * panel.
	 * 
	 * @return the preferred dimensions for the text field
	 * 
	 */
	Dimension getTextFieldPreferredSize() {
		Dimension d = m_textfield.getPreferredSize();
		return d;
	}

	/**
	 * Notifies all action listeners that the date has changed
	 */
	private void notifyListeners(ActionEvent evt) {
		Iterator iter = m_listeners.iterator();
		while (iter.hasNext()) {
			ActionListener listener = (ActionListener) iter.next();
			listener.actionPerformed(evt);
		}
	}

	/**
	 * Sets the current month or year depending on the mode of the navigator. If
	 * the navigator is in month mode, value should be between 0-11. If the
	 * navigator is in year mode, the value is the year (0-9999)
	 */
	public void setValue(int value) {
		m_textfield.setValue(value);
	}

	/**
	 * This is the layout manager for the DateNavigator
	 */
	class DateNavigatorLayoutManager implements LayoutManager {
		public void addLayoutComponent(String name, Component comp) {

		}

		public void layoutContainer(Container parent) {
			// here's the layout [leftbtn][textfield][rightbtn]
			Dimension tfdim = getTextFieldPreferredSize();
			m_leftbtn.setLocation(0, 0);
			m_leftbtn.setSize(SPIN_WIDTH, tfdim.height);
			m_textfield.setSize(tfdim);
			m_textfield.setLocation(m_leftbtn.getWidth(), 0);
			m_rightbtn.setLocation(m_leftbtn.getWidth() + m_textfield.getWidth(), 0);
			m_rightbtn.setSize(SPIN_WIDTH, tfdim.height);

		}

		public Dimension minimumLayoutSize(Container parent) {
			return DateNavigator.this.getPreferredSize();
		}

		public Dimension preferredLayoutSize(Container parent) {
			return DateNavigator.this.getPreferredSize();
		}

		public void removeLayoutComponent(Component comp) {
		}

	}

	/**
	 * Override for left and right spin buttons. We don't allow them to get
	 * focus. I am doing this because it seems to be waste of keystrokes when
	 * you can spin just as easily with the keyboard
	 */
	class DNButton extends JButton {
		DNButton(Icon icon) {
			super(icon);
		}

		/**
		 * Can this component obtain focus
		 */
		public boolean isFocusTraversable() {
			return false;
		}
	}

}
