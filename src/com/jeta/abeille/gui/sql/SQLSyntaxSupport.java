package com.jeta.abeille.gui.sql;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.swing.text.BadLocationException;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.AcceptorFactory;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.TextBatchProcessor;
import org.netbeans.editor.FinderFactory;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.Analyzer;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.ext.ExtSyntaxSupport;

import org.netbeans.editor.ext.java.*;

/**
 * Support methods for syntax analysis
 * 
 * @author Jeff Tassin
 */

public class SQLSyntaxSupport extends ExtSyntaxSupport {

	// Internal java declaration token processor states
	static final int INIT = 0;
	static final int AFTER_TYPE = 1;
	static final int AFTER_VARIABLE = 2;
	static final int AFTER_COMMA = 3;
	static final int AFTER_DOT = 4;
	static final int AFTER_TYPE_LSB = 5;
	static final int AFTER_MATCHING_VARIABLE_LSB = 6;
	static final int AFTER_MATCHING_VARIABLE = 7;

	private static final TokenID[] COMMENT_TOKENS = new TokenID[] { SQLTokenContext.LINE_COMMENT,
			SQLTokenContext.BLOCK_COMMENT };

	private static final TokenID[] BRACKET_SKIP_TOKENS = new TokenID[] { SQLTokenContext.LINE_COMMENT,
			SQLTokenContext.BLOCK_COMMENT, SQLTokenContext.CHAR_LITERAL, SQLTokenContext.STRING_LITERAL };

	private static final char[] COMMAND_SEPARATOR_CHARS = new char[] { ';' };

	public SQLSyntaxSupport(BaseDocument doc) {
		super(doc);
		tokenNumericIDsValid = true;
	}

	protected void documentModified(DocumentEvent evt) {
		super.documentModified(evt);
	}

	public TokenID[] getCommentTokens() {
		return COMMENT_TOKENS;
	}

	public TokenID[] getBracketSkipTokens() {
		return BRACKET_SKIP_TOKENS;
	}

	/**
	 * Return the position of the last command separator before the given
	 * position. For now, the only command separator we recognize is a semicolon
	 */
	public int getLastCommandSeparator(int pos) throws BadLocationException {
		TextBatchProcessor tbp = new TextBatchProcessor() {
			public int processTextBatch(BaseDocument doc, int startPos, int endPos, boolean lastBatch) {
				try {
					int[] blks = getCommentBlocks(endPos, startPos);
					FinderFactory.CharArrayBwdFinder cmdFinder = new FinderFactory.CharArrayBwdFinder(
							COMMAND_SEPARATOR_CHARS);
					return findOutsideBlocks(cmdFinder, startPos, endPos, blks);
				} catch (BadLocationException e) {
					e.printStackTrace();
					return -1;
				}
			}
		};
		return getDocument().processText(tbp, pos, 0);
	}

	/**
	 * Check and possibly popup, hide or refresh the completion This gets called
	 * byte the DefaultKeyTyped handler when the user types something. We check
	 * if the user typed a character that can cause a popup. In this case it is
	 * always a period.
	 */
	public int checkCompletion(JTextComponent target, String typedText, boolean visible) {
		if (!visible) {
			// popup not visible yet
			switch (typedText.charAt(0)) {
			case '.':
				return ExtSyntaxSupport.COMPLETION_POPUP;
			}
			return ExtSyntaxSupport.COMPLETION_CANCEL;
		} else {
			// the pane is already visible
			switch (typedText.charAt(0)) {
			case '=':
			case '{':
			case ';':
				return ExtSyntaxSupport.COMPLETION_HIDE;
			default:
				return ExtSyntaxSupport.COMPLETION_POPUP;
				// return ExtSyntaxSupport.COMPLETION_POST_REFRESH;
			}
		}
	}

}
