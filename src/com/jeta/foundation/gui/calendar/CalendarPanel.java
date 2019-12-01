/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.calendar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.Calendar;

import javax.swing.JPanel;

/**
 * CalendarPanel is the top level control to include on your dialogs It is
 * comprised of basically 3 different components
 * 
 * 
 * DateNavigator DateNavigator month selector year selector
 * -------------------------------------- | | | calendar widget | | |
 * --------------------------------------
 * 
 * This class manages the layout of the components.
 * 
 * @author Jeff Tassin
 */
public class CalendarPanel extends JPanel implements ActionListener {
	private CalendarWidget m_calendar;
	private DateNavigator m_monthNavigator;
	private DateNavigator m_yearNavigator;

	public static final String DATE_VALUE = "date.value.property";

	public CalendarPanel() {
		initialize();
	}

	/**
	 * Called when user clicks month or year navigator spin buttons We get the
	 * latest month or year depending on the source of the event and update the
	 * current view
	 */
	public void actionPerformed(ActionEvent evt) {
		Calendar olddate = (Calendar) getCalendar().clone();

		int year = m_yearNavigator.getValue();
		int month = m_monthNavigator.getValue();

		Calendar c = m_calendar.getCalendar();
		int currentday = c.get(Calendar.DAY_OF_MONTH);

		c.set(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month);

		int daysinmonth = CalendarWidget.getDaysInMonth(c);
		if (currentday > daysinmonth)
			c.set(Calendar.DAY_OF_MONTH, daysinmonth);
		else
			c.set(Calendar.DAY_OF_MONTH, currentday);

		firePropertyChange(DATE_VALUE, olddate, c);
		m_calendar.repaint();
	}

	/**
	 * @return the underlying calendar object
	 */
	public Calendar getCalendar() {
		return m_calendar.getCalendar();
	}

	/**
	 * @return the preferred size for this panel
	 */
	public Dimension getPreferredSize() {
		Dimension mdim = m_monthNavigator.getPreferredSize();
		Dimension caldim = m_calendar.getPreferredSize();
		return new Dimension(caldim.width, mdim.height + caldim.height);
	}

	void initialize() {
		m_calendar = new CalendarWidget();

		m_calendar.addPropertyChangeListener(CalendarWidget.DATE_VALUE, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				firePropertyChange(DATE_VALUE, evt.getOldValue(), evt.getNewValue());
			}
		});

		m_monthNavigator = new DateNavigator(true);
		m_yearNavigator = new DateNavigator(false);
		setLayout(new CalendarLayoutManager());
		add(m_monthNavigator);
		add(m_yearNavigator);
		add(m_calendar);

		m_monthNavigator.addActionListener(this);
		m_yearNavigator.addActionListener(this);

		m_monthNavigator.setValue(m_calendar.getCalendar().get(Calendar.MONTH));
		m_yearNavigator.setValue(m_calendar.getCalendar().get(Calendar.YEAR));
	}

	/**
	 * Can this component obtain focus
	 */
	public boolean isFocusTraversable() {
		return false;
	}

	public void setCalendar(Calendar c) {
		m_calendar.setCalendar(c);
		m_monthNavigator.setValue(c.get(Calendar.MONTH));
		m_yearNavigator.setValue(c.get(Calendar.YEAR));

		m_calendar.repaint();
	}

	/**
	 * The layout manager for CalendarPanel
	 */
	class CalendarLayoutManager implements LayoutManager {
		public void addLayoutComponent(String name, Component comp) {

		}

		public void layoutContainer(Container parent) {
			int xorg = 0;
			int yorg = 0;
			Dimension d = CalendarPanel.this.getPreferredSize();
			if (parent.getWidth() > d.width)
				xorg = (parent.getWidth() - d.width) / 2;

			if (parent.getHeight() > d.height)
				yorg = (parent.getHeight() - d.height) / 2;
			m_monthNavigator.setSize(m_monthNavigator.getPreferredSize());
			m_yearNavigator.setSize(m_yearNavigator.getPreferredSize());
			m_calendar.setSize(m_calendar.getPreferredSize());

			m_monthNavigator.setLocation(xorg, yorg);
			Dimension caldim = m_calendar.getPreferredSize();
			m_calendar.setLocation(xorg, yorg + m_monthNavigator.getHeight());
			m_yearNavigator.setLocation(xorg + m_calendar.getWidth() - m_yearNavigator.getWidth() - 10, yorg);
		}

		public Dimension minimumLayoutSize(Container parent) {
			return preferredLayoutSize(parent);
		}

		public Dimension preferredLayoutSize(Container parent) {
			return getPreferredSize();
		}

		public void removeLayoutComponent(Component comp) {
		}
	}

}
