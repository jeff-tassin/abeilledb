/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

public class TimeElement {

	private final String m_element;

	TimeElement(String element) {
		this.m_element = element;
	}

	public String toString() {
		return m_element;
	}

	public static final TimeElement HOURS_24 = new TimeElement("hours24");
	public static final TimeElement HOURS_12 = new TimeElement("hours12");
	public static final TimeElement MINUTES = new TimeElement("minutes");
	public static final TimeElement SECONDS = new TimeElement("seconds");
	public static final TimeElement AMPM = new TimeElement("ampm");
}
