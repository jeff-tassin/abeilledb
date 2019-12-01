package com.jeta.abeille.gui.sql;

import java.util.HashMap;
import java.util.TreeMap;

import org.netbeans.editor.BaseTokenCategory;
import org.netbeans.editor.BaseTokenID;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.BaseImageTokenID;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;

/**
 * SQL token-context defines token-ids and token-categories used in SQL
 * language.
 * 
 * @author Jeff Tassin
 */

public class SQLTokenContext extends TokenContext {
	private static TreeMap m_keywords = new TreeMap(String.CASE_INSENSITIVE_ORDER);

	// Token category-ids
	public static final int KEYWORDS_ID = 1;
	public static final int OPERATORS_ID = KEYWORDS_ID + 1;
	public static final int QUESTION_ID = OPERATORS_ID + 1;
	public static final int AT_ID = QUESTION_ID + 1;
	public static final int NUMERIC_LITERALS_ID = AT_ID + 1;
	public static final int ERRORS_ID = NUMERIC_LITERALS_ID + 1;

	// Numeric-ids for token-ids
	public static final int WHITESPACE_ID = ERRORS_ID + 1;
	public static final int IDENTIFIER_ID = WHITESPACE_ID + 1;
	public static final int LINE_COMMENT_ID = IDENTIFIER_ID + 1;
	public static final int BLOCK_COMMENT_ID = LINE_COMMENT_ID + 1;
	public static final int CHAR_LITERAL_ID = BLOCK_COMMENT_ID + 1;
	public static final int STRING_LITERAL_ID = CHAR_LITERAL_ID + 1;
	public static final int INT_LITERAL_ID = STRING_LITERAL_ID + 1;
	public static final int LONG_LITERAL_ID = INT_LITERAL_ID + 1;
	public static final int HEX_LITERAL_ID = LONG_LITERAL_ID + 1;
	public static final int OCTAL_LITERAL_ID = HEX_LITERAL_ID + 1;
	public static final int FLOAT_LITERAL_ID = OCTAL_LITERAL_ID + 1;
	public static final int DOUBLE_LITERAL_ID = FLOAT_LITERAL_ID + 1;

	// Operator numeric-ids
	public static final int EQ_ID = DOUBLE_LITERAL_ID + 1; // =
	public static final int LT_ID = EQ_ID + 1; // <
	public static final int GT_ID = LT_ID + 1; // >
	public static final int LSHIFT_ID = GT_ID + 1; // <<
	public static final int RSSHIFT_ID = LSHIFT_ID + 1; // >>
	public static final int RUSHIFT_ID = RSSHIFT_ID + 1; // >>>
	public static final int PLUS_ID = RUSHIFT_ID + 1; // +
	public static final int MINUS_ID = PLUS_ID + 1; // -
	public static final int MUL_ID = MINUS_ID + 1; // *
	public static final int DIV_ID = MUL_ID + 1; // /
	public static final int AND_ID = DIV_ID + 1; // &
	public static final int OR_ID = AND_ID + 1; // |
	public static final int XOR_ID = OR_ID + 1; // ^
	public static final int MOD_ID = XOR_ID + 1; // %
	public static final int NOT_ID = MOD_ID + 1; // !
	public static final int NEG_ID = NOT_ID + 1; // ~
	public static final int EQ_EQ_ID = NEG_ID + 1; // ==
	public static final int LT_EQ_ID = EQ_EQ_ID + 1; // <=
	public static final int GT_EQ_ID = LT_EQ_ID + 1; // >=
	public static final int LSHIFT_EQ_ID = GT_EQ_ID + 1; // <<=
	public static final int RSSHIFT_EQ_ID = LSHIFT_EQ_ID + 1; // >>=
	public static final int RUSHIFT_EQ_ID = RSSHIFT_EQ_ID + 1; // >>>=
	public static final int PLUS_EQ_ID = RUSHIFT_EQ_ID + 1; // +=
	public static final int MINUS_EQ_ID = PLUS_EQ_ID + 1; // -=
	public static final int MUL_EQ_ID = MINUS_EQ_ID + 1; // *=
	public static final int DIV_EQ_ID = MUL_EQ_ID + 1; // /=
	public static final int AND_EQ_ID = DIV_EQ_ID + 1; // &=
	public static final int OR_EQ_ID = AND_EQ_ID + 1; // |=
	public static final int XOR_EQ_ID = OR_EQ_ID + 1; // ^=
	public static final int MOD_EQ_ID = XOR_EQ_ID + 1; // %=
	public static final int NOT_EQ_ID = MOD_EQ_ID + 1; // !=
	public static final int DOT_ID = NOT_EQ_ID + 1; // .
	public static final int COMMA_ID = DOT_ID + 1; // ,
	public static final int COLON_ID = COMMA_ID + 1; // :
	public static final int SEMICOLON_ID = COLON_ID + 1; // ;
	public static final int LPAREN_ID = SEMICOLON_ID + 1; // (
	public static final int RPAREN_ID = LPAREN_ID + 1; // )
	public static final int LBRACKET_ID = RPAREN_ID + 1; // [
	public static final int RBRACKET_ID = LBRACKET_ID + 1; // ]
	public static final int LBRACE_ID = RBRACKET_ID + 1; // {
	public static final int RBRACE_ID = LBRACE_ID + 1; // }
	public static final int PLUS_PLUS_ID = RBRACE_ID + 1; // ++
	public static final int MINUS_MINUS_ID = PLUS_PLUS_ID + 1; // --
	public static final int AND_AND_ID = MINUS_MINUS_ID + 1; // &&
	public static final int OR_OR_ID = AND_AND_ID + 1; // ||

	// Data type keyword numeric-ids
	public static final int BOOLEAN_ID = OR_OR_ID + 1;
	public static final int BYTE_ID = BOOLEAN_ID + 1;
	public static final int CHAR_ID = BYTE_ID + 1;
	public static final int DOUBLE_ID = CHAR_ID + 1;
	public static final int FLOAT_ID = DOUBLE_ID + 1;
	public static final int INT_ID = FLOAT_ID + 1;
	public static final int LONG_ID = INT_ID + 1;
	public static final int SHORT_ID = LONG_ID + 1;

	// Other keywords numeric-ids
	public static final int AS_ID = SHORT_ID + 1;
	public static final int FROM_ID = SHORT_ID + 2;
	public static final int SELECT_ID = SHORT_ID + 3;
	public static final int CREATE_ID = SHORT_ID + 4;
	public static final int TABLE_ID = SHORT_ID + 5;
	public static final int INSERT_ID = SHORT_ID + 6;
	public static final int UPDATE_ID = SHORT_ID + 7;
	public static final int DELETE_ID = SHORT_ID + 8;
	public static final int INTO_ID = SHORT_ID + 9;
	public static final int WHERE_ID = SHORT_ID + 10;
	public static final int COMMIT_ID = SHORT_ID + 11;
	public static final int ROLLBACK_ID = SHORT_ID + 12;
	public static final int SET_ID = SHORT_ID + 13;
	public static final int VALUES_ID = SHORT_ID + 14;
	public static final int ON_ID = SHORT_ID + 15;
	public static final int FOREIGN_ID = SHORT_ID + 16;
	public static final int PRIMARY_ID = SHORT_ID + 17;
	public static final int NOT_KW_ID = SHORT_ID + 18;
	public static final int NULL_ID = SHORT_ID + 19;
	public static final int IS_ID = SHORT_ID + 20;
	public static final int ALTER_ID = SHORT_ID + 21;
	public static final int ADD_ID = SHORT_ID + 22;
	public static final int DROP_ID = SHORT_ID + 23;
	public static final int CONSTRAINT_ID = SHORT_ID + 24;
	public static final int TO_ID = SHORT_ID + 25;
	public static final int GRANT_ID = SHORT_ID + 26;
	public static final int REVOKE_ID = SHORT_ID + 27;
	public static final int AND_KW_ID = SHORT_ID + 28;
	public static final int OR_KW_ID = SHORT_ID + 29;
	public static final int KEY_ID = SHORT_ID + 30;
	public static final int INDEX_ID = SHORT_ID + 31;

	public static final int LAST_KEYWORD_ID = SHORT_ID + 200;

	// Token-categories
	/** All the keywords belong to this category. */
	public static final BaseTokenCategory KEYWORDS = new BaseTokenCategory("keywords", KEYWORDS_ID);

	public static final BaseImageTokenID AS = registerKeyword("as", AS_ID);
	public static final BaseImageTokenID FROM = registerKeyword("from", FROM_ID);
	public static final BaseImageTokenID SELECT = registerKeyword("select", SELECT_ID);
	public static final BaseImageTokenID CREATE = registerKeyword("create", CREATE_ID);
	public static final BaseImageTokenID TABLE = registerKeyword("table", TABLE_ID);
	public static final BaseImageTokenID INSERT = registerKeyword("insert", INSERT_ID);
	public static final BaseImageTokenID UPDATE = registerKeyword("update", UPDATE_ID);
	public static final BaseImageTokenID DELETE = registerKeyword("delete", DELETE_ID);
	public static final BaseImageTokenID INTO = registerKeyword("into", INTO_ID);
	public static final BaseImageTokenID WHERE = registerKeyword("where", WHERE_ID);
	public static final BaseImageTokenID COMMIT = registerKeyword("commit", COMMIT_ID);
	public static final BaseImageTokenID ROLLBACK = registerKeyword("rollback", ROLLBACK_ID);
	public static final BaseImageTokenID SET = registerKeyword("set", SET_ID);
	public static final BaseImageTokenID VALUES = registerKeyword("values", VALUES_ID);
	public static final BaseImageTokenID ON = registerKeyword("on", ON_ID);
	public static final BaseImageTokenID FOREIGN = registerKeyword("foreign", FOREIGN_ID);
	public static final BaseImageTokenID PRIMARY = registerKeyword("primary", PRIMARY_ID);
	public static final BaseImageTokenID NOT_KW = registerKeyword("not", NOT_KW_ID);
	public static final BaseImageTokenID NULL = registerKeyword("null", NULL_ID);
	public static final BaseImageTokenID IS = registerKeyword("is", IS_ID);
	public static final BaseImageTokenID ALTER = registerKeyword("alter", ALTER_ID);
	public static final BaseImageTokenID ADD = registerKeyword("add", ADD_ID);
	public static final BaseImageTokenID DROP = registerKeyword("drop", DROP_ID);
	public static final BaseImageTokenID CONSTRAINT = registerKeyword("constraint", CONSTRAINT_ID);
	public static final BaseImageTokenID TO = registerKeyword("to", TO_ID);
	public static final BaseImageTokenID GRANT = registerKeyword("grant", GRANT_ID);
	public static final BaseImageTokenID REVOKE = registerKeyword("revoke", REVOKE_ID);
	public static final BaseImageTokenID AND_KW = registerKeyword("and", AND_KW_ID);
	public static final BaseImageTokenID OR_KW = registerKeyword("or", OR_KW_ID);
	public static final BaseImageTokenID KEY = registerKeyword("key", KEY_ID);
	public static final BaseImageTokenID INDEX = registerKeyword("index", INDEX_ID);

	public static BaseImageTokenID registerKeyword(String keyword, int id) {
		BaseImageTokenID token = new BaseImageTokenID(keyword, id, KEYWORDS);
		m_keywords.put(keyword, token);
		return token;
	}

	/**
	 * @return the token id for the given keywords
	 */
	public static TokenID getKeywordToken(String keyword) {
		return (TokenID) m_keywords.get(keyword);
	}

	// Incomplete tokens
	public static final int INCOMPLETE_STRING_LITERAL_ID = LAST_KEYWORD_ID + 1;
	public static final int INCOMPLETE_CHAR_LITERAL_ID = INCOMPLETE_STRING_LITERAL_ID + 1;
	public static final int INCOMPLETE_HEX_LITERAL_ID = INCOMPLETE_CHAR_LITERAL_ID + 1;
	public static final int INVALID_CHAR_ID = INCOMPLETE_HEX_LITERAL_ID + 1;
	public static final int INVALID_OPERATOR_ID = INVALID_CHAR_ID + 1;
	public static final int INVALID_OCTAL_LITERAL_ID = INVALID_OPERATOR_ID + 1;
	public static final int INVALID_COMMENT_END_ID = INVALID_OCTAL_LITERAL_ID + 1;

	/** All the operators belong to this category. */
	public static final BaseTokenCategory OPERATORS = new BaseTokenCategory("operators", OPERATORS_ID);

	/** Question mark category */
	public static final BaseTokenCategory QUESTION_MARK = new BaseTokenCategory("question", QUESTION_ID);
	public static final BaseTokenCategory AT_MARK = new BaseTokenCategory("at", AT_ID);

	/** All the numeric literals belong to this category. */
	public static final BaseTokenCategory NUMERIC_LITERALS = new BaseTokenCategory("numeric-literals",
			NUMERIC_LITERALS_ID);

	/**
	 * All the errorneous constructions and incomplete tokens belong to this
	 * category.
	 */
	public static final BaseTokenCategory ERRORS = new BaseTokenCategory("errors", ERRORS_ID);

	// Token-ids
	public static final BaseTokenID WHITESPACE = new BaseTokenID("whitespace", WHITESPACE_ID);

	public static final BaseTokenID IDENTIFIER = new BaseTokenID("identifier", IDENTIFIER_ID);

	/** Comment with the '//' prefix */
	public static final BaseTokenID LINE_COMMENT = new BaseTokenID("line-comment", LINE_COMMENT_ID);

	/** Block comment */
	public static final BaseTokenID BLOCK_COMMENT = new BaseTokenID("block-comment", BLOCK_COMMENT_ID);

	/** Character literal e.g. 'c' */
	public static final BaseTokenID CHAR_LITERAL = new BaseTokenID("char-literal", CHAR_LITERAL_ID);

	/** SQL string literal e.g. "hello" */
	public static final BaseTokenID STRING_LITERAL = new BaseTokenID("string-literal", STRING_LITERAL_ID);

	/** SQL integer literal e.g. 1234 */
	public static final BaseTokenID INT_LITERAL = new BaseTokenID("int-literal", INT_LITERAL_ID, NUMERIC_LITERALS);

	/** SQL long literal e.g. 12L */
	public static final BaseTokenID LONG_LITERAL = new BaseTokenID("long-literal", LONG_LITERAL_ID, NUMERIC_LITERALS);

	/** SQL hexadecimal literal e.g. 0x5a */
	public static final BaseTokenID HEX_LITERAL = new BaseTokenID("hex-literal", HEX_LITERAL_ID, NUMERIC_LITERALS);

	/** SQL octal literal e.g. 0123 */
	public static final BaseTokenID OCTAL_LITERAL = new BaseTokenID("octal-literal", OCTAL_LITERAL_ID, NUMERIC_LITERALS);

	/** SQL float literal e.g. 1.5e+20f */
	public static final BaseTokenID FLOAT_LITERAL = new BaseTokenID("float-literal", FLOAT_LITERAL_ID, NUMERIC_LITERALS);

	/** SQL double literal e.g. 1.5e+20 */
	public static final BaseTokenID DOUBLE_LITERAL = new BaseTokenID("double-literal", DOUBLE_LITERAL_ID,
			NUMERIC_LITERALS);

	// Operators
	public static final BaseImageTokenID EQ = new BaseImageTokenID("eq", EQ_ID, OPERATORS, "=");

	public static final BaseImageTokenID LT = new BaseImageTokenID("lt", LT_ID, OPERATORS, "<");

	public static final BaseImageTokenID GT = new BaseImageTokenID("gt", GT_ID, OPERATORS, ">");

	public static final BaseImageTokenID PLUS = new BaseImageTokenID("plus", PLUS_ID, OPERATORS, "+");

	public static final BaseImageTokenID MINUS = new BaseImageTokenID("minus", MINUS_ID, OPERATORS, "-");

	public static final BaseImageTokenID MUL = new BaseImageTokenID("mul", MUL_ID, OPERATORS, "*");

	public static final BaseImageTokenID DIV = new BaseImageTokenID("div", DIV_ID, OPERATORS, "/");

	public static final BaseImageTokenID AND = new BaseImageTokenID("and", AND_ID, OPERATORS, "&");

	public static final BaseImageTokenID OR = new BaseImageTokenID("or", OR_ID, OPERATORS, "|");

	public static final BaseImageTokenID XOR = new BaseImageTokenID("xor", XOR_ID, OPERATORS, "^");

	public static final BaseImageTokenID MOD = new BaseImageTokenID("mod", MOD_ID, OPERATORS, "%");

	public static final BaseImageTokenID NOT = new BaseImageTokenID("not", NOT_ID, OPERATORS, "!");

	public static final BaseImageTokenID NEG = new BaseImageTokenID("neg", NEG_ID, OPERATORS, "~");

	public static final BaseImageTokenID EQ_EQ = new BaseImageTokenID("eq-eq", EQ_EQ_ID, OPERATORS, "==");

	public static final BaseImageTokenID LT_EQ = new BaseImageTokenID("le", LT_EQ_ID, OPERATORS, "<=");

	public static final BaseImageTokenID GT_EQ = new BaseImageTokenID("ge", GT_EQ_ID, OPERATORS, ">=");

	public static final BaseImageTokenID NOT_EQ = new BaseImageTokenID("not-eq", NOT_EQ_ID, OPERATORS, "!=");

	public static final BaseImageTokenID DOT = new BaseImageTokenID("dot", DOT_ID, OPERATORS, ".");

	public static final BaseImageTokenID COMMA = new BaseImageTokenID("comma", COMMA_ID, OPERATORS, ",");

	public static final BaseImageTokenID COLON = new BaseImageTokenID("colon", COLON_ID, OPERATORS, ":");

	public static final BaseImageTokenID SEMICOLON = new BaseImageTokenID("semicolon", SEMICOLON_ID, OPERATORS, ";");

	public static final BaseImageTokenID LPAREN = new BaseImageTokenID("lparen", LPAREN_ID, OPERATORS, "(");

	public static final BaseImageTokenID RPAREN = new BaseImageTokenID("rparen", RPAREN_ID, OPERATORS, ")");

	public static final BaseImageTokenID LBRACKET = new BaseImageTokenID("lbracket", LBRACKET_ID, OPERATORS, "[");

	public static final BaseImageTokenID RBRACKET = new BaseImageTokenID("rbracket", RBRACKET_ID, OPERATORS, "]");

	public static final BaseImageTokenID LBRACE = new BaseImageTokenID("lbrace", LBRACE_ID, OPERATORS, "{");

	public static final BaseImageTokenID RBRACE = new BaseImageTokenID("rbrace", RBRACE_ID, OPERATORS, "}");

	public static final BaseImageTokenID QUESTION = new BaseImageTokenID("question", QUESTION_ID, QUESTION_MARK, "?");
	public static final BaseImageTokenID AT = new BaseImageTokenID("at", AT_ID, AT_MARK, "@");

	// Data types

	// Incomplete and error token-ids
	public static final BaseTokenID INCOMPLETE_STRING_LITERAL = new BaseTokenID("incomplete-string-literal",
			INCOMPLETE_STRING_LITERAL_ID, ERRORS);

	public static final BaseTokenID INCOMPLETE_CHAR_LITERAL = new BaseTokenID("incomplete-char-literal",
			INCOMPLETE_CHAR_LITERAL_ID, ERRORS);

	public static final BaseTokenID INCOMPLETE_HEX_LITERAL = new BaseTokenID("incomplete-hex-literal",
			INCOMPLETE_HEX_LITERAL_ID, ERRORS);

	public static final BaseTokenID INVALID_CHAR = new BaseTokenID("invalid-char", INVALID_CHAR_ID, ERRORS);

	public static final BaseTokenID INVALID_OPERATOR = new BaseTokenID("invalid-operator", INVALID_OPERATOR_ID, ERRORS);

	public static final BaseTokenID INVALID_OCTAL_LITERAL = new BaseTokenID("invalid-octal-literal",
			INVALID_OCTAL_LITERAL_ID, ERRORS);

	public static final BaseTokenID INVALID_COMMENT_END = new BaseTokenID("invalid-comment-end",
			INVALID_COMMENT_END_ID, ERRORS);

	// Context instance declaration
	public static final SQLTokenContext context = new SQLTokenContext();

	public static final TokenContextPath contextPath = context.getContextPath();

	private SQLTokenContext() {
		super("sql-");

		try {
			addDeclaredTokenIDs();
		} catch (Exception e) {
			if (Boolean.getBoolean("netbeans.debug.exceptions")) {
				e.printStackTrace();
			}
		}
	}

}
