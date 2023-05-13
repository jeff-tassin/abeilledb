package com.jeta.abeille.gui.security;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;

import com.jeta.abeille.database.security.Group;
import com.jeta.abeille.database.security.User;

import com.jeta.foundation.gui.components.TSController;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;

/**
 * Controller for GroupView class
 * 
 * @author Jeff Tassin
 */
public class GroupViewController extends TSController implements JETARule {
	/** the view we are handling events for */
	private GroupView m_view;

	/**
	 * ctor
	 */
	public GroupViewController(GroupView view) {
		super(view);
		m_view = view;

		assignAction(GroupView.ID_ADD_USER, new AddUserAction());
		assignAction(GroupView.ID_REMOVE_USER, new RemoveUserAction());
	}

	/**
	 * Overriden from TSControoler to provide a validation
	 * 
	 * @return an error message if the validation failed. Return null if the
	 *         validation succeeded
	 */
	public RuleResult check(Object[] params) {
		// group name must be valid
		String groupname = m_view.getName();
		if (groupname.trim().length() == 0) {
			return new RuleResult(I18N.getLocalizedMessage("Invalid Group Name"));
		}

		return RuleResult.SUCCESS;
	}

	/**
	 * ActionListener that gets called when the user presses the Add Group
	 * button
	 */
	public class AddUserAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JList grouplist = (JList) m_view.getComponentByName(GroupView.ID_USERS_LIST);
			User user = (User) grouplist.getSelectedValue();
			if (user != null) {
				JList assignedlist = (JList) m_view.getComponentByName(GroupView.ID_ASSIGNED_USERS_LIST);
				DefaultListModel assignedmodel = (DefaultListModel) assignedlist.getModel();
				if (!assignedmodel.contains(user)) {
					assignedmodel.addElement(user);
				}
			}
		}
	}

	/**
	 * ActionListener that gets called when the user presses the Remove Group
	 * button
	 */
	public class RemoveUserAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JList assignedlist = (JList) m_view.getComponentByName(GroupView.ID_ASSIGNED_USERS_LIST);
			User user = (User) assignedlist.getSelectedValue();
			if (user != null) {
				DefaultListModel listmodel = (DefaultListModel) assignedlist.getModel();
				listmodel.removeElement(user);
			}
		}
	}

}
