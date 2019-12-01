/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

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

package com.jeta.foundation.gui.editor;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Toolkit;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowAdapter;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import javax.swing.SwingUtilities;
import javax.swing.JButton;
import javax.swing.Timer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.FindSupport;
import org.netbeans.editor.DialogSupport;
import org.netbeans.editor.LocaleSupport;
import org.netbeans.editor.GuardedException;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.WeakTimerListener;
import org.netbeans.editor.EditorState;
import org.netbeans.editor.BaseCaret;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * Support for displaying find and replace dialogs
 * 
 * @author Miloslav Metelka
 * @version 1.00
 */

public class FindDialogSupport extends WindowAdapter implements ActionListener {

	private static final String HISTORY_KEY_BASE = "FindDialogSupport."; // NOI18N
	/** The EditorSettings key storing the last location of the dialog. */
	private static final String BOUNDS_FIND = HISTORY_KEY_BASE + "bounds-find"; // NOI18N
	private static final String BOUNDS_REPLACE = HISTORY_KEY_BASE + "bounds-replace"; // NOI18N

	/**
	 * This lock is used to create a barrier between showing/hiding/changing the
	 * dialog and testing if the dialog is already shown. it is used to make
	 * test-and-change / test-and-display actions atomic. It covers the
	 * following four fields: findDialog, isReplaceDialog, findPanel,
	 * findButtons
	 */
	private Object dialogLock = new Object();

	/** Whether the currently visible dialog is for replace */
	private boolean isReplaceDialog;

	/** The buttons used in the visible dialog */
	private JButton findButtons[];

	/** The FindPanel used inside the visible dialog */
	private FindPanel findPanel;

	/** Currently visible dialog */
	private Dialog findDialog;

	protected Timer incSearchTimer;

	private static final String MNEMONIC_SUFFIX = "-mnemonic"; // NOI18N
	private static final String A11Y_PREFIX = "ACSD_"; // NOI18N

	public FindDialogSupport() {
		int delay = SettingsUtil.getInteger(null, SettingsNames.FIND_INC_SEARCH_DELAY, 200);
		incSearchTimer = new Timer(delay, new WeakTimerListener(this));
		incSearchTimer.setRepeats(false);
	}

	private JButton[] createFindButtons() {
		JButton[] buttons = new JButton[] { new JButton(LocaleSupport.getString("find-button-find")), // NOI18N
				new JButton(LocaleSupport.getString("find-button-replace")), // NOI18N
				new JButton(LocaleSupport.getString("find-button-replace-all")), // NOI18N
				new JButton(LocaleSupport.getString("find-button-cancel")) // NOI18N
		};
		/*
		 * buttons[0].setMnemonic( LocaleSupport.getChar( "find-button-find" +
		 * MNEMONIC_SUFFIX, 'F' ) ); // NOI18N
		 */
		buttons[1].setMnemonic(LocaleSupport.getChar("find-button-replace" + MNEMONIC_SUFFIX, 'R')); // NOI18N

		buttons[2].setMnemonic(LocaleSupport.getChar("find-button-replace-all" + MNEMONIC_SUFFIX, 'A')); // NOI18N

		buttons[0].getAccessibleContext().setAccessibleDescription(
				LocaleSupport.getString(A11Y_PREFIX + "find-button-find")); // NOI18N
		buttons[1].getAccessibleContext().setAccessibleDescription(
				LocaleSupport.getString(A11Y_PREFIX + "find-button-replace")); // NOI18N
		buttons[2].getAccessibleContext().setAccessibleDescription(
				LocaleSupport.getString(A11Y_PREFIX + "find-button-replace-all")); // NOI18N
		buttons[3].getAccessibleContext().setAccessibleDescription(
				LocaleSupport.getString(A11Y_PREFIX + "find-button-cancel")); // NOI18N

		return buttons;
	}

	private void loadBounds(Dialog d, String key) {
		d.pack();
		// Position the dialog according to the history
		Rectangle lastBounds = (Rectangle) EditorState.get(key);
		if (lastBounds != null) {
			d.setBounds(lastBounds);
		} else { // no history, center it on the screen
			Dimension dim = d.getPreferredSize();
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			int x = Math.max(0, (screen.width - dim.width) / 2);
			int y = Math.max(0, (screen.height - dim.height) / 2);
			d.setLocation(x, y);
		}
	}

	private void saveBounds(Dialog d, String key) {
		EditorState.put(key, d.getBounds());
	}

	private Dialog createFindDialog(JPanel findPanel, final JButton[] buttons, final ActionListener l) {
		DialogBuilder builder = new DialogBuilder();
		Dialog d = builder.createDialog(
				isReplaceDialog ? LocaleSupport.getString("replace-title") : LocaleSupport.getString("find-title"), // NOI18N
				findPanel, false, // non-modal
				buttons, true, // sidebuttons,
				0, // defaultIndex = 0 => findButton
				3, // cancelIndex = 3 => cancelButton
				l // listener
				);

		return d;
	}

	private void showFindDialogImpl(boolean isReplace) {
		synchronized (dialogLock) {
			if (findDialog != null) { // we have a dialog, change or raise
				if (isReplaceDialog == isReplace) { // raise only
					findDialog.toFront();
				} else { // change (and raise ?)
					saveBounds(findDialog, isReplaceDialog ? BOUNDS_REPLACE : BOUNDS_FIND);
					isReplaceDialog = isReplace;
					findButtons[1].setVisible(isReplace);
					findButtons[2].setVisible(isReplace);
					if (isReplace) {
						findDialog.setTitle(LocaleSupport.getString("replace-title")); // NOI18N
						findPanel.updateReplace();
					} else {
						findDialog.setTitle(LocaleSupport.getString("find-title")); // NOI18N
						findPanel.updateFind();
					}
					loadBounds(findDialog, isReplace ? BOUNDS_REPLACE : BOUNDS_FIND);
				}
			} else { // create and show new dialog of reqiured type.
				isReplaceDialog = isReplace;
				findButtons = createFindButtons();
				findButtons[1].setVisible(isReplace);
				findButtons[2].setVisible(isReplace);
				findPanel = new FindPanel();
				if (isReplace) {
					findPanel.updateReplace();
				} else {
					findPanel.updateFind();
				}
				findDialog = createFindDialog(findPanel, findButtons, this);
				loadBounds(findDialog, isReplace ? BOUNDS_REPLACE : BOUNDS_FIND);

				findDialog.addWindowListener(this);
			}
		} // end of synchronized section

		if (findDialog != null) {
			findDialog.setVisible(true);
			// findDialog.requestFocus();
			// findPanel.updateFocus();
		}
	}

	public void windowActivated(WindowEvent evt) {
		incSearchTimer.start();
	}

	public void windowDeactivated(WindowEvent evt) {
		incSearchTimer.stop();
		FindSupport.getFindSupport().incSearchReset();
	}

	public void windowClosing(WindowEvent evt) {
		hideDialog();
	}

	public void windowClosed(WindowEvent evt) {
		incSearchTimer.stop();
		FindSupport.getFindSupport().incSearchReset();
		Utilities.returnFocus();
	}

	public void showFindDialog() {
		showFindDialogImpl(false);
	}

	public void showReplaceDialog() {
		showFindDialogImpl(true);
	}

	private void hideDialog() {
		synchronized (dialogLock) {
			if (findDialog != null) {
				saveBounds(findDialog, isReplaceDialog ? BOUNDS_REPLACE : BOUNDS_FIND);
				findDialog.dispose();
				findButtons = null; // let it gc()
				findPanel = null;
				findDialog = null;
			}
		}
	}

	public void actionPerformed(ActionEvent evt) {
		JButton[] fb = findButtons;
		if (fb == null)
			return;

		Object src = evt.getSource();
		FindSupport fSup = FindSupport.getFindSupport();
		if (src == fb[0]) { // Find button
			findPanel.updateFindHistory();
			findPanel.save();
			if (fSup.find(null, false)) { // found
			}
			// if (!isReplaceDialog) {
			// hideDialog();
			// }
		} else if (src == fb[1]) { // Replace button
			findPanel.updateReplaceHistory();
			findPanel.save();

			try {
				if (fSup.replace(null, false)) { // replaced
					fSup.find(null, false);
				}
			} catch (GuardedException e) {
				// replace in guarded block
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		} else if (src == fb[2]) { // Replace All button
			findPanel.updateReplaceHistory();
			findPanel.save();
			fSup.replaceAll(null);
		} else if (src == fb[3]) { // Cancel button
			hideDialog();

			// fix for issue 13502
			// canceling dialog must scroll back to the caret position in the
			// document
			// (in case the visible area of document has changed because of
			// incremental search)
			JTextComponent c = Utilities.getLastActiveComponent();
			if (c != null)
				c.getCaret().setDot(c.getCaret().getDot());

		} else if (src == incSearchTimer) {
			fSup.incSearch(findPanel.getFindProps());
		} else {
			// fix for issue 13502
			// canceling dialog must scroll back to the caret position in the
			// document
			// (in case the visible area of document has changed because of
			// incremental search)
			JTextComponent c = Utilities.getLastActiveComponent();
			if (c != null)
				c.getCaret().setDot(c.getCaret().getDot());
		}
	}

	/** Panel that holds the find logic */
	public class FindPanel extends FindDialogPanel implements ItemListener, KeyListener, ActionListener {

		Map findProps = Collections.synchronizedMap(new HashMap(20));

		Map objToProps = Collections.synchronizedMap(new HashMap(20));

		FindSupport findSupport = FindSupport.getFindSupport();

		static final long serialVersionUID = 917425125419841466L;

		FindPanel() {
			objToProps.put(findWhat, SettingsNames.FIND_WHAT);
			objToProps.put(replaceWith, SettingsNames.FIND_REPLACE_WITH);
			objToProps.put(highlightSearch, SettingsNames.FIND_HIGHLIGHT_SEARCH);
			objToProps.put(incSearch, SettingsNames.FIND_INC_SEARCH);
			objToProps.put(matchCase, SettingsNames.FIND_MATCH_CASE);
			objToProps.put(smartCase, SettingsNames.FIND_SMART_CASE);
			objToProps.put(wholeWords, SettingsNames.FIND_WHOLE_WORDS);
			objToProps.put(regExp, SettingsNames.FIND_REG_EXP);
			objToProps.put(bwdSearch, SettingsNames.FIND_BACKWARD_SEARCH);
			objToProps.put(wrapSearch, SettingsNames.FIND_WRAP_SEARCH);

			regExp.setEnabled(false); // !!! remove when regexp search is fine
			regExp.setVisible(false);

			load();
			findWhat.getEditor().getEditorComponent().addKeyListener(this);
			findWhat.addActionListener(this);
			replaceWith.getEditor().getEditorComponent().addKeyListener(this);
			replaceWith.addActionListener(this);
			highlightSearch.addItemListener(this);
			incSearch.addItemListener(this);
			matchCase.addItemListener(this);
			smartCase.addItemListener(this);
			wholeWords.addItemListener(this);
			regExp.addItemListener(this);
			bwdSearch.addItemListener(this);
			wrapSearch.addItemListener(this);
		}

		protected Map getFindProps() {
			return findProps;
		}

		void putProperty(Object component, Object value) {
			String prop = (String) objToProps.get(component);
			if (prop != null) {
				findProps.put(prop, value);
				incSearchTimer.restart();
				// findSupport.incSearch(findProps);
			}
		}

		Object getProperty(Object component) {
			String prop = (String) objToProps.get(component);
			return (prop != null) ? findProps.get(prop) : null;
		}

		boolean getBooleanProperty(Object component) {
			Object prop = getProperty(component);
			return (prop != null) ? ((Boolean) prop).booleanValue() : false;
		}

		private void changeVisibility(boolean v) {
			replaceWith.setVisible(v);
			replaceWithLabel.setVisible(v);
		}

		protected void updateFocus() {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JTextComponent c = Utilities.getLastActiveComponent();
					if (c != null) {
						String selText = c.getSelectedText();
						if (selText != null) {
							int n = selText.indexOf('\n');
							if (n >= 0)
								selText = selText.substring(0, n);
							findPanel.updateFindWhat(selText.trim());
						} else {
							if (getProperty(findWhat) != null) {
								findWhat.getEditor().setItem(getProperty(findWhat));
							}
						}
					}

					findWhat.getEditor().getEditorComponent().requestFocus();
					findWhat.requestFocus();
					findWhat.getEditor().selectAll();
				}
			});
		}

		protected void updateFind() {
			changeVisibility(false);
		}

		protected void updateReplace() {
			changeVisibility(true);
		}

		private List getHistory(String identifier) {
			List history = (List) EditorState.get(HISTORY_KEY_BASE + identifier + "-history"); // NOI18N
			if (history == null) {
				history = new ArrayList();
			}
			return history;
		}

		private void putHistory(String identifier, List history) {
			EditorState.put(HISTORY_KEY_BASE + identifier + "-history", history); // NOI18N
		}

		private void updateHistory(JComboBox c, String identifier) {
			Object item = c.getEditor().getItem();

			List history = getHistory(identifier);
			if (item != null) {
				int index = history.indexOf(item);
				if (index >= 0) {
					history.remove(index);
				}
				history.add(0, item);
			}
			putHistory(identifier, history);

			javax.swing.DefaultComboBoxModel m = new javax.swing.DefaultComboBoxModel(history.toArray());
			c.setModel(m);
		}

		protected void updateFindHistory() {
			updateHistory(findWhat, "find"); // NOI18N
		}

		protected void updateReplaceHistory() {
			updateHistory(replaceWith, "replace"); // NOI18N
		}

		protected void updateFindWhat(String selectedText) {
			findWhat.getEditor().setItem(selectedText);
		}

		/** Load the current find properties from those in FindSupport */
		void load() {
			findProps.putAll(findSupport.getFindProperties());

			java.util.List history = getHistory("find"); // NOI18N
			javax.swing.DefaultComboBoxModel m = new javax.swing.DefaultComboBoxModel(history.toArray());
			findWhat.setModel(m);

			history = getHistory("replace"); // NOI18N
			m = new javax.swing.DefaultComboBoxModel(history.toArray());
			replaceWith.setModel(m);

			findWhat.getEditor().setItem(getProperty(findWhat));
			replaceWith.getEditor().setItem(getProperty(replaceWith));
			highlightSearch.setSelected(getBooleanProperty(highlightSearch));
			incSearch.setSelected(getBooleanProperty(incSearch));
			matchCase.setSelected(getBooleanProperty(matchCase));
			smartCase.setSelected(getBooleanProperty(smartCase));
			wholeWords.setSelected(getBooleanProperty(wholeWords));
			regExp.setSelected(getBooleanProperty(regExp));
			bwdSearch.setSelected(getBooleanProperty(bwdSearch));
			wrapSearch.setSelected(getBooleanProperty(wrapSearch));
		}

		/** Save the current find properties into those in FindSupport */
		void save() {
			findSupport.putFindProperties(findProps);
		}

		void changeFindWhat() {
			Object old = getProperty(findWhat);
			Object cur = findWhat.getEditor().getItem();
			if (old == null || !old.equals(cur)) {
				putProperty(findWhat, cur);
			}
		}

		void changeReplaceWith() {
			Object old = getProperty(replaceWith);
			Object cur = replaceWith.getEditor().getItem();
			if (old == null || !old.equals(cur)) {
				putProperty(replaceWith, cur);
			}
		}

		private void postChangeCombos() {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					changeFindWhat();
					changeReplaceWith();
					if (regExp.isSelected()) {

					}
				}
			});
		}

		public void keyPressed(KeyEvent evt) {
			postChangeCombos();
		}

		public void keyReleased(KeyEvent evt) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					changeFindWhat();
					changeReplaceWith();
				}
			});
		}

		public void keyTyped(KeyEvent evt) {
			if (evt.getKeyChar() == '\n') {
				FindDialogSupport.this.actionPerformed(new ActionEvent(findButtons[0], 0, null));
			}
		}

		public void itemStateChanged(ItemEvent evt) {
			Boolean val = (evt.getStateChange() == ItemEvent.SELECTED) ? Boolean.TRUE : Boolean.FALSE;
			putProperty(evt.getSource(), val);
		}

		public void actionPerformed(ActionEvent evt) {
			postChangeCombos();
		}

	}

	/**
	 * This needed to be pulled from netbeans because they are creating the
	 * JDialog using the following line: JDialog d = new JDialog(
	 * (javax.swing.JFrame)null ,... );
	 * 
	 * This prevents us from invoking the file dialog from another modal dialog,
	 * so we need to override.
	 */
	static class DialogBuilder extends WindowAdapter implements ActionListener {
		private JButton cancelButton;

		/**
		 * Create a panel with buttons that will be placed according to the
		 * required alignment
		 */
		JPanel createButtonPanel(JButton[] buttons, boolean sidebuttons) {
			int count = buttons.length;

			JPanel outerPanel = new JPanel(new BorderLayout());
			outerPanel.setBorder(new EmptyBorder(new Insets(sidebuttons ? 5 : 0, sidebuttons ? 0 : 5, 5, 5)));

			LayoutManager lm = new GridLayout(sidebuttons ? count : 1, sidebuttons ? 1 : count, 5, 5);

			JPanel innerPanel = new JPanel(lm);
			for (int i = 0; i < count; i++)
				innerPanel.add(buttons[i]);

			outerPanel.add(innerPanel, sidebuttons ? BorderLayout.NORTH : BorderLayout.EAST);
			return outerPanel;
		}

		Dialog createDialog(String title, JPanel panel, boolean modal, JButton[] buttons, boolean sidebuttons,
				int defaultIndex, int cancelIndex, ActionListener listener) {

			// create the dialog with given content
			JDialog d = (JDialog) TSGuiToolbox.createDialog(JDialog.class, Utilities.getLastActiveComponent(), true);

			// JDialog d = new JDialog( (javax.swing.JFrame)null, title, modal
			// );
			d.setTitle(title);
			d.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			d.getContentPane().add(panel, BorderLayout.CENTER);

			// Add the buttons to it
			JPanel buttonPanel = createButtonPanel(buttons, sidebuttons);
			String buttonAlign = sidebuttons ? BorderLayout.EAST : BorderLayout.SOUTH;
			d.getContentPane().add(buttonPanel, buttonAlign);

			// add listener to buttons
			if (listener != null) {
				for (int i = 0; i < buttons.length; i++) {
					buttons[i].addActionListener(listener);
				}
			}

			// register the default button, if available
			if (defaultIndex >= 0) {
				d.getRootPane().setDefaultButton(buttons[defaultIndex]);
			}

			// register the cancel button helpers, if available
			if (cancelIndex >= 0) {
				cancelButton = buttons[cancelIndex];
				// redirect the Esc key to Cancel button
				d.getRootPane().registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true),
						JComponent.WHEN_IN_FOCUSED_WINDOW);

				// listen on windowClosing and redirect it to Cancel button
				d.addWindowListener(this);
			}

			d.pack();
			return d;
		}

		public void actionPerformed(ActionEvent evt) {
			cancelButton.doClick(10);
		}

		public void windowClosing(WindowEvent evt) {
			cancelButton.doClick(10);
		}
	}

}
