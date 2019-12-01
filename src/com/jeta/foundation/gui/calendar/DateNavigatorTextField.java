/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.calendar;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;

import com.jeta.foundation.i18n.*;

/**
 * This is the text field component of the DateNavigator. It allows the user to
 * enter the year with the keyboard or can act as a read-only field for the
 * month.
 * 
 * @author Jeff Tassin
 */
public class DateNavigatorTextField extends JComponent implements FocusListener, MouseListener {
	private int m_value; // if in year mode, then this is the actual 4 digit
							// year, otherwise
							// this is the number of the month
	private boolean m_bYear; // if this component supports editing years
	private static final Color selectedBackground = new Color(225, 225, 255);
	private static ArrayList m_months;
	private LinkedList m_listeners = new LinkedList(); // listeners interested
														// in year change
														// events, since
														// we can change the
														// year directly in the
														// editor

	static {
		m_months = new ArrayList();
		m_months.add(I18N.getResource("January"));
		m_months.add(I18N.getResource("February"));
		m_months.add(I18N.getResource("March"));
		m_months.add(I18N.getResource("April"));
		m_months.add(I18N.getResource("May"));
		m_months.add(I18N.getResource("June"));
		m_months.add(I18N.getResource("July"));
		m_months.add(I18N.getResource("August"));
		m_months.add(I18N.getResource("September"));
		m_months.add(I18N.getResource("October"));
		m_months.add(I18N.getResource("November"));
		m_months.add(I18N.getResource("December"));
	}

	/**
	 * if this is a year navigator, then val is the year if this is a month
	 * navigator, then val is the Calendar.JANUARY ... Calendar.DECEMBER value
	 */
	public DateNavigatorTextField(int val, boolean bYear) {

		m_bYear = bYear;
		m_value = val;

		// setFont( new Font("Arial", Font.NORMAL, 12 ) );
		setBorder(BorderFactory.createEtchedBorder());
		JTextField tf = new JTextField();
		// we need to do this go get input events
		// A JComponent by itself will not respond to key events, so we need to
		// borrow some from JTextField
		setActionMap(tf.getActionMap());
		setInputMap(JComponent.WHEN_FOCUSED, tf.getInputMap());

		addFocusListener(this);
		addMouseListener(this);
	}

	/**
	 * Adds a listener to receive year changed events
	 * 
	 * @param listener
	 *            the listener interested in the year change events
	 * 
	 */
	public void addActionListener(ActionListener listener) {
		m_listeners.add(listener);
	}

	public Dimension getMinimumSize() {
		return new Dimension(25, 25);
	}

	public Dimension getPreferredSize() {
		Font f = getFont();
		FontMetrics metrics = getFontMetrics(f);
		if (m_bYear)
			return new Dimension(metrics.stringWidth("##2000##"), metrics.getHeight() + 2 * metrics.getDescent());
		else
			return new Dimension(metrics.stringWidth("##SEPTEMBER##"), metrics.getHeight() + 2 * metrics.getDescent());

	}

	String getText() {
		if (m_bYear) {
			DecimalFormat format = new DecimalFormat("0000");
			return format.format(m_value);
		} else {
			int month = m_value;
			if (month < 0 || month > 11)
				month = 0;

			String smonth = (String) m_months.get(month);
			return smonth;
		}
	}

	/**
	 * @return the current value for this navigator if in months mode ( 0-11 )
	 *         if in year mode ( 0 - 9999 )
	 */
	public int getValue() {
		return m_value;
	}

	/**
	 * When we gain focus, do a repaint to add focus rectangle
	 */
	public void focusGained(FocusEvent e) {
		repaint();
	}

	/**
	 * When we lose focus, do a repaint to remove the focus rectangle
	 */
	public void focusLost(FocusEvent e) {
		repaint();
	}

	/**
	 * Decrements the current value. If the value is the month, then we move to
	 * the previous month. Likewise, if the value is the year, then we move to
	 * the previous year. We automatically wrap around when month is
	 * December/Jan and year is 0/9999
	 */
	public void decrement() {
		if (m_bYear) {
			m_value--;
			if (m_value < 0)
				m_value = 9999;
		} else {
			m_value--;
			if (m_value < 0)
				m_value = 11;
		}
		repaint();
	}

	/**
	 * Increments the current value. If the value is the month, then we move to
	 * the next month. Likewise, if the value is the year, then we move to the
	 * next year. We automatically wrap around when month is December/Jan and
	 * year is 0/9999
	 */
	public void increment() {
		if (m_bYear) {
			m_value++;
			if (m_value > 9999)
				m_value = 0;
		} else {
			m_value++;
			if (m_value > 11)
				m_value = 0;

		}
		repaint();
	}

	/**
	 * Draws the DateNavigatorTextField widget.
	 */
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		Font font = getFont();
		g2.setFont(font);
		FontMetrics metrics = g2.getFontMetrics();

		int height = getHeight();
		int width = getWidth();

		int stringwidth = metrics.stringWidth(getText());
		int marginx = (width - stringwidth) / 2;
		int line_height = metrics.getHeight();

		g2.setColor(Color.white);
		g2.fillRect(0, 0, width, height);
		int y = height - (height - line_height) / 2 - metrics.getDescent();

		if (hasFocus()) {
			int focusy1 = y - metrics.getAscent() - 3;
			int focusy2 = y + metrics.getDescent();
			g2.setColor(selectedBackground);
			g2.fillRect(marginx, focusy1, stringwidth, focusy2 - focusy1);
		}
		g2.setColor(Color.black);
		g2.setFont(font);
		g2.drawString(getText(), marginx, y);
	}

	public void mousePressed(MouseEvent e) {
		requestFocus();
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
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
	 * Override to handle key events. If we are in year mode (i.e. this
	 * component is showing the year ), we allow the user to modify the year
	 * with the keyboard.
	 * 
	 * @param e
	 *            the key event
	 */
	public void processKeyEvent(KeyEvent e) {
		if (!m_bYear) {
			super.processKeyEvent(e);
			return;
		}
		if (e.getID() == KeyEvent.KEY_PRESSED) {
			char keyChar = e.getKeyChar();
			if (keyChar >= '0' && keyChar <= '9') {
				// okay, if we have a valid number
				String year = String.valueOf(m_value) + String.valueOf(keyChar);
				if (year.length() > 4)
					year = year.substring(year.length() - 4, year.length());

				m_value = Integer.parseInt(year);

				ActionEvent evt = new ActionEvent(this, 0, "yearchange");
				notifyListeners(evt);

				repaint();
			}
		}
	}

	/**
	 * Sets the current month or year depending on the mode of the navigator. If
	 * the navigator is in month mode, value should be between 0-11. If the
	 * navigator is in year mode, the value is the year (0-9999)
	 */
	public void setValue(int value) {
		m_value = value;
	}

}
