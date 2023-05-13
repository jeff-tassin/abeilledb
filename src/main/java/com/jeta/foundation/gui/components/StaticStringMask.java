/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import com.jeta.foundation.gui.components.maskedtextfield.*;

/**
 * This is the simplest type of mask. It is simply a string that is constant.
 * This is useful for things like delimiters in the time field.
 * 
 * @author Jeff Tassin
 */
public class StaticStringMask extends MaskComponent {
	private String m_text;

	public StaticStringMask(String str) {
		m_text = str;
	}

	/**
	 * @return the string representation of this component
	 */
	public String toString() {
		return m_text;
	}

}
