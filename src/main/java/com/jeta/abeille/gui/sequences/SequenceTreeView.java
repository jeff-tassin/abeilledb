package com.jeta.abeille.gui.sequences;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.sequences.Sequence;

import com.jeta.abeille.gui.model.ObjectTreeNames;
import com.jeta.abeille.gui.model.ObjectTreeNode;
import com.jeta.abeille.gui.model.ObjectTreeView;

import com.jeta.foundation.gui.components.TSToolBarTemplate;
import com.jeta.foundation.i18n.I18N;

/**
 * This class shows sequence objects in a tree view
 * 
 * @author Jeff Tassin
 */
public class SequenceTreeView {

	/**
	 * ctor
	 */
	public SequenceTreeView() {
		/*
		 * TSToolBarTemplate template = new TSToolBarTemplate(); template.add(
		 * _createToolBarButton( "general/New16.gif",
		 * SequenceNames.ID_NEW_SEQUENCE, I18N.getLocalizedMessage(
		 * "Create Sequence" ) ) );
		 * 
		 * template.add( _createToolBarButton( "general/Edit16.gif",
		 * SequenceNames.ID_EDIT_SEQUENCE, I18N.getLocalizedMessage(
		 * "Modify Sequence" ) ) );
		 * 
		 * template.add( _createToolBarButton( "general/Delete16.gif",
		 * SequenceNames.ID_DROP_SEQUENCE, I18N.getLocalizedMessage(
		 * "Drop Sequence" ) ) );
		 * 
		 * template.add( _createToolBarButton( "reset16.gif",
		 * ObjectTreeNames.ID_RELOAD, I18N.getLocalizedMessage( "Reload" ) ) );
		 * 
		 * setToolBarTemplate( template );
		 */
	}

	/**
	 * Sets the connection and initializes the view
	 */
	protected void initialize() {
		/*
		 * TSConnection connection = getConnection(); SequenceTreeModel model =
		 * new SequenceTreeModel(connection); SequenceTree tree = new
		 * SequenceTree(model); setTree( tree ); setController( new
		 * SequenceTreeViewController(this) );
		 */
	}

}
