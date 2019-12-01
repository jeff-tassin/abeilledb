package com.jeta.abeille.gui.formbuilder;

import javax.swing.Icon;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.model.DbObjectClassFactory;
import com.jeta.abeille.gui.model.DbObjectClassTree;
import com.jeta.abeille.gui.model.DbObjectClassModel;
import com.jeta.abeille.gui.model.DbObjectTreeModel;
import com.jeta.abeille.gui.model.ObjectTree;

public class FormClassFactory extends DbObjectClassFactory {
	public DbObjectClassModel createClassModel(TSConnection conn, Catalog catalog, Schema schema,
			DbObjectTreeModel treeModel) {
		return new FormTreeModel(conn, catalog, schema, treeModel);
	}

	public DbObjectClassTree createClassTree(ObjectTree tree) {
		return new FormTree(tree);
	}

	public String getClassKey() {
		return FormTreeModel.CLASS_KEY;
	}

}
