package com.jeta.plugins.abeille.postgres;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.DbObjectId;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.View;
import com.jeta.abeille.database.model.ViewService;

import com.jeta.abeille.gui.command.SQLCommand;
import com.jeta.foundation.i18n.I18N;

/**
 * This is the Postgres implementation for managing database views
 * 
 * @author Jeff Tassin
 */
public class PostgresViewService implements ViewService {
	/** the underlying database connection */
	private TSConnection m_connection;

	/**
	 * ctor
	 */
	public PostgresViewService() {
	}

	/**
	 * ctor
	 */
	public PostgresViewService(TSConnection conn) {
		setConnection(conn);
	}

	/**
	 * Creates the given view in the database
	 */
	public void createView(View view) throws SQLException {
		SQLCommand.runMetaDataCommand(m_connection, I18N.getLocalizedMessage("Create View"), getCreateSQL(view));
	}

	/**
	 * Drops the view from the database
	 */
	public void dropView(DbObjectId objectId, boolean cascade) throws SQLException {
		SQLCommand.runMetaDataCommand(m_connection, I18N.getLocalizedMessage("Drop View"),
				getDropSQL(objectId, cascade));
	}

	/**
	 * Creates the given view in the database
	 */
	public String getCreateSQL(View view) {
		StringBuffer sql = new StringBuffer();
		sql.append("CREATE VIEW ");
		sql.append(view.getFullyQualifiedName());
		sql.append(" AS ");
		sql.append(view.getDefinition());
		return sql.toString();
	}

	/**
	 * Drops the view from the database
	 */
	public String getDropSQL(DbObjectId objectId, boolean cascade) {
		StringBuffer sql = new StringBuffer();
		sql.append("DROP VIEW ");
		sql.append(objectId.getFullyQualifiedName());
		if (m_connection.supportsSchemas()) {
			if (cascade)
				sql.append(" CASCADE");
		}

		sql.append(';');
		return sql.toString();
	}

	/**
	 * @return the view for the corresponding id. Null is returned if the view
	 *         cannot be found.
	 */
	public View getView(DbObjectId viewid) throws SQLException {
		if (viewid == null)
			return null;

		StringBuffer sqlbuff = new StringBuffer();
		if (m_connection.supportsSchemas()) {
			sqlbuff.append("SELECT * from pg_catalog.pg_views WHERE schemaname = '");
			sqlbuff.append(viewid.getSchemaName());
			sqlbuff.append("' ");
			sqlbuff.append("AND viewname = '");
			sqlbuff.append(viewid.getObjectName());
			sqlbuff.append("'");
		} else {
			sqlbuff.append("SELECT * from pg_views WHERE ");
			sqlbuff.append("viewname = '");
			sqlbuff.append(viewid.getObjectName());
			sqlbuff.append("'");
		}

		View view = null;
		Connection conn = m_connection.getMetaDataConnection();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(sqlbuff.toString());
			if (rset.next()) {
				String definition = rset.getString("definition");
				view = new View(viewid);
				view.setDefinition(definition);
			}
		} catch (SQLException e) {
			conn.rollback();
			throw e;
		} finally {
			if (stmt != null)
				stmt.close();

			conn.commit();
		}
		return view;
	}

	/**
	 * @return all views (as DbObjectId objects) defined in the given schema
	 */
	public Collection getViews2(Catalog cat, Schema schema) throws SQLException {
		LinkedList results = new LinkedList();
		DbModel model = m_connection.getModel(cat);
		Collection tables = model.getTables(schema);
		Iterator tableiter = tables.iterator();
		while (tableiter.hasNext()) {
			TableId tableid = (TableId) tableiter.next();
			TableMetaData tmd = model.getTable(tableid);
			if (tmd.isView()) {
				results.add(tableid);
			}
		}

		return results;
	}

	/**
	 * @return all views (as DbObjectId objects) defined in the given schema
	 */
	public Collection getViews(Catalog cat, Schema schema) throws SQLException {
		LinkedList results = new LinkedList();
		DbModel model = m_connection.getModel(cat);
		Collection tables = model.getTables(schema);
		Iterator tableiter = tables.iterator();
		while (tableiter.hasNext()) {
			TableId tableid = (TableId) tableiter.next();
			TableMetaData tmd = model.getTableFast(tableid);
			if (tmd.isView()) {
				results.add(tableid);
			}
		}
		return results;
	}

	/**
	 * @return true if the given id represents a view
	 */
	private boolean isView(DbObjectId viewid) throws SQLException {
		return false;
	}

	/**
	 * Modifies the given view in the database.
	 */
	public void modifyView(View newView, View oldView) throws SQLException {
		// if ( m_connection.supportsSchemas() )
		// {
		// StringBuffer sql = new StringBuffer();
		// sql.append( "CREATE OR REPLACE VIEW " );
		// sql.append( oldView.getSchemaQualifiedViewName() );
		// / sql.append( " AS " );
		// sql.append( newView.getDefinition() );
		// SQLCommand.runMetaDataCommand( m_connection,
		// I18N.getLocalizedMessage("Modify View"), sql.toString() );
		// }
		// else
		// {
		String sql1 = getDropSQL(oldView.getTableId(), false);
		String sql2 = getCreateSQL(newView);
		SQLCommand.runMetaDataCommand(m_connection, I18N.getLocalizedMessage("Modify View"), sql1, sql2);
		// }
	}

	/**
	 * ctor
	 */
	public void setConnection(TSConnection connection) {
		m_connection = connection;
	}

}
