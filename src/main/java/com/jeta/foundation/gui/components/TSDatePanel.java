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
 * This panel contains a date widget and a spin button. It manages the layout
 * for both and responds to events from the spin button by updating the time
 * field.
 * 
 * [TSDateField][spin btn]
 * 
 * @author Jeff Tassin
 */
public class TSDatePanel extends TSSpinPanel {
	TSDateField m_dateField;

	public TSDatePanel() {
		initialize();
	}

	public TSDateField getDateField() {
		return m_dateField;
	}

	void initialize() {
		m_dateField = new TSDateField();
		super.initialize(m_dateField);
	}

	/**
	 * called by base class for spin down events
	 */
	public void spindown() {
		if (m_dateField.isNull())
			return;

		m_dateField.requestFocus();
		InputMaskComponent mask = m_dateField.getSelectedInputField();
		if (mask instanceof NumericMaskComponent) {
			NumericMaskComponent nmask = (NumericMaskComponent) mask;
			nmask.decrement();
		} else {
			System.out.println("******* Error:  no element selected in time field ******");
		}
	}

	/**
	 * called by base class for spin up events
	 */
	public void spinup() {
		if (m_dateField.isNull())
			return;

		m_dateField.requestFocus();
		InputMaskComponent mask = m_dateField.getSelectedInputField();
		if (mask instanceof NumericMaskComponent) {
			NumericMaskComponent nmask = (NumericMaskComponent) mask;
			nmask.increment();
		} else {
			System.out.println("******* Error:  no element selected in time field ******");
		}
	}

}
