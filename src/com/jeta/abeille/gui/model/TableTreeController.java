package com.jeta.abeille.gui.model;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.tree.TreePath;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.common.DefaultTableSelectorModel;
import com.jeta.abeille.gui.modeler.TableEditorDialog;
import com.jeta.abeille.gui.table.TableFrame;
import com.jeta.abeille.gui.update.ShowInstanceFrameAction;
import com.jeta.abeille.gui.update.TableInstanceViewBuilder;

import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.TSComponentUtils;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.utils.TSUtils;

import com.jeta.open.gui.framework.JETAController;

/**
 * This is the main controller for the TableTreeView It responds to user events
 * from the TableTreeView.
 * 
 * @author Jeff Tassin
 */
public class TableTreeController extends JETAController {
	/** the view we are controlling */
	private TableTree m_view;

	/**
	 * ctor
	 */
	public TableTreeController(TableTree view) {
		super(view);
		m_view = view;
		assignAction(TableTreeNames.ID_TABLE_PROPERTIES, new MyTablePropertiesAction());
		assignAction(TableTreeNames.ID_INSTANCE_VIEW, new InstanceViewAction());
		assignAction(TableTreeNames.ID_QUERY_TABLE, new QueryTable());
	}

	/**
	 * Action handler that shows the instance view for a selected table
	 */
	public class InstanceViewAction extends ShowInstanceFrameAction {
		public void actionPerformed(ActionEvent evt) {
			ObjectTreeNode objnode = m_view.getSelectedNode();
			if (objnode != null && objnode.getUserObject() instanceof TableId) {
				TableId tableid = (TableId) objnode.getUserObject();
				TSConnection tsconn = m_view.getConnection(objnode);
				TableInstanceViewBuilder builder = new TableInstanceViewBuilder(tsconn, tableid);
				showFrame(tsconn, builder);
			}
		}
	}

	/**
    */
	public class MyTablePropertiesAction extends ShowInstanceFrameAction {
		public void actionPerformed(ActionEvent evt) {
			ObjectTreeNode objnode = m_view.getSelectedNode();
			if (objnode != null && objnode.getUserObject() instanceof TableId) {
				TableId tableid = (TableId) objnode.getUserObject();
				TSConnection tsconn = m_view.getConnection(objnode);
				TablePropertiesAction.showPropertiesDialog(tsconn, tableid);
			}
		}
	}

	/**
	 * Queries all the rows in the table and opens the SQL results frame.
	 */
	public class QueryTable implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ObjectTreeNode objnode = m_view.getSelectedNode();
			if (objnode != null && objnode.getUserObject() instanceof TableId) {
				TableId tableid = (TableId) objnode.getUserObject();
				TSConnection tsconn = m_view.getConnection(objnode);
				QueryTableAction.invoke(tsconn, tableid);
			}
		}
	}

}
