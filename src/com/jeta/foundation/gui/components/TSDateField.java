/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

import com.jeta.foundation.gui.components.maskedtextfield.NumericMaskComponent;

/**
 * This is a masked field that handles dates It supports locales as well by
 * running a plugin to build the locale specific date mask plugin ->
 * com.jeta.foundation.gui.components.TSDateField
 * 
 * Users can get date change events by adding an action listener and responding
 * to 'valuechanged' events
 * 
 * @author Jeff Tassin
 */
public class TSDateField extends TSTimeBase {
	public static final String DAY_MASK = "daymask";
	public static final String MONTH_NUMBER_MASK = "monthmask";
	public static final String MONTH_ABBREV_MASK = "monthabbrvmask";
	public static final String YEAR_MASK = "yearmask";

	public TSDateField() {
		TSDateField.buildDateField(this);
		setNow();
	}

	/**
	 * Builds a date field
	 */
	public static void buildDateField(TSTimeBase df) {
		String defaultmask = DateMask.getDefaultMask();
		DateMask dmask = DateMask.parse(defaultmask);
		if (dmask != null && dmask.isValid()) {
			StaticStringMask margin = new StaticStringMask(" ");
			df.addMask(margin);
			Collection elems = dmask.getElements();
			Iterator iter = elems.iterator();
			while (iter.hasNext()) {
				String el = (String) iter.next();
				if (DateMask.yyyy.equals(el))
					df.addMask(new DateMaskComponent(DateMaskComponent.YEAR));
				else if (DateMask.MM.equals(el))
					df.addMask(new DateMaskComponent(DateMaskComponent.MONTH));
				else if (DateMask.MMM.equals(el))
					df.addMask(new MonthMaskComponent());
				else if (DateMask.dd.equals(el))
					df.addMask(new DateMaskComponent(DateMaskComponent.DAY));
				else
					df.addMask(new StaticStringMask(el));
			}
			df.addMask(margin);
		} else {
			StaticStringMask datedelimiter = new StaticStringMask("-");
			StaticStringMask margin = new StaticStringMask(" ");
			df.addMask(margin);
			MonthMaskComponent month = new MonthMaskComponent();
			df.addMask(month);
			df.addMask(datedelimiter);
			NumericMaskComponent day = new DateMaskComponent(DateMaskComponent.DAY);
			df.addMask(day);
			df.addMask(datedelimiter);
			NumericMaskComponent year = new DateMaskComponent(DateMaskComponent.YEAR);
			df.addMask(year);
			df.addMask(margin);
		}
	}

	/**
	 * Gets a calendar object set to the date represented by this field
	 */
	public Calendar getCalendar() {
		Calendar c = Calendar.getInstance();
		c.set(getYear(), getMonth(), getDay());
		return c;
	}

	/**
	 * @return the date as represented by this field
	 */
	public Calendar getDate() {
		Calendar c = Calendar.getInstance();
		return toCalendar(this, c);
	}

	/**
	 * @return the year that is currently displayed in the field
	 */
	public int getYear() {
		return getYear(this);
	}

	/**
	 * @return the year that is currently displayed in the field
	 */
	public static int getYear(TSTimeBase df) {
		NumericMaskComponent mask = (NumericMaskComponent) df.getMask(TSDateField.YEAR_MASK);
		return (int) mask.getValue();
	}

	/**
	 * @return the month that is currently displayed in the field. The month is
	 *         returned according to the Java month definition (i.e. 0 for
	 *         January )
	 */
	public int getMonth() {
		return getMonth(this);
	}

	/**
	 * @return the month that is currently displayed in the field. The month is
	 *         returned according to the Java month definition (i.e. 0 for
	 *         January )
	 */
	public static int getMonth(TSTimeBase df) {
		NumericMaskComponent mask = (NumericMaskComponent) df.getMask(TSDateField.MONTH_NUMBER_MASK);
		if (mask == null) {
			MonthMaskComponent mmask = (MonthMaskComponent) df.getMask(TSDateField.MONTH_ABBREV_MASK);
			assert (mmask != null);
			return mmask.getMonth() - 1;
		} else {
			return (int) mask.getValue() - 1;
		}
	}

	/**
	 * @return the day that is currently displayed in the field
	 */
	public int getDay() {
		return getDay(this);
	}

	/**
	 * @return the day that is currently displayed in the field
	 */
	public static int getDay(TSTimeBase df) {
		NumericMaskComponent mask = (NumericMaskComponent) df.getMask(TSDateField.DAY_MASK);
		return (int) mask.getValue();
	}

	/**
	 * Sets the day displayed in this month
	 */
	public void setDay(int day) {
		setDay(this, day);
	}

	/**
	 * Sets the day displayed in this month
	 */
	public static void setDay(TSTimeBase df, int day) {
		NumericMaskComponent mask = (NumericMaskComponent) df.getMask(TSDateField.DAY_MASK);
		mask.setValue(day);
		df.setNull(false);
	}

	/**
	 * Sets the month displayed in this field
	 * 
	 * @param month
	 *            the month to set (0 is for January)
	 */
	public void setMonth(int month) {
		setMonth(this, month);
	}

	/**
	 * Sets the month displayed in this field
	 * 
	 * @param month
	 *            the month to set (0 is for January)
	 */
	public static void setMonth(TSTimeBase df, int month) {
		month++;
		NumericMaskComponent mask = (NumericMaskComponent) df.getMask(TSDateField.MONTH_NUMBER_MASK);
		if (mask == null) {
			MonthMaskComponent mmask = (MonthMaskComponent) df.getMask(TSDateField.MONTH_ABBREV_MASK);
			assert (mmask != null);
			mmask.setMonth(month);
		} else {
			mask.setValue(month);
		}
		df.setNull(false);
	}

	/**
	 * Sets the year displayed in this field
	 */
	public void setYear(int year) {
		setYear(this, year);
	}

	/**
	 * Sets the year displayed in this field
	 */
	public static void setYear(TSTimeBase df, int year) {
		NumericMaskComponent mask = (NumericMaskComponent) df.getMask(TSDateField.YEAR_MASK);
		mask.setValue(year);
		df.setNull(false);
	}

	/**
	 * Sets the date values in this field to the current date
	 */
	public void setNow() {
		Calendar c = Calendar.getInstance();
		setCalendar(c);
	}

	/**
	 * Sets the date with the given calendar instance
	 */
	public void setCalendar(Calendar c) {
		if (c == null) {
			setNull(true);
		} else {
			setMonth(c.get(Calendar.MONTH));
			setDay(c.get(Calendar.DAY_OF_MONTH));
			setYear(c.get(Calendar.YEAR));
		}
	}

	/**
	 * Sets a calendar object set to the date represented by this field
	 * 
	 * @param c
	 *            sets the given calendar to the date represented by this field.
	 * @return the same calendar instance
	 */
	public static Calendar toCalendar(TSDateField df, Calendar c) {
		c.set(Calendar.MONTH, df.getMonth());
		c.set(Calendar.DAY_OF_MONTH, df.getDay());
		c.set(Calendar.YEAR, df.getYear());
		return c;
	}

}
