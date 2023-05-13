package com.jeta.abeille.gui.keys;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.event.TableModelEvent;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSDatabase;

import com.jeta.abeille.gui.common.MetaDataTableModel;

import com.jeta.abeille.gui.indexes.TableIndex;
import com.jeta.abeille.gui.indexes.IndexColumn;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * @author Jeff Tassin
 */
public class KeysModel extends MetaDataTableModel {
	private boolean m_imported = true;

	/** column definitions */
	static final int PK_TABLE_COLUMN = 0;
	static final int PK_COLUMN_COLUM = 1;
	static final int FK_TABLE_COLUMN = 2;
	static final int FK_COLUMN_COLUMN = 3;
	static final int KEY_SEQ_COLUMN = 4;
	static final int UPDATE_RULE_COLUMN = 5;
	static final int DELETE_RULE_COLUMN = 6;
	static final int DEFERABILITY_COLUMN = 7;

	static final int PK_NAME = 8;
	static final int PK_CATALOG_COLUMN = 9;
	static final int PK_SCHEMA_COLUMN = 10;

	static final int FK_NAME = 11;
	static final int FK_CATALOG_COLUMN = 12;
	static final int FK_SCHEMA_COLUMN = 13;

	/**
	 * ctor.
	 * 
	 * @param connection
	 *            the underlying database connection
	 * @param tableId
	 *            the id of the table we are displaying the indices for
	 * @param imported
	 *            if true get the imported keys for the given table. if false,
	 *            get the exported keys
	 */
	public KeysModel(TSConnection connection, TableId tableId, boolean imported) {
		super(connection, tableId);
		m_imported = imported;

		String[] names = { "PK_TABLE", "PK_COLUMN", "FK_TABLE", "FK_COLUMN", I18N.getLocalizedMessage("Key Seq"),
				"UPDATE_RULE", "DELETE_RULE", "DEFERABILITY", "PK_NAME", "PK_CATALOG", "PK_SCHEMA", "FK_NAME",
				"FK_CATALOG", "FK_SCHEMA" };

		Class[] types = { String.class, String.class, String.class, String.class, Integer.class, String.class,
				String.class, String.class, String.class, String.class, String.class, String.class, String.class,
				String.class };

		setColumnNames(names);
		setColumnTypes(types);
	}

	/**
	 * @return the column value at the given row
	 */
	public Object getValueAt(int row, int column) {
		Key ek = (Key) getRow(row);

		if (column == PK_CATALOG_COLUMN) {
			return ek.getPKCatalog();
		} else if (column == PK_SCHEMA_COLUMN) {
			return ek.getPKSchema();
		} else if (column == PK_TABLE_COLUMN) {
			return ek.getPKTable();
		} else if (column == PK_COLUMN_COLUM) {
			return ek.getPKColumn();
		} else if (column == FK_CATALOG_COLUMN) {
			return ek.getFKCatalog();
		} else if (column == FK_SCHEMA_COLUMN) {
			return ek.getFKSchema();
		} else if (column == FK_TABLE_COLUMN) {
			return ek.getFKTable();
		} else if (column == FK_COLUMN_COLUMN) {
			return ek.getFKColumn();
		} else if (column == KEY_SEQ_COLUMN) {
			return ek.getKeySequence();
		} else if (column == UPDATE_RULE_COLUMN) {
			return ek.getUpdateRuleDescription();
		} else if (column == DELETE_RULE_COLUMN) {
			return ek.getDeleteRuleDescription();
		} else if (column == DEFERABILITY_COLUMN) {
			return ek.getDeferrabilityDescription();
		} else if (column == PK_NAME) {
			return ek.getPKName();
		} else if (column == FK_NAME) {
			return ek.getFKName();
		} else
			return "";
	}

	/**
	 * Reloads the model
	 */
	public void reload() {
		removeAll();
		if (getTableId() == null) {
			fireTableDataChanged();
			return;
		}

		try {
			TSConnection tsconn = getConnection();
			Connection conn = tsconn.getMetaDataConnection();
			DatabaseMetaData metadata = conn.getMetaData();
			TableId tableid = getTableId();

			Catalog catalog = tableid.getCatalog();
			Schema schema = tableid.getSchema();

			String catname = null;
			String schemaname = null;

			if (catalog.getName() == null || catalog.getName().length() == 0 || catalog == Catalog.EMPTY_CATALOG
					|| catalog == Catalog.VIRTUAL_CATALOG) {
				catname = null;
			} else {
				catname = catalog.getName();
			}

			if (schema.getName() == null || schema.getName().length() == 0 || schema == Schema.VIRTUAL_SCHEMA) {
				schemaname = null;
			} else {
				schemaname = schema.getName();
			}

			ResultSet rset = null;

			if (m_imported) {
				rset = metadata.getImportedKeys(catname, schemaname, tableid.getTableName());
			} else {
				rset = metadata.getExportedKeys(catname, schemaname, tableid.getTableName());
			}

			while (rset.next()) {

				String pk_catalog = rset.getString("PKTABLE_CAT");
				String pk_schema = rset.getString("PKTABLE_SCHEM");
				String pk_table = rset.getString("PKTABLE_NAME");
				String pk_column = rset.getString("PKCOLUMN_NAME");

				String fk_catalog = rset.getString("FKTABLE_CAT");
				String fk_schema = rset.getString("FKTABLE_SCHEM");
				String fk_table = rset.getString("FKTABLE_NAME");
				String fk_column = rset.getString("FKCOLUMN_NAME");
				int key_seq = rset.getShort("KEY_SEQ");

				Key key = new Key();
				key.setPKCatalog(pk_catalog);
				key.setPKSchema(pk_schema);
				key.setPKTable(pk_table);
				key.setPKColumn(pk_column);

				key.setFKCatalog(fk_catalog);
				key.setFKSchema(fk_schema);
				key.setFKTable(fk_table);
				key.setFKColumn(fk_column);

				key.setUpdateRule(TSUtils.getInteger(rset.getShort("UPDATE_RULE")));
				key.setDeleteRule(TSUtils.getInteger(rset.getShort("DELETE_RULE")));
				key.setPKName(rset.getString("PK_NAME"));
				key.setFKName(rset.getString("FK_NAME"));
				key.setDeferrability(TSUtils.getInteger(rset.getShort("DEFERRABILITY")));

				key.setKeySequence(TSUtils.getInteger(key_seq));
				addRow(key);
			}

			if (rset != null)
				rset.close();
		} catch (Exception e) {
			TSUtils.printException(e);
		}

		fireTableDataChanged();
	}

	/**
	 * Sets the current table id for the model. Reloads the indices.
	 */
	public void setTableId(TableId tableId) {
		super.setTableId(tableId);
		reload();
	}

}
