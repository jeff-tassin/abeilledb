/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import java.util.ArrayList;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.gui.components.maskedtextfield.InputMaskComponent;

/**
 * This is a 3 letter abbreviate month mask in the TSDateField
 * 
 * @author Jeff Tassin
 */
public class MonthMaskComponent extends InputMaskComponent {
	/** the 1-based month */
	private int m_month = 1;

	/** an array of month abbrevations */
	private static ArrayList m_months = new ArrayList();

	public static final String VALUE_CHANGE_EVENT = "monthvaluechangeevent";

	static {
		m_months.add(I18N.getLocalizedMessage("Jan"));
		m_months.add(I18N.getLocalizedMessage("Feb"));
		m_months.add(I18N.getLocalizedMessage("Mar"));
		m_months.add(I18N.getLocalizedMessage("Apr"));
		m_months.add(I18N.getLocalizedMessage("May"));
		m_months.add(I18N.getLocalizedMessage("Jun"));
		m_months.add(I18N.getLocalizedMessage("Jul"));
		m_months.add(I18N.getLocalizedMessage("Aug"));
		m_months.add(I18N.getLocalizedMessage("Sep"));
		m_months.add(I18N.getLocalizedMessage("Oct"));
		m_months.add(I18N.getLocalizedMessage("Nov"));
		m_months.add(I18N.getLocalizedMessage("Dec"));
	}

	/**
	 * ctor
	 */
	public MonthMaskComponent() {
		setId(TSDateField.MONTH_ABBREV_MASK);
	}

	/**
	 * Decrements the current month. If the month is January, then we wrap
	 * around to December
	 */
	public void decrement() {
		m_month--;
		if (m_month < 1)
			m_month = 12;

		setMonth(m_month);
	}

	/**
	 * @return the current month (1-based)
	 */
	public int getMonth() {
		return m_month;
	}

	/**
	 * Key event handler
	 */
	public boolean handleKeyEvent(KeyEvent evt) {
		if (evt.getKeyCode() == KeyEvent.VK_UP) {
			// decrement();
			return true;
		} else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
			// increment();
			return true;
		}
		return false;
	}

	/**
	 * Increments the current month count. If the current month is Dec, we wrap
	 * around to Jan.
	 */
	public void increment() {
		m_month++;
		if (m_month > 12)
			m_month = 1;

		setMonth(m_month);
	}

	/**
	 * Sets the current month.
	 * 
	 * @param month
	 *            the month to set (1-based)
	 */
	public void setMonth(int month) {
		assert (month >= 1 && month <= 12);

		m_month = month;
		// allow any listeners to respond to changes
		ActionEvent evt = new ActionEvent(this, 0, VALUE_CHANGE_EVENT);
		notifyListeners(evt);
	}

	public String getPreSelection() {
		return "";
	}

	public String getSelection() {
		return toString();
	}

	public String toString() {
		return (String) m_months.get(m_month - 1);
	}
}
