package com.jeta.abeille.gui.security.postgres;

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
import com.jeta.abeille.gui.utils.SQLErrorDialog;
import com.jeta.abeille.gui.utils.SQLOptionDialog;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

import com.jeta.open.rules.JETARule;

/**
 * Controller for UsersView class
 * 
 * @author Jeff Tassin
 */
public class PostgresUsersViewController extends UsersViewController {

	/**
	 * ctor
	 */
	public PostgresUsersViewController(UsersView view) {
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
	private void modifyUser2(User user) {
		PostgresUsersModel model = (PostgresUsersModel) ((UsersView) getView()).getModel();
		final boolean bcreate = (user == null);

		String dlgmsg = null;
		if (bcreate)
			dlgmsg = I18N.getLocalizedMessage("Create User");
		else
			dlgmsg = I18N.getLocalizedMessage("Modify User");

		SQLCommandDialog dlg = SQLCommandDialog.createDialog(getConnection(), (UsersView) getView(), true);
		dlg.setMessage(dlgmsg);
		final PostgresUserView userview = new PostgresUserView(getConnection(), user, model.getGroups(user));
		dlg.setPrimaryPanel(userview);
		dlg.addValidator((JETARule) userview.getController());
		dlg.setSize(dlg.getPreferredSize());
		dlg.addDialogListener(new SQLDialogListener() {
			public boolean cmdOk() throws SQLException {
				User user = userview.createUser();
				SecurityService srv = (SecurityService) getConnection().getImplementation(SecurityService.COMPONENT_ID);
				if (bcreate) {
					srv.addUser(user, userview.getGroups());
				} else {
					srv.modifyUser(user, null, userview.getGroups());
				}
				return true;
			}
		});

		dlg.showCenter();
		try {
			if (dlg.isOk())
				model.reload();
		} catch (Exception e) {
			TSUtils.printStackTrace(e);
		}
	}

	/**
	 * Action handler for creating a new user
	 */
	public class CreateUserAction2 implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			// modifyUser( null );
		}
	}

	/**
	 * Action handler for editing the selected user
	 */
	public class EditUserAction2 implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			User user = ((UsersView) getView()).getSelectedUser();
			if (user != null) {
				// modifyUser( user );
			}
		}
	}

}
