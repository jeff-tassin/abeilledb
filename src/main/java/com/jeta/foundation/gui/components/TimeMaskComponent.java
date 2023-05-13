/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.text.*;
import com.jeta.foundation.gui.components.maskedtextfield.*;

/**
 * This is a mask component that makes up a JTimeField A time field (for US
 * locale) is composed of the following components:
 * [hours][:][minutes][:][seconds][space][AMPM] This mask is used for hours,
 * minutes, seconds, and AMPM in the JTimeField
 * 
 * @author Jeff Tassin
 */
public class TimeMaskComponent extends NumericMaskComponent {
	private TimeElement m_timeElement; // the time element (hours, min, seconds,
										// ampm ) that is
										// represented by this component

	public TimeMaskComponent(TimeElement tmElement) {
		super(2); // two digits for this mask
		m_timeElement = tmElement;
		setId(tmElement.toString());
	}

	/**
	 * @return the time element assoicated with this mask
	 */
	public TimeElement getTimeElement() {
		return m_timeElement;
	}

	/**
	 * @return the string representation of this component
	 */
	public String toString() {
		int val = (int) getValue();
		DecimalFormat format = new DecimalFormat("00");
		return format.format(val);
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
		if (inputPos == 1)
			return 9;
		else if (inputPos == 0) {
			if (m_timeElement == TimeElement.HOURS_12) {
				return 1;
			} else if (m_timeElement == TimeElement.HOURS_24) {
				return 2;
			} else
				return 5;
		} else {
			throw new IllegalArgumentException("inputpos = " + inputPos);
		}

	}

	/**
	 * @return the min value that can be displayed by this component
	 */
	public long getMinValue() {
		if (m_timeElement == TimeElement.HOURS_12) {
			return 1;
		} else if (m_timeElement == TimeElement.HOURS_24) {
			return 0;
		} else {
			return 0;
		}
	}

	/**
	 * @return the max value that can be displayed by this component
	 */
	public long getMaxValue() {
		if (m_timeElement == TimeElement.HOURS_12) {
			return 12;
		} else if (m_timeElement == TimeElement.HOURS_24) {
			return 23;
		} else {
			return 59;
		}
	}

	public boolean is24hour() {
		return false;
	}

}
