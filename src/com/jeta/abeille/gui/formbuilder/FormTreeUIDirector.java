package com.jeta.abeille.gui.formbuilder;

import javax.swing.tree.TreePath;

import com.jeta.abeille.gui.model.ObjectTreeNode;
import com.jeta.abeille.gui.model.ObjectTreeUIDirector;

import com.jeta.open.gui.framework.UIDirector;

/**
 * This class is responsible for updating (enable/disable) GUI controls on the
 * FormTreeView.
 * 
 * @author Jeff Tassin
 */
public class FormTreeUIDirector implements UIDirector {
	/** the view we are updating */
	private FormTree m_view;

	/**
	 * ctor
	 */
	public FormTreeUIDirector(FormTree view) {
		m_view = view;
	}

	/**
	 * UIDirector implementation
	 */
	public void updateComponents(java.util.EventObject evt) {
		// super.updateComponents(evt);

		int sel_forms = 0;
		TreePath[] selections = m_view.getTree().getSelectionPaths();
		if (selections != null) {
			for (int index = 0; index < selections.length; index++) {
				TreePath path = selections[index];
				ObjectTreeNode node = (ObjectTreeNode) path.getLastPathComponent();
				Object userobj = node.getUserObject();
				if (userobj instanceof FormProxy) {
					FormProxy proxy = (FormProxy) userobj;
					sel_forms++;
				}
			}
		}

		if (sel_forms == 0) {
			m_view.enableComponent(FormNames.ID_EDIT_FORM, false);
			m_view.enableComponent(FormNames.ID_RENAME_FORM, false);
			m_view.enableComponent(FormNames.ID_DELETE_FORM, false);
			m_view.enableComponent(FormNames.ID_SHOW_FORM, false);
		} else if (sel_forms == 1) {
			m_view.enableComponent(FormNames.ID_EDIT_FORM, true);
			m_view.enableComponent(FormNames.ID_RENAME_FORM, true);
			m_view.enableComponent(FormNames.ID_DELETE_FORM, true);
			m_view.enableComponent(FormNames.ID_SHOW_FORM, true);

		} else if (sel_forms > 1) {
			m_view.enableComponent(FormNames.ID_EDIT_FORM, false);
			m_view.enableComponent(FormNames.ID_RENAME_FORM, false);
			m_view.enableComponent(FormNames.ID_DELETE_FORM, true);
			m_view.enableComponent(FormNames.ID_SHOW_FORM, false);
		}
	}
}
