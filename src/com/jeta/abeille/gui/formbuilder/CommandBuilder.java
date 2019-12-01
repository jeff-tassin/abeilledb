package com.jeta.abeille.gui.formbuilder;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.utils.ConnectionReference;
import com.jeta.abeille.database.utils.SQLAction;
import com.jeta.abeille.gui.command.AbstractCommand;
import com.jeta.abeille.gui.command.CompositeCommand;

/**
 * This class builds a command that runs a prepared statement. This is used for
 * the form builder
 * 
 * @author Jeff Tassin
 */
public class CommandBuilder {
	/** the map of ValueProxies (values) keyed on ColumnMetaData objects */
	private HashMap m_proxies = new HashMap();

	/** the set of subqueries to run */
	private Collection m_queries;

	/** the controller */
	private FormInstanceController m_controller;

	/** the form model */

	/**
	 * ctor
	 */
	public CommandBuilder(FormInstanceController controller) {
		m_controller = controller;
	}

	/**
	 * Builds the command used to update the anchor table for the given form.
	 * This is basically a set of queries followed by an update (i.e. INSERT,
	 * UDPATE, DELETE )
	 * 
	 * @return the command that is used to perform the entire operation (this is
	 *         a composite of abstact commands)
	 */
	public AbstractCommand buildCommand(SQLAction update) {
		try {
			if (update == SQLAction.INSERT) {
				ConnectionReference connection = m_controller.getModel().getWriteConnection();
				FormAddCommand add = new FormAddCommand(connection, m_controller, m_proxies);

				Iterator iter = m_queries.iterator();
				while (iter.hasNext()) {
					SubQuery query = (SubQuery) iter.next();
					FormQueryCommand cmd = new FormQueryCommand(connection, m_controller, query);
					add.addCommand(cmd);
				}
				return add;
			} else if (update == SQLAction.UPDATE) {
				ConnectionReference connection = m_controller.getModel().getWriteConnection();
				FormUpdateCommand cc = new FormUpdateCommand(connection, m_controller);
				return cc;
			} else if (update == SQLAction.DELETE) {
				ConnectionReference connection = m_controller.getModel().getWriteConnection();
				FormDeleteCommand cc = new FormDeleteCommand(connection, m_controller);
				return cc;
			} else
				return null;
		} catch (SQLException se) {
			se.printStackTrace();
			return null;
		}

	}

	/**
	 * Gets the proxy object used to get the value for a given column in the
	 * FormInstanceView. This proxy may get the value indirectly through a query
	 * or it may get the value directly from the form.
	 */
	public ValueProxy getProxy(ColumnMetaData cmd) {
		return (ValueProxy) m_proxies.get(cmd);
	}

	/**
	 * Sets the proxy object used to get the value for a given column in the
	 * FormInstanceView. This proxy may get the value indirectly through a query
	 * or it may get the value directly from the form.
	 */
	public void setProxy(ColumnMetaData cmd, ValueProxy proxy) {
		// System.out.println( "commandbuilder.setProxy: " +
		// cmd.getQualifiedName() + "  " + proxy );
		assert (m_proxies.get(cmd) == null);
		m_proxies.put(cmd, proxy);
	}

	/**
	 * Sets the set of subqueries to run to get the values for the command
	 */
	public void setQueries(Collection queries) {
		m_queries = queries;
	}

}
