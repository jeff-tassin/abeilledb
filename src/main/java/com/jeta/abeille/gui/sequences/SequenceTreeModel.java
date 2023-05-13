package com.jeta.abeille.gui.sequences;

import java.sql.SQLException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.sequences.Sequence;
import com.jeta.abeille.database.sequences.SequenceService;

import com.jeta.abeille.gui.model.DbObjectClassModel;
import com.jeta.abeille.gui.model.DbObjectTreeModel;
import com.jeta.abeille.gui.model.ObjectTreeNode;

import com.jeta.foundation.interfaces.app.ObjectStore;
import com.jeta.foundation.utils.TSUtils;

/**
 * SequenceTreeModel is the GUI model for the tree view of sequences in the
 * database that a available for the given user
 * 
 * @author Jeff Tassin
 */
public class SequenceTreeModel extends DbObjectClassModel {
	public static final String CLASS_KEY = "sequence.sequencetree.";

	public SequenceTreeModel(TSConnection connection, Catalog catalog, Schema schema, DbObjectTreeModel tmodel) {
		super(CLASS_KEY, connection, catalog, schema, tmodel, true);
	}

	/**
	 * TableTrees display tableids
	 */
	public Class getObjectClass() {
		return Sequence.class;
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
	 * Reloads the tree view. Reads all stored queries and inserts them into the
	 * tree.
	 */
	public void refreshModel(TSConnection conn, Catalog catalog, Schema schema) {
		try {
			removeAllChildren(conn, catalog, schema);
			loadModelState(conn, catalog, schema, Sequence.class);

			ObjectTreeNode basenode = getClassNode(conn, catalog, schema);
			assert (basenode != null);

			ObjectStore os = conn.getObjectStore();

			DbModel model = conn.getModel(catalog);
			// now load the sequences from the sequence service
			SequenceService ssrv = (SequenceService) conn.getImplementation(SequenceService.COMPONENT_ID);
			Collection seqs = ssrv.getSequences(schema);
			Iterator siter = seqs.iterator();
			while (siter.hasNext()) {
				Sequence seq = (Sequence) siter.next();
				addDatabaseObjectNode(conn, seq);
			}
			fireTreeStructureChanged(basenode);
		} catch (Exception se) {
			TSUtils.printException(se);
		} finally {
			clearModelState(conn, catalog, schema);
		}
	}

	/**
	 * Causes the database or object store to reload itself. Then this model
	 * will refresh with the latest data.
	 */
	public void reloadModel(TSConnection conn, Catalog catalog, Schema schema) {
		refreshModel(conn, catalog, schema);
	}

}
