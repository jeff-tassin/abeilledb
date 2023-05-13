package com.jeta.abeille.gui.security.mysql;

import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableColumn;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.common.MetaDataTableRenderer;
import com.jeta.abeille.gui.security.SecurityMgrFrame;
import com.jeta.abeille.gui.security.UsersView;
import com.jeta.abeille.gui.security.UsersViewController;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * Builds the views in the SecurityManager frame for MySQL databases
 * 
 * @author Jeff Tassin
 */
public class MySQLViewBuilder {
	public static void buildViews(SecurityMgrFrame frame) {
		TSConnection conn = frame.getConnection();
		UsersView usersview = new UsersView(new MySQLUsersModel(conn));
		usersview.enableComponent(UsersView.ID_EDIT_USER, false);
		usersview.setController(new UsersViewController(usersview));

		JTable table = usersview.getTable();

		TableColumn column = table.getColumnModel().getColumn(MySQLUsersModel.NAME_COLUMN);
		column.setCellRenderer(MetaDataTableRenderer.createRenderer(conn));
		column = table.getColumnModel().getColumn(MySQLUsersModel.HOST_COLUMN);
		column.setCellRenderer(MetaDataTableRenderer.createRenderer(TSGuiToolbox.loadImage("development/Host16.gif")));

		final JTabbedPane tabpane = frame.getTabbedPane();
		tabpane.addTab(I18N.getLocalizedMessage("Users"), TSGuiToolbox.loadImage("incors/16x16/businessman.png"),
				usersview);

		final MySQLGrantsView grantsview = new MySQLGrantsView(conn, usersview.getModel(), null);
		grantsview.setController(new MySQLGrantsViewController(grantsview));

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
