package com.jeta.abeille.gui.procedures;

import java.sql.SQLException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.procedures.ProcedureLanguage;
import com.jeta.abeille.database.procedures.StoredProcedureService;
import com.jeta.abeille.database.procedures.StoredProcedure;

import com.jeta.abeille.gui.model.DbObjectClassModel;
import com.jeta.abeille.gui.model.DbObjectTreeModel;
import com.jeta.abeille.gui.model.ObjectTreeNode;

import com.jeta.foundation.utils.TSUtils;
import com.jeta.foundation.interfaces.app.ObjectStore;

/**
 * ProcedureTreeModel is the GUI model for the tree view of stored procedures in
 * the database that a available for the given user
 * 
 * @author Jeff Tassin
 */
public class ProcedureTreeModel extends DbObjectClassModel {
	public static final String CLASS_KEY = "procedure.proceduretreemodel";

	public ProcedureTreeModel(TSConnection connection, Catalog catalog, Schema schema, DbObjectTreeModel tmodel) {
		super(CLASS_KEY, connection, catalog, schema, tmodel, true);
	}

	/**
	 * TableTrees display tableids
	 */
	public Class getObjectClass() {
		return StoredProcedure.class;
	}

	/**
	 * Called when a tree node is being expanded. Allows specialized classes to
	 * load child elements for the node if the node has not been loaded yet
	 */
	public void reloadNode(ObjectTreeNode node) {
		if (node != null) {
			Object userobj = node.getUserObject();
			if (userobj == this) {
				TSConnection tsconn = getConnection(node);
				Catalog catalog = getCatalog(node);
				Schema schema = getSchema(node);
				if (catalog != null && schema != null) {
					reloadModel(tsconn, catalog, schema);
				}
			}
		}
	}

	/**
	 * Initializes the tree view. Reads all stored queries and inserts them into
	 * the tree.
	 */
	public void refreshModel(TSConnection connection, Catalog catalog, Schema schema) {
		try {
			removeAllChildren(connection, catalog, schema);
			loadModelState(connection, catalog, schema, StoredProcedure.class);
			DbModel model = connection.getModel(catalog);
			// now load the procedures from the procedure service and store them
			// in
			// any folders that were created during a previous instance of this
			// application
			StoredProcedureService tsprocs = (StoredProcedureService) connection
					.getImplementation(StoredProcedureService.COMPONENT_ID);
			Collection procs = null;
			try {
				procs = tsprocs.getUserProcedures();
			} catch (SQLException sqe) {
				TSUtils.printException(sqe);
			}

			if (procs != null) {
				Iterator iter = procs.iterator();
				while (iter.hasNext()) {
					StoredProcedure proc = (StoredProcedure) iter.next();
					assert (proc.getObjectId().getCatalog().equals(catalog));
					if (schema.equals(proc.getSchema())) {
						addDatabaseObjectNode(connection, proc);
					}
				}

				ObjectTreeNode classnode = getClassNode(connection, catalog, schema);
				reload(classnode);
			}
		} catch (Exception se) {
			TSUtils.printException(se);
		} finally {
			clearModelState(connection, catalog, schema);
		}
	}

	/**
	 * Causes the database or object store to reload itself. Then this model
	 * will refresh with the latest data.
	 */
	public void reloadModel(TSConnection conn, Catalog catalog, Schema schema) {
		StoredProcedureService tsprocs = (StoredProcedureService) conn
				.getImplementation(StoredProcedureService.COMPONENT_ID);
		tsprocs.reload();

		refreshModel(conn, catalog, schema);
	}

}
