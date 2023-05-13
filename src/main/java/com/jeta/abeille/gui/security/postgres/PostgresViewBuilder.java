package com.jeta.abeille.gui.security.postgres;

import javax.swing.JTable;
import javax.swing.JTabbedPane;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableColumn;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.common.MetaDataTableRenderer;

import com.jeta.abeille.gui.security.GrantsView;
import com.jeta.abeille.gui.security.GroupsModel;
import com.jeta.abeille.gui.security.GroupsView;
import com.jeta.abeille.gui.security.SecurityMgrFrame;
import com.jeta.abeille.gui.security.UsersView;
import com.jeta.abeille.gui.security.UsersViewController;

import com.jeta.foundation.gui.table.TableUtils;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

public class PostgresViewBuilder {
	public static void buildViews(SecurityMgrFrame frame) {
		TSConnection conn = frame.getConnection();
		GroupsView groupsview = new GroupsView(new GroupsModel(conn));
		UsersView usersview = new UsersView(new PostgresUsersModel(conn));
		usersview.setController(new UsersViewController(usersview));

		JTable table = usersview.getTable();
		TableUtils.setColumnWidth(table, PostgresUsersModel.NAME_COLUMN, 25);
		TableUtils.setColumnWidth(table, PostgresUsersModel.USER_ID_COLUMN, 8);
		TableUtils.setColumnWidth(table, PostgresUsersModel.CREATE_DB_COLUMN, 8);
		TableUtils.setColumnWidth(table, PostgresUsersModel.CREATE_USER_COLUMN, 8);

		TableColumn column = table.getColumnModel().getColumn(PostgresUsersModel.NAME_COLUMN);
		column.setCellRenderer(MetaDataTableRenderer.createRenderer(conn));

		column = table.getColumnModel().getColumn(PostgresUsersModel.VALID_UNTIL_COLUMN);
		column.setCellRenderer(new com.jeta.abeille.gui.queryresults.TimeStampRenderer(null));

		final JTabbedPane tabpane = frame.getTabbedPane();
		tabpane.addTab(I18N.getLocalizedMessage("Users"), TSGuiToolbox.loadImage("incors/16x16/businessman.png"),
				usersview);
		tabpane.addTab(I18N.getLocalizedMessage("Groups"), TSGuiToolbox.loadImage("incors/16x16/businessmen.png"),
				groupsview);
		final GrantsView grantsview = new PostgresGrantsView(conn, usersview.getModel(), groupsview.getModel());
		grantsview.setController(new PostgresGrantsViewController(grantsview));
		tabpane.addTab(I18N.getLocalizedMessage("Grants"), TSGuiToolbox.loadImage("incors/16x16/certificate.png"),
				grantsview);

		tabpane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				java.awt.Component comp = tabpane.getSelectedComponent();
				if (comp == grantsview) {
					grantsview.refreshGroupsAndUsers();
				}
			}
		});
	}

}
