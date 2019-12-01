/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

import java.awt.Toolkit;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import com.jeta.foundation.utils.TSUtils;

/**
 * Document that only allows floating point values for a text field
 * 
 * @author Jeff Tassin
 */
public class FloatDocument extends PlainDocument {
	private boolean m_signed = true;

	public FloatDocument() {

	}

	public FloatDocument(boolean signed) {
		m_signed = signed;
	}

	public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
		if (str == null) {
			return;
		}

		str = TSUtils.fastTrim(str);
		StringBuffer sbuff = new StringBuffer();
		for (int index = 0; index < str.length(); index++) {
			char c = str.charAt(index);
			if (Character.isDigit(c) || c == '.' || c == ' ') {
				if (m_signed || c != '-') {
					sbuff.append(c);
					continue;
				}
			}

			// Toolkit toolkit = Toolkit.getDefaultToolkit();
			// toolkit.beep();
			return;
		}

		if (sbuff.length() > 0) {
			super.insertString(offs, sbuff.toString(), a);
		}

	}
}
