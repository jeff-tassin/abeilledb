package com.jeta.abeille.gui.sql;

import org.netbeans.editor.TokenID;

/**
 * This class represents a token that has been parsed in the SQL Editor
 * 
 * @author Jeff Tassin
 */
public class SQLToken {
	/** The token id */
	private TokenID m_tokenid;

	/** The actual token text as parsed from the editor */
	private String m_token;

	/** The position in the document where this token is located */
	private int m_docPos;

	/**
	 * ctor This constructor is for tokens whose text is obvious (e.g. comma,
	 * dot, line comment )
	 * 
	 * @param tokenID
	 *            the id of the token
	 */
	public SQLToken(TokenID tokenID, int docPos) {
		m_tokenid = tokenID;
		m_docPos = docPos;
	}

	/**
	 * ctor
	 * 
	 * @param tokenID
	 *            the id of the token
	 * @param token
	 *            the actual token text
	 */
	public SQLToken(TokenID tokenID, String token, int docPos) {
		m_tokenid = tokenID;
		m_token = token;
		m_docPos = docPos;
	}

	/**
	 * ctor for an empty token of zero length
	 */
	public SQLToken(int docPos) {
		m_tokenid = SQLTokenContext.WHITESPACE;
		m_token = "";
		m_docPos = docPos;
	}

	/**
	 * @return The position in the document where this token is located
	 */
	public int getDocumentPos() {
		return m_docPos;
	}

	/** @return the token */
	public String getToken() {
		return m_token;
	}

	/** @return the token id */
	public TokenID getTokenID() {
		return m_tokenid;
	}

}
