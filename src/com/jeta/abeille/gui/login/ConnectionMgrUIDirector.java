package com.jeta.abeille.gui.login;

import javax.swing.JList;

import com.jeta.open.gui.framework.UIDirector;

/**
 * ctor
 */
public class ConnectionMgrUIDirector implements UIDirector {
	private ConnectionMgrView m_view;

	public ConnectionMgrUIDirector(ConnectionMgrView view) {
		m_view = view;
	}

	/**
	 * Updates the ComponentMgrView UI
	 */
	public void updateComponents(java.util.EventObject evt) {
		JList list = (JList) m_view.getComponentByName(ConnectionMgrView.ID_CONNECTION_LIST);
		int index = list.getSelectedIndex();
		if (index >= 0) {
			m_view.enableComponent(ConnectionMgrView.ID_DELETE_CONNECTION, true);
			m_view.enableComponent(ConnectionMgrView.ID_EDIT_CONNECTION, true);
			m_view.enableComponent(ConnectionMgrView.ID_LOGIN, true);
		} else {
			m_view.enableComponent(ConnectionMgrView.ID_DELETE_CONNECTION, false);
			m_view.enableComponent(ConnectionMgrView.ID_EDIT_CONNECTION, false);
			m_view.enableComponent(ConnectionMgrView.ID_LOGIN, false);
		}
	}
}
