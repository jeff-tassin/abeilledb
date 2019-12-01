package com.jeta.abeille.gui.indexes.postgres;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Collection;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JTextField;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.gui.procedures.ProcedureBrowser;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;

/**
 * Controller class for the IndexView
 * 
 * @author Jeff Tassin
 */
public class IndexViewController extends TSController implements JETARule {
	/**
	 * The view we are controlling
	 */
	private IndexView m_view;

	/**
	 * ctor
	 */
	public IndexViewController(IndexView view) {
		super(view);
		m_view = view;
		assignAction(IndexView.ID_ADD_COLUMN, new AddColumnAction());
		assignAction(IndexView.ID_REMOVE_COLUMN, new RemoveColumnAction());
		assignAction(IndexView.ID_SHOW_PROCEDURE_BROWSER, new GetProcedureAction());
	}

	/**
	 * Validate inputs
	 */
	public RuleResult check(Object[] params) {
		RuleResult result = RuleResult.SUCCESS;
		String indexname = m_view.getName();
		if (indexname.trim().length() == 0) {
			result = new RuleResult(I18N.getLocalizedMessage("Invalid Name"));
		}

		Collection cols = m_view.getAssignedColumns();
		if (cols.size() == 0) {
			result = new RuleResult(I18N.getLocalizedMessage("Index requires at least one column"));
		}

		if (m_view.isFunctional()) {
			String fname = m_view.getFunctionName();
			if (fname.length() == 0 || fname.charAt(0) == '-') {
				result = new RuleResult(I18N.getLocalizedMessage("Invalid Function Name"));
			}
		}

		return result;
	}

	/**
	 * Adds a column to the assigned columns list for the index
	 */
	public class AddColumnAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JList tablelist = (JList) m_view.getComponentByName(IndexView.ID_AVAILABLE_COLS_LIST);
			ColumnMetaData cmd = (ColumnMetaData) tablelist.getSelectedValue();
			if (cmd != null) {
				JList indexlist = (JList) m_view.getComponentByName(IndexView.ID_ASSIGNED_COLS_LIST);
				DefaultListModel listmodel = (DefaultListModel) indexlist.getModel();
				if (!listmodel.contains(cmd)) {
					listmodel.addElement(cmd);
				}
			}

		}
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
				JTextField tf = (JTextField) m_view.getComponentByName(IndexView.ID_FUNCTION_FIELD);
				tf.setText(fbuff.toString());
			}
		}
	}

	/**
	 * Removes a column from the assigned columns list for the index
	 */
	public class RemoveColumnAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JList indexlist = (JList) m_view.getComponentByName(IndexView.ID_ASSIGNED_COLS_LIST);
			Object obj = indexlist.getSelectedValue();
			if (obj != null) {
				DefaultListModel listmodel = (DefaultListModel) indexlist.getModel();
				listmodel.removeElement(obj);
			}
		}
	}

}
