package com.jeta.foundation.gui.editor.options;

import com.jeta.open.gui.framework.UIDirector;

import com.jeta.foundation.gui.editor.KitKeyBindingModel;

/**
 * The UIDirector for the EditorOptionsView
 */
public class EditorOptionsUIDirector implements UIDirector {
	private EditorOptionsView m_view;

	public EditorOptionsUIDirector(EditorOptionsView view) {
		m_view = view;
	}

	public void updateComponents(java.util.EventObject evt) {
		String editor = m_view.getSelectedEditor();
		if (editor.equals(KitKeyBindingModel.DEFAULT_BINDING)) {
			m_view.enableComponent(EditorOptionsView.ID_DELETE_BINDINGS, false);
		} else {
			m_view.enableComponent(EditorOptionsView.ID_DELETE_BINDINGS, true);
		}
	}
}
