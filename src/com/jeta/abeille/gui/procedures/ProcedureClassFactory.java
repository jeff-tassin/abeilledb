package com.jeta.abeille.gui.procedures;

import javax.swing.Icon;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.model.*;

public class ProcedureClassFactory extends DbObjectClassFactory {
	public DbObjectClassModel createClassModel(TSConnection conn, Catalog catalog, Schema schema,
			DbObjectTreeModel treeModel) {
		return new ProcedureTreeModel(conn, catalog, schema, treeModel);
	}

	public DbObjectClassTree createClassTree(ObjectTree tree) {
		return new ProcedureTree(tree);
	}

	public String getClassKey() {
		return ProcedureTreeModel.CLASS_KEY;
	}

}
