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
public class DelimitedTextTokenBuilder extends TokenBuilder {
	private int m_startpos;

	DelimitedTextTokenBuilder(char c, char[] data, int pos, int offset, int length) {
		super(c, data, pos, offset, length);
		addToken(new TSToken(TSToken.STARTQUOTE, pos, 1));
		m_startpos = pos + 1;
	}

	public int processStream(char[] data, int pos, int offset, int length) {
		while (pos < (offset + length)) {
			char c = data[pos];
			// System.out.println(
			// "DelimitedTextTokenBuilder.processStream c = " + c + "  pos = " +
			// pos + "  offset = " + offset + "  length = " + length );
			if (c == '\'') {
				if ((pos + 1) < (offset + length)) {
					char nextc = data[pos + 1];
					// System.out.println( "nextc = " + nextc );
					if (nextc == '\'') // then we have a valid quote character
										// inside the quote block
					{
						pos++;
					} else {
						// System.out.println(
						// "DelimitedTextTokenBuilder.processStream c = " + c +
						// "  pos = " + pos + " startpos = " + m_startpos );
						addToken(new TSToken(TSToken.DELIMITEDVALUE, m_startpos, pos - m_startpos));
						addToken(new TSToken(TSToken.ENDQUOTE, pos, 1));
						pos++;
						break;
					}
				} else {
					// ok, we are at the end of the stream, so we have a token
					// (assume an endquote)
					if (pos > m_startpos)
						addToken(new TSToken(TSToken.DELIMITEDVALUE, m_startpos, pos - m_startpos));

					addToken(new TSToken(TSToken.ENDQUOTE, pos, 1));
				}
			} else {
				// if this is the last character in the stream, add the token
				if ((pos + 1) == (offset + length))
					addToken(new TSToken(TSToken.DELIMITEDVALUE, m_startpos, pos - m_startpos + 1));
			}
			pos++;
		}
		return pos;
	}

}
