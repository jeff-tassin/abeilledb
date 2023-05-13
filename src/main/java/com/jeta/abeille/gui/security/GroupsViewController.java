package com.jeta.abeille.gui.security;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.sql.SQLException;

import java.util.Collection;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.database.security.Group;
import com.jeta.abeille.database.security.SecurityService;

import com.jeta.abeille.gui.utils.SQLCommandDialog;
import com.jeta.abeille.gui.utils.SQLDialogListener;
import com.jeta.abeille.gui.utils.SQLErrorDialog;
import com.jeta.abeille.gui.utils.SQLOptionDialog;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;

import com.jeta.open.gui.framework.UIDirector;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

import com.jeta.open.rules.JETARule;

import com.jeta.foundation.utils.TSUtils;

/**
 * Controller for GroupsView class
 * 
 * @author Jeff Tassin
 */
public class GroupsViewController extends TSController implements UIDirector {
	/** the view we are handling events for */
	private GroupsView m_view;

	/**
	 * ctor
	 */
	public GroupsViewController(GroupsView view) {
		super(view);
		m_view = view;
		assignAction(GroupsView.ID_CREATE_GROUP, new CreateGroupAction());
		assignAction(GroupsView.ID_EDIT_GROUP, new EditGroupAction());
		assignAction(GroupsView.ID_DROP_GROUP, new DropGroupAction());
		assignAction(GroupsView.ID_RELOAD, new ReloadAction());

		view.setUIDirector(this);
		updateComponents(null);
	}

	private void editGroup(Group group) {
		final Group oldgroup = group;
		SQLCommandDialog dlg = SQLCommandDialog.createDialog(m_view.getConnection(), m_view, true);

		if (oldgroup == null)
			dlg.setMessage(I18N.getLocalizedMessage("Create Group"));
		else
			dlg.setMessage(I18N.getLocalizedMessage("Modify Group"));

		GroupsModel model = m_view.getModel();
		final GroupView groupview = new GroupView(m_view.getConnection(), oldgroup);
		dlg.setPrimaryPanel(groupview);
		dlg.addValidator((JETARule) groupview.getController());
		TSGuiToolbox.setReasonableWindowSize(dlg, dlg.getPreferredSize());
		dlg.addDialogListener(new SQLDialogListener() {
			public boolean cmdOk() throws SQLException {
				Group newgroup = groupview.createGroup();
				SecurityService srv = (SecurityService) getConnection().getImplementation(SecurityService.COMPONENT_ID);

				if (oldgroup == null) {
					// create a new group
					srv.addGroup(newgroup, groupview.getUsers());
				} else {
					// modify an existing group
					srv.modifyGroup(newgroup, groupview.getUsers(), oldgroup);
				}
				return true;
			}
		});

		dlg.showCenter();
		if (dlg.isOk())
			model.reload();
	}

	/**
	 * @return the underlying database connection
	 */
	TSConnection getConnection() {
		return m_view.getConnection();
	}

	/**
	 * UIDirector implementation
	 */
	public void updateComponents(java.util.EventObject evt) {
		Group group = m_view.getSelectedGroup();
		if (group == null) {
			m_view.enableComponent(GroupsView.ID_EDIT_GROUP, false);
			m_view.enableComponent(GroupsView.ID_DROP_GROUP, false);
		} else {
			m_view.enableComponent(GroupsView.ID_EDIT_GROUP, true);
			m_view.enableComponent(GroupsView.ID_DROP_GROUP, true);
		}
	}

	/**
	 * Action handler for creating a new user
	 */
	public class CreateGroupAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			editGroup(null);
		}
	}

	/**
	 * Action handler for dropping the selected group
	 */
	public class DropGroupAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			final Group group = m_view.getSelectedGroup();
			if (group != null) {
				String msg = I18N.format("Drop_1", group.getName());
				SQLOptionDialog dlg = SQLOptionDialog.createOptionDialog(getConnection(), true);
				dlg.setMessage(msg);
				dlg.addDialogListener(new SQLDialogListener() {
					public boolean cmdOk() throws SQLException {
						GroupsModel model = m_view.getModel();
						SecurityService srv = (SecurityService) getConnection().getImplementation(
								SecurityService.COMPONENT_ID);
						srv.dropGroup(group);
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
	 * Action handler for editing the selected group
	 */
	public class EditGroupAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			Group group = m_view.getSelectedGroup();
			if (group != null) {
				editGroup(group);
			}
		}
	}

	/**
	 * Action handler for reloading the users view
	 */
	public class ReloadAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			GroupsModel model = m_view.getModel();
			model.reload();
		}
	}
}
