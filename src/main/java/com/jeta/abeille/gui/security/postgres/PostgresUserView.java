package com.jeta.abeille.gui.security.postgres;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.security.Group;
import com.jeta.abeille.database.security.User;

import com.jeta.abeille.gui.security.UserView;
import com.jeta.abeille.gui.security.GroupsModel;
import com.jeta.abeille.gui.common.MetaDataPopupRenderer;

import com.jeta.foundation.gui.components.TSDateSpinner;
import com.jeta.foundation.gui.components.TSTimeSpinner;
import com.jeta.foundation.gui.components.TSListPanel;
import com.jeta.foundation.gui.components.TSPanel;

import com.jeta.foundation.gui.layouts.AssignmentPanelLayout;
import com.jeta.foundation.gui.layouts.ColumnLayout;

import com.jeta.foundation.gui.utils.ControlsAlignLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * This class implements a view for a user in the database
 * 
 * @author Jeff Tassin
 */
public class PostgresUserView extends UserView {

	/** the user */
	private User m_user;

	/** the database connection */
	private TSConnection m_connection;

	private JCheckBox m_createdbcheck = new JCheckBox(I18N.getLocalizedMessage("Can Create Database"));
	private JCheckBox m_createusercheck = new JCheckBox(I18N.getLocalizedMessage("Can Create User"));
	private JCheckBox m_encryptedcheck = new JCheckBox(I18N.getLocalizedMessage("Encrypted"));

	private JCheckBox m_alwayscheck = new JCheckBox(I18N.getLocalizedMessage("Always"));

	private TSListPanel m_groupslist = new TSListPanel();
	private TSListPanel m_assignedgroupslist = new TSListPanel();

	private TSDateSpinner m_expiredatefield;
	private TSTimeSpinner m_expiretimefield;

	private JTextField m_namefield = new JTextField();
	private JPasswordField m_password = new JPasswordField();
	private JPasswordField m_confirmpassword = new JPasswordField();

	/** command ids */
	public static final String ID_ADD_GROUP = "add.group";
	public static final String ID_REMOVE_GROUP = "remove.group";

	public static final String ID_GROUPS_LIST = "groups.list.box";
	public static final String ID_ASSIGNED_GROUPS_LIST = "assigned.groups.list.box";
	public static final String ID_VALID_DATE = "valid.date.comp";
	public static final String ID_VALID_TIME = "valid.time.comp";
	public static final String ID_ALWAYS_CHECK = "always.check";

	/**
	 * ctor
	 * 
	 * @param connection
	 *            the underlying database connection
	 * @param user
	 *            the user we are displaying properties for
	 * @param assignGroups
	 *            a list of groups (Group objects) that this user belongs to
	 */
	public PostgresUserView(TSConnection connection, User user, Collection assignedGroups) {
		m_connection = connection;
		m_user = user;

		setLayout(new BorderLayout());

		JPanel bottompanel = new JPanel(new BorderLayout());
		bottompanel.add(createOptionsPanel(), BorderLayout.NORTH);
		bottompanel.add(createGroupsPanel(), BorderLayout.CENTER);

		add(createTextFieldsPanel(), BorderLayout.NORTH);
		add(bottompanel, BorderLayout.CENTER);

		loadData(assignedGroups);
	}

	/**
	 * create the groups assignment view
	 */
	private JComponent createGroupsPanel() {
		JPanel panel = new JPanel();

		m_groupslist.setHeadingText("Groups");
		JList groupslist = m_groupslist.getJList();
		groupslist.setName(ID_GROUPS_LIST);
		groupslist.setCellRenderer(MetaDataPopupRenderer.createInstance(m_connection));

		JPanel btnpanel = new JPanel();
		btnpanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
		btnpanel.setLayout(new BoxLayout(btnpanel, BoxLayout.Y_AXIS));
		JButton addbtn = createButton(TSGuiToolbox.loadImage("navigation/Forward16.gif"), ID_ADD_GROUP);
		JButton removebtn = createButton(TSGuiToolbox.loadImage("navigation/Back16.gif"), ID_REMOVE_GROUP);
		btnpanel.add(addbtn);
		btnpanel.add(removebtn);

		Dimension d = new Dimension(32, 24);
		addbtn.setPreferredSize(d);
		addbtn.setMinimumSize(d);
		addbtn.setMaximumSize(d);

		removebtn.setPreferredSize(d);
		removebtn.setMinimumSize(d);
		removebtn.setMaximumSize(d);

		m_assignedgroupslist.setHeadingText("Assigned");
		JList assignedlist = m_assignedgroupslist.getJList();
		assignedlist.setName(ID_ASSIGNED_GROUPS_LIST);
		assignedlist.setCellRenderer(MetaDataPopupRenderer.createInstance(m_connection));

		panel.add(m_groupslist);
		panel.add(btnpanel);
		panel.add(m_assignedgroupslist);
		panel.setLayout(new AssignmentPanelLayout(m_groupslist, btnpanel, m_assignedgroupslist));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		return panel;
	}

	/**
	 * Create the panel that contains the user options check boxes and
	 * properties
	 */
	private JComponent createOptionsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new ColumnLayout());

		JPanel topbtnpanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		topbtnpanel.add(m_createdbcheck);
		topbtnpanel.add(Box.createHorizontalStrut(30));
		topbtnpanel.add(m_encryptedcheck);

		JPanel bottombtnpanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bottombtnpanel.add(m_createusercheck);

		panel.add(topbtnpanel);
		panel.add(bottombtnpanel);
		panel.add(Box.createVerticalStrut(10));
		panel.add(createValidUntilPanel());

		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		return panel;
	}

	private JComponent createValidUntilPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		m_alwayscheck.setName(ID_ALWAYS_CHECK);
		// setCommandHandler( m_alwayscheck, ID_ALWAYS_CHECK );

		m_alwayscheck.setSelected(true);
		panel.add(m_alwayscheck);
		panel.add(Box.createHorizontalStrut(30));

		m_expiredatefield = new TSDateSpinner();
		m_expiredatefield.setName(ID_VALID_DATE);
		m_expiredatefield.setEnabled(false);
		panel.add(m_expiredatefield);
		panel.add(Box.createHorizontalStrut(5));

		m_expiretimefield = new TSTimeSpinner();
		m_expiretimefield.setName(ID_VALID_TIME);
		m_expiretimefield.setEnabled(false);
		panel.add(m_expiretimefield);
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);

		String title = I18N.getLocalizedMessage("Valid Until");
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(title),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		return panel;
	}

	/**
	 * Creates the topmost panel
	 */
	private JComponent createTextFieldsPanel() {
		Component[] left = new Component[3];
		left[0] = new JLabel(I18N.getLocalizedMessage("Name"));
		left[1] = new JLabel(I18N.getLocalizedMessage("Password"));
		left[2] = new JLabel(I18N.getLocalizedMessage("Confirm"));

		Component[] right = new Component[3];
		right[0] = getNameField();
		right[1] = getPasswordField();
		right[2] = getConfirmPasswordField();

		ControlsAlignLayout layout = new ControlsAlignLayout();
		layout.setMaxTextFieldWidth(getNameField(), 30);
		layout.setMaxTextFieldWidth(getPasswordField(), 30);
		layout.setMaxTextFieldWidth(getConfirmPasswordField(), 30);

		JPanel panel = TSGuiToolbox.alignLabelTextRows(layout, left, right, new java.awt.Insets(10, 10, 0, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return panel;
	}

	/**
	 * Creates a User object based on the information entered into the dialog
	 */
	public User createUser() {
		User user = new User(getUserName());
		user.setPassword(getPassword());

		user.canCreateDB(m_createdbcheck.isSelected());
		user.canCreateUser(m_createusercheck.isSelected());
		user.setPasswordEncrypted(m_encryptedcheck.isSelected());

		user.setValidAlways(isValidAlways());
		user.setExpireDate(getExpireDate());
		// we don't add groups here, the caller must get this information
		// by calling getGroups
		return user;
	}

	/**
	 * @return the expiration date entered by the user
	 */
	public Calendar getExpireDate() {
		Calendar c = Calendar.getInstance();
		m_expiredatefield.toCalendar(c);
		m_expiretimefield.toCalendar(c);
		return c;
	}

	public JTextField getNameField() {
		return m_namefield;
	}

	public JPasswordField getConfirmPasswordField() {
		return m_confirmpassword;
	}

	public JPasswordField getPasswordField() {
		return m_password;
	}

	/**
	 * @return the user name entered by the user
	 */
	public String getUserName() {
		return m_namefield.getText();
	}

	/**
	 * @return the password entered by the user
	 */
	public char[] getPassword() {
		return m_password.getPassword();
	}

	/**
	 * @return the confirmed password entered by the user
	 */
	public char[] getConfirmPassword() {
		return m_confirmpassword.getPassword();
	}

	/**
	 * Sets the password in the password and confirm password boxes
	 */
	protected void setPassword(char[] password) {
		if (password == null) {
			m_password.setText("");
			m_confirmpassword.setText("");
		} else {
			m_password.setText(String.valueOf(password));
			m_confirmpassword.setText(String.valueOf(password));
		}
	}

	/**
	 * @return the set of groups (Group objects) this user has been assigned to
	 */
	public Collection getGroups() {
		LinkedList results = new LinkedList();

		JList assignedlist = (JList) getComponentByName(PostgresUserView.ID_ASSIGNED_GROUPS_LIST);
		DefaultListModel assignedmodel = (DefaultListModel) assignedlist.getModel();
		for (int index = 0; index < assignedmodel.getSize(); index++) {
			Group group = (Group) assignedmodel.getElementAt(index);
			if (group != null)
				results.add(group);
		}

		return results;
	}

	/**
	 * @return the preferred size for this view
	 */
	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(7, 18);
	}

	public boolean isEncrypted() {
		return m_encryptedcheck.isSelected();
	}

	/**
	 * @return true if we are creating a new user
	 */
	public boolean isNew() {
		return (m_user == null);
	}

	/**
	 * @return true if the isValid check box is selected
	 */
	public boolean isValidAlways() {
		return m_alwayscheck.isSelected();
	}

	/**
	 * Initializes the GUI state from the user and groups data
	 */
	private void loadData(Collection assignedGroups) {
		try {
			GroupsModel groups = new GroupsModel(m_connection);
			DefaultListModel lmodel = (DefaultListModel) m_groupslist.getModel();
			for (int index = 0; index < groups.getRowCount(); index++) {
				Group group = groups.getRow(index);
				lmodel.addElement(group);
			}

			if (m_user != null) {

				lmodel = (DefaultListModel) m_assignedgroupslist.getModel();
				Iterator iter = assignedGroups.iterator();
				while (iter.hasNext()) {
					Group group = (Group) iter.next();
					lmodel.addElement(group);
				}

				JButton btn = (JButton) getComponentByName(ID_ADD_GROUP);
				btn.setEnabled(false);
				btn = (JButton) getComponentByName(ID_REMOVE_GROUP);
				btn.setEnabled(false);

				getNameField().setEnabled(false);
				getNameField().setText(m_user.getName());

				setPassword(m_user.getPassword());

				m_createdbcheck.setSelected(m_user.canCreateDB());
				m_createusercheck.setSelected(m_user.canCreateUser());
				m_alwayscheck.setSelected(m_user.isValidAlways());
				m_expiredatefield.setCalendar(m_user.getExpireDate());
				m_expiretimefield.setCalendar(m_user.getExpireDate());

				getPasswordField().setEnabled(false);
				getConfirmPasswordField().setEnabled(false);
				m_encryptedcheck.setEnabled(false);

				if (!m_user.isValidAlways()) {
					m_alwayscheck.setEnabled(false);
					m_expiredatefield.setEnabled(true);
					m_expiretimefield.setEnabled(true);
				}
			}
		} catch (Exception e) {

		}
	}

}
