/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.Toolkit;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import com.jeta.foundation.utils.TSUtils;

/**
 * This class implements a text field that only accepts integer values
 * 
 * @author Jeff Tassin
 */
public class IntegerTextField extends JTextField {
	/** @todo implement code that restricts entered text to digits */

	public IntegerTextField() {

	}

	public IntegerTextField(int width) {
		super(width);
	}

	public int getIntegerValue() {
		String txt = TSUtils.fastTrim(getText());
		if (txt.length() == 0)
			return 0;
		else
			return Integer.parseInt(txt);
	}

	protected Document createDefaultModel() {
		return new IntegerDocument();
	}

}
