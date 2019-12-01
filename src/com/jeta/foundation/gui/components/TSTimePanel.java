/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

import com.jeta.foundation.gui.components.maskedtextfield.*;

/**
 * This panel contains a time widget and a spin button. It manages the layout
 * for both and responds to events from the spin button by updating the time
 * field.
 * 
 * [TSTimeField][spin btn]
 * 
 * @author Jeff Tassin
 */
public class TSTimePanel extends TSSpinPanel {
	TSTimeField m_timeField;

	public TSTimePanel() {
		initialize();
	}

	/**
	 * @return the selected hour Always in 24 hour format
	 */
	public int getHours() {
		return m_timeField.getHours();
	}

	/**
	 * @return the selected minute
	 */
	public int getMinutes() {
		return m_timeField.getMinutes();
	}

	/**
	 * @return the selected seconds
	 */
	public int getSeconds() {
		return m_timeField.getSeconds();
	}

	/**
	 * Gets the underlying time field for this panel
	 */
	public TSTimeField getTimeField() {
		return m_timeField;
	}

	void initialize() {
		m_timeField = new TSTimeField();
		super.initialize(m_timeField);
	}

	/**
	 * called by base class for spin down events
	 */
	public void spindown() {
		if (m_timeField.isNull())
			return;

		m_timeField.requestFocus();
		InputMaskComponent mask = m_timeField.getSelectedInputField();
		if (mask instanceof TimeMaskComponent) {
			TimeMaskComponent tmask = (TimeMaskComponent) mask;
			tmask.decrement();
		} else if (mask instanceof AMPMMaskComponent) {
			AMPMMaskComponent ampmmask = (AMPMMaskComponent) mask;
			ampmmask.toggle();
		} else {
			System.out.println("******* Error:  no element selected in time field ******");
		}
	}

	/**
	 * called by base class for spin up events
	 */
	public void spinup() {
		if (m_timeField.isNull())
			return;

		m_timeField.requestFocus();
		InputMaskComponent mask = m_timeField.getSelectedInputField();
		if (mask instanceof TimeMaskComponent) {
			TimeMaskComponent tmask = (TimeMaskComponent) mask;
			tmask.increment();
		} else if (mask instanceof AMPMMaskComponent) {
			AMPMMaskComponent ampmmask = (AMPMMaskComponent) mask;
			ampmmask.toggle();
		} else {
			System.out.println("******* Error:  no element selected in time field ******");
		}
	}

}
