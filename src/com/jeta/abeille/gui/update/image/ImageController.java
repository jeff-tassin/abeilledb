package com.jeta.abeille.gui.update.image;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JLabel;

import com.jeta.abeille.database.utils.ResultSetReference;
import com.jeta.abeille.gui.queryresults.QueryResultSet;

import com.jeta.abeille.gui.update.InstanceProxy;
import com.jeta.abeille.gui.update.ButtonPopup;
import com.jeta.abeille.gui.update.InstanceModel;
import com.jeta.abeille.gui.update.InstanceNames;
import com.jeta.abeille.gui.update.InstanceView;
import com.jeta.abeille.gui.update.LOBController;

import com.jeta.abeille.gui.utils.SQLErrorDialog;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.editor.TSEditorUtils;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * 
 * @author Jeff Tassin
 */
public class ImageController extends LOBController {
	/**
	 * ctor
	 */
	public ImageController(InstanceView view, ImageComponent comp) {
		super(view, comp);

		final ButtonPopup popup = comp.getPopup();
		popup.addButton(TSGuiToolbox.loadImage("general/Import16.gif"), InstanceNames.DOWNLOAD_OBJECT);
		popup.addButton(TSGuiToolbox.loadImage("general/Export16.gif"), InstanceNames.UPLOAD_OBJECT);

		popup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (InstanceNames.DOWNLOAD_OBJECT.equals(evt.getActionCommand())) {
					downloadObject();
				} else if (InstanceNames.UPLOAD_OBJECT.equals(evt.getActionCommand())) {
					try {
						uploadObject();
					} catch (Exception e) {
						showError(e);
					}
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
		try {
			if (getComponent().isNull()) {
				pstmt.setNull(count, getComponent().getDataType());
			} else {
				byte[] bytes = (byte[]) getComponent().getValue();
				// ByteArrayInputStream istream = new ByteArrayInputStream(
				// bytes );
				// pstmt.setBinaryStream( count, istream, bytes.length );
				pstmt.setBytes(count, bytes);
			}
		} catch (Exception e) {
			TSUtils.printException(e);
		}
	}

	/**
	 * Called in response to a user clicking the upload button on the popup
	 * window. Loads a user specified file into the components value. The user
	 * can then hit the modify button on the InstanceFrame to save the file
	 * object to the database.
	 */
	public void uploadObject() throws FileNotFoundException, IOException {
		byte[] data = loadFile();
		if (data != null) {
			ImageComponent icomp = (ImageComponent) getComponent();
			icomp.setValue(data);
			icomp.setModified(true);
		}
	}

}
