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
public abstract class TokenBuilder {
	private LinkedList m_tokens = new LinkedList();

	TokenBuilder(char c, char[] data, int pos, int offset, int length) {
	}

	public void addToken(TSToken token) {
		m_tokens.add(token);
	}

	Iterator getTokens() {
		return m_tokens.iterator();
	}

	public abstract int processStream(char[] data, int pos, int offset, int length);

}
