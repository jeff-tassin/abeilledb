package com.jeta.abeille.gui.checks.postgres;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.TableColumnModel;

import com.jeta.foundation.gui.components.TSPanel;

import com.jeta.foundation.gui.table.AbstractTablePanel;
import com.jeta.foundation.gui.table.TableUtils;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * This class displays a list of check constraints assigned to a given table
 * 
 * @author Jeff Tassin
 */
public class ChecksView extends TSPanel {
	/** the table that displays the checks */
	private JTable m_table;

	/* model for the checks */
	private ChecksModel m_model;

	public static final String ID_CREATE_CHECK = "create.check";
	public static final String ID_EDIT_CHECK = "edit.check";
	public static final String ID_DELETE_CHECK = "delete.check";

	/**
	 * ctor
	 */
	public ChecksView(ChecksModel model) {
		setLayout(new BorderLayout());
		m_model = model;
		// add( createButtonPanel(), BorderLayout.NORTH );
		add(createTable(), BorderLayout.CENTER);
		// setController( new ChecksViewController(this) );
		setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 0, 5));
	}

	/**
	 * Creates the toolbar buttons panel
	 */
	private JComponent createButtonPanel() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);

		JButton btn = i18n_createToolBarButton("general/New16.gif", ID_CREATE_CHECK,
				I18N.getLocalizedMessage("Create Check"));
		toolbar.add(btn);

		btn = i18n_createToolBarButton("general/Edit16.gif", ID_EDIT_CHECK, I18N.getLocalizedMessage("Edit Check"));
		toolbar.add(btn);

		btn = i18n_createToolBarButton("general/Delete16.gif", ID_DELETE_CHECK, I18N.getLocalizedMessage("Drop Check"));
		toolbar.add(btn);

		return toolbar;
	}

	/**
	 * Creates the JTable that displays the table triggers
	 * 
	 * @returns the table component
	 */
	private JComponent createTable() {
		AbstractTablePanel tspanel = TableUtils.createBasicTablePanel(m_model, true);
		m_table = tspanel.getTable();
		TableUtils.setColumnWidth(m_table, ChecksModel.EXPRESSION_COLUMN, 50);
		TableUtils.setColumnWidth(m_table, ChecksModel.NAME_COLUMN, 10);
		return tspanel;
	}

	/**
	 * @return the underlying data model
	 */
	public ChecksModel getModel() {
		return m_model;
	}

	/**
	 * @return the selected check. Null is returned if no check is selected
	 */
	public CheckConstraint getSelectedCheck() {
		int row = m_table.getSelectedRow();
		if (row >= 0) {
			row = TableUtils.convertTableToModelIndex(m_table, row);
			return m_model.getRow(row);
		} else {
			return null;
		}
	}
}
