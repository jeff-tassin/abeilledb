/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

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
 * [TSTimeField][spin btn]
 * 
 * @author Jeff Tassin
 */
public class TSTimeSpinner extends JSpinner {
	TSTimeField m_timefield;

	public TSTimeSpinner() {
		initialize();
	}

	/**
	 * @return the selected hour Always in 24 hour format
	 */
	public int getHours() {
		return m_timefield.getHours();
	}

	/**
	 * @return the selected minute
	 */
	public int getMinutes() {
		return m_timefield.getMinutes();
	}

	/**
	 * @return the selected seconds
	 */
	public int getSeconds() {
		return m_timefield.getSeconds();
	}

	/**
	 * Gets the underlying time field for this panel
	 */
	public TSTimeField getTimeField() {
		return m_timefield;
	}

	void initialize() {
		m_timefield = new TSTimeField();
		TimeSpinnerModel model = new TimeSpinnerModel();
		setModel(model);
		setEditor(m_timefield);
		m_timefield.setBorder(null);
		m_timefield.setForeground(java.awt.Color.black);
	}

	/**
	 * @return true if the time field is currently null
	 */
	public boolean isNull() {
		return m_timefield.isNull();
	}

	/**
	 * Sets the date with the given calendar instance
	 */
	public void setCalendar(Calendar c) {
		m_timefield.setCalendar(c);
	}

	/**
	 * called by base class for spin down events
	 */
	public void spindown() {
		if (m_timefield.isNull()) {
			m_timefield.setNow();
		} else {
			m_timefield.requestFocus();
			InputMaskComponent mask = m_timefield.getSelectedInputField();
			mask.decrement();
		}

	}

	/**
	 * called by base class for spin up events
	 */
	public void spinup() {
		if (m_timefield.isNull()) {
			m_timefield.setNow();
		} else {
			m_timefield.requestFocus();
			InputMaskComponent mask = m_timefield.getSelectedInputField();
			mask.increment();
		}
	}

	/**
	 * Sets a calendar object set to the date represented by this field
	 */
	public Calendar toCalendar(Calendar c) {
		return TSTimeField.toCalendar(m_timefield, c);
	}

	public class TimeSpinnerModel implements SpinnerModel {

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
