package com.jeta.abeille.gui.procedures;

import javax.swing.tree.TreePath;

import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.procedures.StoredProcedure;

import com.jeta.abeille.gui.model.ObjectTreeNode;

import com.jeta.open.gui.framework.UIDirector;

/**
 * This class is responsible for updating (enable/disable) GUI controls on the
 * ProcedureTreeView.
 * 
 * @author Jeff Tassin
 */
public class ProcedureTreeUIDirector implements UIDirector {
	/** the view we are updating */
	private ProcedureTree m_view;

	/**
	 * ctor
	 */
	public ProcedureTreeUIDirector(ProcedureTree view) {
		m_view = view;
	}

	/**
	 * UIDirector implementation
	 */
	public void updateComponents(java.util.EventObject evt) {
		// super.updateComponents(evt );

		boolean selproc = false;
		boolean reload = false;

		ObjectTreeNode node = m_view.getTree().getSelectedNode();
		if (node != null) {
			Object userobj = node.getUserObject();
			if (userobj instanceof StoredProcedure)
				selproc = true;
			else if (userobj instanceof Schema)
				reload = true;
		}

		m_view.enableComponent(ProcedureNames.ID_VIEW_PROCEDURE, selproc);
		m_view.enableComponent(ProcedureNames.ID_DROP_PROCEDURE, selproc);
		// m_view.enableComponent( ProcedureNames.ID_INVOKE_PROCEDURE, selproc
		// );
		// m_view.enableComponent( ProcedureNames.ID_INVOKE_OPTIONS, selproc );
		// m_view.enableComponent( ProcedureNames.ID_RELOAD, reload );
	}
}
