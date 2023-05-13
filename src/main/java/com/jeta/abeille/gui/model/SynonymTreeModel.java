package com.jeta.abeille.gui.model;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

/**
 * 
 * @author Jeff Tassin
 */
public class SynonymTreeModel extends TableTreeModel {
	public static final String CLASS_KEY = "synonym.tabletreemodel.";

	/**
	 * ctor
	 */
	public SynonymTreeModel(TSConnection connection, Catalog catalog, Schema schema, DbObjectTreeModel parentModel) {
		super(CLASS_KEY, connection, catalog, schema, parentModel);
	}

	protected String getTableType() {
		return "SYNONYM";
	}

}
