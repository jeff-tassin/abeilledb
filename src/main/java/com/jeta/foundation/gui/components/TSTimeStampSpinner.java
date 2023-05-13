/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

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
 * [TSTimeStampField][spin btn]
 * 
 * @author Jeff Tassin
 */
public class TSTimeStampSpinner extends JSpinner {

	TSTimeStampField m_timestampfield;

	/**
	 * ctor
	 */
	public TSTimeStampSpinner() {
		initialize();
	}

	/**
	 * @return the time stamp field associated with this panel
	 */
	public TSTimeStampField getTimeStampField() {
		return m_timestampfield;
	}

	void initialize() {
		m_timestampfield = new TSTimeStampField();

		TimeStampSpinnerModel model = new TimeStampSpinnerModel();
		setModel(model);
		setEditor(m_timestampfield);
		m_timestampfield.setBorder(null);
		m_timestampfield.setForeground(java.awt.Color.black);
	}

	/**
	 * called by base class for spin down events
	 */
	public void spindown() {
		if (m_timestampfield.isNull()) {
			m_timestampfield.setNow();
		} else {
			m_timestampfield.requestFocus();
			InputMaskComponent mask = m_timestampfield.getSelectedInputField();
			mask.decrement();
		}
	}

	/**
	 * called by base class for spin up events
	 */
	public void spinup() {
		if (m_timestampfield.isNull()) {
			m_timestampfield.setNow();
		} else {
			m_timestampfield.requestFocus();
			InputMaskComponent mask = m_timestampfield.getSelectedInputField();
			mask.increment();
		}
	}

	public class TimeStampSpinnerModel implements SpinnerModel {

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
