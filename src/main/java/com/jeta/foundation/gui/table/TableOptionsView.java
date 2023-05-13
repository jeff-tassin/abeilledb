/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.table;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.jeta.foundation.gui.components.TSPanel;

/**
 * This view shows the options for a given table. Basically, it allows the user
 * to make a column visible/invisible and change the ordering of a column for a
 * table in the TSTable panel.
 * 
 * @author Jeff Tassin
 */
public class TableOptionsView extends TSPanel {
	/** the table model for our table */
	private TableOptionsModel m_optionsmodel;

	/** the JTable */
	private JTable m_optionstable;

	/**
	 * the table panel we are configuring options for. We need this becuase in
	 * most cases we don't allow the user to hide every column, so we need to do
	 * some checks
	 */
	private TSTablePanel m_tablepanel;

	/** command ids */
	public static final String ID_MOVE_UP = "moveup";
	public static final String ID_MOVE_DOWN = "movedown";

	public static final String ID_OPTIONS_TABLE = "tableoptions.table";
	public static final String ID_TABLE_SCROLL = "tableoptions.scroll";
	public static final String ID_TOOLBAR = "tableoptions.toolbar";

	public TableOptionsView(TableOptionsModel model, TSTablePanel panel) {
		m_optionsmodel = model;
		m_tablepanel = panel;
		initialize();

	}

	/**
	 * @return the model coordinates of the table row. Remember that the table
	 *         can be sorted, so the table row can have a different value than
	 *         the model row
	 */
	public int convertTableToModelIndex(int tableRow) {
		return tableRow;
	}

	/**
	 * creates the panel that contains the buttons for this view
	 */
	private JComponent createControlsComponent() {
		javax.swing.JToolBar toolbar = new javax.swing.JToolBar();
		toolbar.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
		toolbar.setName(ID_TOOLBAR);

		toolbar.setFloatable(false);

		JButton moveupbtn = i18n_createToolBarButton(null, ID_MOVE_UP, "incors/16x16/navigate_up.png");
		moveupbtn.setBorderPainted(false);
		moveupbtn.setFocusPainted(false);

		JButton movedownbtn = i18n_createToolBarButton(null, ID_MOVE_DOWN, "incors/16x16/navigate_down.png");
		movedownbtn.setBorderPainted(false);
		movedownbtn.setFocusPainted(false);

		toolbar.add(moveupbtn);
		toolbar.add(movedownbtn);

		return toolbar;
	}

	/**
	 * @return the preferred size for this panel
	 */
	public Dimension getPreferredSize() {
		return new Dimension(600, 200);
	}

	/**
	 * @return the data model
	 */
	public TableOptionsModel getModel() {
		return m_optionsmodel;
	}

	/**
	 * @return the index of the selected row in the table. The row is in table
	 *         coordinates, not model coordinates
	 */
	public int getSelectedRow() {
		return m_optionstable.getSelectedRow();
	}

	/**
	 * @return the JTable component that displays the options for the given
	 *         database table
	 */
	public JTable getTable() {
		return m_optionstable;
	}

	/**
	 * @return the TSTablePanel component that we are configuring options for
	 */
	public TSTablePanel getTablePanel() {
		return m_tablepanel;
	}

	/**
	 * Creates and initializes the components and view
	 */
	private void initialize() {
		setLayout(new BorderLayout());
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(5, 5));

		m_optionstable = new JTable(m_optionsmodel);
		m_optionstable.setName(ID_OPTIONS_TABLE);
		// m_optionstable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF );
		m_optionstable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_optionstable.setColumnSelectionAllowed(true);
		m_optionstable.setRowSelectionAllowed(true);
		m_optionstable.setPreferredScrollableViewportSize(new Dimension(500, 70));

		JScrollPane scrollpane = new JScrollPane(m_optionstable);
		scrollpane.setName(ID_TABLE_SCROLL);
		TableUtils.setRowHeader(m_optionstable, 3);

		panel.add(scrollpane, BorderLayout.CENTER);

		panel.add(createControlsComponent(), BorderLayout.NORTH);

		add(panel, BorderLayout.CENTER);
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// set table widths. we only need to set the table width of the name
		// column, all
		// subsequent columns will be sized automatically
		FontMetrics fm = m_optionstable.getFontMetrics(m_optionstable.getFont());
		String coltitle = m_optionsmodel.getColumnName(TableOptionsModel.VISIBLE_COLUMN);
		// int colwidth = fm.stringWidth( coltitle ) + 32;

		Dimension d = getPreferredSize();
		int colwidth = d.width / 2;
		TableColumnModel cmodel = m_optionstable.getColumnModel();
		TableColumn col = cmodel.getColumn(TableOptionsModel.COLUMN_NAME_COLUMN);
		col.setWidth(colwidth);
		col.setPreferredWidth(colwidth);

	}

}
