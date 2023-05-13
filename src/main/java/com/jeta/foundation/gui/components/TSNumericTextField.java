/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import javax.swing.*;
import javax.swing.text.*;

/**
 * This is a text field that accepts only numeric input.
 * 
 * @author Jeff Tassin
 */
public class TSNumericTextField extends JTextField {
	public TSNumericTextField() {

	}

	protected Document createDefaultModel() {
		return new NumericDocument();
	}

	static class NumericDocument extends PlainDocument {
		public void insertString(int offs, String str, javax.swing.text.AttributeSet a) throws BadLocationException {
			if (str == null)
				return;

			if (str.length() == 1 && Character.isDigit(str.charAt(0)))
				super.insertString(offs, str, a);
			else {
				StringBuffer buff = new StringBuffer(str.length());
				for (int index = 0; index < str.length(); index++) {
					char c = str.charAt(index);
					if (Character.isDigit(c))
						buff.append(c);
				}

				if (buff.length() > 0)
					super.insertString(offs, buff.toString(), a);
			}
		}
	}

}
