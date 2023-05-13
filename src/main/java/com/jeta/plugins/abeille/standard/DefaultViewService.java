package com.jeta.plugins.abeille.standard;

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
 * This is the Default implementation for managing database views
 * 
 * @author Jeff Tassin
 */
public class DefaultViewService implements ViewService {
	/** the underlying database connection */
	private TSConnection m_connection;

	/**
	 * ctor
	 */
	public DefaultViewService() {
	}

	/**
	 * ctor
	 */
	public DefaultViewService(TSConnection conn) {
		setConnection(conn);
	}

	/**
	 * Creates the given view in the database
	 */
	public void createView(View view) throws SQLException {

	}

	/**
	 * Drops the view from the database
	 */
	public void dropView(DbObjectId objectId, boolean cascade) throws SQLException {

	}

	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * Creates the given view in the database
	 */
	public String getCreateSQL(View view) {
		return "";
	}

	/**
	 * Drops the view from the database
	 */
	public String getDropSQL(DbObjectId objectId, boolean cascade) {
		return "";
	}

	/**
	 * @return the view for the corresponding id. Null is returned if the view
	 *         cannot be found.
	 */
	public View getView(DbObjectId viewid) throws SQLException {
		return null;
	}

	/**
	 * @return all views (as DbObjectId objects) defined in the given schema
	 */
	public Collection getViews(Catalog catalog, Schema schema) throws SQLException {
		LinkedList results = new LinkedList();
		return results;
	}

	/**
	 * @return true if the given id represents a view
	 */
	public boolean isView(DbObjectId viewid) throws SQLException {
		return false;
	}

	/**
	 * Modifies the given view in the database.
	 */
	public void modifyView(View newView, View oldView) throws SQLException {

	}

	/**
	 * ctor
	 */
	public void setConnection(TSConnection connection) {
		m_connection = connection;
	}

}
