package com.jeta.abeille.gui.triggers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextField;

import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.gui.procedures.ProcedureBrowser;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;

/**
 * Controller/event handler for the TriggerView class.
 * 
 * @author Jeff Tassin
 */
public class TriggerViewController extends TSController implements JETARule {
	/** the view we are controlling */
	private TriggerView m_view;

	/**
	 * ctor
	 */
	public TriggerViewController(TriggerView view) {
		super(view);
		m_view = view;
		assignAction(TriggerView.ID_SHOW_PROCEDURE_BROWSER, new GetProcedureAction());
	}

	/**
	 * Validate the information in the view
	 */
	public RuleResult check(Object[] params) {
		String triggername = m_view.getTriggerName();
		if (triggername.length() == 0) {
			return new RuleResult(I18N.getLocalizedMessage("Invalid Name"));
		}

		String funcname = m_view.getFunctionName();
		if (funcname.length() == 0) {
			return new RuleResult(I18N.getLocalizedMessage("Invalid Function Name"));
		}

		if (!m_view.isEventSelected()) {
			return new RuleResult(I18N.getLocalizedMessage("At least one event must be selected"));
		}
		return RuleResult.SUCCESS;
	}

	/**
	 * Action Handler for getting a procedure. Launches the ProcedureBrowser and
	 * allows the user to selected a procedure.
	 */
	public class GetProcedureAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ProcedureBrowser browser = new ProcedureBrowser(m_view.getConnection(), m_view.getFunctionName());
			TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_view, true);
			dlg.setPrimaryPanel(browser);
			TSGuiToolbox.setReasonableWindowSize(dlg, dlg.getPreferredSize());
			dlg.showCenter();
			if (dlg.isOk()) {
				Schema schema = browser.getSelectedSchema();
				String name = browser.getSelectedProcedureName();

				StringBuffer fbuff = new StringBuffer();
				if (schema != null && schema != Schema.VIRTUAL_SCHEMA) {
					fbuff.append(schema.getName());
					fbuff.append(".");
				}
				fbuff.append(name);
				JTextField tf = (JTextField) m_view.getComponentByName(TriggerView.ID_FUNCTION_FIELD);
				tf.setText(fbuff.toString());
			}
		}
	}
}
