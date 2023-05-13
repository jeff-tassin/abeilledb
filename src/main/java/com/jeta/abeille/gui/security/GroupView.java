package com.jeta.abeille.gui.security;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.security.Group;
import com.jeta.abeille.database.security.SecurityService;
import com.jeta.abeille.database.security.User;
import com.jeta.abeille.gui.common.MetaDataPopupRenderer;

import com.jeta.foundation.gui.components.TSListPanel;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.layouts.AssignmentPanelLayout;

import com.jeta.foundation.gui.utils.ControlsAlignLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class implements a view for a group in the database
 * 
 * @author Jeff Tassin
 */
public class GroupView extends TSPanel {

	/** the user */
	private Group m_group;

	/** the database connection */
	private TSConnection m_connection;

	private JTextField m_namefield = new JTextField();

	private TSListPanel m_userslist = new TSListPanel();
	private TSListPanel m_assigneduserslist = new TSListPanel();

	/** command ids */
	public static final String ID_ADD_USER = "add.user";
	public static final String ID_REMOVE_USER = "remove.user";

	public static final String ID_ASSIGNED_USERS_LIST = "assigned.users.list";
	public static final String ID_USERS_LIST = "users.list";

	/**
	 * ctor
	 */
	public GroupView(TSConnection connection, Group group) {
		m_connection = connection;
		m_group = group;

		setLayout(new BorderLayout());

		add(createPropertiesPanel(), BorderLayout.NORTH);
		add(createUsersPanel(), BorderLayout.CENTER);
		loadData();

		setController(new GroupViewController(this));
	}

	/**
	 * Create a group object based on the values entered by the user
	 */
	public Group createGroup() {
		Group group = new Group();
		group.setName(getName());
		Collection groups = getUsers();
		Iterator iter = groups.iterator();
		while (iter.hasNext()) {
			User user = (User) iter.next();
			group.addUser(user);
		}
		return group;
	}

	/**
	 * create the users assignment view
	 */
	private JComponent createUsersPanel() {
		JPanel panel = new JPanel();

		m_userslist.setHeadingText("Users");
		JList userslist = m_userslist.getJList();
		userslist.setName(ID_USERS_LIST);
		userslist.setCellRenderer(MetaDataPopupRenderer.createInstance(m_connection));

		panel.add(m_userslist);

		JPanel btnpanel = new JPanel();
		btnpanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
		btnpanel.setLayout(new BoxLayout(btnpanel, BoxLayout.Y_AXIS));
		JButton addbtn = createButton(TSGuiToolbox.loadImage("navigation/Forward16.gif"), ID_ADD_USER);
		JButton removebtn = createButton(TSGuiToolbox.loadImage("navigation/Back16.gif"), ID_REMOVE_USER);
		btnpanel.add(addbtn);
		btnpanel.add(removebtn);

		Dimension d = new Dimension(32, 24);
		addbtn.setPreferredSize(d);
		addbtn.setMinimumSize(d);
		addbtn.setMaximumSize(d);

		removebtn.setPreferredSize(d);
		removebtn.setMinimumSize(d);
		removebtn.setMaximumSize(d);

		panel.add(btnpanel);

		m_assigneduserslist.setHeadingText("Assigned");
		JList assigneduserslist = m_assigneduserslist.getJList();
		assigneduserslist.setName(ID_ASSIGNED_USERS_LIST);
		assigneduserslist.setCellRenderer(MetaDataPopupRenderer.createInstance(m_connection));
		panel.add(m_assigneduserslist);
		panel.setLayout(new AssignmentPanelLayout(m_userslist, btnpanel, m_assigneduserslist));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		return panel;
	}

	/**
	 * Creates the topmost panel
	 */
	private JComponent createPropertiesPanel() {
		Component[] left = new Component[1];
		left[0] = new JLabel(I18N.getLocalizedMessage("Name"));

		Component[] right = new Component[1];
		right[0] = m_namefield;

		ControlsAlignLayout layout = new ControlsAlignLayout();
		layout.setMaxTextFieldWidth(m_namefield, 30);

		JPanel panel = TSGuiToolbox.alignLabelTextRows(layout, left, right, new java.awt.Insets(10, 10, 0, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return panel;
	}

	/**
	 * @return the name entered by the user
	 */
	public String getName() {
		return m_namefield.getText();
	}

	/**
	 * @return the preferred size for this view
	 */
	public Dimension getPreferredSize() {
		return new Dimension(400, 200);
	}

	/**
	 * @return the set of users (User objects) that belong to this group
	 */
	public Collection getUsers() {
		LinkedList results = new LinkedList();
		JList assignedlist = (JList) getComponentByName(ID_ASSIGNED_USERS_LIST);
		DefaultListModel assignedmodel = (DefaultListModel) assignedlist.getModel();
		for (int index = 0; index < assignedmodel.getSize(); index++) {
			User user = (User) assignedmodel.getElementAt(index);
			if (user != null)
				results.add(user);
		}
		return results;
	}

	private void loadData() {
		try {

			DefaultListModel userlist = (DefaultListModel) m_userslist.getModel();
			SecurityService srv = (SecurityService) m_connection.getImplementation(SecurityService.COMPONENT_ID);
			Collection users = srv.getUsers();
			Iterator iter = users.iterator();
			while (iter.hasNext()) {
				User user = (User) iter.next();
				userlist.addElement(user);
			}

			DefaultListModel assignedlist = (DefaultListModel) m_assigneduserslist.getModel();
			if (m_group != null) {

				m_namefield.setText(m_group.getName());
				m_namefield.setEnabled(false);
				users = m_group.getUsers();
				iter = users.iterator();
				while (iter.hasNext()) {
					Object objkey = iter.next();
					User user = srv.getUser(objkey);
					assignedlist.addElement(user);
				}
			}
		} catch (Exception e) {
			TSUtils.printException(e);
		}
	}

}
