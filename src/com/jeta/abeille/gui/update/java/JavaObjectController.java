package com.jeta.abeille.gui.update.java;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.jeta.abeille.database.utils.ResultSetReference;
import com.jeta.abeille.gui.queryresults.QueryResultSet;

import com.jeta.abeille.gui.update.InstanceProxy;
import com.jeta.abeille.gui.update.ButtonPopup;
import com.jeta.abeille.gui.update.InstanceModel;
import com.jeta.abeille.gui.update.InstanceNames;
import com.jeta.abeille.gui.update.InstanceUtils;
import com.jeta.abeille.gui.update.InstanceView;
import com.jeta.abeille.gui.update.LOBController;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSErrorDialog;
import com.jeta.foundation.gui.java.ObjectView;
import com.jeta.foundation.gui.java.ObjectModel;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * 
 * @author Jeff Tassin
 */
public class JavaObjectController extends LOBController {
	JavaObjectComponent m_component;
	InstanceView m_view;

	public JavaObjectController(InstanceView view, JavaObjectComponent comp) {
		super(view, comp);
		m_view = view;
		m_component = comp;

		final ButtonPopup popup = m_component.getPopup();
		popup.addButton(TSGuiToolbox.loadImage("general/Add16.gif"), InstanceNames.ADD_OBJECT);
		popup.addButton(TSGuiToolbox.loadImage("general/Edit16.gif"), InstanceNames.EDIT_OBJECT);
		popup.addButton(TSGuiToolbox.loadImage("general/Import16.gif"), InstanceNames.DOWNLOAD_OBJECT);
		popup.addButton(TSGuiToolbox.loadImage("general/Export16.gif"), InstanceNames.UPLOAD_OBJECT);

		popup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleCommand(e.getActionCommand());
				popup.setVisible(false);
			}
		});

	}

	/**
	 * Invokes the create object dialog. Allows the user to type the class name
	 * of an ojbect which is then instantiated
	 */
	void addObject() {

		String classname = javax.swing.JOptionPane.showInputDialog(null, I18N.getLocalizedMessage("Input Class Name"));
		if (classname != null) {
			try {
				Class c = Class.forName(classname);
				Object obj = c.newInstance();

				_editObject(obj);
			} catch (Exception e) {
				// e.printStackTrace();
				TSErrorDialog edlg = new TSErrorDialog((java.awt.Frame) null, true);
				edlg.initialize(null, e);
				edlg.setSize(edlg.getPreferredSize());
				edlg.showCenter();
			}
		}
	}

	/**
	 * Invokes the ObjectView/Model in a dialog which allows the user to edit
	 * the fields in an object.
	 * 
	 * @param obj
	 *            the object that we wish to directly edit
	 */
	private void _editObject(Object obj) throws IOException {
		ObjectModel model = new ObjectModel(obj);
		ObjectView panel = new ObjectView(model);
		// panel.expandAll();
		TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_view, true);
		dlg.setPrimaryPanel(panel);
		dlg.setSize(dlg.getPreferredSize());
		dlg.showCenter();

		if (dlg.isOk()) {
			if (m_component.isBlob()) {
				m_component.setValue(model.getBytes());
			} else {
				m_component.setValue(model.getObject());
			}
			m_component.setModified(true);
		}
	}

	/**
	 * Allows the user to edit an existing object. Invoked when the user clicks
	 * the edit object popup button
	 */
	void editObject() throws IOException, ClassNotFoundException, SQLException {
		// we have to deal directly with the result set here
		Object obj = m_component.getValue();
		if (obj == null) {
			String msg = I18N.getLocalizedMessage("Object is null");
			String title = I18N.getLocalizedMessage("Error");
			javax.swing.JOptionPane.showMessageDialog(null, msg, title, javax.swing.JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (m_component.isBlob()) {
			InstanceModel model = m_view.getModel();
			InstanceProxy proxy = (InstanceProxy) model.getInstanceProxy();

			Object lob = InstanceUtils.getBinaryData(obj, proxy, m_component.getFieldName());
			if (lob instanceof byte[]) {
				ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream((byte[]) lob));
				obj = ois.readObject();
				_editObject(obj);
			}
		} else {
			_editObject(obj);
		}
	}

	/**
	 * Generic command handler
	 */
	private void handleCommand(String cmdName) {
		try {
			if (InstanceNames.ADD_OBJECT.equals(cmdName)) {
				addObject();
			} else if (InstanceNames.EDIT_OBJECT.equals(cmdName)) {
				editObject();
			} else if (InstanceNames.DOWNLOAD_OBJECT.equals(cmdName)) {
				downloadObject();
			} else if (InstanceNames.UPLOAD_OBJECT.equals(cmdName)) {
				uploadObject();
			}
		} catch (Exception e) {
			showError(e);
		}
	}

	/**
	 * Sets the value represented by this component into the prepared statement.
	 * Different controllers for the LOBComponent will handle this differently.
	 * 
	 * @param count
	 *            the parameter index to set
	 * @param pstmt
	 *            the prepared statement to act on
	 */
	public void prepareStatement(int count, PreparedStatement pstmt) throws SQLException {
		if (m_component.isBlob()) {
			byte[] bytes = (byte[]) m_component.getValue();
			// ByteArrayInputStream istream = new ByteArrayInputStream( bytes );
			// pstmt.setBinaryStream( count, istream, bytes.length );
			pstmt.setBytes(count, bytes);
		} else {
			pstmt.setObject(count, m_component.getValue());
		}
	}

	/**
	 * Allows the user to load a serialized object from disk
	 */
	void uploadObject() throws IOException {
		byte[] data = loadFile();
		if (data != null) {
			m_component.setValue(data);
			m_component.setModified(true);
		}
	}
}
