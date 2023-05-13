/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.parsers;

import java.io.*;
import java.util.*;

/**
 *
 */
public class TextTokenBuilder extends TokenBuilder {
	private int m_startpos;
	boolean bnumeric = false;

	TextTokenBuilder(char c, char[] data, int pos, int offset, int length) {
		super(c, data, pos, offset, length);
		bnumeric = Character.isDigit(c);
		m_startpos = pos;
	}

	public int processStream(char[] data, int pos, int offset, int length) {
		while (pos < (offset + length)) {
			char c = data[pos];
			if (Character.isWhitespace(c) || c == ';' || c == ',' || c == '\'' || c == '=') // this
																							// is
																							// a
																							// delimiter
			{
				if (bnumeric)
					addToken(new TSToken(TSToken.NUMERIC, m_startpos, pos - m_startpos));
				else
					addToken(new TSToken(TSToken.VALUE, m_startpos, pos - m_startpos));

				break;
			} else {
				if (bnumeric)
					bnumeric = Character.isDigit(c);
				// if this is the last character in the stream, add the token
				if ((pos + 1) == (offset + length)) {
					if (bnumeric)
						addToken(new TSToken(TSToken.NUMERIC, m_startpos, pos - m_startpos + 1));
					else
						addToken(new TSToken(TSToken.VALUE, m_startpos, pos - m_startpos + 1));
				}
			}
			pos++;
		}
		return pos;
	}

}
