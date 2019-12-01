package com.jeta.abeille.gui.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;

import javax.swing.text.BadLocationException;
import javax.swing.text.Segment;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.sql.input.SQLInput;
import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.abeille.query.Operator;

import com.jeta.foundation.utils.TSUtils;

import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenCategory;
import org.netbeans.editor.Utilities;

public class SQLDocumentParser {
	/** the document that contains the SQL text we will parse */
	private BaseDocument m_document;

	/** the actual SQL syntax parser */
	private Syntax m_syntax = new SQLSyntax();

	/** the segment that holds the text we are processing */
	private Segment m_segment;

	/** the line number of the last valid SQL statement */
	private int m_linenum;

	/** the end of the document has been reached */
	private boolean m_completed = false;

	/** the begin position to initially start the parsing */
	private int m_beginpos;
	/** the starting position in the document for the current sql statement */
	private int m_startpos;
	/** the ending position in the document for the current sql statement */
	private int m_endpos;
	/** the starting position in the document for the next sql statement */
	private int m_nextpos;

	/** the set of parsed tokens created from the last sql statement */
	private ArrayList m_tokens = new ArrayList();

	/**
	 * the set of SQLInput objects that were created from the last sql statement
	 */
	private ArrayList m_inputs = new ArrayList();

	/** the actual sql without any inputs */
	private String m_sql;

	/**
	 * The delimiter used to separate SQL commands. By default this is a
	 * semicolon.
	 */
	private char m_delimiter;

	/**
	 * ctor
	 * 
	 * @param doc
	 *            the document that contains the SQL text to run
	 * @param delimiter
	 *            the delimiter used to separate SQL commands. By default this
	 *            is a semicolon.
	 */
	public SQLDocumentParser(TSConnection tsconn, BaseDocument doc, char delimiter) throws BadLocationException {
		this(tsconn, 0, doc, delimiter);
	}

	/**
	 * ctor
	 * 
	 * @param beginpos
	 *            the beginning position in the document to start the parse
	 * @param doc
	 *            the document that contains the SQL text to run
	 * @param delimiter
	 *            the delimiter used to separate SQL commands. By default this
	 *            is a semicolon.
	 */
	public SQLDocumentParser(TSConnection tsconn, int beginpos, BaseDocument doc, char delimiter)
			throws BadLocationException {
		m_beginpos = beginpos;
		m_document = doc;
		m_delimiter = delimiter;
		m_segment = new Segment();
		m_document.getText(0, m_document.getLength(), m_segment);
		m_nextpos = m_segment.offset;
		// @todo this may cause problems for very long documents
		m_syntax.load(null, m_segment.array, m_segment.offset, m_segment.count, true, -1);
	}

	/**
	 * Loads the parser from a sql string
	 * 
	 * @param sql
	 *            the sql to execute
	 * @param delimiter
	 *            the delimiter used to separate SQL commands. By default this
	 *            is a semicolon.
	 */
	public SQLDocumentParser(TSConnection tsconn, String sql, char delimiter) {
		m_delimiter = delimiter;
		char[] buff = new char[sql.length()];
		sql.getChars(0, sql.length(), buff, 0);

		m_segment = new Segment(buff, 0, buff.length);
		m_syntax.load(null, m_segment.array, m_segment.offset, m_segment.count, true, -1);
	}

	/**
	 * @return the ending position in the SQL document for the current SQL
	 *         statement
	 */
	public int getEndPos() {
		return m_endpos;
	}

	/**
	 * @return the inputs found in the last sql statement
	 */
	public Collection getInputs() {
		return m_inputs;
	}

	/**
	 * @return the line number of the last valid SQL statement
	 */
	int getLineNumber() {
		return m_linenum;
	}

	/**
	 * @return the parsed sql statement
	 */
	public String getSQL() {
		StringBuffer buffer = new StringBuffer();
		Iterator iter = m_tokens.iterator();
		while (iter.hasNext()) {
			TokenInfo info = (TokenInfo) iter.next();
			buffer.append(info.getValue());
		}
		return buffer.toString();
	}

	/**
	 * @return the starting position in the SQL document for the current SQL
	 *         statement
	 */
	public int getStartPos() {
		return m_startpos;
	}

	/**
	 * @return the parsed tokens
	 */
	public Collection getTokens() {
		return m_tokens;
	}

	/**
	 * @return true if the parser found inputs in the last sql statement
	 */
	public boolean hasInputs() {
		return (m_inputs.size() > 0);
	}

	/**
	 * @return true if the parser has more tokens to parse
	 */
	public boolean hasMoreTokens() throws BadLocationException {
		if (m_completed)
			return false;
		else {
			nextSQL();
			if (m_completed && m_tokens.size() == 0)
				return false;
		}

		return true;
	}

	/**
	 * @return the next SQL statement found on the document. Multiple SQL
	 *         statements are delimited by a semicolon. Null is returned if
	 *         there are no more statements found
	 */
	public void nextSQL() throws BadLocationException {
		if (m_beginpos <= 0)
			_nextSQL();
		else {
			while (true) {
				_nextSQL();

				if (m_beginpos >= m_startpos && m_beginpos < m_endpos) {
					m_beginpos = 0;
					break;
				}

				if (m_beginpos < m_startpos || m_completed) {
					m_beginpos = 0;
					break;
				}
			}
		}
	}

	/**
	 * @return the next SQL statement found on the document. Multiple SQL
	 *         statements are delimited by a semicolon. Null is returned if
	 *         there are no more statements found
	 */
	private void _nextSQL() throws BadLocationException {
		// keep track of tokens that we have already parsed so we can quickly
		// lookup at the
		// end of the method and determine if the SQL is valid but does not have
		// semicolons
		HashMap tokenlookup = new HashMap();

		// an array of TokenInfo objects used for parsing ? for input support
		m_tokens.clear();
		m_inputs.clear();

		// the result that we will return

		m_sql = null;

		boolean bfirst = true;

		/** set the starting position */
		m_startpos = m_nextpos;

		TokenID token = m_syntax.nextToken();
		while (token != null) {
			int tokenoffset = m_syntax.getTokenOffset();
			int tokenlength = m_syntax.getTokenLength();
			String stoken = new String(m_segment.array, tokenoffset, tokenlength);

			/**
			 * check if this is the first token in the SQL statement. If so and
			 * the token is whitespace or a comment, then we ignore it
			 */
			if (bfirst) {
				if (token == SQLTokenContext.WHITESPACE || token == SQLTokenContext.LINE_COMMENT
						|| token == SQLTokenContext.BLOCK_COMMENT || TSUtils.getChar(stoken) == m_delimiter) {
					token = m_syntax.nextToken();
					continue;
				} else {
					m_startpos = tokenoffset;
					bfirst = false;
				}
			}

			tokenlookup.put(new Integer(token.getNumericID()), token);

			if (stoken != null && (TSUtils.getChar(stoken) == m_delimiter)) {
				/**
				 * some databases such as PointBase allow for dynamic INSERT
				 * statements where the values follow a ; delimiter. So, we
				 * check for this condition here
				 */
				m_sql = new String(m_segment.array, m_startpos, tokenoffset + 1 - m_startpos);
				if (m_document != null) {
					m_linenum = Utilities.getLineOffset(m_document, m_startpos + 1) + 1;
				}
				m_endpos = tokenoffset + 1;
				m_nextpos = m_endpos;
				break;
			}

			/**
			 * add the token to the list of parsed tokens. we use this list to
			 * build the sql and determine if there are any inputs
			 */

			m_tokens.add(new TokenInfo(token, stoken));

			/**
			 * this is a question mark, so we need to determine if the sql is in
			 * out input form
			 */
			if (token == SQLTokenContext.QUESTION || token == SQLTokenContext.AT) {
				parsePossibleInput(m_tokens);
			}

			token = m_syntax.nextToken();

		}

		if (token == null)
			m_completed = true;

		// check for the case where we have a single, valid SQL statement
		// without a semicolon delimiter
		if (m_sql == null && tokenlookup.size() > 0
				&& tokenlookup.containsKey(new Integer(SQLTokenContext.IDENTIFIER_ID))) {
			int tokenoffset = m_syntax.getTokenOffset();
			int tokenlength = m_syntax.getTokenLength();
			// m_endpos = tokenoffset - m_startpos;
			m_sql = new String(m_segment.array, m_startpos, tokenoffset - m_startpos);
			m_completed = true;
			m_endpos = tokenoffset;
		}

	}

	/**
	 * This iterates backwards of the array. It is called when we encounter a
	 * delimiter for the following form of SQL statement (which Pointbase
	 * supports):
	 * 
	 * INSERT INTO table ( "FIRSTNAME", "LASTNAME" ) VALUES ( ?, ? ); { "Joe",
	 * "Smith" "Sandy", "Smith" };
	 * 
	 * We iterate backward to see if there is a valid form of the SQL statement
	 * 
	 * @param tokens
	 *            an array list of TokenInfo objects that represent the tokens
	 *            for a given sql statement
	 */
	private boolean isDynamicSQL(ArrayList tokens) {
		if (tokens.size() < 3)
			return false;

		final int INITIAL = 0;
		final int QUESTION = 1;
		final int PARENS = 2;
		final int PARENS2 = 3;

		int state = INITIAL;

		for (int index = tokens.size() - 1; index >= 0; index--) {
			TokenInfo tinfo = (TokenInfo) tokens.get(index);
			TokenID token = tinfo.getToken();
			if (state == INITIAL) {
				if (token == SQLTokenContext.RPAREN) {
					state = PARENS;
				} else if (token == SQLTokenContext.WHITESPACE)
					continue;
				else {
					/** not valid form */
					return false;
				}
			} else if (state == PARENS || state == PARENS2) {
				if (token == SQLTokenContext.QUESTION) {
					state = PARENS2;
					continue;
				} else if (token == SQLTokenContext.WHITESPACE)
					continue;
				else if (token == SQLTokenContext.COMMA)
					continue;
				else if (token == SQLTokenContext.LPAREN) {
					if (state == PARENS2) {
						return true;
					} else {
						return false;
					}
				} else {
					// this is an invalid input syntax, so ignore and return
					return false;
				}
			} else {
				return false;
			}
		}

		return false;
	}

	/**
	 * This iterates backwards of the array. It is called when ? token is found.
	 * In this case, we wish to determine if it is a valid input. So, we iterate
	 * backward to see if there is a valid operator and literal. If we find the
	 * valid tokens, we store as inputs.
	 * 
	 * @param tokens
	 *            an array list of TokenInfo objects that represent the tokens
	 *            for a given sql statement
	 */
	private void parsePossibleInput(ArrayList tokens) {
		if (tokens.size() == 0)
			return;

		// start from the end of the array list and work towards the front.
		// we are looking for tokens of the form
		// connection.schema.table.column comparison ?
		// where comparison is: =, !=, <>, <, etc.

		final int INITIAL = 0;
		final int QUESTION = 1;
		final int EQUALS = 2;
		final int VALIDATED = 4;
		int state = INITIAL;

		// the question token info
		TokenInfo question = null;
		// for debugging
		TokenInfo op = null;
		TokenInfo id = null;

		for (int index = tokens.size() - 1; index >= 0; index--) {
			TokenInfo tinfo = (TokenInfo) tokens.get(index);
			TokenID token = tinfo.getToken();
			if (state == INITIAL) {
				if (token == SQLTokenContext.QUESTION || token == SQLTokenContext.AT) {
					question = tinfo;
					state = QUESTION;
				} else if (token == SQLTokenContext.WHITESPACE)
					continue;
				else {
					// this is an invalid input syntax, so ignore and return
					return;
				}
			} else if (state == QUESTION) {
				if (Operator.fromString(tinfo.getValue()) != null) {
					op = tinfo;
					state = EQUALS;
				} else if (token == SQLTokenContext.WHITESPACE)
					continue;
				else {
					// this is an invalid input syntax, so ignore and return
					return;
				}
			} else if (state == EQUALS) {
				if (token == SQLTokenContext.IDENTIFIER) {
					id = tinfo;
					state = VALIDATED;
					break;
				} else if (token == SQLTokenContext.WHITESPACE)
					continue;
				else {
					// this is an invalid input syntax, so ignore and return
					return;
				}
			}
		}

		if (state == VALIDATED) {
			assert (id != null);
			assert (op != null);
			assert (question != null);
			ParsedSQLInput input = new ParsedSQLInput(id, op, question);
			m_inputs.add(input);
		}
	}

	/**
	 * Implementation of SQLInput. Basically, we override setValue to set the
	 * value of the Tokeninfo object. This input is used with the
	 * SQLInputModel/View to allow the user to provide input values on the fly.
	 */
	private class ParsedSQLInput implements SQLInput {

		private TokenInfo m_id;
		private TokenInfo m_op;
		private TokenInfo m_question;

		/**
		 * ctor
		 */
		ParsedSQLInput(TokenInfo id, TokenInfo op, TokenInfo question) {
			m_id = id;
			m_op = op;
			m_question = question;
			assert (op != null);
		}

		public String getName() {
			return m_id.getValue();
		}

		/**
		 * Let's put quote delims around all inputs
		 */
		public String getValue() {
			return m_question.getValue();
		}

		/**
		 * Called when the user enters a value in the SQLInputView
		 */
		public void setValue(String value) {
			TokenID token = m_question.getToken();
			if (token == SQLTokenContext.QUESTION) {
				if (value != null && value.length() > 0) {
					if (value.charAt(0) != '\'' || value.charAt(value.length() - 1) != '\'') {
						value = DbUtils.toSQL(value, '\'');
					}
				}
			}
			m_question.setValue(value);
		}

		/**
		 * @return the token entered by the user that identifies the type of
		 *         input. Either ? or @ is accepted. A ? means that the value
		 *         will automatically be 'delimited' as text. A @ means that the
		 *         input is inserted into the SQL command exactly as typed by
		 *         the user
		 */
		public TokenInfo getInputToken() {
			return m_question;
		}

		/**
		 * @return the constraint token entered by the user that identifies the
		 *         type of constraint operator (i.e. >, <, ==, <>, <=, LIKE etc
		 *         )
		 */
		public TokenInfo getOperatorToken() {
			return m_op;
		}

	}

}
