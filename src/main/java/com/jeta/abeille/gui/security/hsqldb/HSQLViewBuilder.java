package com.jeta.abeille.gui.security.hsqldb;

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
import com.jeta.foundation.i18n.I18N;

public class HSQLViewBuilder {
	public static void buildViews(SecurityMgrFrame frame) {
		TSConnection conn = frame.getConnection();
		UsersView usersview = new UsersView(new HSQLUsersModel(conn));
		usersview.setController(new UsersViewController(usersview));

		JTable table = usersview.getTable();
		TableUtils.setColumnWidth(table, HSQLUsersModel.NAME_COLUMN, 35);

		TableColumn column = table.getColumnModel().getColumn(HSQLUsersModel.NAME_COLUMN);
		column.setCellRenderer(MetaDataTableRenderer.createRenderer(conn));

		final JTabbedPane tabpane = frame.getTabbedPane();
		tabpane.addTab(I18N.getLocalizedMessage("Users"), usersview);
		// final GrantsView grantsview = new PostgresGrantsView( conn,
		// usersview.getModel(), groupsview.getModel() );
		// tabpane.addTab( I18N.getLocalizedMessage( "Grants" ), grantsview );

		tabpane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				java.awt.Component comp = tabpane.getSelectedComponent();
				// if ( comp == grantsview )
				// {
				// grantsview.refreshGroupsAndUsers();
				// }
			}
		});
	}

}
