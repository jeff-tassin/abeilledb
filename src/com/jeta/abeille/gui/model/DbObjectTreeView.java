package com.jeta.abeille.gui.model;

import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableIdGetter;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.update.InstanceNames;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.components.TSToolBarTemplate;

import com.jeta.foundation.i18n.I18N;

/**
 * This is the class that shows tables in the system
 * 
 * @author Jeff Tassin
 */
public class DbObjectTreeView extends ObjectTreeView {

	/**
	 * ctor
	 */
	public DbObjectTreeView() {
		DbObjectTreeModel model = new DbObjectTreeModel();
		DbObjectTree tree = new DbObjectTree(model);
		setTree(tree);
	}

}
