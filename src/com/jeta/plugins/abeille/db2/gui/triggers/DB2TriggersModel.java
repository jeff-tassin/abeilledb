package com.jeta.plugins.abeille.db2.gui.triggers;

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
 * This class is the table model for DB2 Triggers for a given table.
 * 
 * @author Jeff Tassin
 */
public class DB2TriggersModel extends MetaDataTableModel {
	/** column definitions */
	static final int NAME_COLUMN = 0;
	static final int DEFINER_COLUMN = 1;
	static final int WHEN_COLUMN = 2;
	static final int EVENT_COLUMN = 3;
	static final int GRANULARITY_COLUMN = 4;
	static final int VALID_COLUMN = 5;
	static final int CREATED_COLUMN = 6;
	static final int PATH_COLUMN = 7;
	static final int DEFINITION_COLUMN = 8;

	/**
	 * ctor.
	 * 
	 * @param connection
	 *            the underlying database connection
	 * @param tableId
	 *            the id of the table we are displaying the indices for
	 */
	public DB2TriggersModel(TSConnection connection, TableId tableId) {
		super(connection, tableId);

		String[] names = { I18N.getLocalizedMessage("Name"), I18N.getLocalizedMessage("Definer"),
				I18N.getLocalizedMessage("When"), I18N.getLocalizedMessage("Event"),
				I18N.getLocalizedMessage("Granularity"), I18N.getLocalizedMessage("Valid"),
				I18N.getLocalizedMessage("Created"), I18N.getLocalizedMessage("Path"),
				I18N.getLocalizedMessage("Definition") };

		Class[] types = { String.class, String.class, String.class, String.class, String.class, Boolean.class,
				String.class, String.class, String.class };

		setColumnNames(names);
		setColumnTypes(types);
	}

	/**
	 * Helper method that returns the first character in the string. If the
	 * string is null or has zero length, a null character is returned.
	 */
	private static char getChar(String sVal) {
		if (sVal == null || sVal.length() == 0)
			return 0;
		else
			return sVal.charAt(0);
	}

	/**
	 * @return the column value at the given row
	 */
	public Object getValueAt(int row, int column) {
		DB2Trigger t = (DB2Trigger) getRow(row);
		switch (column) {
		case NAME_COLUMN:
			return t.getName();

		case DEFINER_COLUMN:
			return t.getDefiner();

		case WHEN_COLUMN:
			return DB2Trigger.getWhenDescription(t.getWhen());

		case EVENT_COLUMN:
			return DB2Trigger.getEventDescription(t.getEvent());

		case GRANULARITY_COLUMN:
			return DB2Trigger.getGranularityDescription(t.getGranularity());

		case VALID_COLUMN:
			return Boolean.valueOf(t.isValid());

		case CREATED_COLUMN:
			Timestamp ts = t.getTimestamp();
			if (ts == null)
				return "";
			else
				return ts.toString();

		case PATH_COLUMN:
			return t.getFunctionPath();

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
			// select * from SYSCAT.TRIGGERS where TABSCHEMA = 'JETA' and
			// TABNAME='IMAGES'
			sqlbuff.append("select * from SYSCAT.TRIGGERS where TABSCHEMA = '");
			sqlbuff.append(tableid.getSchema().getName());
			sqlbuff.append("' and TABNAME = '");
			sqlbuff.append(tableid.getTableName());
			sqlbuff.append("'");

			Connection conn = getConnection().getMetaDataConnection();
			Statement stmt = null;
			try {
				stmt = conn.createStatement();
				ResultSet rset = stmt.executeQuery(sqlbuff.toString());
				while (rset.next()) {
					String tname = rset.getString("TRIGNAME");
					String definer = rset.getString("DEFINER");
					char twhen = getChar(rset.getString("TRIGTIME")); // A, B, I
					char tevent = getChar(rset.getString("TRIGEVENT")); // I, D,
																		// U
					char gran = getChar(rset.getString("GRANULARITY")); // S, R
					char valid = getChar(rset.getString("VALID")); // Y or N
					Timestamp ts = rset.getTimestamp("CREATE_TIME");
					String func_path = rset.getString("FUNC_PATH");
					String def = DbUtils.getCharacterData(rset.getClob("TEXT"));

					DB2Trigger trigger = new DB2Trigger(tname);
					trigger.setDefiner(definer);
					trigger.setWhen(twhen);
					trigger.setEvent(tevent);
					trigger.setGranularity(gran);
					trigger.setValid((valid == 'Y'));
					trigger.setTimestamp(ts);
					trigger.setFunctionPath(func_path);
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
