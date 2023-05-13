/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import com.jeta.foundation.gui.components.TSComboBox;

/**
 * Override combo box to provide some specialized handling for translating
 * delimiter names into actual strings: COMMA -> ,
 */
public class DelimComboBox extends TSComboBox {
	public static final String COMMA = "COMMA";
	public static final String TAB = "TAB";
	public static final String SPACE = "SPACE";

	public DelimComboBox() {
		addItem(DelimComboBox.COMMA);
		addItem(DelimComboBox.TAB);
		addItem(DelimComboBox.SPACE);
	}

	/**
	 * Override and translate string if necessary
	 */
	public String getText() {
		String result = super.getText();
		if (result.equals(COMMA))
			return ",";
		else if (result.equals(TAB))
			return "\t";
		else if (result.equals(SPACE))
			return " ";
		else
			return result;
	}

	public void setSelectedItem(Object item) {
		if (item instanceof String) {
			String txt = (String) item;
			if (txt.equals(","))
				item = COMMA;
			else if (txt.equals(" "))
				item = SPACE;
			else if (txt.equals("\t"))
				item = TAB;
		}
		super.setSelectedItem(item);
	}
}
