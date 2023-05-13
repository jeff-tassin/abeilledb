/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.text.*;
import com.jeta.foundation.gui.components.maskedtextfield.*;

/**
 * This is a mask component that makes up a TSDateField A date field (for US
 * locale) is composed of the following components: [month][-][day][-][year]
 * 
 * @author Jeff Tassin
 */
public class DateMaskComponent extends NumericMaskComponent {
	public static int DAY = 0;
	public static int MONTH = 1;
	public static int YEAR = 2;

	private int m_element; // day, month, or year

	public DateMaskComponent(int element) {
		super(2); // two digits for this mask

		m_element = element;

		if (m_element == DAY) {
			setDigits(2);
			setId(TSDateField.DAY_MASK);
		} else if (m_element == MONTH) {
			setDigits(2);
			setId(TSDateField.MONTH_NUMBER_MASK);
		} else if (m_element == YEAR) {
			setDigits(4);
			setId(TSDateField.YEAR_MASK);
		}
	}

	/**
	 * @return the minimum value for a digit at the given input position
	 */
	public int getMinDigit(int inputPos) {
		if (inputPos == 1)
			return 0;
		else if (inputPos == 0)
			return 0;
		else
			throw new IllegalArgumentException("inputpos = " + inputPos);
	}

	/**
	 * @return the maximum value for a digit at the given input position Note:
	 *         this is not always zero. For example, in a time mask, the max
	 *         digit can be 5 (as in 59 minutes)
	 */
	public int getMaxDigit(int inputPos) {
		if (isMonth()) {
			if (inputPos == 0)
				return 1;
			else
				return 9;
		} else if (isDay()) {
			if (inputPos == 0)
				return 3;
			else
				return 9;
		} else {
			return 9;
		}
	}

	/**
	 * @return the min value that can be displayed by this component
	 */
	public long getMinValue() {
		if (isMonth() || isDay()) {
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * @return the max value that can be displayed by this component
	 */
	public long getMaxValue() {
		if (isMonth()) {
			return 12;
		} else if (isDay()) {
			return 31;
		} else {
			return 9999;
		}
	}

	public boolean isMonth() {
		return m_element == MONTH;
	}

	public boolean isDay() {
		return m_element == DAY;
	}

}
