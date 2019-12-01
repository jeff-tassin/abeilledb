/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.*;
import javax.swing.text.*;
import java.awt.event.*;

import com.jeta.foundation.gui.components.maskedtextfield.*;

/**
 * This is the AMPM portion of a TSTimeField
 * 
 * @author Jeff Tassin
 */
public class AMPMMaskComponent extends InputMaskComponent {
	public static final String AM = "AM";
	public static final String PM = "PM";
	String m_ampm; // the field that indicates the current time of day AM or PM

	public AMPMMaskComponent(String ampm) {
		if (AM.equals(ampm))
			m_ampm = AM;
		else
			m_ampm = PM;

	}

	public void decrement() {
		toggle();
	}

	public void increment() {
		toggle();
	}

	public boolean handleKeyEvent(KeyEvent evt) {
		if (evt.getKeyCode() == KeyEvent.VK_UP) {
			// toggle();
			return true;
		} else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
			// toggle();
			return true;
		}
		return false;
	}

	/**
	 * @return true if the mask is set to AM
	 */
	public boolean isAM() {
		return AM.equals(m_ampm);
	}

	void toggle() {
		if (AM.equals(m_ampm))
			m_ampm = PM;
		else
			m_ampm = AM;

		// allow any listeners to respond to changes
		ActionEvent evt = new ActionEvent(this, 0, VALUE_CHANGE_EVENT);
		notifyListeners(evt);
	}

	public String getPreSelection() {
		return "";
	}

	public String getSelection() {
		return m_ampm;
	}

	public void setAM() {
		m_ampm = AM;
	}

	public void setPM() {
		m_ampm = PM;
	}

	public String toString() {
		return m_ampm;
	}
}
