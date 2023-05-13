package com.jeta.abeille.gui.indexes.mysql;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Collection;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JTable;
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
 * Controller class for the MySQLIndexView
 * 
 * @author Jeff Tassin
 */
public class MySQLIndexViewController extends TSController implements JETARule {
	/**
	 * The view we are controlling
	 */
	private MySQLIndexView m_view;

	/**
	 * ctor
	 */
	public MySQLIndexViewController(MySQLIndexView view) {
		super(view);
		m_view = view;
		assignAction(MySQLIndexView.ID_ADD_COLUMN, new AddColumnAction());
		assignAction(MySQLIndexView.ID_REMOVE_COLUMN, new RemoveColumnAction());
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

		try {
			Collection cols = m_view.getAssignedColumns();
			if (cols.size() == 0) {
				result = new RuleResult(I18N.getLocalizedMessage("Index requires at least one column"));
			}
		} catch (NumberFormatException nfe) {
			result = new RuleResult(I18N.getLocalizedMessage("Invalid length"));
		}
		return result;
	}

	/**
	 * Adds a column to the assigned columns list for the index
	 */
	public class AddColumnAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JTable table = (JTable) m_view.getComponentByName(MySQLIndexView.ID_AVAILABLE_TABLE);
			int row = table.getSelectedRow();
			if (row >= 0) {
				ColumnMetaData cmd = (ColumnMetaData) table.getValueAt(row, 0);
				m_view.assignColumn(cmd);
			}
		}
	}

	/**
	 * Removes a column from the assigned columns list for the index
	 */
	public class RemoveColumnAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {

		}
	}

}
