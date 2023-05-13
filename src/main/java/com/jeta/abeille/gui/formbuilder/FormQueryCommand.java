package com.jeta.abeille.gui.formbuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;

import com.jeta.abeille.database.utils.ConnectionReference;
import com.jeta.abeille.database.utils.SQLFormatter;

import com.jeta.abeille.gui.command.AbstractCommand;

/**
 * This command runs a sql prepared statement in the background. This is for
 * invoking a query. The command is run in a background thread so it can be
 * canceled.
 * 
 * @author Jeff Tassin
 */
public class FormQueryCommand extends AbstractCommand {
	private SubQuery m_subquery;

	private FormInstanceController m_controller;

	private ConnectionReference m_connectionref;

	/**
	 * ctor
	 */
	public FormQueryCommand(ConnectionReference connectionref, FormInstanceController controller, SubQuery query)
			throws SQLException {
		m_controller = controller;
		m_subquery = query;
		m_connectionref = connectionref;

	}

	public void cancel() {
		assert (false);
	}

	/**
	 * Invokes this command
	 */
	public void invoke() {
		try {
			SQLFormatter formatter = m_controller.getFormatter();
			m_subquery.executeQuery(m_connectionref, formatter);
		} catch (SQLException se) {
			se.printStackTrace();
			setException(se);
		}
		commandCompleted();
	}

	/*
	 * Override the commandCompleted method. We don't want to udpate the
	 * interface unless there was an error
	 */
	protected void commandCompleted() {
		// note, since this command is part of a composite, we don't do a
		// rollback here on an exception
	}

}
