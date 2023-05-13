/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.parsers;

public class Token {
	private final String m_element;

	Token(String element) {
		this.m_element = element;
	}

	public String toString() {
		return m_element;
	}

	public static final Token beginquotes = new Token("beginquotes");
	public static final Token endquotes = new Token("endquotes");
	public static final Token newline = new Token("newline");
	public static final Token EOF = new Token("EOF");
	public static final Token value = new Token("value");
	public static final Token comma = new Token("comma");

}
