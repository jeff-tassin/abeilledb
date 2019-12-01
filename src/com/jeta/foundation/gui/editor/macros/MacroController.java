package com.jeta.foundation.gui.editor.macros;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Collection;
import java.util.Iterator;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import javax.swing.text.TextAction;
import javax.swing.text.Keymap;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;

import com.jeta.foundation.gui.editor.SelectActionDialog;
import com.jeta.foundation.gui.editor.TSEditorMgr;

import com.jeta.foundation.i18n.I18N;

import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;

import org.netbeans.editor.BaseDocument;

/**
 * This is the controller for the MacroPanel
 * 
 * @author Jeff Tassin
 */
public class MacroController extends TSController implements JETARule {
	/** the view that displays the macro */
	private MacroPanel m_view;

	/** the model for all macros in the system */
	private MacroMgrModel m_model;

	/** the set of all actions (we cache this from the MacroMgrModel) */
	private Collection m_actionnames;

	/** the original name for the macro if we are editing */
	private String m_originalname;

	public MacroController(MacroPanel view, MacroMgrModel model) {
		super(view);
		m_view = view;
		m_model = model;
		m_originalname = m_view.getName();

		m_actionnames = m_model.getActionNames();

		// let's add the add macro action to the editor
		JEditorPane editor = m_view.getEditorPane();
		KeyStroke key = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SPACE, java.awt.Event.CTRL_MASK);
		Keymap keymap = editor.addKeymap("macropanel", editor.getKeymap());
		Action action = new MacroAddAction();
		keymap.addActionForKeyStroke(key, action);
		editor.setKeymap(keymap);
	}

	/**
	 * Validates the macro in the given panel. Two types of validation are
	 * currently done. 1). the macro name must be unique. No other actions or
	 * macros can have the same name 2). the command sequence must follow the
	 * syntax for macro commands (e.g. action "literal" action action "literal"
	 * etc. )
	 * 
	 * @return a string message if the validation fails. The caller can use this
	 *         message in a error dialog. If the message is null, the validation
	 *         succeeded.
	 */
	public RuleResult check(Object[] params) {
		RuleResult result = RuleResult.SUCCESS;
		Macro macro = m_view.getMacro();
		String macroname = m_view.getName();
		macroname = (macroname == null) ? "" : macroname.trim();

		if (macroname.length() == 0) {
			result = new RuleResult(I18N.getLocalizedMessage("Invalid name"));
		} else {
			// make sure all actions are defined
			Collection actionnames = m_actionnames;
			Iterator iter = actionnames.iterator();
			while (iter.hasNext()) {
				String actionname = (String) iter.next();
				if (!m_model.isDefined(actionname)) {
					result = new RuleResult(I18N.format("Action_name_not_found_1", actionname));
				}
			}

			if (result.equals(RuleResult.SUCCESS)) {
				// now make sure that the macro name does not already exist
				if (m_view.isNew()) {
					if (m_model.isDefined(macroname)) {
						// action name is already defined, unable to use
						result = new RuleResult(I18N.format("Action_name_already_defined_1", macroname));
					}
				} else {
					if (!macroname.equals(m_originalname) && m_model.isDefined(macroname)) {
						// action name is already defined, unable to use
						result = new RuleResult(I18N.format("Action_name_already_defined_1", macroname));
					}
				}
			}
		}
		return result;
	}

	/**
	 * This action popups a dialog to allow the user to select a macro or action
	 * from the list of actions supported by the text editor.
	 */
	public class MacroAddAction extends TextAction {
		/** Create this object with the appropriate identifier. */
		public MacroAddAction() {
			super("macroaddaction");
		}

		public void actionPerformed(ActionEvent evt) {
			SelectActionDialog dlg = (SelectActionDialog) TSGuiToolbox.createDialog(SelectActionDialog.class, m_view,
					true);

			LinkedList actions = new LinkedList();
			Collection names = m_actionnames;
			actions.addAll(names);

			dlg.initialize(actions);
			dlg.setSize(dlg.getPreferredSize());
			dlg.showCenter();
			if (dlg.isOk()) {
				String actionname = dlg.getActionName();

				JEditorPane editor = m_view.getEditorPane();
				BaseDocument doc = (BaseDocument) editor.getDocument();
				doc.atomicLock();
				try {
					Caret caret = editor.getCaret();
					int pos = caret.getDot();
					doc.insertString(pos, actionname, null);
					editor.requestFocus();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					doc.atomicUnlock();
				}
			}
		}
	}

}
