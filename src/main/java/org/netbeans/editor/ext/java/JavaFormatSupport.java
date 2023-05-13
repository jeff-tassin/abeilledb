/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.editor.ext.java;

import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.FormatTokenPosition;
import org.netbeans.editor.ext.ExtFormatSupport;
import org.netbeans.editor.ext.FormatWriter;
import org.netbeans.editor.ext.java.JavaTokenContext;
import org.netbeans.editor.ext.java.JavaSyntax;

/**
 * Java indentation services are located here
 * 
 * @author Miloslav Metelka
 * @version 1.00
 */

public class JavaFormatSupport extends ExtFormatSupport {

	private TokenContextPath tokenContextPath;

	public JavaFormatSupport(FormatWriter formatWriter) {
		this(formatWriter, JavaTokenContext.contextPath);
	}

	public JavaFormatSupport(FormatWriter formatWriter, TokenContextPath tokenContextPath) {
		super(formatWriter);
		this.tokenContextPath = tokenContextPath;
	}

	public TokenContextPath getTokenContextPath() {
		return tokenContextPath;
	}

	public boolean isComment(TokenItem token, int offset) {
		TokenID tokenID = token.getTokenID();
		return (token.getTokenContextPath() == tokenContextPath && (tokenID == JavaTokenContext.LINE_COMMENT || tokenID == JavaTokenContext.BLOCK_COMMENT));
	}

	public boolean isMultiLineComment(TokenItem token) {
		return (token.getTokenID() == JavaTokenContext.BLOCK_COMMENT);
	}

	public boolean isMultiLineComment(FormatTokenPosition pos) {
		TokenItem token = pos.getToken();
		return (token == null) ? false : isMultiLineComment(token);
	}

	/**
	 * Check whether the given token is multi-line comment that starts with
	 * slash and two stars.
	 */
	public boolean isJavaDocComment(TokenItem token) {
		return isMultiLineComment(token) && token.getImage().startsWith("/**");
	}

	public TokenID getWhitespaceTokenID() {
		return JavaTokenContext.WHITESPACE;
	}

	public TokenContextPath getWhitespaceTokenContextPath() {
		return tokenContextPath;
	}

	public boolean canModifyWhitespace(TokenItem inToken) {
		if (inToken.getTokenContextPath() == JavaTokenContext.contextPath) {
			switch (inToken.getTokenID().getNumericID()) {
			case JavaTokenContext.BLOCK_COMMENT_ID:
			case JavaTokenContext.WHITESPACE_ID:
				return true;
			}
		}

		return false;
	}

	/**
	 * Find the starting token of the statement before the given position and
	 * also return all the command delimiters. It searches in the backward
	 * direction for all the delimiters and statement starts and return all the
	 * tokens that are either command starts or delimiters. As the first step it
	 * uses <code>getPreviousToken()</code> so it ignores the initial token.
	 * 
	 * @param token
	 *            token before which the statement-start and delimiter is being
	 *            searched.
	 * @return token that is start of the given statement or command delimiter.
	 *         If the start of the statement is not found, null is retrurned.
	 */
	public TokenItem findStatement(TokenItem token) {
		TokenItem lit = null; // last important token
		TokenItem t = getPreviousToken(token);

		while (t != null) {
			if (t.getTokenContextPath() == tokenContextPath) {

				switch (t.getTokenID().getNumericID()) {
				case JavaTokenContext.SEMICOLON_ID:
					if (!isForLoopSemicolon(t)) {
						return (lit != null) ? lit : t;
					}
					break;

				case JavaTokenContext.LBRACE_ID:
				case JavaTokenContext.RBRACE_ID:
				case JavaTokenContext.COLON_ID:
				case JavaTokenContext.ELSE_ID:
					return (lit != null) ? lit : t;

				case JavaTokenContext.DO_ID:
				case JavaTokenContext.SWITCH_ID:
				case JavaTokenContext.CASE_ID:
				case JavaTokenContext.DEFAULT_ID:
					return t;

				case JavaTokenContext.FOR_ID:
				case JavaTokenContext.IF_ID:
				case JavaTokenContext.WHILE_ID:
					/*
					 * Try to find the statement after ( ... ) If it exists,
					 * then the first important token after it is the stmt
					 * start. Otherwise it's this token.
					 */
					if (lit != null && lit.getTokenID() == JavaTokenContext.LPAREN) {
						// Find matching right paren in fwd dir
						TokenItem mt = findMatchingToken(lit, token, JavaTokenContext.RPAREN, false);
						if (mt != null && mt.getNext() != null) {
							mt = findImportantToken(mt.getNext(), token, false);
							if (mt != null) {
								return mt;
							}
						}
					}

					// No further stmt found, return this one
					return t;

				}

				// Remember last important token
				if (isImportant(t, 0)) {
					lit = t;
				}

			}

			t = t.getPrevious();
		}

		return lit;
	}

	/**
	 * Find the 'if' when the 'else' is provided.
	 * 
	 * @param elseToken
	 *            the token with the 'else' command for which the 'if' is being
	 *            searched.
	 * @return corresponding 'if' token or null if there's no corresponding 'if'
	 *         statement.
	 */
	public TokenItem findIf(TokenItem elseToken) {
		if (elseToken == null || !tokenEquals(elseToken, JavaTokenContext.ELSE, tokenContextPath)) {
			throw new IllegalArgumentException("Only accept 'else'.");
		}

		int braceDepth = 0; // depth of the braces
		int elseDepth = 0; // depth of multiple else stmts
		while (true) {
			elseToken = findStatement(elseToken);
			if (elseToken == null) {
				return null;
			}

			switch (elseToken.getTokenID().getNumericID()) {
			case JavaTokenContext.LBRACE_ID:
				if (--braceDepth < 0) {
					return null; // no corresponding right brace
				}
				break;

			case JavaTokenContext.RBRACE_ID:
				braceDepth++;
				break;

			case JavaTokenContext.ELSE_ID:
				if (braceDepth == 0) {
					elseDepth++;
				}
				break;

			case JavaTokenContext.SEMICOLON_ID:
			case JavaTokenContext.COLON_ID:
			case JavaTokenContext.DO_ID:
			case JavaTokenContext.CASE_ID:
			case JavaTokenContext.DEFAULT_ID:
			case JavaTokenContext.FOR_ID:
			case JavaTokenContext.WHILE_ID:
				break;

			case JavaTokenContext.IF_ID:
				if (braceDepth == 0) {
					if (elseDepth-- == 0) {
						return elseToken; // successful search
					}
				}
				break;
			}
		}
	}

	/**
	 * Find the 'switch' when the 'case' is provided.
	 * 
	 * @param caseToken
	 *            the token with the 'case' command for which the 'switch' is
	 *            being searched.
	 * @return corresponding 'switch' token or null if there's no corresponding
	 *         'switch' statement.
	 */
	public TokenItem findSwitch(TokenItem caseToken) {
		if (caseToken == null
				|| (!tokenEquals(caseToken, JavaTokenContext.CASE, tokenContextPath) && !tokenEquals(caseToken,
						JavaTokenContext.DEFAULT, tokenContextPath))) {
			throw new IllegalArgumentException("Only accept 'case' or 'default'.");
		}

		int braceDepth = 1; // depth of the braces - need one more left
		while (true) {
			caseToken = findStatement(caseToken);
			if (caseToken == null) {
				return null;
			}

			switch (caseToken.getTokenID().getNumericID()) {
			case JavaTokenContext.LBRACE_ID:
				if (--braceDepth < 0) {
					return null; // no corresponding right brace
				}
				break;

			case JavaTokenContext.RBRACE_ID:
				braceDepth++;
				break;

			case JavaTokenContext.SWITCH_ID:
			case JavaTokenContext.DEFAULT_ID:
				if (braceDepth == 0) {
					return caseToken;
				}
				break;
			}
		}
	}

	/**
	 * Find the 'try' when the 'catch' is provided.
	 * 
	 * @param catchToken
	 *            the token with the 'catch' command for which the 'try' is
	 *            being searched.
	 * @return corresponding 'try' token or null if there's no corresponding
	 *         'try' statement.
	 */
	public TokenItem findTry(TokenItem catchToken) {
		if (catchToken == null || (!tokenEquals(catchToken, JavaTokenContext.CATCH, tokenContextPath))) {
			throw new IllegalArgumentException("Only accept 'catch'.");
		}

		int braceDepth = 0; // depth of the braces
		while (true) {
			catchToken = findStatement(catchToken);
			if (catchToken == null) {
				return null;
			}

			switch (catchToken.getTokenID().getNumericID()) {
			case JavaTokenContext.LBRACE_ID:
				if (--braceDepth < 0) {
					return null; // no corresponding right brace
				}
				break;

			case JavaTokenContext.RBRACE_ID:
				braceDepth++;
				break;

			case JavaTokenContext.TRY_ID:
				if (braceDepth == 0) {
					return catchToken;
				}
				break;
			}
		}
	}

	/**
	 * Find the start of the statement.
	 * 
	 * @param token
	 *            token from which to start. It searches backward using
	 *            <code>findStatement()</code> so it ignores the given token.
	 * @return the statement start token (outer statement start for nested
	 *         statements). It returns the same token if there is '{' before the
	 *         given token.
	 */
	public TokenItem findStatementStart(TokenItem token) {
		TokenItem t = findStatement(token);
		if (t != null) {
			switch (t.getTokenID().getNumericID()) {
			case JavaTokenContext.SEMICOLON_ID: // ';' found
				TokenItem scss = findStatement(t);

				// fix for issue 14274
				if (scss == null)
					return token;

				switch (scss.getTokenID().getNumericID()) {
				case JavaTokenContext.LBRACE_ID: // '{' then ';'
				case JavaTokenContext.RBRACE_ID: // '}' then ';'
				case JavaTokenContext.COLON_ID: // ':' then ';'
				case JavaTokenContext.CASE_ID: // 'case' then ';'
				case JavaTokenContext.DEFAULT_ID:
				case JavaTokenContext.SEMICOLON_ID: // ';' then ';'
					return t; // return ';'

				case JavaTokenContext.DO_ID:
				case JavaTokenContext.FOR_ID:
				case JavaTokenContext.IF_ID:
				case JavaTokenContext.WHILE_ID:
					return findStatementStart(t);

				case JavaTokenContext.ELSE_ID: // 'else' then ';'
					// Find the corresponding 'if'
					TokenItem ifss = findIf(scss);
					if (ifss != null) { // 'if' ... 'else' then ';'
						return findStatementStart(ifss);

					} else { // no valid starting 'if'
						return scss; // return 'else'
					}

				default: // something usual then ';'
					TokenItem bscss = findStatement(scss);
					if (bscss != null) {
						switch (bscss.getTokenID().getNumericID()) {
						case JavaTokenContext.SEMICOLON_ID: // ';' then stmt
															// ending with ';'
						case JavaTokenContext.LBRACE_ID:
						case JavaTokenContext.RBRACE_ID:
						case JavaTokenContext.COLON_ID:
							return scss; //

						case JavaTokenContext.DO_ID:
						case JavaTokenContext.FOR_ID:
						case JavaTokenContext.IF_ID:
						case JavaTokenContext.WHILE_ID:
							return findStatementStart(bscss);

						case JavaTokenContext.ELSE_ID:
							// Find the corresponding 'if'
							ifss = findIf(bscss);
							if (ifss != null) { // 'if' ... 'else' ... ';'
								return findStatementStart(ifss);

							} else { // no valid starting 'if'
								return bscss; // return 'else'
							}
						}
					}

					return scss;
				} // semicolon servicing end

			case JavaTokenContext.LBRACE_ID: // '{' found
				return token; // return original token

			case JavaTokenContext.RBRACE_ID: // '}' found
				TokenItem lb = findMatchingToken(t, null, JavaTokenContext.LBRACE, true);
				if (lb != null) { // valid matching left-brace
					// Find a stmt-start of the '{'
					TokenItem lbss = findStatement(lb);
					if (lbss != null) {
						switch (lbss.getTokenID().getNumericID()) {
						case JavaTokenContext.ELSE_ID: // 'else {'
							// Find the corresponding 'if'
							TokenItem ifss = findIf(lbss);
							if (ifss != null) { // valid 'if'
								return findStatementStart(ifss);
							} else {
								return lbss; // return 'else'
							}

						case JavaTokenContext.CATCH_ID: // 'catch (...) {'
							// Find the corresponding 'try'
							TokenItem tryss = findTry(lbss);
							if (tryss != null) { // valid 'try'
								return findStatementStart(tryss);
							} else {
								return lbss; // return 'catch'
							}

						case JavaTokenContext.DO_ID:
						case JavaTokenContext.FOR_ID:
						case JavaTokenContext.IF_ID:
						case JavaTokenContext.WHILE_ID:
							return findStatementStart(lbss);

						}

						// another hack to prevent problem described in issue
						// 17033
						if (lbss.getTokenID().getNumericID() == JavaTokenContext.LBRACE_ID) {
							return t; // return right brace
						}

						return lbss;
					}

				}
				return t; // return right brace

			case JavaTokenContext.COLON_ID:
			case JavaTokenContext.CASE_ID:
			case JavaTokenContext.DEFAULT_ID:
				return token;

			case JavaTokenContext.ELSE_ID:
				// Find the corresponding 'if'
				TokenItem ifss = findIf(t);
				return (ifss != null) ? findStatementStart(ifss) : t;

			case JavaTokenContext.DO_ID:
			case JavaTokenContext.FOR_ID:
			case JavaTokenContext.IF_ID:
			case JavaTokenContext.WHILE_ID:
				return findStatementStart(t);
			}
		}

		return token; // return original token
	}

	/**
	 * Get the indentation for the given token. It first searches whether
	 * there's an non-whitespace and a non-leftbrace character on the line with
	 * the token and if so, it takes indent of the non-ws char instead.
	 * 
	 * @param token
	 *            token for which the indent is being searched. The token itself
	 *            is ignored and the previous token is used as a base for the
	 *            search.
	 * @param forceFirstNonWhitespace
	 *            set true to ignore leftbrace and search directly for first
	 *            non-whitespace
	 */
	public int getTokenIndent(TokenItem token, boolean forceFirstNonWhitespace) {
		FormatTokenPosition tp = getPosition(token, 0);
		// this is fix for bugs: 7980 and 9111
		// see the findLineFirstNonWhitespaceAndNonLeftBrace definition
		// for more info about the fix
		FormatTokenPosition fnw;
		if (forceFirstNonWhitespace)
			fnw = findLineFirstNonWhitespace(tp);
		else
			fnw = findLineFirstNonWhitespaceAndNonLeftBrace(tp);

		if (fnw != null) { // valid first non-whitespace
			tp = fnw;
		}
		return getVisualColumnOffset(tp);
	}

	public int getTokenIndent(TokenItem token) {
		return getTokenIndent(token, false);
	}

	/**
	 * Find the indentation for the first token on the line. The given token is
	 * also examined in some cases.
	 */
	public int findIndent(TokenItem token) {
		int indent = -1; // assign invalid indent

		// First check the given token
		if (token != null) {
			switch (token.getTokenID().getNumericID()) {
			case JavaTokenContext.ELSE_ID:
				TokenItem ifss = findIf(token);
				if (ifss != null) {
					indent = getTokenIndent(ifss);
				}
				break;

			case JavaTokenContext.LBRACE_ID:
				TokenItem stmt = findStatement(token);
				if (stmt == null) {
					indent = 0;

				} else {
					switch (stmt.getTokenID().getNumericID()) {
					case JavaTokenContext.DO_ID:
					case JavaTokenContext.FOR_ID:
					case JavaTokenContext.IF_ID:
					case JavaTokenContext.WHILE_ID:
					case JavaTokenContext.ELSE_ID:
						indent = getTokenIndent(stmt);
						break;

					case JavaTokenContext.LBRACE_ID:
						indent = getTokenIndent(stmt) + getShiftWidth();
						break;

					default:
						stmt = findStatementStart(token);
						if (stmt == null) {
							indent = 0;

						} else if (stmt == token) {
							stmt = findStatement(token); // search for delimiter
							indent = (stmt != null) ? indent = getTokenIndent(stmt) : 0;

						} else { // valid statement
							indent = getTokenIndent(stmt);
							switch (stmt.getTokenID().getNumericID()) {
							case JavaTokenContext.LBRACE_ID:
								indent += getShiftWidth();
								break;
							}
						}
					}
				}
				break;

			case JavaTokenContext.RBRACE_ID:
				TokenItem rbmt = findMatchingToken(token, null, JavaTokenContext.LBRACE, true);
				if (rbmt != null) { // valid matching left-brace
					TokenItem t = findStatement(rbmt);
					boolean forceFirstNonWhitespace = false;
					if (t == null) {
						t = rbmt; // will get indent of the matching brace

					} else {
						switch (t.getTokenID().getNumericID()) {
						case JavaTokenContext.SEMICOLON_ID:
						case JavaTokenContext.LBRACE_ID:
						case JavaTokenContext.RBRACE_ID: {
							t = rbmt;
							forceFirstNonWhitespace = true;
						}
						}
					}
					// the right brace must be indented to the first
					// non-whitespace char - forceFirstNonWhitespace=true
					indent = getTokenIndent(t, forceFirstNonWhitespace);

				} else { // no matching left brace
					indent = getTokenIndent(token); // leave as is
				}
				break;

			case JavaTokenContext.CASE_ID:
			case JavaTokenContext.DEFAULT_ID:
				TokenItem swss = findSwitch(token);
				if (swss != null) {
					indent = getTokenIndent(swss) + getShiftWidth();
				}
				break;

			}
		}

		// If indent not found, search back for the first important token
		if (indent < 0) { // if not yet resolved
			TokenItem t = findImportantToken(token, null, true);
			if (t != null) { // valid important token
				switch (t.getTokenID().getNumericID()) {
				case JavaTokenContext.SEMICOLON_ID: // semicolon found
					TokenItem tt = findStatementStart(token);
					indent = getTokenIndent(tt);

					break;

				case JavaTokenContext.LBRACE_ID:
					TokenItem lbss = findStatementStart(t);
					indent = getTokenIndent(t) + getShiftWidth();
					break;

				case JavaTokenContext.RBRACE_ID:
					if (true) {
						TokenItem t3 = findStatementStart(token);
						indent = getTokenIndent(t3);
						break;
					}

					/**
					 * Check whether the following situation occurs: if (t1) if
					 * (t2) { ... }
					 * 
					 * In this case the indentation must be shifted one level
					 * back.
					 */
					TokenItem rbmt = findMatchingToken(t, null, JavaTokenContext.LBRACE, true);
					if (rbmt != null) { // valid matching left-brace
						// Check whether there's a indent stmt
						TokenItem t6 = findStatement(rbmt);
						if (t6 != null) {
							switch (t6.getTokenID().getNumericID()) {
							case JavaTokenContext.ELSE_ID:
								/*
								 * Check the following situation: if (t1) if
								 * (t2) c1(); else { c2(); }
								 */

								// Find the corresponding 'if'
								t6 = findIf(t6);
								if (t6 != null) { // valid 'if'
									TokenItem t7 = findStatement(t6);
									if (t7 != null) {
										switch (t7.getTokenID().getNumericID()) {
										case JavaTokenContext.DO_ID:
										case JavaTokenContext.FOR_ID:
										case JavaTokenContext.IF_ID:
										case JavaTokenContext.WHILE_ID:
											indent = getTokenIndent(t7);
											break;

										case JavaTokenContext.ELSE_ID:
											indent = getTokenIndent(findStatementStart(t6));
										}
									}
								}
								break;

							case JavaTokenContext.DO_ID:
							case JavaTokenContext.FOR_ID:
							case JavaTokenContext.IF_ID:
							case JavaTokenContext.WHILE_ID:
								/*
								 * Check the following: if (t1) if (t2) { c1();
								 * }
								 */
								TokenItem t7 = findStatement(t6);
								if (t7 != null) {
									switch (t7.getTokenID().getNumericID()) {
									case JavaTokenContext.DO_ID:
									case JavaTokenContext.FOR_ID:
									case JavaTokenContext.IF_ID:
									case JavaTokenContext.WHILE_ID:
										indent = getTokenIndent(t7);
										break;

									case JavaTokenContext.ELSE_ID:
										indent = getTokenIndent(findStatementStart(t6));

									}
								}
								break;

							case JavaTokenContext.LBRACE_ID: // '{' ... '{'
								indent = getTokenIndent(rbmt);
								break;

							}

						}

						if (indent < 0) {
							indent = getTokenIndent(t); // indent of original
														// rbrace
						}

					} else { // no matching left-brace
						indent = getTokenIndent(t); // return indent of '}'
					}
					break;

				case JavaTokenContext.RPAREN_ID:
					// Try to find the matching left paren
					TokenItem rpmt = findMatchingToken(t, null, JavaTokenContext.LPAREN, true);
					if (rpmt != null) {
						rpmt = findImportantToken(rpmt, null, true);
						// Check whether there are the indent changing kwds
						if (rpmt != null && rpmt.getTokenContextPath() == tokenContextPath) {
							switch (rpmt.getTokenID().getNumericID()) {
							case JavaTokenContext.FOR_ID:
							case JavaTokenContext.IF_ID:
							case JavaTokenContext.WHILE_ID:
								// Indent one level
								indent = getTokenIndent(rpmt) + getShiftWidth();
								break;
							}
						}
					}
					break;

				case JavaTokenContext.COLON_ID:
					// Indent of line with ':' plus one indent level
					indent = getTokenIndent(t) + getShiftWidth();
					break;

				case JavaTokenContext.DO_ID:
				case JavaTokenContext.ELSE_ID:
					indent = getTokenIndent(t) + getShiftWidth();
					break;

				}

				if (indent < 0) { // no indent found yet
					indent = getTokenIndent(t);
				}
			}
		}

		if (indent < 0) { // no important token found
			indent = 0;
		}

		return indent;
	}

	public FormatTokenPosition indentLine(FormatTokenPosition pos) {
		int indent = 0; // Desired indent

		// Get the first non-whitespace position on the line
		FormatTokenPosition firstNWS = findLineFirstNonWhitespace(pos);
		if (firstNWS != null) { // some non-WS on the line
			if (isComment(firstNWS)) { // comment is first on the line
				if (isMultiLineComment(firstNWS) && firstNWS.getOffset() != 0) {

					// Indent the inner lines of the multi-line comment by one
					indent = getLineIndent(getPosition(firstNWS.getToken(), 0), true) + 1;

					// If the line is inside multi-line comment and doesn't
					// contain '*'
					if (!isIndentOnly() && getChar(firstNWS) != '*') {
						if (isJavaDocComment(firstNWS.getToken())) {
							if (getFormatLeadingStarInComment()) {
								// For java-doc it should be OK to add the star
								insertString(firstNWS, "* ");
							}

						} else {
							// For non-java-doc not because it can be commented
							// code
							indent = getLineIndent(pos, true);
						}
					}

				} else if (!isMultiLineComment(firstNWS)) { // line-comment
					indent = findIndent(firstNWS.getToken());
				} else { // multi-line comment
					if (isJavaDocComment(firstNWS.getToken())) {
						indent = findIndent(firstNWS.getToken());
					} else {
						// check whether the multiline comment isn't finished on
						// the same line (see issue 12821)
						if (firstNWS.getToken().getImage().indexOf('\n') == -1)
							indent = findIndent(firstNWS.getToken());
						else
							indent = getLineIndent(firstNWS, true);
					}
				}

			} else { // first non-WS char is not comment
				indent = findIndent(firstNWS.getToken());
			}

		} else { // whole line is WS
			// Can be empty line inside multi-line comment
			TokenItem token = pos.getToken();
			if (token == null) {
				token = findLineStart(pos).getToken();
				if (token == null) { // empty line
					token = getLastToken();
				}
			}

			if (token != null && isMultiLineComment(token)) {
				if (getFormatLeadingStarInComment() && (isIndentOnly() || isJavaDocComment(token))) {
					// Insert initial '*'
					insertString(pos, "*");
					setIndentShift(1);
				}

				// Indent the multi-comment by one more space
				indent = getVisualColumnOffset(getPosition(token, 0)) + 1;

			} else { // non-multi-line comment
				indent = findIndent(pos.getToken());
			}
		}

		// For indent-only always indent
		return changeLineIndent(pos, indent);
	}

	/**
	 * Check whether the given semicolon is inside the for() statement.
	 * 
	 * @param token
	 *            token to check. It must be a semicolon.
	 * @return true if the given semicolon is inside the for() statement, or
	 *         false otherwise.
	 */
	public boolean isForLoopSemicolon(TokenItem token) {
		if (token == null || !tokenEquals(token, JavaTokenContext.SEMICOLON, tokenContextPath)) {
			throw new IllegalArgumentException("Only accept ';'.");
		}

		int parDepth = 0; // parenthesis depth
		int braceDepth = 0; // brace depth
		boolean semicolonFound = false; // next semicolon
		token = token.getPrevious(); // ignore this semicolon
		while (token != null) {
			if (tokenEquals(token, JavaTokenContext.LPAREN, tokenContextPath)) {
				if (parDepth == 0) { // could be a 'for ('
					FormatTokenPosition tp = getPosition(token, 0);
					tp = findImportant(tp, null, false, true);
					if (tp != null && tokenEquals(tp.getToken(), JavaTokenContext.FOR, tokenContextPath)) {
						return true;
					}
					return false;

				} else { // non-zero depth
					parDepth--;
				}

			} else if (tokenEquals(token, JavaTokenContext.RPAREN, tokenContextPath)) {
				parDepth++;

			} else if (tokenEquals(token, JavaTokenContext.LBRACE, tokenContextPath)) {
				if (braceDepth == 0) { // unclosed left brace
					return false;
				}
				braceDepth--;

			} else if (tokenEquals(token, JavaTokenContext.RBRACE, tokenContextPath)) {
				braceDepth++;

			} else if (tokenEquals(token, JavaTokenContext.SEMICOLON, tokenContextPath)) {
				if (semicolonFound) { // one semicolon already found
					return false;
				}
				semicolonFound = true;
			}

			token = token.getPrevious();
		}

		return false;
	}

	public boolean getFormatSpaceBeforeParenthesis() {
		return getSettingBoolean(JavaSettingsNames.JAVA_FORMAT_SPACE_BEFORE_PARENTHESIS,
				JavaSettingsDefaults.defaultJavaFormatSpaceBeforeParenthesis);
	}

	public boolean getFormatSpaceAfterComma() {
		return getSettingBoolean(JavaSettingsNames.JAVA_FORMAT_SPACE_AFTER_COMMA,
				JavaSettingsDefaults.defaultJavaFormatSpaceAfterComma);
	}

	public boolean getFormatNewlineBeforeBrace() {
		return getSettingBoolean(JavaSettingsNames.JAVA_FORMAT_NEWLINE_BEFORE_BRACE,
				JavaSettingsDefaults.defaultJavaFormatNewlineBeforeBrace);
	}

	public boolean getFormatLeadingSpaceInComment() {
		return getSettingBoolean(JavaSettingsNames.JAVA_FORMAT_LEADING_SPACE_IN_COMMENT,
				JavaSettingsDefaults.defaultJavaFormatLeadingSpaceInComment);
	}

	public boolean getFormatLeadingStarInComment() {
		return getSettingBoolean(JavaSettingsNames.JAVA_FORMAT_LEADING_STAR_IN_COMMENT,
				JavaSettingsDefaults.defaultJavaFormatLeadingStarInComment);
	}

	/*
	 * this is fix for bugs: 7980 and 9111. if user enters { foo(); and press
	 * enter at the end of the line, she wants to be indented just under "f" in
	 * "foo();" and not under the "{" as it happens now. and this is what
	 * findLineFirstNonWhitespaceAndNonLeftBrace checks
	 */
	public FormatTokenPosition findLineFirstNonWhitespaceAndNonLeftBrace(FormatTokenPosition pos) {
		// first call the findLineFirstNonWhitespace
		FormatTokenPosition ftp = super.findLineFirstNonWhitespace(pos);
		if (ftp == null) { // no line start, no WS
			return null;
		}

		// now checks if the first non-whitespace char is "{"
		// if it is, find the next non-whitespace char
		if (!ftp.getToken().getImage().startsWith("{"))
			return ftp;

		// if the left brace is closed on the same line - "{ foo(); }"
		// it must be ignored. otherwise next statement is incorrectly indented
		// under the "f" and not under the "{" as expected
		FormatTokenPosition eolp = findNextEOL(ftp);
		TokenItem rbmt = findMatchingToken(ftp.getToken(), eolp != null ? eolp.getToken() : null,
				JavaTokenContext.RBRACE, false);
		if (rbmt != null)
			return ftp;

		FormatTokenPosition ftp_next = getNextPosition(ftp);
		if (ftp_next == null)
			return ftp;

		FormatTokenPosition ftp2 = findImportant(ftp_next, null, true, false);
		if (ftp2 != null)
			return ftp2;
		else
			return ftp;
	}

}
