package com.jeta.abeille.gui.login;

import com.jeta.abeille.database.model.ConnectionInfo;
import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.form.FormAccessor;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.i18n.I18N;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Iterator;

/**
 * This view allows the user to manage connections. It basically has two
 * components. To the left is a list of connections. To the right is a panel
 * that shows the information for the selected connection. [connection list]
 * [connection info panel]
 * 
 * 
 * @author Jeff Tassin
 */
public class ConnectionMgrView extends TSPanel {
	/** the data model containing all the connections */
	private ConnectionMgr m_model;

	/** the list of connections */
	private JList m_list;

	/** the view of the information for a single connection */
	private ConnectionViewContainer m_connectionview;

	private ConnectionInfo m_currentmodel;

	/** command ids */
	public static final String ID_ADD_CONNECTION = "add.connection";
	public static final String ID_EDIT_CONNECTION = "edit.connection";
	public static final String ID_DELETE_CONNECTION = "delete.connection";
	public static final String ID_LOGIN = "cmgr.login";

	public static final String ID_CONNECTION_LIST = "connection.list";

	/**
	 * ctor
	 */
	public ConnectionMgrView(ConnectionMgr model) {
		m_model = model;

		setLayout(new BorderLayout());
		FormPanel panel = new FormPanel("com/jeta/abeille/gui/login/connectionMgrView.jfrm");
		add(panel, BorderLayout.CENTER);

		FormAccessor fa = panel.getFormAccessor("cmgr.form");
		fa.replaceBean("replace.bean", createComponents());

		loadModel();
	}

	/**
	 * Adds the connection to the connection mgr and selects the connection for
	 * editing
	 */
	public void addConnection(ConnectionInfo model) {
		m_model.addConnection(model);

		DefaultListModel listmodel = (DefaultListModel) m_list.getModel();
		listmodel.addElement(model);

		setConnection(model);

		m_list.setSelectedValue(model, true);
		save();
	}

	/**
	 * create the main content panel
	 */
	JPanel createComponents() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(createListPanel(), BorderLayout.WEST);

		m_connectionview = new ConnectionViewContainer(null, false);
		panel.add(m_connectionview, BorderLayout.CENTER);

		m_connectionview.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0),
				BorderFactory.createTitledBorder(I18N.getLocalizedMessage("Connection Information"))));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return panel;
	}

	/**
	 * create the panel that contains the list of connections to the left of the
	 * view
	 */
	private JPanel createListPanel() {
		// create users panel
		m_list = new JList();
		m_list.setName(ID_CONNECTION_LIST);

		m_list.setPreferredSize(new Dimension(150, 100));
		m_list.setModel(new DefaultListModel());

		JScrollPane scroll = new JScrollPane(m_list);
		JPanel upanel = new JPanel(new BorderLayout());

		javax.swing.JToolBar toolbar = new javax.swing.JToolBar();
		// JPanel toolbar = new JPanel( new FlowLayout( FlowLayout.LEFT, 0, 0 )
		// );
		toolbar.setBorder(null);
		toolbar.setFloatable(false);

		JButton loginbtn = i18n_createButton(null, ID_LOGIN, "incors/24x24/server_connection.png");
		loginbtn.setBorderPainted(false);
		loginbtn.setFocusPainted(false);
		loginbtn.setContentAreaFilled(false);
		loginbtn.setMaximumSize(new Dimension(38, 32));
		loginbtn.setPreferredSize(new Dimension(38, 32));
		loginbtn.setToolTipText(I18N.getLocalizedMessage("Login"));
		toolbar.add(loginbtn);
		toolbar.add(Box.createHorizontalStrut(5));

		JButton addbtn = i18n_createButton(null, ID_ADD_CONNECTION, "incors/24x24/server_add.png");
		addbtn.setBorderPainted(false);
		addbtn.setFocusPainted(false);
		addbtn.setContentAreaFilled(false);
		addbtn.setMaximumSize(new Dimension(28, 32));
		addbtn.setPreferredSize(new Dimension(28, 32));
		addbtn.setToolTipText(I18N.getLocalizedMessage("Create Connection"));
		toolbar.add(addbtn);

		JButton editbtn = i18n_createButton(null, ID_EDIT_CONNECTION, "incors/24x24/server_view.png");
		editbtn.setBorderPainted(false);
		editbtn.setFocusPainted(false);
		editbtn.setContentAreaFilled(false);
		editbtn.setMaximumSize(new Dimension(28, 32));
		editbtn.setPreferredSize(new Dimension(28, 32));
		editbtn.setToolTipText(I18N.getLocalizedMessage("Edit Connection"));

		// toolbar.add( Box.createHorizontalStrut(8) );
		toolbar.add(editbtn);

		JButton delbtn = i18n_createButton(null, ID_DELETE_CONNECTION, "incors/24x24/server_delete.png");
		delbtn.setBorderPainted(false);
		delbtn.setFocusPainted(false);
		delbtn.setContentAreaFilled(false);
		delbtn.setMaximumSize(new Dimension(28, 32));
		delbtn.setPreferredSize(new Dimension(28, 32));
		delbtn.setToolTipText(I18N.getLocalizedMessage("Delete Connection"));

		// toolbar.add( Box.createHorizontalStrut(5) );
		toolbar.add(delbtn);

		upanel.add(toolbar, BorderLayout.NORTH);
		upanel.add(scroll, BorderLayout.CENTER);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(upanel, BorderLayout.CENTER);

		panel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(I18N.getLocalizedMessage("Connections")),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		return panel;
	}

	/**
	 * Removes the connection from the list and the connection mgr
	 */
	public void deleteConnection(ConnectionInfo info) {
		DefaultListModel lmodel = (DefaultListModel) m_list.getModel();

		int index = lmodel.indexOf(info);
		lmodel.removeElement(info);
		m_model.deleteConnection(info);

		info = null;
		index--;
		if (index < 0)
			index = 0;

		if (lmodel.getSize() > 0) {
			info = (ConnectionInfo) lmodel.getElementAt(index);
			m_list.setSelectedIndex(index);
		}
		setConnection(info);
		save();
	}

	/**
	 * @return the currently selected connection
	 */
	public ConnectionInfo getCurrentConnection() {
		return m_currentmodel;
	}

	public ConnectionMgr getConnectionMgr() {
		return m_model;
	}

	/**
	 * @return the preferred size for this view
	 */
	public Dimension getPreferredSize() {
		Dimension d1 = m_list.getPreferredSize();
		Dimension d2 = m_connectionview.getPreferredSize();

		if (d1.height < d2.height)
			d1.height = d2.height;

		Dimension d = new Dimension();
		d.width = d1.width + d2.width + 10;
		d.height = d1.height + 50;

		return d;
	}

	/**
	 * @return the currently selected connection in the list
	 */
	public ConnectionInfo getSelectedConnection() {
		return (ConnectionInfo) m_list.getSelectedValue();
	}

	/**
	 * Loads the information from the connection mgr
	 */
	private void loadModel() {
		DefaultListModel listmodel = (DefaultListModel) m_list.getModel();

		Collection c = m_model.getDefinedConnections();
		Iterator iter = c.iterator();
		while (iter.hasNext()) {
			System.out.println("------- ConnectionMgrView.load model ---------");
			ConnectionInfo info = (ConnectionInfo) iter.next();
			info.print();
			listmodel.addElement(info);
		}

		if (listmodel.getSize() > 0) {
			ConnectionInfo current = (ConnectionInfo) listmodel.elementAt(0);
			setConnection(current);
			m_list.setSelectedIndex(0);
		} else {
			setConnection(null);
		}
	}

	/**
	 * Modifies an existing connection
	 */
	public void modifyConnection(ConnectionInfo newModel, ConnectionInfo oldModel) {
		assert (newModel.getUID().equals(oldModel.getUID()));
		m_currentmodel = newModel;
		m_connectionview.setModel(newModel);
		m_model.modifyConnection(newModel, oldModel);
		DefaultListModel listmodel = (DefaultListModel) m_list.getModel();
		int index = listmodel.indexOf(oldModel);
		assert (index >= 0);
		listmodel.set(index, newModel);
		m_list.setSelectedValue(newModel, true);
		save();

	}

	void save() {
		try {
			m_model.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets the model in the view
	 */
	public void setConnection(ConnectionInfo model) {
		m_currentmodel = model;
		m_list.setSelectedValue(model, true);
		m_connectionview.setModel(model);
	}
}
