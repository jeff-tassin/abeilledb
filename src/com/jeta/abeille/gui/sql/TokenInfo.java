package com.jeta.abeille.gui.sql;

import org.netbeans.editor.TokenID;

/**
 * Helper class used by the SQLDocumentParser
 * 
 * @author Jeff Tassin
 */
public class TokenInfo {
	private TokenID m_token;
	private String m_value;

	TokenInfo(TokenID tok, String value) {
		m_token = tok;
		m_value = value;
	}

	/**
	 * @return the token object
	 */
	public TokenID getToken() {
		return m_token;
	}

	/**
	 * @return the value represented by the token
	 */
	public String getValue() {
		return m_value;
	}

	/**
	 * Sets the value for this token info. Normally, this only used when the
	 * tokeninfo represents an input and we wish to provide a value for that
	 * input.
	 */
	public void setValue(String value) {
		m_value = value;
	}
}
