package com.jeta.foundation.gui.editor.options;

import com.jeta.open.gui.framework.UIDirector;

public class KeyBindingUIDirector implements UIDirector {
	/** the view we are updating */
	private KeyBindingView m_view;

	/**
	 * ctor
	 */
	public KeyBindingUIDirector(KeyBindingView view) {
		m_view = view;
	}

	/**
	 * UIDirector implementation
	 */
	public void updateComponents(java.util.EventObject evt) {
		m_view.enableComponent(KeyBindingView.ID_RESET_DEFAULTS, true);

		if (m_view.getSelectedItem() == null) {
			m_view.enableComponent(KeyBindingView.ID_NEW, false);
			m_view.enableComponent(KeyBindingView.ID_EDIT, false);
			m_view.enableComponent(KeyBindingView.ID_CLEAR, false);
			m_view.enableComponent(KeyBindingView.ID_REMOVE, false);
		} else {
			m_view.enableComponent(KeyBindingView.ID_NEW, true);
			m_view.enableComponent(KeyBindingView.ID_EDIT, true);
			m_view.enableComponent(KeyBindingView.ID_CLEAR, true);
			m_view.enableComponent(KeyBindingView.ID_REMOVE, true);
		}
	}
}
