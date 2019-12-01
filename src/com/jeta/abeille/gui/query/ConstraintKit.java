package com.jeta.abeille.gui.query;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.common.TableSelectorModel;
import com.jeta.abeille.gui.sql.SQLKit;
import com.jeta.abeille.gui.sql.SQLCompletion;

import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.editor.KitInfo;
import com.jeta.foundation.gui.editor.TSEditorMgr;
import com.jeta.foundation.gui.editor.macros.Macro;
import com.jeta.foundation.gui.quickedit.QuickEditDialog;

import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.MultiKeyBinding;
import org.netbeans.editor.ext.Completion;
import org.netbeans.editor.ext.ExtEditorUI;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.editor.ext.ExtUtilities;

/**
 * Override SQLKit so we can provide our own completion class
 * 
 * @author Jeff Tassin
 */
public class ConstraintKit extends SQLKit {
	/** the model for our completion popup */
	private TableSelectorModel m_model;

	static {
		// KitInfo info = new KitInfo( "text/x-java", ConstraintKit.class );
		// TSEditorMgr.registerEditorKit( info );
	}

	/**
	 * ctor
	 */
	public ConstraintKit(TSConnection conn, TableSelectorModel model) {
		super(conn);
		m_model = model;
	}

	public Completion createCompletion(ExtEditorUI extEditorUI) {
		// this can be null, when we are doing our own completion
		return new SQLCompletion(extEditorUI, m_model, getConnection());
	}

	/**
	 * List all actions supported by this class.
	 */
	public static Action[] listDefaultActions() {
		Action[] actions = new Action[] { new InsertEnterAction(), new EscapeAction() };

		return actions;

	}

	/**
	 * @return the list default macros supported by this kit
	 */
	public static Macro[] listDefaultMacros() {
		return new Macro[0];
	}

	/**
	 * Get the default bindings.
	 */
	public static MultiKeyBinding[] listDefaultKeyBindings() {
		MultiKeyBinding[] bindings = new MultiKeyBinding[] {
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), DefaultEditorKit.insertBreakAction),
				new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), escapeAction) };
		return bindings;
	}

	private static TSDialog getDialog(JTextComponent comp) {
		Window win = SwingUtilities.getWindowAncestor(comp);
		if (win instanceof TSDialog) {
			return (TSDialog) win;
		}
		return null;
	}

	public static final String escapeAction = "constraintkit.escape";

	public static class EscapeAction extends BaseAction {
		public EscapeAction() {
			super(escapeAction);
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			TSDialog dlg = getDialog(target);
			if (dlg != null)
				dlg.cmdCancel();
		}
	}

	public static class InsertEnterAction extends ExtKit.ExtInsertBreakAction {
		public InsertEnterAction() {
			super();
		}

		public void actionPerformed(ActionEvent evt, JTextComponent target) {
			if (target != null) {
				Completion completion = ExtUtilities.getCompletion(target);
				if (completion != null && completion.isPaneVisible()) {
					super.actionPerformed(evt, target);
				} else {
					TSDialog dlg = getDialog(target);
					if (dlg != null)
						dlg.cmdOk();
				}
			} else {
				TSDialog dlg = getDialog(target);
				if (dlg != null)
					dlg.cmdOk();
			}
		}
	}

}
