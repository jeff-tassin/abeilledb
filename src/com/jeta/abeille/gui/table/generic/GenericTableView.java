package com.jeta.abeille.gui.table.generic;

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
import com.jeta.abeille.gui.indexes.generic.GenericIndexesModel;

import com.jeta.abeille.gui.keys.KeysView;
import com.jeta.abeille.gui.keys.KeysModel;

import com.jeta.abeille.gui.modeler.ColumnsGuiModel;
import com.jeta.abeille.gui.modeler.ColumnsPanel;
import com.jeta.abeille.gui.modeler.PrimaryKeyView;
import com.jeta.abeille.gui.modeler.SQLPanel;

import com.jeta.abeille.gui.jdbc.ColumnInfoPanel;

import com.jeta.abeille.gui.security.generic.TablePrivilegesView;

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
public class GenericTableView extends TableView implements TableIdGetter {

	/** the model for the table columns */
	private ColumnsGuiModel m_colsmodel;

	/** the model for the table indices */
	private GenericIndexesModel m_indexesmodel;

	/** the model for the imported keys */
	private KeysModel m_importedkeysmodel;

	/** the model for the exported keys */
	private KeysModel m_exportedkeysmodel;

	/** the view for the primary key */
	private PrimaryKeyView m_pkview;

	/** JDBC table privileges view */
	private TablePrivilegesView m_privs_view;

	/** JDBC driver info */
	private ColumnInfoPanel m_jdbcview; // this pane shows raw jdbc information
										// about a table in the database

	/** panel that displays the SQL to create the table */
	private SQLPanel m_sqlview;

	/**
	 * ctor
	 */
	public GenericTableView(TSConnection conn) {
		super(conn);
		initialize();
	}

	/**
	 * Creates the view that displays the columns
	 */
	private TSPanel createColumnsView() {
		m_colsmodel = new ColumnsGuiModel(getConnection(), false, null, false);
		ColumnsPanel colsview = new ColumnsPanel(m_colsmodel, false);
		return colsview;
	}

	/**
	 * Creates the exported keys view for the selected table
	 */
	private TSPanel createKeysView(boolean imported) {
		if (imported) {
			m_importedkeysmodel = new KeysModel(getConnection(), null, imported);
			KeysView view = new KeysView(m_importedkeysmodel);
			return view;
		} else {
			m_exportedkeysmodel = new KeysModel(getConnection(), null, imported);
			KeysView view = new KeysView(m_exportedkeysmodel);
			return view;
		}
	}

	/**
	 * Creates the indices view for the selected table
	 */
	private TSPanel createIndexesView() {
		m_indexesmodel = new GenericIndexesModel(getConnection(), null);
		IndexesView view = new IndexesView(m_indexesmodel);
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
	 * Creates the primary key view
	 */
	private TSPanel createPrimaryKeyView() {
		m_pkview = new PrimaryKeyView(getConnection(), m_colsmodel, null, false);
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
	 * Creates the view that displays the table privileges
	 */
	private TSPanel createTablePrivilegesView() {
		m_privs_view = new TablePrivilegesView(getConnection());
		return m_privs_view;
	}

	/**
	 * Initializes and creates the sub-views for this view
	 */
	private void initialize() {
		addView(I18N.getLocalizedMessage("Columns"), createColumnsView(), null);
		addView(I18N.getLocalizedMessage("Primary Key"), createPrimaryKeyView(), null);
		addView(I18N.getLocalizedMessage("Imported Keys"), createKeysView(true), null);
		addView(I18N.getLocalizedMessage("Exported Keys"), createKeysView(false), null);
		addView(I18N.getLocalizedMessage("Indexes"), createIndexesView(), null);
		addView(I18N.getLocalizedMessage("Table Privileges"), createTablePrivilegesView(), null);
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
			m_jdbcview.refresh(getConnection().getMetaDataConnection(), tableId);
			m_indexesmodel.setTableId(tableId);
			m_importedkeysmodel.setTableId(tableId);
			m_exportedkeysmodel.setTableId(tableId);
			m_privs_view.setTableId(tableId);
			if (tmd == null) {
				m_sqlview.setText("");
			} else {
				assert (tmd.getCatalog() != null);
				assert (tmd.getSchema() != null);
				m_sqlview.setText(DbUtils.createTableSQL(getConnection(), tmd));
			}
		} catch (Exception e) {
			TSUtils.printStackTrace(e);
		}

	}
}
