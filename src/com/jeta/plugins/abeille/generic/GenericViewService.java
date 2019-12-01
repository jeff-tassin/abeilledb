package com.jeta.plugins.abeille.generic;

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

import com.jeta.plugins.abeille.standard.DefaultViewService;

/**
 * This is the generic implementation for managing database views
 * 
 * @author Jeff Tassin
 */
public class GenericViewService extends DefaultViewService {

	/**
	 * ctor
	 */
	public GenericViewService() {
	}

	/**
	 * ctor
	 */
	public GenericViewService(TSConnection conn) {
		super(conn);
	}

	/**
	 * @return all views (as DbObjectId objects) defined in the given schema
	 */
	public Collection getViews(Catalog catalog, Schema schema) throws SQLException {
		LinkedList results = new LinkedList();
		DbModel model = getConnection().getModel(catalog);
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

}
