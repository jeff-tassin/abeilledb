package com.jeta.abeille.gui.sql;

import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.Font;
import java.awt.Color;
import javax.swing.KeyStroke;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import org.netbeans.editor.Acceptor;
import org.netbeans.editor.AcceptorFactory;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsDefaults;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.TokenCategory;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.MultiKeyBinding;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.editor.ext.ExtSettingsDefaults;

/**
 * Default settings values for SQL
 * 
 * @author Jeff Tassin
 */

public class SQLSettingsDefaults extends ExtSettingsDefaults {

	public static final Boolean defaultCaretSimpleMatchBrace = Boolean.FALSE;
	public static final Boolean defaultHighlightMatchingBracket = Boolean.TRUE;

	public static final Acceptor defaultIdentifierAcceptor = AcceptorFactory.JAVA_IDENTIFIER;
	public static final Acceptor defaultAbbrevResetAcceptor = AcceptorFactory.NON_JAVA_IDENTIFIER;
	public static final Boolean defaultWordMatchMatchCase = Boolean.TRUE;

	// Formatting
	public static final Boolean defaultSQLFormatSpaceBeforeParenthesis = Boolean.FALSE;
	public static final Boolean defaultSQLFormatSpaceAfterComma = Boolean.TRUE;
	public static final Boolean defaultSQLFormatNewlineBeforeBrace = Boolean.FALSE;
	public static final Boolean defaultSQLFormatLeadingSpaceInComment = Boolean.FALSE;
	public static final Boolean defaultSQLFormatLeadingStarInComment = Boolean.TRUE;

	/** @deprecated */
	public static final Boolean defaultFormatSpaceBeforeParenthesis = defaultSQLFormatSpaceBeforeParenthesis;
	/** @deprecated */
	public static final Boolean defaultFormatSpaceAfterComma = defaultSQLFormatSpaceAfterComma;
	/** @deprecated */
	public static final Boolean defaultFormatNewlineBeforeBrace = defaultSQLFormatNewlineBeforeBrace;
	/** @deprecated */
	public static final Boolean defaultFormatLeadingSpaceInComment = defaultSQLFormatLeadingSpaceInComment;

	public static final Acceptor defaultIndentHotCharsAcceptor = new Acceptor() {
		public boolean accept(char ch) {
			switch (ch) {
			case '{':
			case '}':
				return true;
			}

			return false;
		}
	};

	public static final String defaultWordMatchStaticWords = "";

	// = "Exception IntrospectionException FileNotFoundException IOException" //
	// NOI18N
	// +
	// " ArrayIndexOutOfBoundsException ClassCastException ClassNotFoundException"
	// // NOI18N
	// +
	// " CloneNotSupportedException NullPointerException NumberFormatException"
	// // NOI18N
	// + " SQLException IllegalAccessException IllegalArgumentException"; //
	// NOI18N

	public static Map getSQLAbbrevMap() {
		Map sqlAbbrevMap = new TreeMap();
		// sqlAbbrevMap.put("se", "select "); // NOI18N
		// sqlAbbrevMap.put("fr", "from "); // NOI18N
		// sqlAbbrevMap.put("wh", "where "); // NOI18N
		// sqlAbbrevMap.put("in", "insert"); // NOI18N
		// sqlAbbrevMap.put("up", "update"); // NOI18N
		return sqlAbbrevMap;
	}

	public static MultiKeyBinding[] getSQLKeyBindings() {
		return new MultiKeyBinding[] {
				new MultiKeyBinding(new KeyStroke[] { KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK),
						KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0) }, "macro-cast-to-String"),
				new MultiKeyBinding(new KeyStroke[] { KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK),
						KeyStroke.getKeyStroke(KeyEvent.VK_D, 0) }, "macro-debug-var"),
				new MultiKeyBinding(new KeyStroke[] { KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK),
						KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0) }, "macro-quote-word"),

		};
	}

	public static Map getSQLMacroMap() {
		Map javaMacroMap = new HashMap();
		javaMacroMap.put("cast-to-String", "cut-to-clipboard \"((String)\" paste-from-clipboard \").\"");
		javaMacroMap.put("quote-word", "caret-begin-word \"\\\"\" caret-end-word \"\\\"\"");
		javaMacroMap.put("debug-var", "select-identifier copy-to-clipboard "
				+ "caret-up caret-end-line insert-break \"System.err.println(\\\"\""
				+ "paste-from-clipboard \" = \\\" + \" paste-from-clipboard \" );");

		return javaMacroMap;
	}

	static class SQLTokenColoringInitializer extends SettingsUtil.TokenColoringInitializer {

		Font boldFont = SettingsDefaults.defaultFont.deriveFont(Font.BOLD);
		Font italicFont = SettingsDefaults.defaultFont.deriveFont(Font.ITALIC);
		Settings.Evaluator boldSubst = new SettingsUtil.FontStylePrintColoringEvaluator(Font.BOLD);
		Settings.Evaluator italicSubst = new SettingsUtil.FontStylePrintColoringEvaluator(Font.ITALIC);
		Settings.Evaluator lightGraySubst = new SettingsUtil.ForeColorPrintColoringEvaluator(Color.lightGray);

		Coloring commentColoring = new Coloring(null, Coloring.FONT_MODE_APPLY_STYLE, new Color(0, 92, 0), null);
		Coloring numbersColoring = new Coloring(null, Color.red.darker(), null);

		public SQLTokenColoringInitializer() {
			super(SQLTokenContext.context);
		}

		public Object getTokenColoring(TokenContextPath tokenContextPath, TokenCategory tokenIDOrCategory,
				boolean printingSet) {
			if (!printingSet) {
				switch (tokenIDOrCategory.getNumericID()) {
				case SQLTokenContext.WHITESPACE_ID:
				case SQLTokenContext.IDENTIFIER_ID:
				case SQLTokenContext.OPERATORS_ID:
					return SettingsDefaults.emptyColoring;

				case SQLTokenContext.ERRORS_ID:
					return new Coloring(null, Color.white, Color.red);

					// case SQLTokenContext.FROM_ID:
					// return new Coloring(null, Coloring.FONT_MODE_APPLY_STYLE,
					// Color.black, null);

				case SQLTokenContext.KEYWORDS_ID:
					return new Coloring(boldFont, Coloring.FONT_MODE_APPLY_STYLE, Color.black, null);

				case SQLTokenContext.QUESTION_ID:
				case SQLTokenContext.AT_ID:
					return new Coloring(boldFont, Coloring.FONT_MODE_APPLY_STYLE, Color.red, null);

				case SQLTokenContext.LINE_COMMENT_ID:
				case SQLTokenContext.BLOCK_COMMENT_ID:
					return commentColoring;

				case SQLTokenContext.CHAR_LITERAL_ID:
					return new Coloring(null, Color.blue, null);

				case SQLTokenContext.STRING_LITERAL_ID:
					return new Coloring(null, Color.blue, null);

				case SQLTokenContext.NUMERIC_LITERALS_ID:
					return numbersColoring;

				}

			} else { // printing set
				switch (tokenIDOrCategory.getNumericID()) {
				case SQLTokenContext.LINE_COMMENT_ID:
				case SQLTokenContext.BLOCK_COMMENT_ID:
					return lightGraySubst; // print fore color will be gray

				default:
					return SettingsUtil.defaultPrintColoringEvaluator;
				}

			}

			return null;

		}

	}

	static class SQLLayerTokenColoringInitializer extends SettingsUtil.TokenColoringInitializer {
		Font boldFont = SettingsDefaults.defaultFont.deriveFont(Font.BOLD);
		Settings.Evaluator italicSubst = new SettingsUtil.FontStylePrintColoringEvaluator(Font.ITALIC);

		public SQLLayerTokenColoringInitializer() {
			super(SQLLayerTokenContext.context);
		}

		public Object getTokenColoring(TokenContextPath tokenContextPath, TokenCategory tokenIDOrCategory,
				boolean printingSet) {
			if (!printingSet) {
				switch (tokenIDOrCategory.getNumericID()) {
				case SQLLayerTokenContext.METHOD_ID:
					return new Coloring(boldFont, Coloring.FONT_MODE_APPLY_STYLE, null, null);
				}

			} else {
				// printing set
				switch (tokenIDOrCategory.getNumericID()) {
				case SQLLayerTokenContext.METHOD_ID:
					return italicSubst;

				default:
					return SettingsUtil.defaultPrintColoringEvaluator;
				}

			}
			return null;
		}
	}

}
