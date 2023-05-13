/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jeta.foundation.gui.components.maskedtextfield.InputMaskComponent;
import com.jeta.foundation.gui.components.maskedtextfield.NumericMaskComponent;

/**
 * This panel contains a date widget and a spin button. It manages the layout
 * for both and responds to events from the spin button by updating the time
 * field.
 * 
 * [TSDateField][spin btn]
 * 
 * @author Jeff Tassin
 */
public class TSDateSpinner extends JSpinner {
	TSDateField m_dateField;

	public TSDateSpinner() {
		initialize();
	}

	public TSDateField getDateField() {
		return m_dateField;
	}

	void initialize() {
		m_dateField = new TSDateField();
		// super.initialize( m_dateField );
		DateSpinnerModel model = new DateSpinnerModel();
		setModel(model);
		setEditor(m_dateField);
		m_dateField.setBorder(null);
		m_dateField.setForeground(java.awt.Color.black);
	}

	/**
	 * @return true if the date field is currently null
	 */
	public boolean isNull() {
		return m_dateField.isNull();
	}

	/**
	 * Sets the date with the given calendar instance
	 */
	public void setCalendar(Calendar c) {
		m_dateField.setCalendar(c);
	}

	/**
	 * called by base class for spin down events
	 */
	public void spindown() {
		if (m_dateField.isNull()) {
			m_dateField.setNow();
		} else {
			m_dateField.requestFocus();
			InputMaskComponent mask = m_dateField.getSelectedInputField();
			mask.decrement();
		}
	}

	/**
	 * called by base class for spin up events
	 */
	public void spinup() {
		if (m_dateField.isNull()) {
			m_dateField.setNow();
		} else {
			m_dateField.requestFocus();
			InputMaskComponent mask = m_dateField.getSelectedInputField();
			mask.increment();
		}
	}

	/**
	 * Prints the date to the console
	 */
	public void print() {
		Calendar c = m_dateField.getCalendar();
		SimpleDateFormat format = new SimpleDateFormat();
		System.out.println("TSDateSpinner.print: " + format.format(c.getTime()));
	}

	/**
	 * Sets a calendar object set to the date represented by this field
	 */
	public Calendar toCalendar(Calendar c) {
		return TSDateField.toCalendar(m_dateField, c);
	}

	public class DateSpinnerModel implements SpinnerModel {

		public void addChangeListener(ChangeListener l) {
		}

		public Object getNextValue() {
			spinup();
			return getValue();
		}

		public Object getPreviousValue() {
			spindown();
			return getValue();
		}

		public Object getValue() {
			return new Integer(1);
		}

		public void removeChangeListener(ChangeListener l) {
		}

		public void setValue(Object value) {

		}
	}

}
