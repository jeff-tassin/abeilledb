package com.jeta.abeille.gui.update;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.foundation.gui.components.TSErrorDialog;
import com.jeta.foundation.gui.filechooser.TSFileChooserFactory;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.utils.TSUtils;

/**
 * 
 * @author Jeff Tassin
 */
public class FileObjectController extends LOBController {
	/**
	 * a flag that indicates if we should call setCharacterStream when loading
	 * files
	 */
	private boolean m_clob = false;

	public FileObjectController(InstanceView view, LOBComponent comp, boolean clob) {
		super(view, comp);
		m_clob = clob;

		final ButtonPopup popup = comp.getPopup();
		popup.addButton(TSGuiToolbox.loadImage("general/Import16.gif"), InstanceNames.DOWNLOAD_OBJECT);
		popup.addButton(TSGuiToolbox.loadImage("general/Export16.gif"), InstanceNames.UPLOAD_OBJECT);

		popup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (InstanceNames.DOWNLOAD_OBJECT.equals(e.getActionCommand())) {
					downloadObject();
				} else if (InstanceNames.UPLOAD_OBJECT.equals(e.getActionCommand())) {
					uploadObject();
				}
				popup.setVisible(false);
			}
		});

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
		if (getComponent().isNull()) {
			pstmt.setNull(count, getComponent().getDataType());
		} else {
			try {
				Object obj = InstanceUtils.getBinaryData(getComponent().getValue(), null, null);
				if (obj instanceof String) {
					pstmt.setString(count, (String) obj);
				} else if (obj instanceof byte[]) {
					byte[] bytes = (byte[]) getComponent().getValue();
					pstmt.setBytes(count, bytes);
				} else if (obj instanceof File) {
					File f = (File) obj;
					if (m_clob) {
						String file_data = DbUtils.getCharacterData(new java.io.FileReader(f));
						pstmt.setString(count, file_data);
					} else {
						FileInputStream fis = new FileInputStream(f);
						pstmt.setBinaryStream(count, fis, (int) f.length());
					}
				} else {
					TSUtils.printMessage("Unexpected type in FileObjectController: " + getComponent().getFieldName()
							+ "  obj: " + obj);
				}
			} catch (Exception e) {
				TSUtils.printException(e);
			}
		}
	}

	/**
	 * Called in response to a user clicking the upload button on the popup
	 * window. Loads a user specified file into the components value. The user
	 * can then hit the modify button on the InstanceFrame to save the file
	 * object to the database.
	 */
	public void uploadObject() {
		try {
			File f = TSFileChooserFactory.showOpenDialog();
			if (f != null && f.exists()) {
				LOBComponent comp = (LOBComponent) getComponent();
				comp.setValue(f);
				comp.setModified(true);
				assert (comp.getValue() != null);
			}
		} catch (Exception e) {
			TSErrorDialog dlg = TSErrorDialog.createDialog(e.getMessage());
			dlg.showCenter();
		}
	}

}
