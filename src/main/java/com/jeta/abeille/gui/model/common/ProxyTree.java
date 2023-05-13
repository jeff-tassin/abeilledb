package com.jeta.abeille.gui.model.common;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import java.util.ArrayList;

import javax.swing.tree.TreePath;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.model.CatalogWrapper;
import com.jeta.abeille.gui.model.DbObjectFlavor;
import com.jeta.abeille.gui.model.DbObjectTransfer;
import com.jeta.abeille.gui.model.DbObjectClassTree;
import com.jeta.abeille.gui.model.MultiTransferable;
import com.jeta.abeille.gui.model.ObjectTree;
import com.jeta.abeille.gui.model.ObjectTreeNode;
import com.jeta.abeille.gui.model.ObjectTreeModel;
import com.jeta.abeille.gui.model.SchemaWrapper;

import com.jeta.abeille.gui.model.TreeFolder;

import com.jeta.foundation.utils.TSUtils;

/**
 * This is a base class for trees that display database objects. Specifically,
 * the formbuilder tree and querybuilder tree share this class.
 * 
 * @author Jeff Tassin
 */
public abstract class ProxyTree extends DbObjectClassTree {
	/**
	 * ctor
	 */
	public ProxyTree(ObjectTree tree) {
		super(tree);
	}

	/**
	 * Drops the given transferable onto the tree's given node
	 */
	public void drop(ObjectTreeNode dropnode, Transferable mt) {
		// this should be an array of tree paths
		try {
			if (true) {
				super.drop(dropnode, mt);
				return;
			}

			ObjectTreeModel model = (ObjectTreeModel) getModel();
			Object dropobj = dropnode.getUserObject();
			ObjectTreeNode defaultdropparent = model.getBaseNode(dropnode);
			TSConnection dropconn = model.getConnection(dropnode);
			Schema dropschema = model.getSchema(dropnode);
			Catalog dropcatalog = model.getCatalog(dropnode);
			assert (dropschema != null);
			assert (dropcatalog != null);
			assert (dropconn != null);

			Object[] paths = (Object[]) mt.getTransferData(DbObjectFlavor.TREE_PATH);
			if (paths != null) {
				/**
				 * loop over all items in the drag list to perform the actual
				 * move
				 */
				for (int index = 0; index < paths.length; index++) {
					TreePath path = (TreePath) paths[index];
					ObjectTreeNode dragnode = (ObjectTreeNode) path.getLastPathComponent();
					Object draguserobj = dragnode.getUserObject();
					ObjectTreeNode defaultdragparent = model.getClassNode(dragnode);
					if (defaultdragparent != null && defaultdropparent != null) {
						// additionally, if this node is a folder or table and
						// the schema and/or catalog
						// of the dropnode has changed, then this is also a
						// rename table case. Additionally, if we
						// are moving a folder, then we need to rename all
						// tables contained within the folder
						if (defaultdragparent != defaultdropparent) {
							if (draguserobj instanceof DatabaseObjectProxy) {
								DatabaseObjectProxy proxy = (DatabaseObjectProxy) draguserobj;
								if (!dropschema.equals(proxy.getSchema()) || !dropcatalog.equals(proxy.getCatalog())) {
									proxy.set(dropcatalog, dropschema);
								}
							} else if (draguserobj instanceof TreeFolder) {
								renameChildObjects(dropcatalog, dropschema, dragnode);
							} else {
								assert (false);
							}
						} else {
							assert (false);
						}
					} else {
						System.out.println("dragnode/dropnode invalid drop - anscestor/descendant");
					}
				}
			}

			if (TSUtils.isTest()) {
				/** now make sure everything is consistent */
				com.jeta.abeille.test.JETATestFactory.runTest("test.jeta.abeille.gui.model.common.ProxyTreeValidator",
						this);
			}

		} catch (Exception e) {
			TSUtils.printException(e);
		}
	}

	/**
	 * @return true if the given transfer has data flavors that are supported by
	 *         this tree
	 */
	public abstract boolean isDataFlavorSupported(Transferable transfer);

	/**
	 * This method recursively searches through all decendants of the given node
	 * for nodes that contain a databaseobjectproxy. If any are found, they are
	 * added to the children list. This method is used primarly whey dragging a
	 * folder to another schema or catalog in the modeler tree. If this happens,
	 * we need to rename all tables in the folder to the new catalog/schema.
	 */
	private void renameChildObjects(Catalog catalog, Schema schema, ObjectTreeNode node) {
		Object userobj = node.getUserObject();
		if (userobj instanceof DatabaseObjectProxy) {
			DatabaseObjectProxy proxy = (DatabaseObjectProxy) userobj;
			assert (!(catalog.equals(proxy.getCatalog()) && schema.equals(proxy.getSchema())));
			proxy.set(catalog, schema);
		}

		for (int index = 0; index < node.getChildCount(); index++) {
			renameChildObjects(catalog, schema, (ObjectTreeNode) node.getChildAt(index));
		}
	}

}
