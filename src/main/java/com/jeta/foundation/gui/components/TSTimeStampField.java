/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.util.Calendar;

import com.jeta.foundation.gui.components.maskedtextfield.NumericMaskComponent;

/**
 * This class is a masked edit field that is used for handling JDBC timestamp
 * types.
 * 
 * @author Jeff Tassin
 */
public class TSTimeStampField extends TSTimeField {

	public TSTimeStampField() {

		// ask the plugin mgr to run the time field handler for the current
		// locale
		try {

			// PluginMgr.runPlugin( "jeta.TSDateField", "build", paramtypes,
			// params );
			// Object[] params = new Object[1];
			// params[0] = this;
			// Class[] paramtypes = new Class[1];
			// paramtypes[0] = TSMaskedTextField.class;
			// PluginMgr.runPlugin( "jeta.TSTimeField", "build", paramtypes,
			// params );

			// StaticStringMask margin = new StaticStringMask(" " );
			// addMask( margin );
			// StaticStringMask decimal = new StaticStringMask("." );
			// addMask( decimal );

			// NumericMaskComponent nanosec = new NumericMaskComponent( 6 );
			// nanosec.setId( "nanos" );
			// addMask( nanosec );
			// addMask( margin );
		} catch (Exception e) {
			// @todo load standard time field builder here
			e.printStackTrace();
		}

	}

	/**
	 * Gets a calendar object set to the date represented by this field
	 */
	public Calendar getCalendar() {
		Calendar c = super.getCalendar();
		c.set(Calendar.DAY_OF_MONTH, getDay());
		c.set(Calendar.MONTH, getMonth());
		c.set(Calendar.YEAR, getYear());
		return c;
	}

	/**
	 * @return the year that is currently displayed in the field
	 */
	public int getYear() {
		return TSDateField.getYear(this);
	}

	/**
	 * @return the month that is currently displayed in the field
	 */
	public int getMonth() {
		return TSDateField.getMonth(this);
	}

	/**
	 * @return the day that is currently displayed in the field
	 */
	public int getDay() {
		return TSDateField.getDay(this);
	}

	/**
	 * @return the nano seconds displayed by this field
	 */
	public int getNanos() {
		// NumericMaskComponent mask = (NumericMaskComponent)getMask( "nanos" );
		// return (int)mask.getValue();
		return 0;
	}

	/**
	 * Override initialize so we can add our own mask
	 */
	protected void initialize() {
		TSDateField.buildDateField(this);
		buildTimeField(this);

		/*
		 * StaticStringMask margin = new StaticStringMask(" " ); addMask( margin
		 * ); StaticStringMask decimal = new StaticStringMask("." ); addMask(
		 * decimal );
		 * 
		 * NumericMaskComponent nanosec = new NumericMaskComponent( 6 );
		 * nanosec.setId( "nanos" ); addMask( nanosec );
		 */
		setNow();
	}

	/**
	 * Sets the date/time for this time field. You should always call this
	 * instead of the setYears, setHours, setMinutes, etc because this will
	 * properly update the AMPM mask.
	 */
	public void set(int year, int month, int day, int hours, int mins, int seconds, int nanos) {
		setNanos(nanos);
		setSeconds(seconds);
		setMinutes(mins);
		setHours(hours);
		setYear(year);
		setMonth(month);
		setDay(day);
	}

	/**
	 * Sets the day displayed in this month
	 */
	public void setDay(int day) {
		TSDateField.setDay(this, day);
	}

	/**
	 * Sets the month displayed in this field
	 */
	public void setMonth(int month) {
		TSDateField.setMonth(this, month);
	}

	/**
	 * Sets the year displayed in this field
	 */
	public void setYear(int year) {
		TSDateField.setYear(this, year);
	}

	/**
	 * Sets the nano seconds displayed by this field
	 */
	public void setNanos(int nanos) {
		// NumericMaskComponent mask = (NumericMaskComponent)getMask( "nanos" );
		// mask.setValue( nanos );
		// setNull( false );
	}

	/**
	 * Sets the date values in this field to the current date
	 */
	public void setNow() {
		Calendar c = Calendar.getInstance();
		setMonth(c.get(Calendar.MONTH));
		setDay(c.get(Calendar.DAY_OF_MONTH));
		setYear(c.get(Calendar.YEAR));
		setHours(c.get(Calendar.HOUR_OF_DAY));
		setMinutes(c.get(Calendar.MINUTE));
		setSeconds(c.get(Calendar.SECOND));
		setNanos(0);
	}

}
