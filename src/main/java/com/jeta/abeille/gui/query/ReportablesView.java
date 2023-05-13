package com.jeta.abeille.gui.query;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;

import com.jeta.abeille.gui.common.MetaDataTableRenderer;

import com.jeta.foundation.gui.components.BasicPopupMenu;
import com.jeta.foundation.gui.components.TSPanelEx;
import com.jeta.foundation.gui.table.TableUtils;

import com.jeta.foundation.i18n.I18N;

/**
 * This class displays a list of constraints for a query in the query builder
 * window.
 * 
 * @author Jeff Tassin
 */
public class ReportablesView extends TSPanelEx {
	/** the table component that displayes the reportables */
	private JTable m_table;

	/** the scroll pane for the table */
	private JScrollPane m_scrollpane;

	private ReportablesModel m_model; // model for the constraint

	public static final String ID_REPORTABLES_SCROLL = "reportables.scroll";
	public static final String ID_REPORTABLES_TABLE = "reportables.table";

	/**
	 * ctor
	 */
	public ReportablesView(ReportablesModel model) {
		m_model = model;
		setLayout(new BorderLayout());
		add(createTable(), BorderLayout.CENTER);

		setPopupMenu(new BasicPopupMenu(this), m_table);
		JScrollPane scroll = (JScrollPane) getComponentByName(ReportablesView.ID_REPORTABLES_SCROLL);
		scroll.addMouseListener(getPopupHandler());
	}

	/**
	 * @return the model coordinates of the table row. Remember that the table
	 *         can be sorted, so the table row can have a different value than
	 *         the model row
	 */
	public int convertTableToModelIndex(int tableRow) {
		return TableUtils.convertTableToModelIndex(m_table, tableRow);
	}

	/**
	 * Creates the JTable that displays the JDBC driver properties
	 * 
	 * @returns the table component
	 */
	private JComponent createTable() {
		m_table = TableUtils.createSimpleTable(m_model, false).getTable();
		m_table.setName(ID_REPORTABLES_TABLE);

		m_scrollpane = new JScrollPane(m_table);
		m_scrollpane.getViewport().setBackground(Color.white);
		m_scrollpane.setName(ID_REPORTABLES_SCROLL);

		TableUtils.setColumnWidths(m_table);

		TableColumnModel cmodel = m_table.getColumnModel();
		MetaDataTableRenderer renderer = MetaDataTableRenderer.createRenderer(m_model.getConnection());
		cmodel.getColumn(ReportablesModel.TABLE_NAME_COLUMN).setCellRenderer(renderer);
		cmodel.getColumn(ReportablesModel.COLUMN_NAME_COLUMN).setCellRenderer(renderer);

		return m_scrollpane;
	}

	/**
	 * @return the underlying data model for this view
	 */
	public ReportablesModel getModel() {
		return m_model;
	}

	/**
	 * @return the selected row in the table
	 */
	int getSelectedRow() {
		return m_table.getSelectedRow();
	}

	/**
	 * @return an array of selected rows
	 */
	int[] getSelectedRows() {
		return m_table.getSelectedRows();
	}

	/**
	 * @return the underlying JTable component that makes up this view
	 */
	JTable getTable() {
		return m_table;
	}

}
