package com.jeta.abeille.gui.sequences;

import javax.swing.Icon;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.model.*;

public class SequenceClassFactory extends DbObjectClassFactory {
	public DbObjectClassModel createClassModel(TSConnection conn, Catalog catalog, Schema schema,
			DbObjectTreeModel treeModel) {
		return new SequenceTreeModel(conn, catalog, schema, treeModel);
	}

	public DbObjectClassTree createClassTree(ObjectTree tree) {
		return new SequenceTree(tree);
	}

	public String getClassKey() {
		return SequenceTreeModel.CLASS_KEY;
	}

}
