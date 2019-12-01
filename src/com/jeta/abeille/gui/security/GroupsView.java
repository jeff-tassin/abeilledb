package com.jeta.abeille.gui.security;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.TableColumn;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.security.Group;
import com.jeta.abeille.gui.common.MetaDataTableRenderer;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.table.AbstractTablePanel;
import com.jeta.foundation.gui.table.TableUtils;

import com.jeta.foundation.i18n.I18N;

/**
 * This class displays a list of groups in the database
 * 
 * @author Jeff Tassin
 */
public class GroupsView extends TSPanel {
	/** the table that displays the users */
	private JTable m_table;

	/* model for the groups */
	private GroupsModel m_model;

	/** command ids */
	public static final String ID_CREATE_GROUP = "create.group";
	public static final String ID_EDIT_GROUP = "edit.group";
	public static final String ID_DROP_GROUP = "drop.group";
	public static final String ID_RELOAD = "reload.groups";

	/**
	 * ctor
	 */
	public GroupsView(GroupsModel model) {
		setLayout(new BorderLayout());
		m_model = model;
		add(createButtonPanel(), BorderLayout.NORTH);
		add(createTable(), BorderLayout.CENTER);

		setController(new GroupsViewController(this));
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

		JButton btn = _createToolBarButton("incors/16x16/document_add.png", ID_CREATE_GROUP,
				I18N.getLocalizedMessage("Create Group"));
		toolbar.add(btn);

		btn = _createToolBarButton("incors/16x16/document_edit.png", ID_EDIT_GROUP,
				I18N.getLocalizedMessage("Edit Group"));
		toolbar.add(btn);

		btn = _createToolBarButton("incors/16x16/document_delete.png", ID_DROP_GROUP,
				I18N.getLocalizedMessage("Drop Group"));
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

		TableUtils.setColumnWidth(m_table, GroupsModel.NAME_COLUMN, 25);
		TableUtils.setColumnWidth(m_table, GroupsModel.GROUP_ID_COLUMN, 8);

		TableColumn column = m_table.getColumnModel().getColumn(GroupsModel.NAME_COLUMN);
		column.setCellRenderer(MetaDataTableRenderer.createInstance(m_model.getConnection()));
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
	public GroupsModel getModel() {
		return m_model;
	}

	/**
	 * @return the selected user. Null is returned if no check is selected
	 */
	public Group getSelectedGroup() {
		int row = m_table.getSelectedRow();
		if (row >= 0) {
			row = TableUtils.convertTableToModelIndex(m_table, row);
			return m_model.getRow(row);
		} else {
			return null;
		}
	}
}
