/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

import java.util.Map;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;

import org.netbeans.editor.*;
import org.netbeans.editor.ext.*;

import com.jeta.foundation.utils.TSUtils;
import com.jeta.foundation.gui.components.TSComponentUtils;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.WindowDelegate;

import com.jeta.foundation.gui.editor.macros.Macro;

import com.jeta.foundation.i18n.I18N;

import com.jeta.open.gui.framework.JETAContainer;
import com.jeta.open.gui.framework.JETAController;

/**
 * Base class for all application editor kits
 * 
 * @author Jeff Tassin
 */
public class TSKit extends ExtKit {
	static final long serialVersionUID = -8570495408376659348L;

	private static com.jeta.foundation.gui.editor.FindDialogSupport m_findsupport = null;

	static {
		Settings.addInitializer(new TSKitSettingsInitializer());
	}

	public TSKit() {

	}

	/** Create caret to navigate through document */
	public Caret createCaret() {
		return new EditorCaret();
	}

	/**
	 * Get the default bindings. We are using a different method for
	 * initializing default bindings than the netbeans callback method.
	 */
	public static MultiKeyBinding[] listDefaultKeyBindings() {
		MultiKeyBinding[] tsbindings = new MultiKeyBinding[] {
				new MultiKeyBinding((KeyStroke) null, // this assigns the
														// default action to
														// keymap
						BaseKit.defaultKeyTypedAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), BaseKit.insertBreakAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), BaseKit.insertTabAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK),
						BaseKit.removeTabAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), BaseKit.deletePrevCharAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.SHIFT_MASK),
						BaseKit.deletePrevCharAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), BaseKit.deleteNextCharAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), BaseKit.forwardAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, 0), // keypad
																						// right
						BaseKit.forwardAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK),
						BaseKit.selectionForwardAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK),
						BaseKit.nextWordAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK
						| InputEvent.CTRL_MASK), BaseKit.selectionNextWordAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), BaseKit.backwardAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, 0), // keypad
																					// left
						BaseKit.backwardAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK),
						BaseKit.selectionBackwardAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_MASK),
						BaseKit.previousWordAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK
						| InputEvent.CTRL_MASK), BaseKit.selectionPreviousWordAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), BaseKit.downAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0), // keypad
																					// down
						BaseKit.downAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_MASK),
						BaseKit.selectionDownAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK),
						BaseKit.scrollUpAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), BaseKit.upAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0), // keypad
																					// up
						BaseKit.upAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_MASK),
						BaseKit.selectionUpAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK),
						BaseKit.scrollDownAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), BaseKit.pageDownAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, InputEvent.SHIFT_MASK),
						BaseKit.selectionPageDownAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), BaseKit.pageUpAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, InputEvent.SHIFT_MASK),
						BaseKit.selectionPageUpAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), BaseKit.beginLineAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.SHIFT_MASK),
						BaseKit.selectionBeginLineAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.CTRL_MASK), BaseKit.beginAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.SHIFT_MASK
						| InputEvent.CTRL_MASK), BaseKit.selectionBeginAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0), BaseKit.endLineAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_END, InputEvent.SHIFT_MASK),
						BaseKit.selectionEndLineAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_END, InputEvent.CTRL_MASK), BaseKit.endAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_END, InputEvent.SHIFT_MASK
						| InputEvent.CTRL_MASK), BaseKit.selectionEndAction),

				// clipboard bindings
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK), BaseKit.copyAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK), BaseKit.cutAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK), BaseKit.pasteAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.CTRL_MASK),
						BaseKit.copyAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.SHIFT_MASK),
						BaseKit.cutAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.SHIFT_MASK),
						BaseKit.pasteAction),

				// undo and redo bindings - handled at system level
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK), BaseKit.undoAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK), BaseKit.redoAction),

				// other bindings
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK),
						BaseKit.selectAllAction),
				new MultiKeyBinding(new KeyStroke[] { KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_MASK),
						KeyStroke.getKeyStroke(KeyEvent.VK_E, 0), }, BaseKit.endWordAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK),
						BaseKit.removeWordAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_MASK),
						BaseKit.removeLineBeginAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK),
						BaseKit.removeLineAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), BaseKit.toggleTypingModeAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_F2, InputEvent.CTRL_MASK),
						BaseKit.toggleBookmarkAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), BaseKit.gotoNextBookmarkAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), BaseKit.findNextAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_F3, InputEvent.SHIFT_MASK),
						BaseKit.findPreviousAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_F3, InputEvent.CTRL_MASK),
						BaseKit.findSelectionAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK),
						BaseKit.toggleHighlightSearchAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK),
						BaseKit.wordMatchNextAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_MASK),
						BaseKit.wordMatchPrevAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.SHIFT_MASK),
						BaseKit.abbrevResetAction),
				new MultiKeyBinding(new KeyStroke[] { KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_MASK),
						KeyStroke.getKeyStroke(KeyEvent.VK_T, 0), }, BaseKit.adjustWindowTopAction),
				new MultiKeyBinding(new KeyStroke[] { KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_MASK),
						KeyStroke.getKeyStroke(KeyEvent.VK_M, 0), }, BaseKit.adjustWindowCenterAction),
				new MultiKeyBinding(new KeyStroke[] { KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_MASK),
						KeyStroke.getKeyStroke(KeyEvent.VK_B, 0), }, BaseKit.adjustWindowBottomAction),

				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK),
						BaseKit.adjustCaretTopAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK),
						BaseKit.adjustCaretCenterAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK),
						BaseKit.adjustCaretBottomAction),
				new MultiKeyBinding(new KeyStroke[] { KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_MASK),
						KeyStroke.getKeyStroke(KeyEvent.VK_U, 0), }, BaseKit.toUpperCaseAction),
				new MultiKeyBinding(new KeyStroke[] { KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_MASK),
						KeyStroke.getKeyStroke(KeyEvent.VK_L, 0), }, BaseKit.toLowerCaseAction),
				new MultiKeyBinding(new KeyStroke[] { KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_MASK),
						KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), }, BaseKit.switchCaseAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK), ExtKit.findAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_MASK), ExtKit.replaceAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK), gotoAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, InputEvent.CTRL_MASK),
						ExtKit.matchBraceAction),
				new MultiKeyBinding(
						KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK),
						ExtKit.selectionMatchBraceAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK),
						ExtKit.shiftInsertBreakAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), ExtKit.escapeAction)

		};

		// MultiKeyBinding[] basebindings =
		// org.netbeans.editor.SettingsDefaults.defaultKeyBindings;
		// MultiKeyBinding[] extbindings =
		// org.netbeans.editor.ext.ExtSettingsDefaults.defaultExtKeyBindings;
		// MultiKeyBinding[] result = new MultiKeyBinding[tsbindings.length +
		// extbindings.length + basebindings.length];

		// System.arraycopy( basebindings, 0, result, 0, basebindings.length );
		// System.arraycopy( extbindings, 0, result, basebindings.length,
		// extbindings.length );
		// System.arraycopy( tsbindings, 0, result, basebindings.length +
		// extbindings.length, tsbindings.length );
		return tsbindings;
	}

	/**
	 * This lists all actions that can be applied to a text field. The
	 * JEditorPane is a super set of these
	 */
	public static Action[] listBasicActions() {
		Action[] tsactions = new Action[] { new CutAction(), new CopyAction(), new PasteAction(),
				new ForwardAction(forwardAction, false), new ForwardAction(selectionForwardAction, true),
				new BackwardAction(backwardAction, false), new BackwardAction(selectionBackwardAction, true),
				new BeginLineAction(beginLineAction, false), new BeginLineAction(selectionBeginLineAction, true),
				new EndLineAction(endLineAction, false), new EndLineAction(selectionEndLineAction, true),
				new BeginAction(beginAction, false), new BeginAction(selectionBeginAction, true),
				new EndAction(endAction, false), new EndAction(selectionEndAction, true),
				new NextWordAction(nextWordAction, false), new NextWordAction(selectionNextWordAction, true),
				new PreviousWordAction(previousWordAction, false),
				new PreviousWordAction(selectionPreviousWordAction, true), new BeginWordAction(beginWordAction, false),
				new BeginWordAction(selectionBeginWordAction, true), new EndWordAction(endWordAction, false),
				new EndWordAction(selectionEndWordAction, true), new SelectWordAction(), new SelectLineAction(),
				new SetMarkAction(), new TSEscapeAction(), new TSGotoAction() };
		return tsactions;
	}

	/**
	 * List all actions supported by this class.
	 */
	public static Action[] listDefaultActions() {
		Action[] tsactions = new Action[] {
				new ExtKit.ExtDefaultKeyTypedAction(),
				new ExtKit.InsertContentAction(),
				new ExtKit.ExtInsertBreakAction(), // need for completion
				new ExtKit.ExtInsertTabAction(), // need this guy for completion
				new FindAction(), new ReplaceAction(), new ExtKit.DeleteCharAction(deletePrevCharAction, false),
				new ExtKit.DeleteCharAction(deleteNextCharAction, true), new ExtKit.ReadOnlyAction(),
				new ExtKit.WritableAction(), new ExtKit.BeepAction(), new UpAction(upAction, false),
				new UpAction(selectionUpAction, true), new PageUpAction(pageUpAction, false),
				new PageUpAction(selectionPageUpAction, true), new DownAction(downAction, false),
				new DownAction(selectionDownAction, true), new PageDownAction(selectionPageDownAction, true),
				new PageDownAction(pageDownAction, false), new ExtKit.SelectAllAction(),
				new ActionFactory.RemoveTabAction(), new ActionFactory.RemoveWordAction(),
				new ActionFactory.RemoveLineBeginAction(), new ActionFactory.RemoveLineAction(),
				new ActionFactory.RemoveSelectionAction(), new ActionFactory.ToggleTypingModeAction(),
				new ActionFactory.ToggleBookmarkAction(),
				new ActionFactory.GotoNextBookmarkAction(gotoNextBookmarkAction, false),
				new ActionFactory.AbbrevExpandAction(), new ActionFactory.AbbrevResetAction(),
				new ActionFactory.ChangeCaseAction(toUpperCaseAction, Utilities.CASE_UPPER),
				new ActionFactory.ChangeCaseAction(toLowerCaseAction, Utilities.CASE_LOWER),
				new ActionFactory.ChangeCaseAction(switchCaseAction, Utilities.CASE_SWITCH),
				new ActionFactory.FindNextAction(), new ActionFactory.FindPreviousAction(),
				new ActionFactory.FindSelectionAction(), new ActionFactory.ToggleHighlightSearchAction(),
				new ActionFactory.UndoAction(), new ActionFactory.RedoAction(),
				new ActionFactory.WordMatchAction(wordMatchNextAction, true),
				new ActionFactory.WordMatchAction(wordMatchPrevAction, false),
				new ActionFactory.ShiftLineAction(shiftLineLeftAction, false),
				new ActionFactory.ShiftLineAction(shiftLineRightAction, true),
				new ActionFactory.AdjustWindowAction(adjustWindowTopAction, 0),
				new ActionFactory.AdjustWindowAction(adjustWindowCenterAction, 50),
				new ActionFactory.AdjustWindowAction(adjustWindowBottomAction, 100),
				new ActionFactory.AdjustCaretAction(adjustCaretTopAction, 0),
				new ActionFactory.AdjustCaretAction(adjustCaretCenterAction, 50),
				new ActionFactory.AdjustCaretAction(adjustCaretBottomAction, 100), new ActionFactory.FormatAction(),
				new ActionFactory.FirstNonWhiteAction(firstNonWhiteAction, false),
				new ActionFactory.FirstNonWhiteAction(selectionFirstNonWhiteAction, true),
				new ActionFactory.LastNonWhiteAction(lastNonWhiteAction, false),
				new ActionFactory.LastNonWhiteAction(selectionLastNonWhiteAction, true),
				new ActionFactory.SelectIdentifierAction(), new ActionFactory.SelectNextParameterAction(),
				new ActionFactory.JumpListPrevAction(), new ActionFactory.JumpListNextAction(),
				new ActionFactory.JumpListPrevComponentAction(), new ActionFactory.JumpListNextComponentAction(),
				new ActionFactory.ScrollUpAction(), new ActionFactory.ScrollDownAction() };

		Action[] baseactions = listBasicActions();
		Action[] result = new Action[baseactions.length + tsactions.length];
		System.arraycopy(baseactions, 0, result, 0, baseactions.length);
		System.arraycopy(tsactions, 0, result, baseactions.length, tsactions.length);
		return result;
	}

	/**
	 * @return the list default macros supported by this kit
	 */
	public static Macro[] listDefaultMacros() {
		Macro[] macros = new Macro[] { new Macro("quote-word", "caret-begin-word \"\\\"\" caret-end-word \"\\\"\"") };

		return macros;
	}

	public static final String genericAction = "generic-action"; // NOI18N
	/** Move one page up and make or extend selection */
	public static final String selectionPageUpAction = "selection-page-up"; // NOI18N

	/** Move one page down and make or extend selection */
	public static final String selectionPageDownAction = "selection-page-down"; // NOI18N

	/** Remove indentation */
	public static final String removeTabAction = "remove-tab"; // NOI18N

	/** Remove selected block or do nothing - useful for popup menu */
	public static final String removeSelectionAction = "remove-selection"; // NOI18N

	/** Toggle bookmark on the current line */
	public static final String toggleBookmarkAction = "bookmark-toggle"; // NOI18N

	/** Goto the next bookmark */
	public static final String gotoNextBookmarkAction = "bookmark-next"; // NOI18N

	/** Expand the abbreviation */
	public static final String abbrevExpandAction = "abbrev-expand"; // NOI18N

	/** Reset the abbreviation accounting string */
	public static final String abbrevResetAction = "abbrev-reset"; // NOI18N

	/** Remove to the begining of the word */
	public static final String removeWordAction = "remove-word"; // NOI18N

	/** Remove to the begining of the line */
	public static final String removeLineBeginAction = "remove-line-begin"; // NOI18N

	/** Remove line */
	public static final String removeLineAction = "remove-line"; // NOI18N

	/** Toggle the typing mode to overwrite mode or back to insert mode */
	public static final String toggleTypingModeAction = "toggle-typing-mode"; // NOI18N

	/** Change the selected text or current character to uppercase */
	public static final String toUpperCaseAction = "to-upper-case"; // NOI18N

	/** Change the selected text or current character to lowercase */
	public static final String toLowerCaseAction = "to-lower-case"; // NOI18N

	/** Switch the case of the selected text or current character */
	public static final String switchCaseAction = "switch-case"; // NOI18N

	/** Find next occurence action */
	public static final String findNextAction = "find-next"; // NOI18N

	/** Find previous occurence action */
	public static final String findPreviousAction = "find-previous"; // NOI18N

	/** Toggle highlight search action */
	public static final String toggleHighlightSearchAction = "toggle-highlight-search"; // NOI18N

	/** Find current word */
	public static final String findSelectionAction = "find-selection"; // NOI18N

	/** goto line number */
	public static final String gotoAction = "goto";

	/** Undo action */
	public static final String undoAction = "undo"; // NOI18N

	/** Redo action */
	public static final String redoAction = "redo"; // NOI18N

	/** Word match next */
	public static final String wordMatchNextAction = "word-match-next"; // NOI18N

	/** Word match prev */
	public static final String wordMatchPrevAction = "word-match-prev"; // NOI18N

	/** Shift line right action */
	public static final String shiftLineRightAction = "shift-line-right"; // NOI18N

	/** Shift line left action */
	public static final String shiftLineLeftAction = "shift-line-left"; // NOI18N

	/**
	 * Action that scrolls the window so that caret is at the center of the
	 * window
	 */
	public static final String adjustWindowCenterAction = "adjust-window-center"; // NOI18N

	/** Action that scrolls the window so that caret is at the top of the window */
	public static final String adjustWindowTopAction = "adjust-window-top"; // NOI18N

	/**
	 * Action that scrolls the window so that caret is at the bottom of the
	 * window
	 */
	public static final String adjustWindowBottomAction = "adjust-window-bottom"; // NOI18N

	/** Action that moves the caret so that caret is at the center of the window */
	public static final String adjustCaretCenterAction = "adjust-caret-center"; // NOI18N

	/** Action that moves the caret so that caret is at the top of the window */
	public static final String adjustCaretTopAction = "adjust-caret-top"; // NOI18N

	/** Action that moves the caret so that caret is at the bottom of the window */
	public static final String adjustCaretBottomAction = "adjust-caret-bottom"; // NOI18N

	/** Format part of the document text using Indent */
	public static final String formatAction = "format"; // NOI18N

	/** First non-white character on the line */
	public static final String firstNonWhiteAction = "first-non-white"; // NOI18N

	/** Last non-white character on the line */
	public static final String lastNonWhiteAction = "last-non-white"; // NOI18N

	/** First non-white character on the line */
	public static final String selectionFirstNonWhiteAction = "selection-first-non-white"; // NOI18N

	/** Last non-white character on the line */
	public static final String selectionLastNonWhiteAction = "selection-last-non-white"; // NOI18N

	/** Select the nearest identifier around caret */
	public static final String selectIdentifierAction = "select-identifier"; // NOI18N

	/** Select the next parameter (after the comma) in the given context */
	public static final String selectNextParameterAction = "select-next-parameter"; // NOI18N

	/** Go to the previous position stored in the jump-list */
	public static final String jumpListNextAction = "jump-list-next"; // NOI18N

	/** Go to the next position stored in the jump-list */
	public static final String jumpListPrevAction = "jump-list-prev"; // NOI18N

	/**
	 * Go to the last position in the previous component stored in the jump-list
	 */
	public static final String jumpListNextComponentAction = "jump-list-next-component"; // NOI18N

	/**
	 * Go to the next position in the previous component stored in the jump-list
	 */
	public static final String jumpListPrevComponentAction = "jump-list-prev-component"; // NOI18N

	/** Scroll window one line up */
	public static final String scrollUpAction = "scroll-up"; // NOI18N

	/** Scroll window one line down */
	public static final String scrollDownAction = "scroll-down"; // NOI18N

	/** Prefix of all macro-based actions */
	public static final String macroActionPrefix = "macro-"; // NOI18N

	public static final String setMarkAction = "set-mark";

	/**
	 * Start recording of macro. Only one macro recording can be active at the
	 * time
	 */
	public static final String startMacroRecordingAction = "start-macro-recording"; // NOI18N

	/** Stop the active recording */
	public static final String stopMacroRecordingAction = "stop-macro-recording"; // NOI18N

	public static class GenericAction extends BaseAction {
		GenericAction() {
			super(TSKit.genericAction);
		}

		public void actionPerformed(ActionEvent e, JTextComponent textComp) {

		}
	}

	/**
	 * Sets the mark for selection
	 */
	public static class SetMarkAction extends BaseAction {
		SetMarkAction() {
			super(TSKit.setMarkAction);
		}

		public void actionPerformed(ActionEvent e, JTextComponent target) {
			TSCaret caret = (TSCaret) target.getCaret();
			caret.setMarked(true);
		}
	}

	/** Default typed action */
	public static class DefaultKeyTypedAction extends BaseAction {
		static final long serialVersionUID = 3069164318144463899L;

		public DefaultKeyTypedAction() {
			super(defaultKeyTypedAction, MAGIC_POSITION_RESET | SAVE_POSITION | CLEAR_STATUS_TEXT);
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {

		}
	}

	public static class InsertBreakAction extends BaseAction {

		static final long serialVersionUID = 7966576342334158659L;

		public InsertBreakAction() {
			super(insertBreakAction, MAGIC_POSITION_RESET | ABBREV_RESET | WORD_MATCH_RESET);
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			if (target != null) {
				if (!target.isEditable() || !target.isEnabled()) {
					target.getToolkit().beep();
					return;
				}

				Caret caret = target.getCaret();
				int dotPos = caret.getDot();
				BaseDocument doc = (BaseDocument) target.getDocument();
				dotPos = doc.getFormatter().indentNewLine(doc, dotPos);
				caret.setDot(dotPos);
			}
		}

	}

	public static class TSGotoAction extends BaseAction {
		static final long serialVersionUID = 8425585413146373251L;

		public TSGotoAction() {
			super(gotoAction, ABBREV_RESET | MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
		}

		protected int getOffsetFromLine(BaseDocument doc, int lineOffset) {
			return Utilities.getRowStartFromLineOffset(doc, lineOffset);
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			if (target != null) {
				try {
					String linestr = JOptionPane.showInputDialog(I18N.getLocalizedDialogLabel("Goto Line"));
					if (linestr == null)
						return;

					int line = Integer.parseInt(linestr);

					BaseDocument doc = Utilities.getDocument(target);
					if (doc != null) {
						// Obtain the offset where to jump
						int pos = Utilities.getRowStartFromLineOffset(doc, line - 1);

						BaseKit kit = Utilities.getKit(target);
						if (kit != null) {
							Action a = kit.getActionByName(gotoAction);
							if (a instanceof TSKit.TSGotoAction) {
								pos = ((TSKit.TSGotoAction) a).getOffsetFromLine(doc, line - 1);
							}
						}

						if (pos != -1) {
							target.getCaret().setDot(pos);
						} else {
							target.getToolkit().beep();
							return;
						}
					}
				} catch (NumberFormatException e) {
					target.getToolkit().beep();
					return;
				}

			}
		}
	}

	/** Compound action that encapsulates several actions */
	public static class CompoundAction extends BaseAction {

		Action[] actions;

		static final long serialVersionUID = 1649688300969753758L;

		public CompoundAction(String nm, Action actions[]) {
			this(nm, 0, actions);
		}

		public CompoundAction(String nm, int resetMask, Action actions[]) {
			super(nm, resetMask);
			this.actions = actions;
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			if (target != null) {
				for (int i = 0; i < actions.length; i++) {
					Action a = actions[i];
					if (a instanceof BaseAction) {
						((BaseAction) a).actionPerformed(evt, target);
					} else {
						a.actionPerformed(evt);
					}
				}
			}
		}
	}

	/**
	 * Compound action that gets and executes its actions depending on the kit
	 * of the component. The other advantage is that it doesn't create
	 * additional instances of compound actions.
	 */
	public static class KitCompoundAction extends BaseAction {

		private String[] actionNames;

		static final long serialVersionUID = 8415246475764264835L;

		public KitCompoundAction(String nm, String actionNames[]) {
			this(nm, 0, actionNames);
		}

		public KitCompoundAction(String nm, int resetMask, String actionNames[]) {
			super(nm, resetMask);
			this.actionNames = actionNames;
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			if (target != null) {
				BaseKit kit = Utilities.getKit(target);
				if (kit != null) {
					for (int i = 0; i < actionNames.length; i++) {
						Action a = kit.getActionByName(actionNames[i]);
						if (a != null) {
							if (a instanceof BaseAction) {
								((BaseAction) a).actionPerformed(evt, target);
							} else {
								a.actionPerformed(evt);
							}
						}
					}
				}
			}
		}
	}

	public static class InsertContentAction extends BaseAction {

		static final long serialVersionUID = 5647751370952797218L;

		public InsertContentAction() {
			super(insertContentAction, MAGIC_POSITION_RESET | ABBREV_RESET | WORD_MATCH_RESET);
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			if ((target != null) && (evt != null)) {
				if (!target.isEditable() || !target.isEnabled()) {
					target.getToolkit().beep();
					return;
				}

				String content = evt.getActionCommand();
				if (content != null) {
					target.replaceSelection(content);
				} else {
					target.getToolkit().beep();
				}
			}
		}
	}

	/** Insert text specified in constructor */
	public static class InsertStringAction extends BaseAction {

		String text;

		static final long serialVersionUID = -2755852016584693328L;

		public InsertStringAction(String nm, String text) {
			super(nm, MAGIC_POSITION_RESET | ABBREV_RESET | WORD_MATCH_RESET);
			this.text = text;
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			if (target != null) {
				if (!target.isEditable() || !target.isEnabled()) {
					target.getToolkit().beep();
					return;
				}

				target.replaceSelection(text);
			}
		}
	}

	/** Remove previous or next character */
	public static class DeleteCharAction extends BaseAction {

		boolean nextChar;

		static final long serialVersionUID = -4321971925753148556L;

		public DeleteCharAction(String nm, boolean nextChar) {
			super(nm, MAGIC_POSITION_RESET | ABBREV_RESET | WORD_MATCH_RESET);
			this.nextChar = nextChar;
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			if (target != null) {
				if (!target.isEditable() || !target.isEnabled()) {
					target.getToolkit().beep();
					return;
				}
				try {
					Document doc = target.getDocument();
					Caret caret = target.getCaret();
					int dot = caret.getDot();
					int mark = caret.getMark();
					if (dot != mark) {
						// remove selection
						doc.remove(Math.min(dot, mark), Math.abs(dot - mark));
					} else {
						if (nextChar) { // remove next char
							doc.remove(dot, 1);
						} else { // remove previous char
							doc.remove(dot - 1, 1);
						}
					}
				} catch (BadLocationException e) {
					target.getToolkit().beep();
				}
			}
		}
	}

	public static class ReadOnlyAction extends BaseAction {

		static final long serialVersionUID = 9204335480208463193L;

		public ReadOnlyAction() {
			super(readOnlyAction);
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			if (target != null) {
				target.setEditable(false);
			}
		}
	}

	public static class WritableAction extends BaseAction {

		static final long serialVersionUID = -5982547952800937954L;

		public WritableAction() {
			super(writableAction);
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			if (target != null) {
				target.setEditable(true);
			}
		}
	}

	public static class CutAction extends BaseAction {
		static final long serialVersionUID = 6377157040901778853L;

		public CutAction() {
			super(cutAction, ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
			// setEnabled(false);
			// putValue ("helpID", CutAction.class.getName ());
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			if (target != null) {
				if (!target.isEditable() || !target.isEnabled()) {
					target.getToolkit().beep();
					return;
				}

				target.cut();
				TSCaret caret = (TSCaret) target.getCaret();
				caret.setMarked(false);

			}
		}
	}

	public static class CopyAction extends BaseAction {

		static final long serialVersionUID = -5119779005431986964L;

		public CopyAction() {
			super(copyAction, ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
			// setEnabled(false);
			// putValue ("helpID", CopyAction.class.getName() );
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			if (target != null) {
				target.copy();
				Caret caret = (Caret) target.getCaret();
				int dot = caret.getDot();
				((TSCaret) caret).setMarked(false);
				caret.setDot(dot);
			} else {

			}
		}
	}

	public static class PasteAction extends BaseAction {

		static final long serialVersionUID = 5839791453996432149L;

		public PasteAction() {
			super(pasteAction, ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
			// putValue ("helpID", PasteAction.class.getName ());
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			if (target != null) {
				if (!target.isEditable() || !target.isEnabled()) {
					target.getToolkit().beep();
					return;
				}

				target.paste();
			}
		}
	}

	public static class BeepAction extends BaseAction {

		static final long serialVersionUID = -4474054576633223968L;

		public BeepAction() {
			super(beepAction);
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			if (target != null) {
				target.getToolkit().beep();
			}
		}
	}

	public static class UpAction extends BaseAction {

		boolean select;

		static final long serialVersionUID = 4621760742646981563L;

		public UpAction(String nm, boolean select) {
			super(nm, ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET | CLEAR_STATUS_TEXT);
			this.select = select;
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			if (target != null) {
				try {
					Caret caret = (Caret) target.getCaret();
					int dot = caret.getDot();
					Point p = caret.getMagicCaretPosition();
					if (p == null) {
						Rectangle r = target.modelToView(dot);
						p = new Point(r.x, r.y);
						caret.setMagicCaretPosition(p);
					}
					try {
						dot = Utilities.getPositionAbove(target, dot, p.x);
						if (select || ((TSCaret) caret).isMarked()) {
							caret.moveDot(dot);
						} else {
							if (caret instanceof BaseCaret) {
								BaseCaret bCaret = (BaseCaret) caret;
								bCaret.setDot(dot, bCaret, EditorUI.SCROLL_MOVE);
							} else {
								caret.setDot(dot);
							}
						}
					} catch (BadLocationException e) {
						// the position stays the same
					}
				} catch (BadLocationException ex) {
					target.getToolkit().beep();
				}
			}
		}
	}

	public static class DownAction extends BaseAction {

		boolean select;

		static final long serialVersionUID = -5635702355125266822L;

		public DownAction(String nm, boolean select) {
			super(nm, ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET | CLEAR_STATUS_TEXT);
			this.select = select;
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			if (target != null) {
				try {
					Caret caret = (Caret) target.getCaret();
					int dot = caret.getDot();
					Point p = caret.getMagicCaretPosition();
					if (p == null) {
						Rectangle r = target.modelToView(dot);
						p = new Point(r.x, r.y);
						caret.setMagicCaretPosition(p);
					}
					try {
						dot = Utilities.getPositionBelow(target, dot, p.x);
						if (select || ((TSCaret) caret).isMarked()) {
							caret.moveDot(dot);
						} else {
							if (caret instanceof BaseCaret) {
								BaseCaret bCaret = (BaseCaret) caret;
								bCaret.setDot(dot, bCaret, EditorUI.SCROLL_MOVE);

							} else {
								caret.setDot(dot);
							}
						}
					} catch (BadLocationException e) {
						// position stays the same
					}
				} catch (BadLocationException ex) {
					target.getToolkit().beep();
				}
			}
		}
	}

	/** Go one page up */
	public static class PageUpAction extends BaseAction {

		boolean select;

		static final long serialVersionUID = -3107382148581661079L;

		public PageUpAction(String nm, boolean select) {
			super(nm, ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET | CLEAR_STATUS_TEXT);
			this.select = select;
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			if (target != null) {
				try {
					Caret caret = target.getCaret();
					BaseDocument doc = (BaseDocument) target.getDocument();
					int dot = caret.getDot();
					Rectangle tgtRect = ((BaseTextUI) target.getUI()).modelToView(target, dot);
					Point p = caret.getMagicCaretPosition();
					if (p == null) {
						p = new Point((int) tgtRect.x, (int) tgtRect.y);
						caret.setMagicCaretPosition(p);
					} else {
						p.y = (int) tgtRect.y;
					}
					EditorUI editorUI = ((BaseTextUI) target.getUI()).getEditorUI();
					Rectangle bounds = editorUI.getExtentBounds();
					int trHeight = Math.max(tgtRect.height, 1); // prevent
																// division by
																// zero
					int baseY = (bounds.y + trHeight - 1) / trHeight * trHeight;
					int lines = (int) (bounds.height / trHeight);
					int baseHeight = lines * trHeight;
					tgtRect.y = Math.max(baseY - baseHeight, 0);
					tgtRect.height = bounds.height;
					p.y = (int) Math.max(p.y - baseHeight, 0);
					int newDot = target.viewToModel(p);
					editorUI.scrollRectToVisible(tgtRect, EditorUI.SCROLL_DEFAULT);
					if (select) {
						caret.moveDot(newDot);
					} else {
						caret.setDot(newDot);
					}
				} catch (BadLocationException ex) {
					target.getToolkit().beep();
				}
			}
		}
	}

	public static class ForwardAction extends BaseAction {

		boolean select;

		static final long serialVersionUID = 8007293230193334414L;

		public ForwardAction(String nm, boolean select) {
			super(nm, MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET | CLEAR_STATUS_TEXT);
			this.select = select;
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			if (target != null) {
				Caret caret = (Caret) target.getCaret();
				try {
					int dot = target.getUI().getNextVisualPositionFrom(target, caret.getDot(), null,
							SwingConstants.EAST, null);
					if (select || ((TSCaret) caret).isMarked()) {
						caret.moveDot(dot);
					} else {
						caret.setDot(dot);
					}
				} catch (BadLocationException ex) {
					target.getToolkit().beep();
				}
			}
		}
	}

	/** Go one page down */
	public static class PageDownAction extends BaseAction {

		boolean select;

		static final long serialVersionUID = 8942534850985048862L;

		public PageDownAction(String nm, boolean select) {
			super(nm, ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET | CLEAR_STATUS_TEXT);
			this.select = select;
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			if (target != null) {
				try {
					Caret caret = (Caret) target.getCaret();
					BaseDocument doc = (BaseDocument) target.getDocument();
					int dot = caret.getDot();
					Rectangle tgtRect = ((BaseTextUI) target.getUI()).modelToView(target, dot);
					Point p = caret.getMagicCaretPosition();
					if (p == null) {
						p = new Point(tgtRect.x, tgtRect.y);
						caret.setMagicCaretPosition(p);
					} else {
						p.y = tgtRect.y;
					}
					EditorUI editorUI = ((BaseTextUI) target.getUI()).getEditorUI();
					Rectangle bounds = editorUI.getExtentBounds();
					int trHeight = Math.max(tgtRect.height, 1); // prevent
																// division by
																// zero
					int baseY = bounds.y / trHeight * trHeight;
					int lines = bounds.height / trHeight;
					int baseHeight = lines * trHeight;
					tgtRect.y = Math.max(baseY + baseHeight, 0);
					tgtRect.height = bounds.height;
					p.y = Math.max(p.y + baseHeight, 0);
					int newDot = target.viewToModel(p);
					editorUI.scrollRectToVisible(tgtRect, EditorUI.SCROLL_DEFAULT);
					if (select || ((TSCaret) caret).isMarked()) {
						caret.moveDot(newDot);
					} else {
						if (caret instanceof BaseCaret) {
							BaseCaret bCaret = (BaseCaret) caret;
							bCaret.setDot(newDot, bCaret, EditorUI.SCROLL_MOVE);
						} else {
							caret.setDot(newDot);
						}
					}
				} catch (BadLocationException ex) {
					target.getToolkit().beep();
				}
			}
		}
	}

	public static class BackwardAction extends BaseAction {

		boolean select;

		static final long serialVersionUID = -3048379822817847356L;

		public BackwardAction(String nm, boolean select) {
			super(nm, MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET | CLEAR_STATUS_TEXT);
			this.select = select;
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			if (target != null) {
				Caret caret = (Caret) target.getCaret();
				try {
					int dot = target.getUI().getNextVisualPositionFrom(target, caret.getDot(), null,
							SwingConstants.WEST, null);
					if (select || ((TSCaret) caret).isMarked()) {
						caret.moveDot(dot);
					} else {
						if (caret instanceof BaseCaret) {
							BaseCaret bCaret = (BaseCaret) caret;
							bCaret.setDot(dot, bCaret, EditorUI.SCROLL_MOVE);
						} else {
							caret.setDot(dot);
						}
					}
				} catch (BadLocationException ex) {
					target.getToolkit().beep();
				}
			}
		}
	}

	public static class BeginLineAction extends BaseAction {

		boolean select;

		boolean homeKeyColumnOne;

		static final long serialVersionUID = 3269462923524077779L;

		public BeginLineAction(String nm, boolean select) {
			super(nm, MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET | CLEAR_STATUS_TEXT);
			this.select = select;
		}

		protected void settingsChange(SettingsChangeEvent evt, Class kitClass) {
			homeKeyColumnOne = SettingsUtil.getBoolean(kitClass, SettingsNames.HOME_KEY_COLUMN_ONE, false);
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			if (target != null) {
				Caret caret = (Caret) target.getCaret();
				BaseDocument doc = (BaseDocument) target.getDocument();
				try {
					int dot = caret.getDot();
					int lineStartPos = Utilities.getRowStart(doc, dot, 0);
					if (homeKeyColumnOne) { // to first column
						dot = lineStartPos;
					} else { // either to line start or text start
						int textStartPos = Utilities.getRowFirstNonWhite(doc, lineStartPos);
						if (textStartPos < 0) { // no text on the line
							textStartPos = Utilities.getRowEnd(doc, lineStartPos);
						}
						if (dot == lineStartPos) { // go to the text start pos
							dot = textStartPos;
						} else if (dot <= textStartPos) {
							dot = lineStartPos;
						} else {
							dot = textStartPos;
						}
					}
					if (select || ((TSCaret) caret).isMarked()) {
						caret.moveDot(dot);
					} else {
						caret.setDot(dot);
					}
				} catch (BadLocationException e) {
					target.getToolkit().beep();
				}
			}
		}
	}

	public static class EndLineAction extends BaseAction {
		boolean select;

		static final long serialVersionUID = 5216077634055190170L;

		public EndLineAction(String nm, boolean select) {
			super(nm, MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET | CLEAR_STATUS_TEXT);
			this.select = select;
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			if (target != null) {
				Caret caret = (Caret) target.getCaret();
				try {
					int dot = Utilities.getRowEnd(target, caret.getDot());
					if (select || ((TSCaret) caret).isMarked()) {
						caret.moveDot(dot);
					} else {
						caret.setDot(dot);
					}
					// now move the magic caret position far to the right
					Rectangle r = target.modelToView(dot);
					Point p = new Point(Integer.MAX_VALUE / 2, r.y);
					caret.setMagicCaretPosition(p);
				} catch (BadLocationException e) {
					target.getToolkit().beep();
				}
			}
		}
	}

	public static class BeginAction extends BaseAction {

		boolean select;

		static final long serialVersionUID = 3463563396210234361L;

		public BeginAction(String nm, boolean select) {
			super(nm, MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET | SAVE_POSITION
					| CLEAR_STATUS_TEXT);
			this.select = select;
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			if (target != null) {
				Caret caret = (Caret) target.getCaret();
				int dot = 0; // begin of document
				if (select || ((TSCaret) caret).isMarked()) {
					caret.moveDot(dot);
				} else {
					caret.setDot(dot);
				}
			}
		}
	}

	public static class EndAction extends BaseAction {
		boolean select;

		static final long serialVersionUID = 8547506353130203657L;

		public EndAction(String nm, boolean select) {
			super(nm, MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET | SAVE_POSITION
					| CLEAR_STATUS_TEXT);
			this.select = select;
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			if (target != null) {
				Caret caret = (Caret) target.getCaret();
				int dot = target.getDocument().getLength(); // end of document
				if (select || ((TSCaret) caret).isMarked())
					caret.moveDot(dot);
				else
					caret.setDot(dot);
			}
		}
	}

	public static class NextWordAction extends BaseAction {
		boolean select;

		static final long serialVersionUID = -5909906947175434032L;

		public NextWordAction(String nm, boolean select) {
			super(nm, MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET | CLEAR_STATUS_TEXT);
			this.select = select;
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			if (target != null) {
				Caret caret = (Caret) target.getCaret();
				try {
					int dotPos = caret.getDot();
					dotPos = Utilities.getNextWord(target, dotPos);
					if (select || ((TSCaret) caret).isMarked())
						caret.moveDot(dotPos);
					else
						caret.setDot(dotPos);
				} catch (BadLocationException ex) {
					target.getToolkit().beep();
				}
			}
		}
	}

	public static class PreviousWordAction extends BaseAction {
		boolean select;

		static final long serialVersionUID = -5465143382669785799L;

		public PreviousWordAction(String nm, boolean select) {
			super(nm, MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET | CLEAR_STATUS_TEXT);
			this.select = select;
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			if (target != null) {
				Caret caret = (Caret) target.getCaret();
				try {
					int dot = Utilities.getPreviousWord(target, caret.getDot());
					if (select || ((TSCaret) caret).isMarked())
						caret.moveDot(dot);
					else
						caret.setDot(dot);
				} catch (BadLocationException ex) {
					target.getToolkit().beep();
				}
			}
		}
	}

	public static class BeginWordAction extends BaseAction {

		boolean select;

		static final long serialVersionUID = 3991338381212491110L;

		public BeginWordAction(String nm, boolean select) {
			super(nm, MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET | CLEAR_STATUS_TEXT);
			this.select = select;
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			if (target != null) {
				Caret caret = (Caret) target.getCaret();
				try {
					int dot = Utilities.getWordStart(target, caret.getDot());
					if (select || ((TSCaret) caret).isMarked())
						caret.moveDot(dot);
					else
						caret.setDot(dot);
				} catch (BadLocationException ex) {
					target.getToolkit().beep();
				}
			}
		}
	}

	public static class EndWordAction extends BaseAction {
		boolean select;

		static final long serialVersionUID = 3812523676620144633L;

		public EndWordAction(String nm, boolean select) {
			super(nm, MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET | CLEAR_STATUS_TEXT);
			this.select = select;
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			if (target != null) {
				Caret caret = (Caret) target.getCaret();
				try {
					int dot = Utilities.getWordEnd(target, caret.getDot());
					if (select || ((TSCaret) caret).isMarked())
						caret.moveDot(dot);
					else
						caret.setDot(dot);
				} catch (BadLocationException ex) {
					target.getToolkit().beep();
				}
			}
		}
	}

	public static class FindAction extends BaseAction {

		static final long serialVersionUID = 719554648887497427L;

		public FindAction() {
			super(findAction, ABBREV_RESET | MAGIC_POSITION_RESET | UNDO_MERGE_RESET | NO_RECORDING);
		}

		public FindDialogSupport getSupport() {
			if (m_findsupport == null) {
				m_findsupport = new com.jeta.foundation.gui.editor.FindDialogSupport();
			}
			return m_findsupport;
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			if (target != null) {
				getSupport().showFindDialog();
			}
		}

	}

	public static class ReplaceAction extends BaseAction {

		static final long serialVersionUID = 1828017436079834384L;

		public ReplaceAction() {
			super(replaceAction, ABBREV_RESET | MAGIC_POSITION_RESET | UNDO_MERGE_RESET | NO_RECORDING);
		}

		public FindDialogSupport getSupport() {
			if (m_findsupport == null) {
				m_findsupport = new FindDialogSupport();
			}
			return m_findsupport;
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			if (target != null) {
				getSupport().showReplaceDialog();
			}
		}

	}

	/** Select word around caret */
	public static class SelectWordAction extends KitCompoundAction {

		static final long serialVersionUID = 7678848538073016357L;

		public SelectWordAction() {
			super(selectWordAction, new String[] { beginWordAction, selectionEndWordAction });
		}

	}

	/** Select line around caret */
	public static class SelectLineAction extends KitCompoundAction {
		static final long serialVersionUID = -7407681863035740281L;

		public SelectLineAction() {
			super(selectLineAction, new String[] { beginLineAction, selectionEndLineAction });
		}

	}

	/** Select text of whole document */
	public static class SelectAllAction extends KitCompoundAction {
		static final long serialVersionUID = -3502499718130556524L;

		public SelectAllAction() {
			super(selectAllAction, new String[] { beginAction, selectionEndAction });
		}

	}

	public static class TSEscapeAction extends ExtKit.EscapeAction {
		TSEscapeAction() {
		}

		public void actionPerformed(ActionEvent e, JTextComponent target) {
			super.actionPerformed(e, target);
			Component parent = TSComponentUtils.getParentFrame(target);
			if (parent != null) {
				WindowDelegate container = null;
				if (parent instanceof WindowDelegate) {
					WindowDelegate window = (WindowDelegate) parent;
					TSInternalFrame iframe = window.getTSInternalFrame();
					JETAController controller = iframe.getController();
					if (controller != null) {
						controller.actionPerformed((String) getValue(Action.NAME), e);
					}
				} else if (parent instanceof TSDialog) {
					TSDialog dlg = (TSDialog) parent;
					JETAController controller = dlg.getController();
					if (controller != null) {
						controller.actionPerformed((String) getValue(Action.NAME), e);
					}
				} else {
					assert (false);
				}

			}
		}
	}

}
