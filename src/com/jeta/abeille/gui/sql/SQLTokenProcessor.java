package com.jeta.abeille.gui.sql;

import java.util.List;
import java.util.LinkedList;

import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenProcessor;
import org.netbeans.editor.Syntax;

/**
 * Token processor that parses the sql text
 * 
 * @author Jeff Tasssin
 */

public class SQLTokenProcessor implements TokenProcessor {

	/** Buffer that is scanned */
	private char[] m_buffer;

	/** Start position of the buffer in the document */
	private int m_bufferStartPos;

	/**
	 * Delta of the token processor buffer offsets against the offsets given in
	 * the source buffer.
	 */
	private int m_bufferOffsetDelta;

	/** TokenID of the last found token except Syntax.EOT and Syntax.EOL */
	private TokenID m_lastValidTokenID;

	/** The list of accumlated tokens */
	private LinkedList m_tokens = new LinkedList();

	/**
	 * ctor
	 */
	public SQLTokenProcessor() {

	}

	/**
	 * Adds a token to the list of tokens we are accumlating
	 */
	public void addToken(SQLToken token) {
		m_tokens.add(token);
	}

	/**
	 * Get the last token that was processed that wasn't either Syntax.EOT or
	 * Syntax.EOL.
	 */
	public final TokenID getLastValidTokenID() {
		return m_lastValidTokenID;
	}

	/**
	 * Get the last token text that was processed that wasn't either Syntax.EOT
	 * or Syntax.EOL.
	 */
	public final String getLastValidTokenText() {
		return null;
	}

	/**
	 * @return the list of tokens after we have done a parse operation
	 */
	public LinkedList getTokens() {
		return m_tokens;
	}

	/**
	 * Notify that the token was found.
	 * 
	 * @param tokenID
	 *            ID of the token found
	 * @param tokenContextPath
	 *            Context-path in which the token that was found.
	 * @param tokenOffset
	 *            Offset of the token in the buffer. The buffer is provided in
	 *            the <tt>nextBuffer()</tt> method.
	 * @param tokenLength
	 *            Length of the token found
	 * @return true if the next token should be searched or false if the scan
	 *         should be stopped completely.
	 */
	public boolean token(TokenID tokenID, TokenContextPath tokenContextPath, int tokenOffset, int tokenLen) {
		if (tokenID != null) {
			m_lastValidTokenID = tokenID;
		}

		tokenOffset += m_bufferOffsetDelta;
		int currenttokenpos = m_bufferStartPos + tokenOffset;

		SQLToken sqltoken = null;

		if (tokenID == SQLTokenContext.IDENTIFIER) {
			String token = new String(m_buffer, tokenOffset, tokenLen);
			sqltoken = new SQLToken(tokenID, token, currenttokenpos);
		} else if (tokenID == SQLTokenContext.SELECT) {
			sqltoken = new SQLToken(tokenID, currenttokenpos);
		} else if (tokenID == SQLTokenContext.AS) {
			sqltoken = new SQLToken(tokenID, currenttokenpos);
		} else if (tokenID == SQLTokenContext.FROM) {
			sqltoken = new SQLToken(tokenID, currenttokenpos);
		} else {
			sqltoken = new SQLToken(tokenID, m_bufferStartPos + tokenOffset);
		}

		addToken(sqltoken);
		return true;
	}

	/**
	 * Notify that end of scanned buffer was found. The method decides whether
	 * to continue the scan or stop. The rest of characters that were not
	 * scanned, because the is not completed is also provided.
	 * 
	 * @param offset
	 *            offset of the rest of the characters
	 * @return 0 to stop token processing, &gt 0 process additional characters
	 *         in the document
	 */
	public int eot(int offset) {
		// System.out.println( "SQLTokenProcessor.eot  offset = " + offset );
		return 0;
	}

	/**
	 * Notify that the following buffer will be scanned. This method is called
	 * before the buffer is being scanned.
	 * 
	 * @param buffer
	 *            buffer that will be scanned. To get the text of the tokens the
	 *            buffer should be stored in some instance variable.
	 * @param offset
	 *            offset in the buffer with the first character to be scanned.
	 *            If doesn't reflect the possible preScan. If the preScan would
	 *            be non-zero then the first buffer offset that contains the
	 *            valid data is <tt>offset - preScan</tt>.
	 * @param len
	 *            count of the characters that will be scanned. It doesn't
	 *            reflect the ppossible reScan.
	 * @param startPos
	 *            starting position of the scanning in the document. It
	 *            logically corresponds to the <tt>offset</tt> because of the
	 *            same text data both in the buffer and in the document. It
	 *            again doesn't reflect the possible preScan and the
	 *            <tt>startPos - preScan</tt> gives the real start of the first
	 *            token. If it's necessary to know the position of each token,
	 *            it's a good idea to store the value <tt>startPos - offset</tt>
	 *            in an instance variable that could be called
	 *            <tt>bufferStartPos</tt>. The position of the token can be then
	 *            computed as <tt>bufferStartPos + tokenBufferOffset</tt>.
	 * @param preScan
	 *            preScan needed for the scanning.
	 * @param lastBuffer
	 *            whether this is the last buffer to scan in the document so
	 *            there are no more characters in the document after this
	 *            buffer.
	 * @*/
	public void nextBuffer(char[] buffer, int offset, int len, int startPos, int preScan, boolean lastBuffer) {
		assert (preScan == 0);

		// System.out.println( "SQLTokenProcessor.nextBuffer    buff = " + new
		// String(buffer) + "   buff.LENGTH = " + buffer.length + "   offset = "
		// + offset + "   len = " + len + " startPos = " + startPos +
		// "  preScan = " + preScan );
		m_buffer = new char[len + preScan];
		System.arraycopy(buffer, offset - preScan, m_buffer, 0, len + preScan);
		m_bufferOffsetDelta = preScan - offset;
		m_bufferStartPos = startPos - preScan;
	}

}
