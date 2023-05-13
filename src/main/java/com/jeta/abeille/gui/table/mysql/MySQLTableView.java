package com.jeta.abeille.gui.table.mysql;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableIdGetter;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.abeille.gui.common.DefaultTableSelectorModel;

import com.jeta.abeille.gui.indexes.IndexesView;
import com.jeta.abeille.gui.indexes.mysql.MySQLIndexesModel;
import com.jeta.abeille.gui.indexes.mysql.MySQLIndexesViewController;

import com.jeta.abeille.gui.model.ModelerModel;
import com.jeta.abeille.gui.modeler.AlterColumnsController;
import com.jeta.abeille.gui.modeler.AlterForeignKeysController;
import com.jeta.abeille.gui.modeler.AlterPrimaryKeyController;
import com.jeta.abeille.gui.modeler.ColumnsGuiModel;
import com.jeta.abeille.gui.modeler.ColumnsPanel;
import com.jeta.abeille.gui.modeler.ForeignKeysModel;
import com.jeta.abeille.gui.modeler.ForeignKeysView;
import com.jeta.abeille.gui.modeler.PrimaryKeyView;
import com.jeta.abeille.gui.modeler.SQLPanel;
import com.jeta.abeille.gui.modeler.TableChangedListener;

import com.jeta.abeille.gui.jdbc.ColumnInfoPanel;

import com.jeta.abeille.gui.table.TableView;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This view displays all the properties for a table in a JTabbedPane
 * 
 * @author Jeff Tassin
 */
public class MySQLTableView extends TableView implements TableIdGetter, TableChangedListener {
	/** the columns model for the table */
	private ColumnsGuiModel m_colsmodel;

	/** the view for the primary key */
	private PrimaryKeyView m_pkview;

	/** the foreign key model */
	private ForeignKeysModel m_foreignkeymodel;

	/** the model for the table indices */
	private MySQLIndexesModel m_indicesmodel;

	/** the table attributes view for MySQL */
	private MySQLTableAttributesView m_mysqlview;

	/** displays the MySQL column status */
	private MySQLColumnStatusView m_colstatusview;

	/** displays the MySQL table status */
	private MySQLTableStatusView m_tablestatusview;

	/** JDBC driver info */
	private ColumnInfoPanel m_jdbcview; // this pane shows raw jdbc information
										// about a table in the database

	// //////////////////////////////////////////////////////
	// sql pane
	private SQLPanel m_sqlview;

	/**
	 * ctor
	 */
	public MySQLTableView(TSConnection conn) {
		super(conn);
		initialize();
		setController(new MySQLTableViewController(this));
	}

	/**
	 * @return the view that displays the MySQL column status
	 */
	private TSPanel createColumnStatusView() {
		m_colstatusview = new MySQLColumnStatusView(getConnection());
		return m_colstatusview;
	}

	/**
	 * Creates the view that displays the columns
	 */
	private TSPanel createColumnsView() {
		m_colsmodel = new ColumnsGuiModel(getConnection(), false, null, false);
		ColumnsPanel colsview = new ColumnsPanel(m_colsmodel, false);
		// colsview.setController( new AlterColumnsController( colsview ));
		return colsview;
	}

	/**
	 * Creates the foreign keys view
	 */
	private TSPanel createForeignKeysView() {

		ForeignKeysView fkview = new ForeignKeysView(m_colsmodel, getConnection(),
				ModelerModel.getDefaultInstance(getConnection()), this, false);
		// fkview.setController( new AlterForeignKeysController( fkview ) );
		m_foreignkeymodel = fkview.getModel();
		return fkview;
	}

	/**
	 * Creates the indices view for the selected table
	 */
	private TSPanel createIndexesView() {
		m_indicesmodel = new MySQLIndexesModel(getConnection(), null);
		IndexesView view = new IndexesView(m_indicesmodel);
		// view.setController( new MySQLIndexesViewController( view ) );
		return view;
	}

	/**
	 * Creates the JDBC Panel. Called by initialize. This panel allows the user
	 * to raw information about the table from the JDBC driver
	 */
	private TSPanel createJDBCView() {
		m_jdbcview = new ColumnInfoPanel();
		m_jdbcview.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return m_jdbcview;
	}

	/**
	 * Creates the MySQLAttributes View
	 */
	private TSPanel createMySQLAttributesView() {
		m_mysqlview = new MySQLTableAttributesView(getConnection());
		return m_mysqlview;
	}

	/**
	 * Creates the primary key view
	 */
	private TSPanel createPrimaryKeyView() {
		m_pkview = new PrimaryKeyView(getConnection(), m_colsmodel, null, false);
		// m_pkview.setController( new AlterPrimaryKeyController( m_pkview, this
		// ) );
		return m_pkview;
	}

	/**
	 * Creates the SQL Panel. Called by initialize. This panel allows the user
	 * to view/tweak the generated SQL
	 */
	private SQLPanel createSQLView() {
		m_sqlview = new SQLPanel(getConnection());
		return m_sqlview;
	}

	/**
	 * @return the view that displays the MySQL table status for the selected
	 *         table
	 */
	private TSPanel createTableStatusView() {
		m_tablestatusview = new MySQLTableStatusView(getConnection());
		return m_tablestatusview;
	}

	/**
	 * Initializes and creates the sub-views for this view
	 */
	private void initialize() {
		addView(I18N.getLocalizedMessage("Columns"), createColumnsView(), null);
		addView(I18N.getLocalizedMessage("Primary Key"), createPrimaryKeyView(), null);
		addView("MySQL", createMySQLAttributesView(), null);
		addView(I18N.getLocalizedMessage("Foreign Keys"), createForeignKeysView(), null);
		addView(I18N.getLocalizedMessage("Indexes"), createIndexesView(), null);
		addView(I18N.getLocalizedMessage("Column Status"), createColumnStatusView(), null);
		addView(I18N.getLocalizedMessage("Table Status"), createTableStatusView(), null);
		addView(I18N.getLocalizedMessage("SQL"), createSQLView(), null);
		addView("JDBC", createJDBCView(), null);
	}

	/**
	 * Sets the current table id for the view. All tabs are updated to display
	 * the properties for the given table
	 */
	public void setTableId(TableId tableId) {
		try {
			super.setTableId(tableId);

			// make sure the table is fully loaded
			TableMetaData tmd = null;
			if (tableId != null) {
				tmd = getConnection().getModel(tableId.getCatalog()).getTableEx(tableId,
						TableMetaData.LOAD_FOREIGN_KEYS | TableMetaData.LOAD_COLUMNS_EX);
			}

			m_colsmodel.setTableId(tableId);
			m_pkview.loadData(m_colsmodel);

			m_foreignkeymodel.setTableId(tableId);
			m_indicesmodel.setTableId(tableId);
			m_mysqlview.setTableId(tableId);
			m_jdbcview.refresh(getConnection().getMetaDataConnection(), tableId);
			m_colstatusview.setTableId(tableId);
			m_tablestatusview.setTableId(tableId);

			if (tmd == null)
				m_sqlview.setText("");
			else
				m_sqlview.setText(DbUtils.createTableSQL(getConnection(), tmd));
		} catch (Exception e) {
			TSUtils.printStackTrace(e);
		}

	}

	/**
	 * TableChangedListener implementation
	 */
	public void tableChanged(TableId tableid) {
		getConnection().getModel(tableid.getCatalog()).reloadTable(tableid);
		setTableId(tableid);
	}

}
