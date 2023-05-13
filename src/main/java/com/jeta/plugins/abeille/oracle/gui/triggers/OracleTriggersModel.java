package com.jeta.plugins.abeille.oracle.gui.triggers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.abeille.gui.common.MetaDataTableModel;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class is the table model for Oracle Triggers for a given table.
 * 
 * @author Jeff Tassin
 */
public class OracleTriggersModel extends MetaDataTableModel {
	/** column definitions */
	static final int NAME_COLUMN = 0; // trigger name
	static final int OWNER_COLUMN = 1; // trigger owner
	static final int WHEN_COLUMN = 2;
	static final int EVENT_COLUMN = 3;
	static final int REFERENCING_COLUMN = 4;
	static final int WHEN_CLAUSE_COLUMN = 5;
	static final int STATUS_COLUMN = 6; // true/false
	static final int ACTION_TYPE_COLUMN = 7;
	static final int DEFINITION_COLUMN = 8;

	/**
	 * ctor.
	 * 
	 * @param connection
	 *            the underlying database connection
	 * @param tableId
	 *            the id of the table we are displaying the indices for
	 */
	public OracleTriggersModel(TSConnection connection, TableId tableId) {
		super(connection, tableId);

		String[] names = { I18N.getLocalizedMessage("Name"), I18N.getLocalizedMessage("Owner"),
				I18N.getLocalizedMessage("When"), I18N.getLocalizedMessage("Event"),
				I18N.getLocalizedMessage("Referencing"), I18N.getLocalizedMessage("When Clause"),
				I18N.getLocalizedMessage("Enabled"), I18N.getLocalizedMessage("Type"),
				I18N.getLocalizedMessage("Definition") };

		Class[] types = { String.class, String.class, String.class, String.class, String.class, String.class,
				Boolean.class, String.class, String.class };

		setColumnNames(names);
		setColumnTypes(types);
	}

	/**
	 * @return the column value at the given row
	 */
	public Object getValueAt(int row, int column) {
		OracleTrigger t = (OracleTrigger) getRow(row);
		switch (column) {
		case NAME_COLUMN:
			return t.getName();

		case OWNER_COLUMN:
			return t.getOwner();

		case WHEN_COLUMN:
			return t.getWhen();

		case EVENT_COLUMN:
			return t.getEvent();

		case REFERENCING_COLUMN:
			return t.getReferencing();

		case WHEN_CLAUSE_COLUMN:
			return t.getWhenClause();

		case STATUS_COLUMN:
			return Boolean.valueOf(t.isEnabled());

		case ACTION_TYPE_COLUMN:
			return t.getActionType();

		case DEFINITION_COLUMN:
			return t.getDefinition();

		default:
			return "";
		}
	}

	/**
	 * Reloads the model
	 */
	public void reload() {
		removeAll();

		TableId tableid = getTableId();

		if (tableid == null) {
			fireTableDataChanged();
			return;
		}

		try {
			// now get the index
			StringBuffer sqlbuff = new StringBuffer();
			// select * from SYS.ALL_TRIGGERS where TABLE_OWNER = 'JETA' and
			// TABLE_NAME = 'IMAGES' and ( BASE_OBJECT_TYPE='VIEW' OR
			// BASE_OBJECT_TYPE='TABLE')

			sqlbuff.append("select * from SYS.ALL_TRIGGERS where TABLE_OWNER = '");
			sqlbuff.append(tableid.getSchema().getName());
			sqlbuff.append("' and TABLE_NAME = '");
			sqlbuff.append(tableid.getTableName());
			sqlbuff.append("' and ( BASE_OBJECT_TYPE='VIEW' OR BASE_OBJECT_TYPE='TABLE')");

			Connection conn = getConnection().getMetaDataConnection();
			Statement stmt = null;
			try {
				stmt = conn.createStatement();
				ResultSet rset = stmt.executeQuery(sqlbuff.toString());
				while (rset.next()) {
					String tname = rset.getString("TRIGGER_NAME");
					String towner = rset.getString("OWNER");
					String when = rset.getString("TRIGGER_TYPE");
					String event = rset.getString("TRIGGERING_EVENT");
					String ref = rset.getString("REFERENCING_NAMES");
					String when_clause = rset.getString("WHEN_CLAUSE");
					String status = TSUtils.fastTrim(rset.getString("STATUS"));
					boolean enabled = true;
					if (status == null || status.equalsIgnoreCase("DISABLED"))
						enabled = false;

					String action_type = rset.getString("ACTION_TYPE");
					String def = rset.getString("TRIGGER_BODY");

					OracleTrigger trigger = new OracleTrigger(tname);
					trigger.setName(tname);
					trigger.setOwner(towner);
					trigger.setWhen(when);
					trigger.setEvent(event);
					trigger.setReferencing(ref);
					trigger.setWhenClause(when_clause);
					trigger.setEnabled(enabled);
					trigger.setActionType(action_type);
					trigger.setDefinition(def);
					addRow(trigger);
				}
				conn.commit();
			} finally {
				if (stmt != null)
					stmt.close();
			}
		} catch (SQLException se) {
			TSUtils.printException(se);
		}
		fireTableDataChanged();
	}

	/**
	 * Sets the current table id for the model. Reloads the triggers.
	 */
	public void setTableId(TableId tableId) {
		super.setTableId(tableId);
		reload();

	}
}
