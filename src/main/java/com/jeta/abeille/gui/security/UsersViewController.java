package com.jeta.abeille.gui.security;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.sql.SQLException;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.security.User;
import com.jeta.abeille.database.security.SecurityService;

import com.jeta.abeille.gui.security.UserView;

import com.jeta.abeille.gui.utils.SQLCommandDialog;
import com.jeta.abeille.gui.utils.SQLDialogListener;
import com.jeta.abeille.gui.utils.SQLErrorDialog;
import com.jeta.abeille.gui.utils.SQLOptionDialog;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSPasswordDialog;
import com.jeta.open.gui.framework.UIDirector;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;
import com.jeta.open.rules.JETARule;

/**
 * Controller for UsersView class
 * 
 * @author Jeff Tassin
 */
public class UsersViewController extends TSController implements UIDirector {
	/** the view we are handling events for */
	private UsersView m_view;

	/**
	 * ctor
	 */
	public UsersViewController(UsersView view) {
		super(view);
		m_view = view;
		assignAction(UsersView.ID_CREATE_USER, new CreateUserAction());
		assignAction(UsersView.ID_EDIT_USER, new EditUserAction());
		assignAction(UsersView.ID_DROP_USER, new DropUserAction());
		assignAction(UsersView.ID_RELOAD, new ReloadAction());
		assignAction(UsersView.ID_RESET_PASSWORD, new ChangePasswordAction());

		view.setUIDirector(this);
		updateComponents(null);
	}

	/**
	 * @return the database connection
	 */
	public TSConnection getConnection() {
		return m_view.getConnection();
	}

	/**
	 * UIDirector implementation
	 */
	public void updateComponents(java.util.EventObject evt) {
		User user = m_view.getSelectedUser();
		if (user == null) {
			m_view.enableComponent(UsersView.ID_EDIT_USER, false);
			m_view.enableComponent(UsersView.ID_DROP_USER, false);
			m_view.enableComponent(UsersView.ID_RESET_PASSWORD, false);
		} else {
			if (Database.MYSQL.equals(getConnection().getDatabase()))
				m_view.enableComponent(UsersView.ID_EDIT_USER, false);
			else
				m_view.enableComponent(UsersView.ID_EDIT_USER, true);

			m_view.enableComponent(UsersView.ID_DROP_USER, true);
			m_view.enableComponent(UsersView.ID_RESET_PASSWORD, true);
		}
	}

	/**
	 * Modify a user.
	 * 
	 * @param user
	 *            if user is null, then this will create a new user by invoking
	 *            the new user dialog. If not null, then this will allow editing
	 *            of the user parameters.
	 */
	private void modifyUser(User user) {
		UsersModel model = ((UsersView) getView()).getModel();
		final boolean bcreate = (user == null);
		final User olduser = user;

		String dlgmsg = null;
		if (bcreate)
			dlgmsg = I18N.getLocalizedMessage("Create User");
		else
			dlgmsg = I18N.getLocalizedMessage("Modify User");

		SQLCommandDialog dlg = SQLCommandDialog.createDialog(getConnection(), (UsersView) getView(), true);
		dlg.setMessage(dlgmsg);

		SecurityBuilder builder = SecurityBuilder.createInstance(getConnection());
		final UserView userview = builder.createUserView(dlg, user, model.getGroups(user));
		dlg.setPrimaryPanel(userview);
		dlg.setSize(dlg.getPreferredSize());
		dlg.addDialogListener(new SQLDialogListener() {
			public boolean cmdOk() throws SQLException {
				User user = userview.createUser();
				SecurityService srv = (SecurityService) getConnection().getImplementation(SecurityService.COMPONENT_ID);
				if (bcreate) {
					srv.addUser(user, userview.getGroups());
				} else {
					srv.modifyUser(user, olduser, userview.getGroups());
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
	public class CreateUserAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			modifyUser(null);
		}
	}

	/**
	 * Action handler for editing the selected user
	 */
	public class EditUserAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			User user = ((UsersView) getView()).getSelectedUser();
			if (user != null) {
				modifyUser(user);
			}
		}
	}

	/**
	 * Action handler for changing the password for the selected user
	 */
	public class ChangePasswordAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			User user = m_view.getSelectedUser();
			if (user != null) {
				TSPasswordDialog dlg = (TSPasswordDialog) TSGuiToolbox.createDialog(TSPasswordDialog.class, m_view,
						true);
				dlg.setSize(dlg.getPreferredSize());
				dlg.setTitle(I18N.format("Change_password_1", user.getName()));
				dlg.showCenter();
				if (dlg.isOk()) {
					try {
						SecurityService srv = (SecurityService) getConnection().getImplementation(
								SecurityService.COMPONENT_ID);
						srv.changePassword(user, dlg.getPassword());
					} catch (SQLException e) {
						SQLErrorDialog.showErrorDialog(m_view, e, null);
					}
				}
			}
		}
	}

	/**
	 * Action handler for dropping the selected user
	 */
	public class DropUserAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			final User user = m_view.getSelectedUser();
			if (user != null) {
				String msg = I18N.format("Drop_1", user.getName());
				SQLOptionDialog dlg = SQLOptionDialog.createOptionDialog(getConnection(), true);
				dlg.setMessage(msg);
				dlg.addDialogListener(new SQLDialogListener() {
					public boolean cmdOk() throws SQLException {
						SecurityService srv = (SecurityService) getConnection().getImplementation(
								SecurityService.COMPONENT_ID);
						srv.dropUser(user);
						return true;
					}
				});

				dlg.showCenter();
				if (dlg.isOk())
					m_view.getModel().reload();
			}
		}
	}

	/**
	 * Action handler for reloading the users view
	 */
	public class ReloadAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			UsersModel model = m_view.getModel();
			model.reload();
		}
	}

}
