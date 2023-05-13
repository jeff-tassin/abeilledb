package com.jeta.abeille.gui.model;

import javax.swing.Icon;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

public class TableClassFactory extends DbObjectClassFactory {
	private String m_table_type;
	public final static String TABLE = "TABLE";
	public final static String SYNONYM = "SYNONYM";

	public TableClassFactory(String tableType) {
		m_table_type = tableType;
	}

	public DbObjectClassModel createClassModel(TSConnection conn, Catalog catalog, Schema schema,
			DbObjectTreeModel treeModel) {
		if (SYNONYM.equals(m_table_type))
			return new SynonymTreeModel(conn, catalog, schema, treeModel);
		else
			return new TableTreeModel(conn, catalog, schema, treeModel);
	}

	public DbObjectClassTree createClassTree(ObjectTree tree) {
		return new TableTree(tree);
	}

	public String getClassKey() {
		if (SYNONYM.equals(m_table_type))
			return SynonymTreeModel.CLASS_KEY;
		else
			return TableTreeModel.CLASS_KEY;
	}

}
