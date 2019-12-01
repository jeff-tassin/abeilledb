package com.jeta.foundation.gui.editor.macros;

import java.util.HashSet;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.text.JTextComponent;
import javax.swing.text.Document;

import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.BaseDocument;

/**
 * This class is pulled on the netbeans ActionFactory.RunMacroAction However, i
 * needed to modify the basic behavior to get macros from the MacroMgr instead
 * of the Settings cache
 * 
 * @author Jeff Tassin
 */
public class RunMacroAction extends BaseAction {
	static HashSet m_runningActions = new HashSet();
	private Macro m_macro;

	public RunMacroAction(Macro macro) {
		super(macro.getName());
		m_macro = macro;
	}

	protected void error(JTextComponent target, String messageKey) {
		// Utilities.setStatusText( target, LocaleSupport.getString( messageKey,
		// "Error in macro: " + messageKey ) );
		Toolkit.getDefaultToolkit().beep();
	}

	public void actionPerformed(ActionEvent evt, JTextComponent target) {
		if (!m_runningActions.add(m_macro.getName())) {
			// this macro is already running, beware of loops
			error(target, "loop");
			return;
		}

		if (target == null)
			return;

		BaseKit kit = Utilities.getKit(target);
		if (kit == null)
			return;

		String commandstring = m_macro.getCommand();

		if (commandstring == null) {
			error(target, "macro-not-found");
			m_runningActions.remove(m_macro.getName());
			return;
		}

		StringBuffer actionName = new StringBuffer();
		char[] command = commandstring.toCharArray();
		int len = command.length;

		BaseDocument doc = (BaseDocument) target.getDocument();
		try {
			doc.atomicLock();

			for (int i = 0; i < len; i++) {
				if (Character.isWhitespace(command[i]))
					continue;
				if (command[i] == '"') {
					while (++i < len && command[i] != '"') {
						char ch = command[i];
						if (ch == '\\') {
							if (++i >= len) { // '\' at the end
								error(target, "macro-malformed");
								return;
							}
							ch = command[i];
							if (ch != '"' && ch != '\\') {
								// neither \\ nor \"
								error(target, "macro-malformed");
								return;
							} // else fall through
						}
						Action a = target.getKeymap().getDefaultAction();

						if (a != null) {
							ActionEvent newEvt = new ActionEvent(target, 0, new String(new char[] { ch }));
							if (a instanceof BaseAction) {
								((BaseAction) a).actionPerformed(newEvt, target);
							} else {
								a.actionPerformed(newEvt);
							}
						}
					}
				} else {
					// parse the action name
					actionName.setLength(0);
					while (i < len && !Character.isWhitespace(command[i])) {
						char ch = command[i++];
						if (ch == '\\') {
							if (i >= len) {
								// macro ending with single '\'
								error(target, "macro-malformed");
								return;
							}
							;
							ch = command[i++];
							if (ch != '\\' && !Character.isWhitespace(ch)) {//
								error(target, "macro-malformed"); // neither
																	// "\\" nor
																	// "\ "
								return;
							} // else fall through
						}
						actionName.append(ch);
					}
					// execute the action
					Action a = kit.getActionByName(actionName.toString());
					if (a != null) {
						ActionEvent fakeEvt = new ActionEvent(target, 0, "");
						if (a instanceof BaseAction) {
							((BaseAction) a).actionPerformed(fakeEvt, target);
						} else {
							a.actionPerformed(fakeEvt);
						}
					} else {
						error(target, "macro-unknown-action");
						return;
					}
				}
			}
		} finally {
			doc.atomicUnlock();
			m_runningActions.remove(m_macro.getName());
		}
	}
}
