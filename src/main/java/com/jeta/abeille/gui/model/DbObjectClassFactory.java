package com.jeta.abeille.gui.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.Icon;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.procedures.ProcedureClassFactory;
import com.jeta.abeille.gui.procedures.ProcedureTreeModel;
import com.jeta.abeille.gui.sequences.SequenceClassFactory;
import com.jeta.abeille.gui.sequences.SequenceTreeModel;
import com.jeta.abeille.gui.views.ViewClassFactory;
import com.jeta.abeille.gui.views.ViewTreeModel;

public abstract class DbObjectClassFactory {
	private static HashMap m_factories = new HashMap();

	static {
		m_factories.put(TableTreeModel.CLASS_KEY, new TableClassFactory(TableClassFactory.TABLE));
		m_factories.put(SynonymTreeModel.CLASS_KEY, new TableClassFactory(TableClassFactory.SYNONYM));
		m_factories.put(ViewTreeModel.CLASS_KEY, new ViewClassFactory());
		m_factories.put(ProcedureTreeModel.CLASS_KEY, new ProcedureClassFactory());
		m_factories.put(SequenceTreeModel.CLASS_KEY, new SequenceClassFactory());

	}

	public abstract DbObjectClassModel createClassModel(TSConnection conn, Catalog catalog, Schema schema,
			DbObjectTreeModel treeModel);

	public abstract DbObjectClassTree createClassTree(ObjectTree tree);

	public abstract String getClassKey();

	public static Collection getFactories(TSConnection tsconn) {
		LinkedList list = new LinkedList();
		list.add(new TableClassFactory(TableClassFactory.TABLE));
		list.add(new TableClassFactory(TableClassFactory.SYNONYM));
		list.add(new ViewClassFactory());

		if (Database.POSTGRESQL.equals(tsconn.getDatabase())) {
			list.add(new ProcedureClassFactory());
			list.add(new SequenceClassFactory());
		}
		return list;
	}

	public static DbObjectClassFactory getFactory(String classKey) {
		return (DbObjectClassFactory) m_factories.get(classKey);
	}

}
