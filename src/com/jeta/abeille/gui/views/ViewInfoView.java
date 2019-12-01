package com.jeta.abeille.gui.views;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.View;

import com.jeta.abeille.gui.jdbc.ColumnInfoPanel;
import com.jeta.abeille.gui.modeler.ColumnsGuiModel;
import com.jeta.abeille.gui.modeler.ColumnsPanel;
import com.jeta.abeille.gui.modeler.ColumnsPanelController;
import com.jeta.abeille.gui.rules.postgres.RulesModel;
import com.jeta.abeille.gui.rules.postgres.RulesView;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.utils.TSUtils;

import com.jeta.foundation.i18n.I18N;

/**
 * This view displays all the properties for a VIEW in a JTabbedPane
 * 
 * @author Jeff Tassin
 */
public class ViewInfoView extends TSPanel {
	/** the table id */
	private TableId m_tableid;

	/** the underlying database connection */
	private TSConnection m_connection;

	/** the model for the table columns */
	private ColumnsGuiModel m_colsmodel;

	/** the view */
	private ViewView m_view;

	/** the model for the table rules */
	private RulesModel m_rulesmodel;
	private RulesView m_rulesview;

	/** JDBC driver info */
	private ColumnInfoPanel m_jdbcview; // this pane shows raw jdbc information
										// about a table in the database

	/**
	 * ctor
	 */
	public ViewInfoView(TSConnection conn) {
		m_connection = conn;
		initialize();
	}

	/**
	 * ctor
	 */
	public ViewInfoView(TSConnection conn, View view) {
		this(conn);
		setView(view);
	}

	/**
	 * Initializes the view
	 */
	private void initialize() {
		setLayout(new BorderLayout());
		JTabbedPane tab = new JTabbedPane();

		m_view = new ViewView(m_connection, true);
		tab.addTab(I18N.getLocalizedMessage("View"), m_view);
		tab.addTab(I18N.getLocalizedMessage("Columns"), createColumnsView());

		if (m_connection.getDatabase().equals(Database.POSTGRESQL)) {
			tab.addTab(I18N.getLocalizedMessage("Rules"), createRulesView());
		}

		tab.addTab("JDBC", createJDBCView());
		add(tab, BorderLayout.CENTER);
	}

	/**
	 * Creates the view that displays the columns
	 */
	private JPanel createColumnsView() {
		m_colsmodel = new ColumnsGuiModel(m_connection, false, null, false);
		ColumnsPanel colsview = new ColumnsPanel(m_colsmodel, false);
		colsview.setToolbarVisible(false);
		colsview.setController(new ColumnsPanelController(colsview));
		return colsview;
	}

	/**
	 * Creates the JDBC Panel. Called by initialize. This panel allows the user
	 * to raw information about the table from the JDBC driver
	 */
	private JPanel createJDBCView() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		m_jdbcview = new ColumnInfoPanel();
		panel.add(m_jdbcview, BorderLayout.CENTER);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return panel;
	}

	/**
	 * Creates the rules view for the selected table
	 */
	private TSPanel createRulesView() {
		m_rulesmodel = new RulesModel(m_connection, null);
		m_rulesview = new RulesView(m_rulesmodel);
		return m_rulesview;
	}

	/**
	 * @return the View represented by the parameters in the GUI
	 */
	public View createView() {
		return m_view.createView();
	}

	/**
	 * @return the preferred size for this view
	 */
	public Dimension getPreferredSize() {
		return m_view.getPreferredSize();
	}

	/**
	 * Sets the current table id for the view. All tabs are updated to display
	 * the properties for the given table
	 */
	public void setTableId(TableId tableId) {
		try {
			TableMetaData tmd = m_connection.getTable(tableId);
			m_colsmodel.setTableMetaData(tmd);
			m_jdbcview.refresh(m_connection.getMetaDataConnection(), tableId);
			if (m_rulesmodel != null) {
				m_rulesmodel.setTableId(tableId);
			}
		} catch (Exception e) {
			TSUtils.printStackTrace(e);
		}
	}

	/**
	 * Sets the view to display
	 */
	public void setView(View view) {
		setTableId((TableId) view.getTableId());
		m_view.setView(view);
	}

	/**
	 * Update the view components
	 */
	public void updateComponents() {
		if (m_rulesview != null) {
			m_rulesview.updateComponents();
		}
	}
}
