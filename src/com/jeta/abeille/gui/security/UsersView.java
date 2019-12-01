package com.jeta.abeille.gui.security;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.security.Group;
import com.jeta.abeille.database.security.User;
import com.jeta.abeille.gui.common.MetaDataTableRenderer;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.table.AbstractTablePanel;
import com.jeta.foundation.gui.table.TableUtils;

import com.jeta.foundation.i18n.I18N;

/**
 * This class displays a list of users in the database
 * 
 * @author Jeff Tassin
 */
public class UsersView extends TSPanel {
	/** the table that displays the users */
	private JTable m_table;

	/* model for the users */
	private UsersModel m_model;

	/** command ids */
	public static final String ID_CREATE_USER = "create.user";
	public static final String ID_EDIT_USER = "edit.user";
	public static final String ID_DROP_USER = "drop.user";
	public static final String ID_RESET_PASSWORD = "reset.password";
	public static final String ID_RELOAD = "reload.users";

	/**
	 * ctor
	 */
	public UsersView(UsersModel model) {
		setLayout(new BorderLayout());
		m_model = model;
		add(createButtonPanel(), BorderLayout.NORTH);
		add(createTable(), BorderLayout.CENTER);

		setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
	}

	/**
	 * Helper method to create a button
	 */
	private JButton _createToolBarButton(String iconName, String id, String tooltip) {
		JButton btn = i18n_createToolBarButton(iconName, id, tooltip);
		btn.setBorderPainted(false);
		btn.setFocusPainted(false);
		if (tooltip != null)
			btn.setToolTipText(tooltip);

		return btn;
	}

	/**
	 * Creates the toolbar buttons panel
	 */
	private JComponent createButtonPanel() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);

		JButton btn = _createToolBarButton("incors/16x16/document_add.png", ID_CREATE_USER,
				I18N.getLocalizedMessage("Create User"));
		toolbar.add(btn);

		btn = _createToolBarButton("incors/16x16/document_edit.png", ID_EDIT_USER,
				I18N.getLocalizedMessage("Edit User"));
		toolbar.add(btn);

		btn = _createToolBarButton("incors/16x16/document_delete.png", ID_DROP_USER,
				I18N.getLocalizedMessage("Drop User"));
		toolbar.add(btn);

		btn = _createToolBarButton("incors/16x16/key1.png", ID_RESET_PASSWORD,
				I18N.getLocalizedMessage("Reset Password"));
		toolbar.add(btn);

		toolbar.addSeparator();

		btn = _createToolBarButton("incors/16x16/refresh.png", ID_RELOAD, I18N.getLocalizedMessage("Reload"));
		toolbar.add(btn);

		return toolbar;
	}

	/**
	 * Creates the JTable that displays the users
	 * 
	 * @returns the table component
	 */
	private JComponent createTable() {
		AbstractTablePanel tablepanel = TableUtils.createBasicTablePanel(m_model, true);
		m_table = tablepanel.getTable();
		return tablepanel;
	}

	/**
	 * @return the database connection
	 */
	public TSConnection getConnection() {
		return m_model.getConnection();
	}

	/**
	 * @return the underlying data model
	 */
	public UsersModel getModel() {
		return m_model;
	}

	/**
	 * @return the selected user. Null is returned if no check is selected
	 */
	public User getSelectedUser() {
		int row = m_table.getSelectedRow();
		if (row >= 0) {
			row = TableUtils.convertTableToModelIndex(m_table, row);
			return (User) m_model.getRow(row);
		} else {
			return null;
		}
	}

	/**
	 * @return the main table for this view
	 */
	public JTable getTable() {
		return m_table;
	}

}
