package com.jeta.plugins.abeille.postgres;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.database.procedures.ParameterDirection;
import com.jeta.abeille.database.procedures.ProcedureParameter;
import com.jeta.abeille.database.triggers.Trigger;
import com.jeta.abeille.database.triggers.TriggerService;
import com.jeta.abeille.gui.command.SQLCommand;

import com.jeta.foundation.i18n.I18N;

/**
 * This class defines a service for listing all triggers for a given table as
 * well as locating an individual trigger from a trigger key
 * 
 * @author Jeff Tassin
 */
public class TriggerServiceImplementation implements TriggerService {
	/** the underlying database connection */
	private TSConnection m_connection;

	/**
	 * ctor
	 */
	public TriggerServiceImplementation() {

	}

	/**
	 * ctor
	 */
	public TriggerServiceImplementation(TSConnection conn) {
		setConnection(conn);
	}

	/**
	 * Creates the given trigger in the database
	 */
	public void createTrigger(Trigger newtrigger) throws SQLException {
		String sql = getCreateTriggerSQL(newtrigger);
		SQLCommand.runMetaDataCommand(getConnection(), I18N.getLocalizedMessage("Create Trigger"), sql);
	}

	/**
	 * Drops the given trigger from its table
	 */
	public void dropTrigger(Trigger dropTrigger, boolean cascade) throws SQLException {
		String sql = getDropTriggerSQL(dropTrigger, cascade);
		SQLCommand.runMetaDataCommand(getConnection(), I18N.getLocalizedMessage("Drop Trigger"), sql);
	}

	/**
	 * @return the database connection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return the SQL that creates the given trigger in the database
	 */
	public String getCreateTriggerSQL(Trigger trigger) {

		/*
		 * CREATE TRIGGER name { BEFORE | AFTER } { event [OR ...] } ON table
		 * FOR EACH { ROW | STATEMENT } EXECUTE PROCEDURE func ( arguments )
		 */
		StringBuffer sql = new StringBuffer();

		sql.append("CREATE TRIGGER ");
		sql.append(trigger.getName());

		if (trigger.isBefore())
			sql.append(" BEFORE ");
		else
			sql.append(" AFTER ");

		boolean isor = false;
		if (trigger.isDeleteEvent()) {
			sql.append("DELETE ");
			isor = true;
		}

		if (trigger.isUpdateEvent()) {
			if (isor)
				sql.append("OR ");

			sql.append("UPDATE ");
			isor = true;
		}

		if (trigger.isInsertEvent()) {
			if (isor)
				sql.append("OR ");

			sql.append("INSERT ");
		}

		sql.append("ON ");
		sql.append(trigger.getTableId().getFullyQualifiedName());
		sql.append(" FOR EACH ROW ");
		sql.append("EXECUTE PROCEDURE ");
		sql.append(trigger.getFunctionName());

		String args = trigger.getFunctionArgs();
		if (args == null)
			args = "";

		args = args.trim();

		boolean open_parens = false;
		if (args.length() > 0) {
			if (args.charAt(0) != '(')
				sql.append('(');
		} else {
			sql.append('(');
		}

		if (args != null && args.trim().length() > 0) {
			sql.append(args);
		}

		if (args.length() > 0) {
			if (args.charAt(args.length() - 1) != ')')
				sql.append(')');
		} else
			sql.append(')');

		sql.append(';');

		return sql.toString();
	}

	/**
	 * @return the SQL that drops the given trigger from the database
	 */
	public String getDropTriggerSQL(Trigger trigger, boolean cascade) {
		/** DROP TRIGGER name ON table */
		StringBuffer sql = new StringBuffer();
		sql.append("DROP TRIGGER ");
		sql.append(trigger.getName());
		sql.append(" ON ");
		sql.append(trigger.getTableId().getFullyQualifiedName());

		if (cascade)
			sql.append(" CASCADE");

		sql.append(';');

		return sql.toString();
	}

	/**
	 * @return all triggers (Trigger objects) for the given table id
	 */
	public Collection getTriggers(TableId tableId) throws SQLException {
		LinkedList results = new LinkedList();
		Statement stmt = null;
		try {
			assert (tableId != null);
			StringBuffer sqlbuff = new StringBuffer();

			sqlbuff.append("SELECT pg_trigger.oid, pg_trigger.tgname, pg_trigger.tgtype, pg_trigger.tgnargs, pg_trigger.tgargs, pg_proc.proname, pg_class.relname, pg_proc.oid ");
			sqlbuff.append("FROM pg_trigger, pg_proc, pg_class ");
			sqlbuff.append("WHERE pg_proc.oid=pg_trigger.tgfoid AND pg_class.oid=pg_trigger.tgrelid ");
			sqlbuff.append("AND pg_class.relname = '");
			sqlbuff.append(tableId.getTableName());
			sqlbuff.append("' ");
			sqlbuff.append("AND pg_trigger.tgisconstraint = false ");

			stmt = m_connection.createStatement();
			String sql = sqlbuff.toString();

			ResultSet rset = stmt.executeQuery(sql);
			while (rset.next()) {
				int oid = rset.getInt(1);
				String trigger_name = rset.getString("tgname");
				int conditions = rset.getInt("tgtype");
				String proc_name = rset.getString("proname");
				int proc_oid = rset.getInt(8);

				Trigger trigger = new Trigger(new Integer(oid), tableId);

				trigger.setName(trigger_name);
				trigger.setProcedureKey(new Integer(proc_oid));
				trigger.setBefore(isBefore(conditions));
				trigger.setInsert(isInsertEvent(conditions));
				trigger.setUpdate(isUpdateEvent(conditions));
				trigger.setDelete(isDeleteEvent(conditions));

				Collection args = getProcedureArguments(rset);
				Iterator iter = args.iterator();
				while (iter.hasNext()) {
					ProcedureParameter param = (ProcedureParameter) iter.next();
					trigger.addParameter(param);
				}

				results.add(trigger);
			}
		} finally {
			if (stmt != null)
				stmt.close();
		}
		return results;
	}

	/**
	 * Get the procedure arguments for a given trigger. The args are stored as a
	 * bytea (i.e. blob), so we need to process and parse the value accordingly
	 * 
	 * @return a set of ProcedureParameter objects that are passed to the stored
	 *         procedure when the trigger is fired
	 */
	private Collection getProcedureArguments(ResultSet rset) throws SQLException {
		try {
			InputStream istream = rset.getBinaryStream("tgargs");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buff = new byte[1024];
			int numread = istream.read(buff);
			while (numread > 0) {
				bos.write(buff, 0, numread);
				numread = istream.read(buff);
			}

			byte[] data = bos.toByteArray();
			String str = new String(data);

			int index = 1;

			LinkedList results = new LinkedList();
			// now tokenize the string
			StringTokenizer st = new StringTokenizer(str, "\0");
			while (st.hasMoreTokens()) {
				String arg = st.nextToken();
				ProcedureParameter param = new ProcedureParameter(I18N.format("procedure_arg_1", new Integer(index)),
						ParameterDirection.IN, 0, "unknown");
				param.setValue(arg);
				results.add(param);
				index++;
			}
			return results;
		} catch (IOException io) {
			throw new SQLException(I18N.getLocalizedMessage("Unable to read trigger"));
		}
	}

	/**
	 * @return BEFORE or AFTER depending on the Postgres defined event mask
	 */
	static boolean isBefore(int conditions) {
		// from pg_trigger.h
		final int BEFORE = 0x2;

		// AFTER is NOT BEFORE

		int flag = conditions & BEFORE;
		return (flag != 0);
	}

	/**
	 * @return true if the given tgtype has the INSERT bit set
	 */
	static boolean isInsertEvent(int tgtype) {
		// from pg_trigger.h
		final int INSERT = 0x4;
		return ((tgtype & INSERT) > 0);
	}

	/**
	 * @return true if the given tgtype has the DELETE bit set
	 */
	static boolean isDeleteEvent(int tgtype) {
		final int DELETE = 0x8;
		return ((tgtype & DELETE) > 0);
	}

	/**
	 * @return true if the given tgtype has the UPDATE bit set
	 */
	static boolean isUpdateEvent(int tgtype) {
		final int UPDATE = 0x10;
		return ((tgtype & UPDATE) > 0);
	}

	/**
	 * Modifies the given trigger in the database
	 */
	public void modifyTrigger(Trigger newTrigger, Trigger oldTrigger) throws SQLException {
		String sql1 = getDropTriggerSQL(oldTrigger, false);
		String sql2 = getCreateTriggerSQL(newTrigger);
		SQLCommand.runMetaDataCommand(getConnection(), I18N.getLocalizedMessage("Modify Trigger"), sql1, sql2);
	}

	/**
	 * ctor
	 */
	public void setConnection(TSConnection connection) {
		m_connection = connection;
	}

}
