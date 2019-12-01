/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.calendar;

import java.awt.Color;
import java.awt.Font;

import java.util.Calendar;

import javax.swing.JComponent;
import javax.swing.JTextField;

/**
 * This is a widget class that displays a Gregorian calendar. It support
 * Locales. It works with the CalendarWidgetUI class.
 * 
 * @author Jeff Tassin
 */
public class CalendarWidget extends JComponent {
	private Calendar m_calendar; // the currently selected date

	private static final int[] m_daysInMonth = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
	private static final int[] m_daysInMonthLeap = { 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

	public static final String DATE_VALUE = "calendar.date.value.property";

	public CalendarWidget() {
		m_calendar = Calendar.getInstance();
		setFont(new Font("Arial", Font.BOLD, 12));
		setUI(new CalendarWidgetUI());
		setBackground(Color.white);

		JTextField tf = new JTextField();
		// we need to do this go get input events
		// A JComponent by itself will not respond to key events, so we need to
		// borrow some from JTextField
		setActionMap(tf.getActionMap());
		setInputMap(JComponent.WHEN_FOCUSED, tf.getInputMap());

		// System.out.println( "first day of week = " +
		// m_calendar.getFirstDayOfWeek() );
		// System.out.println( "locale = " + l.toString() + " sunday = " +
		// Calendar.SUNDAY + " january = " + Calendar.JANUARY );
	}

	/**
	 * Can this component obtain focus
	 */
	public boolean isFocusTraversable() {
		return true;
	}

	/**
	 * @return the underlying calendar object for this widget
	 */
	public Calendar getCalendar() {
		return m_calendar;
	}

	/**
	 * @return the number of days in the month of the given date (i.e. 28, 29,
	 *         30, or 31)
	 */
	public static int getDaysInMonth(Calendar cal) {
		if (isLeapYear(cal.get(Calendar.YEAR)))
			return m_daysInMonthLeap[cal.get(Calendar.MONTH)];
		else
			return m_daysInMonth[cal.get(Calendar.MONTH)];
	}

	/**
	 * Gets the day of the week that the first day of the given month falls on.
	 * E.g. May 1st, 2001 falls on a Tuesday
	 */
	public static int getFirstWeekDayOfMonth(Calendar cal) {
		int day = cal.get(Calendar.DAY_OF_MONTH);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		int firstday = cal.get(Calendar.DAY_OF_WEEK);
		cal.set(Calendar.DAY_OF_MONTH, day);
		return firstday;
	}

	/**
	 * @return true if the given year is a leap year or not
	 */
	public static boolean isLeapYear(int year) {
		return ((((year % 4) == 0) && ((year % 100) != 0)) || ((year % 400) == 0));
	}

	/**
	 * Sets the date
	 */
	public void setCalendar(Calendar c) {
		Calendar olddate = (Calendar) m_calendar.clone();
		m_calendar = c;
		repaint();
		// System.out.println( "olddate day = " + olddate.get(
		// Calendar.DAY_OF_MONTH ) + "  newday = " + m_calendar.get(
		// Calendar.DAY_OF_MONTH );
		firePropertyChange(DATE_VALUE, olddate, c);
	}

}
