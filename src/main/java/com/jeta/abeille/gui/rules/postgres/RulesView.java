package com.jeta.abeille.gui.rules.postgres;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.TableColumnModel;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.table.TableUtils;
import com.jeta.foundation.gui.table.AbstractTablePanel;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * This class displays a list of rules assigned to a given table
 * 
 * @author Jeff Tassin
 */
public class RulesView extends TSPanel {
	/** the table that displays the rules */
	private JTable m_table;

	/* model for the rules */
	private RulesModel m_model;

	public static final String ID_CREATE_RULE = "create.rule";
	public static final String ID_EDIT_RULE = "edit.rule";
	public static final String ID_DROP_RULE = "drop.rule";

	/**
	 * ctor
	 */
	public RulesView(RulesModel model) {
		setLayout(new BorderLayout());
		m_model = model;
		// add( createButtonPanel(), BorderLayout.NORTH );
		add(createTable(), BorderLayout.CENTER);
		// setController( new RulesViewController(this) );
		setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
	}

	/**
	 * Creates the toolbar buttons panel
	 */
	private JComponent createButtonPanel() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		JButton btn = i18n_createToolBarButton("general/New16.gif", ID_CREATE_RULE, "Create Rule");
		toolbar.add(btn);

		btn = i18n_createToolBarButton("general/Edit16.gif", ID_EDIT_RULE, "Modify Rule");
		toolbar.add(btn);

		btn = i18n_createToolBarButton("general/Delete16.gif", ID_DROP_RULE, "Drop Rule");
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
		TableUtils.setColumnWidth(m_table, RulesModel.EXPRESSION_COLUMN, 50);
		TableUtils.setColumnWidth(m_table, RulesModel.NAME_COLUMN, 10);
		return tspanel;
	}

	/**
	 * @return the underlying data model
	 */
	public RulesModel getModel() {
		return m_model;
	}

	/**
	 * @return the selected check. Null is returned if no check is selected
	 */
	public Rule getSelectedRule() {
		int row = m_table.getSelectedRow();
		if (row >= 0) {
			row = TableUtils.convertTableToModelIndex(m_table, row);
			return m_model.getRow(row);
		} else {
			return null;
		}
	}
}
