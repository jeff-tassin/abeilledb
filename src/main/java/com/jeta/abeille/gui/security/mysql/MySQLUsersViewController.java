package com.jeta.abeille.gui.security.mysql;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.sql.SQLException;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.security.User;
import com.jeta.abeille.database.security.SecurityService;

import com.jeta.abeille.gui.security.UsersView;
import com.jeta.abeille.gui.security.UsersViewController;

import com.jeta.abeille.gui.utils.SQLCommandDialog;
import com.jeta.abeille.gui.utils.SQLDialogListener;
import com.jeta.abeille.gui.utils.SQLOptionDialog;

import com.jeta.foundation.gui.components.TSController;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

import com.jeta.open.rules.JETARule;

import com.jeta.plugins.abeille.mysql.MySQLUser;
import com.jeta.plugins.abeille.mysql.MySQLSecurityServiceImplementation;

/**
 * Controller for UsersView class for MySQL
 * 
 * @author Jeff Tassin
 */
public class MySQLUsersViewController extends UsersViewController {
	/**
	 * ctor
	 */
	public MySQLUsersViewController(UsersView view) {
		super(view);
		// assignAction( UsersView.ID_CREATE_USER, new CreateUserAction() );
		// assignAction( UsersView.ID_EDIT_USER, new EditUserAction() );
	}

	/**
	 * Modify a user.
	 * 
	 * @param user
	 *            if user is null, then this will create a new user by invoking
	 *            the new user dialog. If not null, then this will allow editing
	 *            of the user parameters.
	 */
	private void modifyUser2(MySQLUser user) {
		MySQLUsersModel model = (MySQLUsersModel) ((UsersView) getView()).getModel();
		final boolean bcreate = (user == null);
		final MySQLUser olduser = user;

		String dlgmsg = null;
		if (bcreate)
			dlgmsg = I18N.getLocalizedMessage("Create User");
		else
			dlgmsg = I18N.getLocalizedMessage("Modify User");

		SQLCommandDialog dlg = SQLCommandDialog.createDialog(getConnection(), (UsersView) getView(), true);
		dlg.setMessage(dlgmsg);
		final MySQLUserView userview = new MySQLUserView(getConnection(), olduser);
		dlg.setPrimaryPanel(userview);
		dlg.addValidator((JETARule) userview.getController());
		TSGuiToolbox.setReasonableWindowSize(dlg, dlg.getPreferredSize());
		dlg.addDialogListener(new SQLDialogListener() {
			public boolean cmdOk() throws SQLException {
				if (bcreate) {
					MySQLSecurityServiceImplementation srv = (MySQLSecurityServiceImplementation) getConnection()
							.getImplementation(MySQLSecurityServiceImplementation.COMPONENT_ID);
					srv.addUser((MySQLUser) userview.createUser(), null);
				} else {
					MySQLSecurityServiceImplementation srv = (MySQLSecurityServiceImplementation) getConnection()
							.getImplementation(MySQLSecurityServiceImplementation.COMPONENT_ID);
					srv.modifyUser((MySQLUser) userview.createUser(), olduser, null);
				}
				return true;
			}
		});

		dlg.showCenter();
		if (dlg.isOk())
			model.reload();
	}

	/**
	 * Action handler for creating a new user
	 */
	public class CreateUserAction2 implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			// modifyUser( null );
		}
	}

}
