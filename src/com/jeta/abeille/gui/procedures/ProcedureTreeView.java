package com.jeta.abeille.gui.procedures;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.database.procedures.StoredProcedure;

import com.jeta.abeille.gui.model.ObjectTreeNames;
import com.jeta.abeille.gui.model.ObjectTreeNode;
import com.jeta.abeille.gui.model.ObjectTreeView;

import com.jeta.foundation.gui.components.TSToolBarTemplate;
import com.jeta.foundation.i18n.I18N;

/**
 * This class shows stored procedure objects in a tree view
 * 
 * @author Jeff Tassin
 */
public class ProcedureTreeView {

	/**
	 * ctor
	 */
	public ProcedureTreeView() {
		/*
		 * addPopupItem( i18n_createMenuItem( "View Procedure",
		 * ProcedureNames.ID_VIEW_PROCEDURE, null ) ); addPopupItem(
		 * i18n_createMenuItem( "Reload", ObjectTreeNames.ID_RELOAD, null ) );
		 * 
		 * TSToolBarTemplate template = new TSToolBarTemplate(); template.add(
		 * _createToolBarButton( "procedure16.gif",
		 * ProcedureNames.ID_VIEW_PROCEDURE, I18N.getLocalizedMessage(
		 * "View Function" ) ) );
		 * 
		 * template.add( _createToolBarButton( "general/Delete16.gif",
		 * ProcedureNames.ID_DROP_PROCEDURE, I18N.getLocalizedMessage(
		 * "Drop Function" ) ) ); template.add( _createToolBarButton(
		 * "reset16.gif", ObjectTreeNames.ID_RELOAD, I18N.getLocalizedMessage(
		 * "Reload" ) ) ); setToolBarTemplate( template );
		 */
	}

	/**
	 * Sets the connection and initializes the view
	 */
	protected void initialize() {
		/*
		 * TSConnection connection = getConnection(); ProcedureTreeModel model =
		 * new ProcedureTreeModel(connection); ProcedureTree tree = new
		 * ProcedureTree(model); setTree( tree ); setController( new
		 * ProcedureTreeController( this ) ); expandRootNode();
		 */
	}

}
