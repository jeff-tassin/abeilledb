package com.jeta.abeille.gui.sql;

import java.util.List;
import java.util.HashMap;
import javax.swing.text.BadLocationException;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseDocumentEvent;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.DrawContext;
import org.netbeans.editor.DrawLayer;
import org.netbeans.editor.MarkFactory;
import org.netbeans.editor.StringMap;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.FinderFactory;
import org.netbeans.editor.Analyzer;

/**
 * Various sql-layers
 * 
 * @author Jeff Tassin
 */

public class SQLDrawLayerFactory {

	public static final String JAVA_LAYER_NAME = "java-layer";

	public static final int JAVA_LAYER_VISIBILITY = 1010;

	/**
	 * Layer that colors extra java information like the methods or special
	 * characters in the character and string literals.
	 */
	public static class SQLLayer extends DrawLayer.AbstractLayer {

		/**
		 * End of the area that is resolved right now. It saves repetitive
		 * searches for '(' for multiple fragments inside one identifier token.
		 */
		private int resolvedEndOffset;

		private boolean resolvedValue;

		private NonWhitespaceFwdFinder nwFinder = new NonWhitespaceFwdFinder();

		public SQLLayer() {
			super(JAVA_LAYER_NAME);
		}

		public void init(DrawContext ctx) {
			resolvedEndOffset = 0; // nothing resolved
		}

		public boolean isActive(DrawContext ctx, MarkFactory.DrawMark mark) {
			int nextOffset = ctx.getTokenOffset() + ctx.getTokenLength();

			setNextActivityChangeOffset(nextOffset);
			return true;
		}

		protected Coloring getMethodColoring(DrawContext ctx) {
			TokenContextPath path = ctx.getTokenContextPath().replaceStart(SQLLayerTokenContext.contextPath);
			return ctx.getEditorUI().getColoring(path.getFullTokenName(SQLLayerTokenContext.METHOD));
		}

		private boolean isMethod(DrawContext ctx) {
			int idEndOffset = ctx.getTokenOffset() + ctx.getTokenLength();
			if (idEndOffset > resolvedEndOffset) { // beyond the resolved area
				resolvedEndOffset = idEndOffset; // will resolve now
				int endOffset = ctx.getEndOffset();
				int bufferStartOffset = ctx.getBufferStartOffset();
				char[] buffer = ctx.getBuffer();
				int nwOffset = Analyzer.findFirstNonWhite(buffer, idEndOffset - bufferStartOffset, endOffset
						- idEndOffset);
				if (nwOffset >= 0) { // found non-white
					resolvedValue = (buffer[nwOffset] == '(');

				} else { // must resolve after buffer end
					try {
						resolvedValue = (ctx.getEditorUI().getDocument().find(nwFinder, endOffset, -1) >= 0)
								&& (nwFinder.getFoundChar() == '(');
					} catch (BadLocationException e) {
						resolvedValue = false;
					}
				}
			}

			return resolvedValue;
		}

		public void updateContext(DrawContext ctx) {
			if (ctx.getTokenID() == SQLTokenContext.IDENTIFIER && isMethod(ctx)) {
				Coloring mc = getMethodColoring(ctx);
				if (mc != null) {
					mc.apply(ctx);
				}
			}
		}

	}

	/** Find first non-white character forward */
	static class NonWhitespaceFwdFinder extends FinderFactory.GenericFwdFinder {

		private char foundChar;

		public char getFoundChar() {
			return foundChar;
		}

		protected int scan(char ch, boolean lastChar) {
			if (!Character.isWhitespace(ch)) {
				found = true;
				foundChar = ch;
				return 0;
			}
			return 1;
		}
	}

	/** Find first non-white character backward */
	public static class NonWhitespaceBwdFinder extends FinderFactory.GenericBwdFinder {

		private char foundChar;

		public char getFoundChar() {
			return foundChar;
		}

		protected int scan(char ch, boolean lastChar) {
			if (!Character.isWhitespace(ch)) {
				found = true;
				foundChar = ch;
				return 0;
			}
			return -1;
		}
	}

	/**
	 * This class watches whether the '(' character was inserted/removed. It
	 * ensures the appropriate part of the document till the previous
	 * non-whitespace will be repainted.
	 */
	public static class LParenWatcher implements DocumentListener {

		NonWhitespaceBwdFinder nwFinder = new NonWhitespaceBwdFinder();

		private void check(DocumentEvent evt) {
			if (evt.getDocument() instanceof BaseDocument) {
				BaseDocument doc = (BaseDocument) evt.getDocument();
				BaseDocumentEvent bevt = (BaseDocumentEvent) evt;
				char[] chars = bevt.getChars();
				if (chars != null) {
					boolean found = false;
					for (int i = chars.length - 1; i >= 0; i--) {
						if (chars[i] == '(') {
							found = true;
							break;
						}
					}

					if (found) {
						int offset = evt.getOffset();
						// Need to repaint
						int redrawOffset = 0;
						if (offset > 0) {
							try {
								redrawOffset = doc.find(nwFinder, offset - 1, 0);
							} catch (BadLocationException e) {
							}

							if (redrawOffset < 0) { // not found non-whitespace
								redrawOffset = 0;
							}
						}
						doc.repaintBlock(redrawOffset, offset);
					}
				}
			}
		}

		public void insertUpdate(DocumentEvent evt) {
			check(evt);
		}

		public void removeUpdate(DocumentEvent evt) {
			check(evt);
		}

		public void changedUpdate(DocumentEvent evt) {
		}

	}

}
