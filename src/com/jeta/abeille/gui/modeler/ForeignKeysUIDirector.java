package com.jeta.abeille.gui.modeler;

import java.lang.ref.WeakReference;

import javax.swing.tree.DefaultMutableTreeNode;

import com.jeta.abeille.database.model.Database;

import com.jeta.open.gui.framework.UIDirector;

/**
 * Enables components for the ForeignKeyView
 * 
 * @author Jeff Tassin
 */
public class ForeignKeysUIDirector implements UIDirector {
	/**
	 * The view we are enabling components for
	 */
	private WeakReference m_viewref;

	/**
	 * ctor
	 * 
	 * @param view
	 *            the view we are updating
	 */
	public ForeignKeysUIDirector(ForeignKeysView view) {
		m_viewref = new WeakReference(view);
	}

	/**
	 * UIDirector implementation
	 */
	public void updateComponents(java.util.EventObject evt) {
		ForeignKeysView view = (ForeignKeysView) m_viewref.get();
		if (view != null) {
			ForeignKeysModel model = view.getModel();

			// System.out.println(
			// "ForeignKeysUIDirector.updateComps  prototype: " +
			// model.isPrototype() + " tableid: " + model.getTableId() );
			if (!model.isPrototype() && model.getTableId() == null) {
				view.enableComponent(ForeignKeysView.ID_ADD_FOREIGN_KEY, false);
				view.enableComponent(ForeignKeysView.ID_EDIT_FOREIGN_KEY, false);
				view.enableComponent(ForeignKeysView.ID_DROP_FOREIGN_KEY, false);
			} else {
				ForeignKeyWrapper wrapper = view.getSelectedForeignKey();
				view.enableComponent(ForeignKeysView.ID_ADD_FOREIGN_KEY, true);
				if (wrapper == null) {
					view.enableComponent(ForeignKeysView.ID_EDIT_FOREIGN_KEY, false);
					view.enableComponent(ForeignKeysView.ID_DROP_FOREIGN_KEY, false);
				} else {
					view.enableComponent(ForeignKeysView.ID_EDIT_FOREIGN_KEY, true);

					if (view.getConnection().getDatabase().equals(Database.MYSQL)) {
						view.enableComponent(ForeignKeysView.ID_DROP_FOREIGN_KEY, model.isPrototype());
					} else {
						view.enableComponent(ForeignKeysView.ID_DROP_FOREIGN_KEY, true);
					}
				}
			}
		}
	}
}
