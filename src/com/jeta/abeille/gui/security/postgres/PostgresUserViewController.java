package com.jeta.abeille.gui.security.postgres;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;

import com.jeta.abeille.database.security.Group;
import com.jeta.abeille.gui.security.UserView;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDateSpinner;
import com.jeta.foundation.gui.components.TSTimeSpinner;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;
import com.jeta.foundation.utils.TSUtils;

/**
 * Controller for UserView class
 * 
 * @author Jeff Tassin
 */
public class PostgresUserViewController extends TSController implements JETARule {
	/** the view we are handling events for */
	private PostgresUserView m_view;

	/**
	 * ctor
	 */
	public PostgresUserViewController(PostgresUserView view) {
		super(view);
		m_view = view;

		assignAction(PostgresUserView.ID_ADD_GROUP, new AddGroupAction());
		assignAction(PostgresUserView.ID_REMOVE_GROUP, new RemoveGroupAction());
		assignAction(PostgresUserView.ID_ALWAYS_CHECK, new ValidAlwaysAction());
	}

	/**
	 * Overriden from TSController to provide a validation
	 * 
	 * @return an error message if the validation failed. Return null if the
	 *         validation succeeded
	 */
	public RuleResult check(Object[] params) {
		// check password matches confirmed password if both are not empty
		char[] password = m_view.getPassword();
		if (password.length == 0) {
			if (m_view.isNew() && m_view.isEncrypted()) {
				return new RuleResult(I18N.getLocalizedMessage("password_required_when_encrypted"));
			}
		}

		if (!m_view.isValidAlways()) {
			TSTimeSpinner tmspin = (TSTimeSpinner) m_view.getComponentByName(PostgresUserView.ID_VALID_TIME);
			TSDateSpinner dtspin = (TSDateSpinner) m_view.getComponentByName(PostgresUserView.ID_VALID_DATE);
			if (tmspin.isNull() || dtspin.isNull()) {
				return new RuleResult(I18N.getLocalizedMessage("Invalid Date"));
			}

			if (TSUtils.isDebug()) {

				dtspin.print();
				java.util.Calendar c = m_view.getExpireDate();
				java.text.SimpleDateFormat format = new java.text.SimpleDateFormat();
				System.out.println("view.getExpireDate: " + format.format(c.getTime()));
			}
		}
		return RuleResult.SUCCESS;
	}

	/**
	 * ActionListener that gets called when the user presses the Add Group
	 * button
	 */
	public class AddGroupAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JList grouplist = (JList) m_view.getComponentByName(PostgresUserView.ID_GROUPS_LIST);
			Group group = (Group) grouplist.getSelectedValue();
			if (group != null) {
				JList assignedlist = (JList) m_view.getComponentByName(PostgresUserView.ID_ASSIGNED_GROUPS_LIST);
				DefaultListModel assignedmodel = (DefaultListModel) assignedlist.getModel();
				if (!assignedmodel.contains(group)) {
					assignedmodel.addElement(group);
				}
			}
		}
	}

	/**
	 * ActionListener that gets called when the user presses the Remove Group
	 * button
	 */
	public class RemoveGroupAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JList assignedlist = (JList) m_view.getComponentByName(PostgresUserView.ID_ASSIGNED_GROUPS_LIST);
			Group group = (Group) assignedlist.getSelectedValue();
			if (group != null) {
				DefaultListModel listmodel = (DefaultListModel) assignedlist.getModel();
				listmodel.removeElement(group);
			}
		}
	}

	/**
	 * ActionListener that gets called when the user selects the Valid Always
	 * check box
	 */
	public class ValidAlwaysAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JCheckBox cbox = (JCheckBox) m_view.getComponentByName(PostgresUserView.ID_ALWAYS_CHECK);
			JComponent comp = (JComponent) m_view.getComponentByName(PostgresUserView.ID_VALID_DATE);
			comp.setEnabled(!cbox.isSelected());
			comp = (JComponent) m_view.getComponentByName(PostgresUserView.ID_VALID_TIME);
			comp.setEnabled(!cbox.isSelected());
		}
	}

}
