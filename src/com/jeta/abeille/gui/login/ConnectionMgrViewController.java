package com.jeta.abeille.gui.login;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.ConnectionInfo;

import com.jeta.abeille.gui.main.MainFrame;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * 
 * 
 * @author Jeff Tassin
 */
public class ConnectionMgrViewController extends TSController {

	/** the view */
	private ConnectionMgrView m_view;

	/**
	 * ctor
	 */
	public ConnectionMgrViewController(ConnectionMgrView view) {
		super(view);
		m_view = view;

		assignAction(ConnectionMgrView.ID_ADD_CONNECTION, new AddConnectionAction());
		assignAction(ConnectionMgrView.ID_EDIT_CONNECTION, new EditConnectionAction());
		assignAction(ConnectionMgrView.ID_DELETE_CONNECTION, new DeleteConnectionAction());
		assignAction(ConnectionMgrView.ID_LOGIN, new LoginAction());

		final JList list = (JList) m_view.getComponentByName(ConnectionMgrView.ID_CONNECTION_LIST);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent evt) {
				if (!evt.getValueIsAdjusting()) {
					ConnectionInfo model = (ConnectionInfo) list.getSelectedValue();
					m_view.setConnection(model);
				}
			}
		});

	}

	/**
	 * Creates a new connection
	 */
	public class AddConnectionAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_view, true);
			dlg.setCloseText(I18N.getLocalizedMessage("Cancel"));
			dlg.setTitle(I18N.getLocalizedMessage("Create Connection"));

			/*
			JButton help_btn = dlg.getHelpButton();
			help_btn.setVisible(true);
			com.jeta.foundation.help.HelpUtils.enableHelpOnButton(help_btn, "abeille.main.basic_connection");
            */

			ConnectionInfo model = new ConnectionInfo();

			model.setDatabase(Database.POSTGRESQL);
			model.setPort(5432);
			model.setDescription(I18N.getLocalizedMessage("New Connection"));
			model.setUID(TSUtils.createUID());

			ConnectionViewContainer view = new ConnectionViewContainer((ConnectionInfo) model.clone(), null, true);

			dlg.setPrimaryPanel(view);
			dlg.setSize(dlg.getPreferredSize());
			dlg.addValidator(view, view.getValidator());

			dlg.showCenter();
			if (dlg.isOk()) {
				ConnectionInfo newinfo = view.createConnectionModel();
				m_view.addConnection(newinfo);
			}
		}
	}

	/**
	 * Deletes an existing connection
	 */
	public class DeleteConnectionAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JList list = (JList) m_view.getComponentByName(ConnectionMgrView.ID_CONNECTION_LIST);
			int index = list.getSelectedIndex();
			if (index >= 0) {
				ConnectionInfo model = (ConnectionInfo) list.getSelectedValue();
				String msg = I18N.format("Delete_Connection_1", model.getDescription());
				int result = JOptionPane.showConfirmDialog(null, msg, I18N.getLocalizedMessage("Confirm"),
						JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					m_view.deleteConnection(model);
				}
			}

		}
	}

	/**
	 * Edits an existing connection
	 */
	public class EditConnectionAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ConnectionInfo cinfo = m_view.getSelectedConnection();
			if (cinfo != null) {
				TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_view, true);
				dlg.setTitle(I18N.getLocalizedMessage("Edit Connection"));
				dlg.setCloseText(I18N.getLocalizedMessage("Cancel"));

				/*
				JButton help_btn = dlg.getHelpButton();
				help_btn.setVisible(true);
				com.jeta.foundation.help.HelpUtils.enableHelpOnButton(help_btn, "abeille.main.basic_connection");
				 */

				ConnectionViewContainer view = new ConnectionViewContainer((ConnectionInfo) cinfo.clone(), null,true);

				dlg.setPrimaryPanel(view);
				dlg.setSize(dlg.getPreferredSize());
				dlg.addValidator(view, view.getValidator());
				dlg.showCenter();
				if (dlg.isOk()) {
					ConnectionInfo newinfo = view.createConnectionModel();

					m_view.modifyConnection(newinfo, cinfo);
				}
			}
		}
	}

	/**
	 * Launches the login dialog which allows the user to connect to a database
	 */
	public class LoginAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			MainFrame mframe = (MainFrame) TSWorkspaceFrame.getInstance();
			ConnectionInfo cinfo = m_view.getSelectedConnection();
			if (cinfo != null) {
				LoginDialog dlg = new LoginDialog(mframe, true);
				dlg.initialize(m_view.getConnectionMgr());
				dlg.setSelectedConnection(cinfo);
				dlg.setSize(dlg.getPreferredSize());
				TSGuiToolbox.centerWindow(dlg);
				LoginView view = dlg.getView();
				view.setController(new com.jeta.abeille.gui.main.MainFrameLoginController(mframe, view));
				dlg.showCenter();
				// the LoginController does the rest
			}
		}
	}

}
