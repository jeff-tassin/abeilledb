/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import javax.swing.*;
import java.awt.*;
import javax.swing.text.*;
import java.awt.event.*;
import java.util.*;
import java.text.*;
import java.lang.*;

import com.jeta.foundation.gui.components.maskedtextfield.*;
import com.jeta.foundation.componentmgr.*;
import com.jeta.foundation.plugin.PluginMgr;

/**
 * This is a text field that provides formatted input and output for time.
 * 
 * Users can get date change events by adding an action listener and responding
 * to 'valuechanged' events
 * 
 * @author Jeff Tassin
 */
public class TSTimeField extends TSTimeBase {
	public TSTimeField() {
		// Calendar c = Calendar.getInstance();
		// DateFormat format = DateFormat.getTimeInstance( DateFormat.LONG );
		// DateFormat dformat = DateFormat.getDateInstance( DateFormat.LONG );
		// System.out.println( "formatted current date: " +
		// dformat.format(c.getTime()) );
		// System.out.println( "formatted current time: " +
		// format.format(c.getTime()) );
		// ask the plugin mgr to run the time field handler for the current
		// locale
		try {
			// Object[] params = new Object[1];
			// params[0] = this;
			// Class[] paramtypes = new Class[1];
			// paramtypes[0] = TSMaskedTextField.class;
			// PluginMgr.runPlugin( "jeta.TSTimeField", "build", paramtypes,
			// params );
		} catch (Exception e) {
			// @todo load standard time field builder here
			e.printStackTrace();
		}

		initialize();
	}

	public static void buildTimeField(TSTimeBase tf) {
		String defaultmask = TimeMask.getDefaultMask();
		TimeMask dmask = TimeMask.parse(defaultmask);
		if (dmask != null && dmask.isValid()) {
			StaticStringMask margin = new StaticStringMask(" ");
			tf.addMask(margin);
			Collection elems = dmask.getElements();
			Iterator iter = elems.iterator();
			while (iter.hasNext()) {
				String el = (String) iter.next();
				if (TimeMask.hh.equals(el))
					tf.addMask(new TimeMaskComponent(TimeElement.HOURS_12));
				else if (TimeMask.HH.equals(el))
					tf.addMask(new TimeMaskComponent(TimeElement.HOURS_24));
				else if (TimeMask.mm.equals(el))
					tf.addMask(new TimeMaskComponent(TimeElement.MINUTES));
				else if (TimeMask.ss.equals(el))
					tf.addMask(new TimeMaskComponent(TimeElement.SECONDS));
				else if (TimeMask.a.equals(el))
					tf.addMask(new AMPMMaskComponent(AMPMMaskComponent.AM));
				else
					tf.addMask(new StaticStringMask(el));
			}
			tf.addMask(margin);
		} else {
			// add the masks for this component
			// [sp][hour mask][:][min mask][:][secs mask][sp][AMPM mask][sp]

			// french dates are day.month.year
			// french times are hour:minute:sec
			StaticStringMask margin = new StaticStringMask(" ");
			tf.addMask(margin);
			TimeMaskComponent hour = new TimeMaskComponent(TimeElement.HOURS_12);
			tf.addMask(hour);
			// @todo change delimited to locale specified
			StaticStringMask tmdelimiter = new StaticStringMask(":");
			tf.addMask(tmdelimiter);
			TimeMaskComponent min = new TimeMaskComponent(TimeElement.MINUTES);
			tf.addMask(min);
			tf.addMask(tmdelimiter);
			TimeMaskComponent sec = new TimeMaskComponent(TimeElement.SECONDS);
			tf.addMask(sec);
			StaticStringMask sp = new StaticStringMask(" ");
			tf.addMask(sp);

			AMPMMaskComponent ampm = new AMPMMaskComponent(AMPMMaskComponent.AM);
			tf.addMask(ampm);

			tf.addMask(margin);
		}
	}

	/**
	 * Gets a calendar object set to the date represented by this field
	 */
	public Calendar getCalendar() {
		Calendar c = Calendar.getInstance();
		c.set(0, 0, 0);
		toCalendar(this, c);
		return c;
	}

	public int getHours() {
		return getHours(this);
	}

	/**
	 * @return the selected hours. Always in 24 hour format
	 */
	public static int getHours(TSTimeField tf) {
		int result = 0;
		if (tf.is12hour()) {
			NumericMaskComponent mask = (NumericMaskComponent) tf.getMask(TimeElement.HOURS_12.toString());
			int hours = (int) mask.getValue();

			int mins = tf.getMinutes();
			int secs = tf.getSeconds();
			if (tf.isPM()) {
				// if ( hours == 12 && mins == 0 && secs == 0 )
				// hours = 0;
				// else
				// {
				if (hours < 12)
					hours = hours + 12;
				// }
			} else {
				// if ( hours == 12 && (mins != 0 && secs != 0) )
				if (hours == 12) {
					hours = 0;
				}
			}
			result = hours;
		} else {
			NumericMaskComponent mask = (NumericMaskComponent) tf.getMask(TimeElement.HOURS_24.toString());
			result = (int) mask.getValue();
		}

		return result;

	}

	/**
	 * @return the minutes specified by this time field
	 */
	public int getMinutes() {
		return TSTimeField.getMinutes(this);
	}

	/**
	 * @return the minutes specified by this time field
	 */
	public static int getMinutes(TSTimeBase tf) {
		NumericMaskComponent mask = (NumericMaskComponent) tf.getMask(TimeElement.MINUTES.toString());
		return (int) mask.getValue();
	}

	/**
	 * @return the seconds specified by this time field
	 */
	public int getSeconds() {
		return TSTimeField.getSeconds(this);
	}

	/**
	 * @return the seconds specified by this time field
	 */
	public static int getSeconds(TSTimeBase tf) {
		NumericMaskComponent mask = (NumericMaskComponent) tf.getMask(TimeElement.SECONDS.toString());
		return (int) mask.getValue();
	}

	/**
	 * Initializes this field
	 */
	protected void initialize() {
		TSTimeField.buildTimeField(this);
		setNow();
	}

	/**
	 * @return true if we are in 12 hour mode and the time is in the AM
	 */
	public boolean isAM() {
		return TSTimeField.isAM(this);
	}

	/**
	 * @return true if we are in 12 hour mode and the time is in the AM
	 */
	public static boolean isAM(TSTimeBase tf) {
		boolean bresult = false;
		AMPMMaskComponent ampm = (AMPMMaskComponent) tf.getMask(AMPMMaskComponent.AM);
		if (ampm != null) {
			bresult = ampm.isAM();
		}
		return bresult;
	}

	/**
	 * @return true if we are in 12 hour mode and the time is in the PM
	 */
	public boolean isPM() {
		return !isAM();
	}

	/**
	 * @return true if this field is in 12 hour mode
	 */
	public boolean is12hour() {
		return TSTimeField.is12hour(this);
	}

	/**
	 * @return true if this field is in 12 hour mode
	 */
	public static boolean is12hour(TSTimeBase tf) {
		AMPMMaskComponent ampm = (AMPMMaskComponent) tf.getMask(AMPMMaskComponent.AM);
		return (ampm != null);
	}

	/**
	 * @return true if this field is in 24 hour mode
	 */
	public boolean is24hour() {
		return !is12hour();
	}

	/**
	 * Sets the time for this time field. You should always call this instead of
	 * the setHours, setMinutes, setSeconds because this will properly update
	 * the AMPM mask.
	 */
	public void set(int hours, int mins, int seconds) {
		setSeconds(seconds);
		setMinutes(mins);
		setHours(hours);
	}

	/**
	 * Sets the hours for this field.
	 * 
	 * @param hours
	 *            the hours to set. *Always* in 24 hour form
	 */
	void setHours(int hours) {
		TSTimeField.setHours(this, hours);
	}

	/**
	 * Sets the hours for this field.
	 * 
	 * @param hours
	 *            the hours to set. *Always* in 24 hour form
	 */
	static void setHours(TSTimeField tf, int hours) {
		if (tf.is12hour()) {
			AMPMMaskComponent ampm = (AMPMMaskComponent) tf.getMask(AMPMMaskComponent.AM);
			if (hours == 0) {
				hours = 12;
				// int mins = tf.getMinutes();
				// int secs = tf.getSeconds();
				// if ( mins == 0 && secs == 0 )
				// ampm.setPM();
				// else
				ampm.setAM();
			} else if (hours == 12) {
				// int mins = tf.getMinutes();
				// nint secs = tf.getSeconds();
				// if ( mins == 0 && secs == 0 )
				// nampm.setAM();
				// else
				// {
				ampm.setPM();
				// }
			} else if (hours > 12) {
				hours = hours - 12;
				ampm.setPM();
			} else if (hours < 12) {
				ampm.setAM();
			}
			NumericMaskComponent mask = (NumericMaskComponent) tf.getMask(TimeElement.HOURS_12.toString());
			mask.setValue(hours);
		} else {
			NumericMaskComponent mask = (NumericMaskComponent) tf.getMask(TimeElement.HOURS_24.toString());
			mask.setValue(hours);
		}

		tf.setNull(false);
	}

	/**
	 * Sets the minutes for this field.
	 * 
	 * @param minutes
	 *            the minutes to set.
	 */
	void setMinutes(int minutes) {
		TSTimeField.setMinutes(this, minutes);
	}

	/**
	 * Sets the minutes for this field.
	 * 
	 * @param minutes
	 *            the minutes to set.
	 */
	static void setMinutes(TSTimeField tf, int minutes) {
		NumericMaskComponent mask = (NumericMaskComponent) tf.getMask(TimeElement.MINUTES.toString());
		mask.setValue(minutes);
		tf.setNull(false);
	}

	/**
	 * Sets the seconds for this field.
	 * 
	 * @param seconds
	 *            the seconds to set.
	 */
	void setSeconds(int seconds) {
		TSTimeField.setSeconds(this, seconds);
	}

	/**
	 * Sets the seconds for this field.
	 * 
	 * @param seconds
	 *            the seconds to set.
	 */
	static void setSeconds(TSTimeField tf, int seconds) {
		NumericMaskComponent mask = (NumericMaskComponent) tf.getMask(TimeElement.SECONDS.toString());
		mask.setValue(seconds);
		tf.setNull(false);
	}

	private void updateAMPM() {
		if (is12hour()) {
			int mins = getMinutes();
			int secs = getSeconds();
			AMPMMaskComponent ampm = (AMPMMaskComponent) getMask(AMPMMaskComponent.AM);
			// first check for special case at 12:00AM (noon) and 12:00PM
			// (midnight)
			int hours = getHours();
			if (hours == 12 && mins == 0 && secs == 0)
				ampm.setPM();
			else if (hours == 0 && mins == 0 && secs == 0)
				ampm.setAM();
			else if (hours == 12)
				ampm.setPM();
			else if (hours == 0)
				ampm.setAM();
			else if (hours < 12)
				ampm.setAM();
			else
				ampm.setPM();
		}
	}

	/**
	 * Sets the time values in this field to the curren time
	 */
	public void setNow() {
		Calendar c = Calendar.getInstance();
		setCalendar(c);
	}

	/**
	 * Sets the time with the given calendar instance
	 */
	public void setCalendar(Calendar c) {
		if (c == null) {
			setNull(true);
		} else {
			setHours(c.get(Calendar.HOUR_OF_DAY));
			setMinutes(c.get(Calendar.MINUTE));
			setSeconds(c.get(Calendar.SECOND));
		}
	}

	/**
	 * Sets a calendar object set to the date represented by this field
	 * 
	 * @param c
	 *            sets the given calendar to the time represented by this field.
	 * @return the same calendar instance
	 */
	public static Calendar toCalendar(TSTimeField tf, Calendar c) {
		c.set(Calendar.HOUR_OF_DAY, tf.getHours());
		c.set(Calendar.MINUTE, tf.getMinutes());
		c.set(Calendar.SECOND, tf.getSeconds());
		return c;
	}

}
