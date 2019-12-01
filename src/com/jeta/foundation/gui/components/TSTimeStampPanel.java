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
 * This panel contains a date widget, a time widget and a spin button. It
 * manages the layout for all and responds to events from the spin button by
 * updating the date and time fields - depending on which has focus.
 * 
 * [TSDateField][TSTimeField][spin btn]
 * 
 * @author Jeff Tassin
 */
public class TSTimeStampPanel extends TSSpinPanel {
	TSTimeStampField m_timestampfield;

	public TSTimeStampPanel() {
		initialize();
	}

	void initialize() {
		m_timestampfield = new TSTimeStampField();
		super.initialize(m_timestampfield);
	}

	/**
	 * @return the time stamp field associated with this panel
	 */
	public TSTimeStampField getTimeStampField() {
		return m_timestampfield;
	}

	/**
	 * called by base class for spin down events
	 */
	public void spindown() {
		if (m_timestampfield.isNull())
			return;

		m_timestampfield.requestFocus();
		InputMaskComponent mask = m_timestampfield.getSelectedInputField();
		mask.decrement();

		/*
		 * if ( mask instanceof TimeMaskComponent ) { TimeMaskComponent tmask =
		 * (TimeMaskComponent)mask; tmask.decrement(); } else if ( mask
		 * instanceof AMPMMaskComponent ) { AMPMMaskComponent ampmmask =
		 * (AMPMMaskComponent)mask; ampmmask.toggle(); } else if ( mask
		 * instanceof NumericMaskComponent ) { NumericMaskComponent nmask =
		 * (NumericMaskComponent)mask; nmask.decrement(); } else {
		 * System.out.println(
		 * "******* Error:  no element selected in time field ******" ); }
		 */
	}

	/**
	 * called by base class for spin up events
	 */
	public void spinup() {
		if (m_timestampfield.isNull())
			return;

		m_timestampfield.requestFocus();
		InputMaskComponent mask = m_timestampfield.getSelectedInputField();
		mask.increment();
		/*
		 * if ( mask instanceof TimeMaskComponent ) { TimeMaskComponent tmask =
		 * (TimeMaskComponent)mask; tmask.increment(); } else if ( mask
		 * instanceof AMPMMaskComponent ) { AMPMMaskComponent ampmmask =
		 * (AMPMMaskComponent)mask; ampmmask.toggle(); } else if ( mask
		 * instanceof NumericMaskComponent ) { NumericMaskComponent nmask =
		 * (NumericMaskComponent)mask; nmask.increment(); } else {
		 * System.out.println(
		 * "******* Error:  no element selected in time field ******" ); }
		 */
	}

}
