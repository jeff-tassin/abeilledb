package com.jeta.abeille.gui.model;

import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TableId;

import com.jeta.open.gui.framework.UIDirector;

/**
 * This class is responsible for updating (enable/disable) GUI controls on the
 * ModelerViewView.
 * 
 * @author Jeff Tassin
 */
public class ModelerViewUIDirector implements UIDirector {
	/** the view we are updating */
	private ModelerView m_view;

	/**
	 * ctor
	 */
	public ModelerViewUIDirector(ModelerView view) {
		m_view = view;
	}

	/**
	 * UIDirector implementation
	 */
	public void updateComponents(java.util.EventObject evt) {
		// super.updateComponents( evt );

		boolean seltable = false;
		TableId tableid = m_view.getSelectedTable();
		if (tableid != null) {
			seltable = true;
		}
		m_view.enableComponent(TableTreeNames.ID_TABLE_PROPERTIES, seltable);
		m_view.enableComponent(ModelerNames.ID_DELETE_PROTOTYPE, seltable);
	}
}
