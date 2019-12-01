/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.parsers;

import java.io.*;
import java.util.*;

public class SQLParser {
	private char[] m_data = null; // this is the array that holds the data to
									// parse
	private int m_offset = 0; // this is the offset into the m_data array where
								// the data starts
	private int m_length = 0; // this is the length of the data to parse (in the
								// m_data array)
	private char m_inputTextQualifier;
	private LinkedList m_tokens = new LinkedList(); // the set of tokens
	private Iterator m_iterator;

	public SQLParser(int offset, int length, char[] data) {
		m_offset = offset;
		m_length = length;
		m_data = data;
		m_inputTextQualifier = '\'';
		// System.out.println( "SQLParser  offset = " + offset + "   length = "
		// + length );
		parse();
		m_iterator = m_tokens.iterator();
	}

	/**
	 * Adds a token to the list and returns a new string buffer object
	 */
	private void addToken(TSToken token) {
		m_tokens.add(token);
	}

	/**
	 * @return true if there are more tokens
	 */
	public boolean hasMoreTokens() {
		return m_iterator.hasNext();
	}

	/**
	 * @return the next token found by the parser
	 */
	public TSToken nextToken() {
		return (TSToken) m_iterator.next();
	}

	/**
	 * Parses the string found in m_data and places the tokens in the m_tokens
	 * linked list.
	 */
	private void parse() {
		int pos = m_offset;
		while (pos < (m_offset + m_length)) {
			char c = m_data[pos];
			TokenBuilder tokenbuilder = createBuilder(c, m_data, pos, m_offset, m_length);
			if (tokenbuilder != null) {
				pos++;
				pos = tokenbuilder.processStream(m_data, pos, m_offset, m_length);
				Iterator tokens = tokenbuilder.getTokens();
				while (tokens.hasNext()) {
					addToken((TSToken) tokens.next());
				}
			} else {
				pos++;
			}
		}
	}

	TokenBuilder createBuilder(char c, char[] data, int pos, int offset, int length) {
		// @todo we should probably check ranges here
		if (c == ' ' || c == ',' || c == ';' || c == '\n' || c == '\t') // this
																		// is
																		// either
																		// a
																		// delimiter
																		// or we
																		// are
																		// inside
																		// quotes
			return null;
		else if (c == '\'')
			return new DelimitedTextTokenBuilder(c, data, pos, offset, length);
		else if (Character.isLetterOrDigit(c))
			return new TextTokenBuilder(c, data, pos, offset, length);
		else {
			return null;
		}
	}

}
