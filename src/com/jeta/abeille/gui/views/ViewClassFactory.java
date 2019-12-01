package com.jeta.abeille.gui.views;

import javax.swing.Icon;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.model.*;

public class ViewClassFactory extends DbObjectClassFactory {
	public DbObjectClassModel createClassModel(TSConnection conn, Catalog catalog, Schema schema,
			DbObjectTreeModel treeModel) {
		return new ViewTreeModel(conn, catalog, schema, treeModel);
	}

	public DbObjectClassTree createClassTree(ObjectTree tree) {
		return new TableTree(tree);
	}

	public String getClassKey() {
		return ViewTreeModel.CLASS_KEY;
	}

}
