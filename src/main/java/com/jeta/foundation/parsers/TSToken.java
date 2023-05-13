/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.parsers;

/**
 * This class represents a token that is found from a SQLParser or any other
 * parser for that matter.
 * 
 * @author Jeff Tassin
 */
public class TSToken {
	public static final int DELIMITED = 1;
	public static final int STARTQUOTE = 2;
	public static final int ENDQUOTE = 3;
	public static final int NUMERIC = 4;
	public static final int VALUE = 5;
	public static final int DELIMITEDVALUE = 6;

	private int m_type;
	private int m_startpos;
	private int m_length;

	public TSToken(int tokenType, int startpos, int length) {
		m_type = tokenType;
		m_startpos = startpos;
		m_length = length;
	}

	public int getType() {
		return m_type;
	}

	public int getStartPos() {
		return m_startpos;
	}

	public int length() {
		return m_length;
	}

}
