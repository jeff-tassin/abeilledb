package com.jeta.abeille.gui.sql;

import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenID;

/**
 * Syntax analyzes for SQL source files. Tokens and internal states are given
 * below.
 * 
 * @author Jeff Tassin
 */

public class SQLSyntax extends Syntax {

	// Internal states
	private static final int ISI_WHITESPACE = 2; // inside white space
	private static final int ISI_LINE_COMMENT = 4; // inside line comment //
	private static final int ISI_BLOCK_COMMENT = 5; // inside block comment /*
													// ... */
	private static final int ISI_STRING = 6; // inside string constant
	private static final int ISI_STRING_A_BSLASH = 7; // inside string constant
														// after backslash
	private static final int ISI_CHAR = 8; // inside char constant
	private static final int ISI_CHAR_A_BSLASH = 9; // inside char constant
													// after backslash
	private static final int ISI_IDENTIFIER = 10; // inside identifier
	private static final int ISA_SLASH = 11; // slash char
	private static final int ISA_EQ = 12; // after '='
	private static final int ISA_GT = 13; // after '>'
	private static final int ISA_LT = 16; // after '<'
	private static final int ISA_PLUS = 18; // after '+'
	private static final int ISA_MINUS = 19; // after '-'
	private static final int ISA_STAR = 20; // after '*'
	private static final int ISA_STAR_I_BLOCK_COMMENT = 21; // after '*'
	private static final int ISA_PIPE = 22; // after '|'
	private static final int ISA_PERCENT = 23; // after '%'
	private static final int ISA_AND = 24; // after '&'
	private static final int ISA_XOR = 25; // after '^'
	private static final int ISA_EXCLAMATION = 26; // after '!'
	private static final int ISA_ZERO = 27; // after '0'
	private static final int ISI_INT = 28; // integer number
	private static final int ISI_OCTAL = 29; // octal number
	private static final int ISI_DOUBLE = 30; // double number
	private static final int ISI_DOUBLE_EXP = 31; // double number
	private static final int ISI_HEX = 32; // hex number
	private static final int ISA_DOT = 33; // after '.'

	public SQLSyntax() {
		tokenContextPath = SQLTokenContext.contextPath;
	}

	protected TokenID parseToken() {
		char actChar;

		while (offset < stopOffset) {
			actChar = buffer[offset];
			switch (state) {
			case INIT:
				switch (actChar) {
				case '"': // NOI18N
					state = ISI_STRING;
					break;
				case '\'':
				case '`':
					state = ISI_CHAR;
					break;
				case '/':
					state = ISA_SLASH;
					break;
				case '=':
					state = ISA_EQ;
					break;
				case '>':
					state = ISA_GT;
					break;
				case '<':
					state = ISA_LT;
					break;
				case '+':
					state = ISA_PLUS;
					break;
				case '-':
					state = ISA_MINUS;
					break;
				case '*':
					state = ISA_STAR;
					break;
				case '|':
					state = ISA_PIPE;
					break;
				case '%':
					state = ISA_PERCENT;
					break;
				case '&':
					state = ISA_AND;
					break;
				case '^':
					state = ISA_XOR;
					break;
				case '~':
					offset++;
					return SQLTokenContext.NEG;
				case '!':
					state = ISA_EXCLAMATION;
					break;
				case '0':
					state = ISA_ZERO;
					break;
				case '.':
					state = ISA_DOT;
					break;
				case ',':
					offset++;
					return SQLTokenContext.COMMA;
				case ';':
					offset++;
					return SQLTokenContext.SEMICOLON;
				case ':':
					offset++;
					return SQLTokenContext.COLON;
				case '?':
					offset++;
					return SQLTokenContext.QUESTION;
				case '@':
					offset++;
					return SQLTokenContext.AT;
				case '(':
					offset++;
					return SQLTokenContext.LPAREN;
				case ')':
					offset++;
					return SQLTokenContext.RPAREN;
				case '[':
					offset++;
					return SQLTokenContext.LBRACKET;
				case ']':
					offset++;
					return SQLTokenContext.RBRACKET;
				case '{':
					offset++;
					return SQLTokenContext.LBRACE;
				case '}':
					offset++;
					return SQLTokenContext.RBRACE;

				default:
					// Check for whitespace
					if (Character.isWhitespace(actChar)) {
						state = ISI_WHITESPACE;
						break;
					}

					// Check for digit
					if (Character.isDigit(actChar)) {
						state = ISI_INT;
						break;
					}

					// Check for identifier
					if (Character.isJavaIdentifierStart(actChar)) {
						state = ISI_IDENTIFIER;
						break;
					}

					offset++;
					// return SQLTokenContext.INVALID_CHAR;
					return SQLTokenContext.WHITESPACE;
				}
				break;

			case ISI_WHITESPACE: // white space
				if (!Character.isWhitespace(actChar)) {
					state = INIT;
					return SQLTokenContext.WHITESPACE;
				}
				break;

			case ISI_LINE_COMMENT:
				switch (actChar) {
				case '\n':
					state = INIT;
					return SQLTokenContext.LINE_COMMENT;
				}
				break;

			case ISI_BLOCK_COMMENT:
				switch (actChar) {
				case '*':
					state = ISA_STAR_I_BLOCK_COMMENT;
					break;
				}
				break;

			case ISI_STRING:
				switch (actChar) {
				case '\\':
					state = ISI_STRING_A_BSLASH;
					break;
				case '\n':
					// state = INIT;
					// supposedTokenID = SQLTokenContext.STRING_LITERAL;
					// return supposedTokenID;
					break;
				case '"': // NOI18N
					offset++;
					state = INIT;
					return SQLTokenContext.STRING_LITERAL;
				}
				break;

			case ISI_STRING_A_BSLASH:
				switch (actChar) {
				case '"': // NOI18N
				case '\\':
					break;
				default:
					offset--;
					break;
				}
				state = ISI_STRING;
				break;

			case ISI_CHAR:
				switch (actChar) {
				case '\\':
					state = ISI_CHAR_A_BSLASH;
					break;
				case '\n':
					// state = INIT;
					// supposedTokenID = SQLTokenContext.CHAR_LITERAL;
					// return supposedTokenID;
					break;
				case '\'':
				case '`':
					offset++;
					state = INIT;
					return SQLTokenContext.CHAR_LITERAL;
				}
				break;

			case ISI_CHAR_A_BSLASH:
				switch (actChar) {
				case '\'':
				case '`':
				case '\\':
					break;
				default:
					offset--;
					break;
				}
				state = ISI_CHAR;
				break;

			case ISI_IDENTIFIER:
				if (!(Character.isJavaIdentifierPart(actChar))) {
					state = INIT;
					TokenID tid = matchKeyword(buffer, tokenOffset, offset - tokenOffset);
					return (tid != null) ? tid : SQLTokenContext.IDENTIFIER;
				}
				break;

			case ISA_SLASH:
				switch (actChar) {
				case '/':
					state = ISI_LINE_COMMENT;
					break;
				case '*':
					state = ISI_BLOCK_COMMENT;
					break;
				default:
					state = INIT;
					return SQLTokenContext.DIV;
				}
				break;

			case ISA_EQ:
				switch (actChar) {
				case '=':
					offset++;
					return SQLTokenContext.EQ_EQ;
				default:
					state = INIT;
					return SQLTokenContext.EQ;
				}
				// break;

			case ISA_GT:
				switch (actChar) {
				case '=':
					offset++;
					return SQLTokenContext.GT_EQ;
				default:
					state = INIT;
					return SQLTokenContext.GT;
				}

			case ISA_LT:
				switch (actChar) {
				case '=':
					offset++;
					return SQLTokenContext.LT_EQ;
				default:
					state = INIT;
					return SQLTokenContext.LT;
				}

			case ISA_PLUS:
			default:
				state = INIT;
				return SQLTokenContext.PLUS;

			case ISA_MINUS:
				switch (actChar) {
				case '-':
					state = ISI_LINE_COMMENT;
					break;
				default:
					state = INIT;
					return SQLTokenContext.MINUS;
				}
				break;

			case ISA_STAR:
				switch (actChar) {
				case '/':
					offset++;
					state = INIT;
					return SQLTokenContext.INVALID_COMMENT_END; // '*/' outside
																// comment
				default:
					state = INIT;
					return SQLTokenContext.MUL;
				}

			case ISA_STAR_I_BLOCK_COMMENT:
				switch (actChar) {
				case '/':
					offset++;
					state = INIT;
					return SQLTokenContext.BLOCK_COMMENT;
				default:
					offset--;
					state = ISI_BLOCK_COMMENT;
					break;
				}
				break;

			case ISA_PIPE:
				state = INIT;
				return SQLTokenContext.OR;

			case ISA_PERCENT:
				state = INIT;
				return SQLTokenContext.MOD;

			case ISA_AND:
				state = INIT;
				return SQLTokenContext.AND;

			case ISA_XOR:
				state = INIT;
				return SQLTokenContext.XOR;

			case ISA_EXCLAMATION:
				switch (actChar) {
				case '=':
					offset++;
					state = INIT;
					return SQLTokenContext.NOT_EQ;
				default:
					state = INIT;
					return SQLTokenContext.NOT;
				}
				// break;

			case ISA_ZERO:
				switch (actChar) {
				case '.':
					state = ISI_DOUBLE;
					break;
				case 'x':
				case 'X':
					state = ISI_HEX;
					break;
				case 'l':
				case 'L':
					offset++;
					state = INIT;
					return SQLTokenContext.LONG_LITERAL;
				case 'f':
				case 'F':
					offset++;
					state = INIT;
					return SQLTokenContext.FLOAT_LITERAL;
				case 'd':
				case 'D':
					offset++;
					state = INIT;
					return SQLTokenContext.DOUBLE_LITERAL;
					// case '8': // it's error to have '8' and '9' in octal
					// number
					// case '9':
					// state = INIT;
					// offset++;
					// return SQLTokenContext.INVALID_OCTAL_LITERAL;
				case 'e':
				case 'E':
					state = ISI_DOUBLE_EXP;
					break;
				default:
					if (Character.isDigit(actChar)) { // '8' and '9' already
														// handled
						state = ISI_OCTAL;
						break;
					}
					state = INIT;
					return SQLTokenContext.INT_LITERAL;
				}
				break;

			case ISI_INT:
				switch (actChar) {
				case 'l':
				case 'L':
					offset++;
					state = INIT;
					return SQLTokenContext.LONG_LITERAL;
				case '.':
					state = ISI_DOUBLE;
					break;
				case 'f':
				case 'F':
					offset++;
					state = INIT;
					return SQLTokenContext.FLOAT_LITERAL;
				case 'd':
				case 'D':
					offset++;
					state = INIT;
					return SQLTokenContext.DOUBLE_LITERAL;
				case 'e':
				case 'E':
					state = ISI_DOUBLE_EXP;
					break;
				default:
					if (!(actChar >= '0' && actChar <= '9')) {
						state = INIT;
						return SQLTokenContext.INT_LITERAL;
					}
				}
				break;

			case ISI_OCTAL:
				if (!(actChar >= '0' && actChar <= '7')) {

					state = INIT;
					return SQLTokenContext.OCTAL_LITERAL;
				}
				break;

			case ISI_DOUBLE:
				switch (actChar) {
				case 'f':
				case 'F':
					offset++;
					state = INIT;
					return SQLTokenContext.FLOAT_LITERAL;
				case 'd':
				case 'D':
					offset++;
					state = INIT;
					return SQLTokenContext.DOUBLE_LITERAL;
				case 'e':
				case 'E':
					state = ISI_DOUBLE_EXP;
					break;
				default:
					if (!((actChar >= '0' && actChar <= '9') || actChar == '.')) {

						state = INIT;
						return SQLTokenContext.DOUBLE_LITERAL;
					}
				}
				break;

			case ISI_DOUBLE_EXP:
				switch (actChar) {
				case 'f':
				case 'F':
					offset++;
					state = INIT;
					return SQLTokenContext.FLOAT_LITERAL;
				case 'd':
				case 'D':
					offset++;
					state = INIT;
					return SQLTokenContext.DOUBLE_LITERAL;
				default:
					if (!(Character.isDigit(actChar) || actChar == '-' || actChar == '+')) {
						state = INIT;
						return SQLTokenContext.DOUBLE_LITERAL;
					}
				}
				break;

			case ISI_HEX:
				if (!((actChar >= 'a' && actChar <= 'f') || (actChar >= 'A' && actChar <= 'F') || Character
						.isDigit(actChar))) {

					state = INIT;
					return SQLTokenContext.HEX_LITERAL;
				}
				break;

			case ISA_DOT:
				if (Character.isDigit(actChar)) {
					state = ISI_DOUBLE;

				} else { // only single dot
					state = INIT;
					return SQLTokenContext.DOT;
				}
				break;

			} // end of switch(state)

			offset++;
		} // end of while(offset...)

		/**
		 * At this stage there's no more text in the scanned buffer. Scanner
		 * first checks whether this is completely the last available buffer.
		 */

		if (lastBuffer) {
			switch (state) {
			case ISI_WHITESPACE:
				state = INIT;
				return SQLTokenContext.WHITESPACE;
			case ISI_IDENTIFIER:
				state = INIT;
				TokenID kwd = matchKeyword(buffer, tokenOffset, offset - tokenOffset);
				return (kwd != null) ? kwd : SQLTokenContext.IDENTIFIER;
			case ISI_LINE_COMMENT:
				return SQLTokenContext.LINE_COMMENT; // stay in line-comment
														// state
			case ISI_BLOCK_COMMENT:
			case ISA_STAR_I_BLOCK_COMMENT:
				return SQLTokenContext.BLOCK_COMMENT; // stay in block-comment
														// state
			case ISI_STRING:
			case ISI_STRING_A_BSLASH:
				return SQLTokenContext.STRING_LITERAL; // hold the state
			case ISI_CHAR:
			case ISI_CHAR_A_BSLASH:
				return SQLTokenContext.CHAR_LITERAL; // hold the state
			case ISA_ZERO:
			case ISI_INT:
				state = INIT;
				return SQLTokenContext.INT_LITERAL;
			case ISI_OCTAL:
				state = INIT;
				return SQLTokenContext.OCTAL_LITERAL;
			case ISI_DOUBLE:
			case ISI_DOUBLE_EXP:
				state = INIT;
				return SQLTokenContext.DOUBLE_LITERAL;
			case ISI_HEX:
				state = INIT;
				return SQLTokenContext.HEX_LITERAL;
			case ISA_DOT:
				state = INIT;
				return SQLTokenContext.DOT;
			case ISA_SLASH:
				state = INIT;
				return SQLTokenContext.DIV;
			case ISA_EQ:
				state = INIT;
				return SQLTokenContext.EQ;
			case ISA_GT:
				state = INIT;
				return SQLTokenContext.GT;
			case ISA_LT:
				state = INIT;
				return SQLTokenContext.LT;
			case ISA_PLUS:
				state = INIT;
				return SQLTokenContext.PLUS;
			case ISA_MINUS:
				state = INIT;
				return SQLTokenContext.MINUS;
			case ISA_STAR:
				state = INIT;
				return SQLTokenContext.MUL;
			case ISA_PIPE:
				state = INIT;
				return SQLTokenContext.OR;
			case ISA_PERCENT:
				state = INIT;
				return SQLTokenContext.MOD;
			case ISA_AND:
				state = INIT;
				return SQLTokenContext.AND;
			case ISA_XOR:
				state = INIT;
				return SQLTokenContext.XOR;
			case ISA_EXCLAMATION:
				state = INIT;
				return SQLTokenContext.NOT;
			}
		}

		/*
		 * At this stage there's no more text in the scanned buffer, but this
		 * buffer is not the last so the scan will continue on another buffer.
		 * The scanner tries to minimize the amount of characters that will be
		 * prescanned in the next buffer by returning the token where possible.
		 */

		switch (state) {
		case ISI_WHITESPACE:
			return SQLTokenContext.WHITESPACE;
		}

		return null; // nothing found
	}

	/**
	 * Convert a state number into its corresponding name
	 */
	public String getStateName(int stateNumber) {
		switch (stateNumber) {
		case ISI_WHITESPACE:
			return "ISI_WHITESPACE"; // NOI18N
		case ISI_LINE_COMMENT:
			return "ISI_LINE_COMMENT"; // NOI18N
		case ISI_BLOCK_COMMENT:
			return "ISI_BLOCK_COMMENT"; // NOI18N
		case ISI_STRING:
			return "ISI_STRING"; // NOI18N
		case ISI_STRING_A_BSLASH:
			return "ISI_STRING_A_BSLASH"; // NOI18N
		case ISI_CHAR:
			return "ISI_CHAR"; // NOI18N
		case ISI_CHAR_A_BSLASH:
			return "ISI_CHAR_A_BSLASH"; // NOI18N
		case ISI_IDENTIFIER:
			return "ISI_IDENTIFIER"; // NOI18N
		case ISA_SLASH:
			return "ISA_SLASH"; // NOI18N
		case ISA_EQ:
			return "ISA_EQ"; // NOI18N
		case ISA_GT:
			return "ISA_GT"; // NOI18N
		case ISA_LT:
			return "ISA_LT"; // NOI18N
		case ISA_PLUS:
			return "ISA_PLUS"; // NOI18N
		case ISA_MINUS:
			return "ISA_MINUS"; // NOI18N
		case ISA_STAR:
			return "ISA_STAR"; // NOI18N
		case ISA_STAR_I_BLOCK_COMMENT:
			return "ISA_STAR_I_BLOCK_COMMENT"; // NOI18N
		case ISA_PIPE:
			return "ISA_PIPE"; // NOI18N
		case ISA_PERCENT:
			return "ISA_PERCENT"; // NOI18N
		case ISA_AND:
			return "ISA_AND"; // NOI18N
		case ISA_XOR:
			return "ISA_XOR"; // NOI18N
		case ISA_EXCLAMATION:
			return "ISA_EXCLAMATION"; // NOI18N
		case ISA_ZERO:
			return "ISA_ZERO"; // NOI18N
		case ISI_INT:
			return "ISI_INT"; // NOI18N
		case ISI_OCTAL:
			return "ISI_OCTAL"; // NOI18N
		case ISI_DOUBLE:
			return "ISI_DOUBLE"; // NOI18N
		case ISI_DOUBLE_EXP:
			return "ISI_DOUBLE_EXP"; // NOI18N
		case ISI_HEX:
			return "ISI_HEX"; // NOI18N
		case ISA_DOT:
			return "ISA_DOT"; // NOI18N

		default:
			return super.getStateName(stateNumber);
		}
	}

	// we are not matching keywords just yet
	public static TokenID matchKeyword(char[] buffer, int offset, int len) {
		// select
		String token = new String(buffer, offset, len);
		return SQLTokenContext.getKeywordToken(token);
	}

}
