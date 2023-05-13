package com.jeta.abeille.gui.update;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.jeta.abeille.gui.utils.SQLErrorDialog;

import com.jeta.foundation.gui.components.TSErrorDialog;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.filechooser.TSFileChooserFactory;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This controller responds/handles events for the LOBComponent.
 * 
 * @author Jeff Tassin
 */
public abstract class LOBController extends TSController {
	/** the instance view this controller is associated with */
	private InstanceView m_view;

	/** the instance component this controller is associated with */
	private InstanceComponent m_component;

	/**
	 * ctor
	 */
	public LOBController(InstanceView view, InstanceComponent comp) {
		super(view);
		m_view = view;
		m_component = comp;
	}

	/**
	 * Called in response to a user clicking the download button on the popup
	 * window. Retrives the object from the database to a user specified file
	 * name
	 */
	public void downloadObject() {
		try {
			Object obj = getComponent().getValue();
			if (obj instanceof byte[]) {
				File f = TSFileChooserFactory.showSaveDialog();
				if (f != null) {
					saveLOB(f, (byte[]) obj);
				}
			} else if (obj instanceof Blob) {
				File f = TSFileChooserFactory.showSaveDialog();
				if (f != null) {
					Blob blob = (Blob) obj;
					InputStream istream = blob.getBinaryStream();
					try {
						/** needed for DB2 */
						istream.reset();
					} catch (Exception e) {
						if (TSUtils.isDebug()) {
							TSUtils.printException(e);
						}
					}
					saveLOB(f, istream);
				}
			} else if (obj instanceof Clob) {
				File f = TSFileChooserFactory.showSaveDialog();
				if (f != null) {
					Clob clob = (Clob) obj;
					Reader reader = clob.getCharacterStream();
					try {
						/** needed for DB2 */
						reader.reset();
					} catch (Exception e) {
						if (TSUtils.isDebug()) {
							TSUtils.printException(e);
						}
					}
					saveLOB(f, reader);
				}
			} else if (obj instanceof String) {
				File f = TSFileChooserFactory.showSaveDialog();
				if (f != null) {
					saveLOB(f, (String) obj);
				}
			} else if (obj instanceof java.io.Serializable) {
				File f = TSFileChooserFactory.showSaveDialog();
				ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(f));
				ois.writeObject(obj);
				ois.flush();
				ois.close();
			} else {
				String msg = I18N.getLocalizedMessage("Object cannot be saved to file");
				String title = I18N.getLocalizedMessage("Error");
				javax.swing.JOptionPane.showMessageDialog(null, msg, title, javax.swing.JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e) {
			showError(e);
		}
	}

	/**
	 * @return the instance component
	 */
	public InstanceComponent getComponent() {
		return m_component;
	}

	public static byte[] loadFile(File f) throws FileNotFoundException, IOException {
		FileInputStream fis = new FileInputStream(f);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buff = new byte[1024];
		int numread = fis.read(buff);
		while (numread > 0) {
			bos.write(buff, 0, numread);
			numread = fis.read(buff);
		}
		return bos.toByteArray();
	}

	/**
	 * Called in response to a user clicking the upload button on the popup
	 * window. Invokes the file open dialog. Allows the user to open and load a
	 * file. The specified file is loaded into the a byte array. The specialized
	 * component controller instance then decides how to process the data.
	 * 
	 * @return the file in byte array form. Null is returned if the user
	 *         canceled the operation.
	 */
	public byte[] loadFile() throws FileNotFoundException, IOException {
		File f = TSFileChooserFactory.showOpenDialog();
		if (f != null) {
			return loadFile(f);
		} else
			return null;
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
	public abstract void prepareStatement(int count, PreparedStatement pstmt) throws SQLException;

	/**
	 * Saves the LOB to the given file
	 * 
	 * @param fileName
	 *            the path and name of the file to save to
	 */
	private void saveLOB(File file, byte[] data) throws SQLException, FileNotFoundException, IOException {
		FileOutputStream ostream = new FileOutputStream(file);
		ostream.write(data);
		ostream.flush();
		ostream.close();
	}

	/**
	 * Saves the LOB to the given file
	 * 
	 * @param fileName
	 *            the path and name of the file to save to
	 */
	private void saveLOB(File file, String data) throws SQLException, FileNotFoundException, IOException {
		assert (data != null);

		FileWriter writer = new FileWriter(file);
		writer.write(data);
		writer.flush();
		writer.close();
	}

	/**
	 * Reads all data from the given input stream and saves to the given file
	 */
	private void saveLOB(File file, InputStream istream) throws IOException {
		FileOutputStream file_stream = new FileOutputStream(file);
		BufferedOutputStream ostream = new BufferedOutputStream(file_stream);

		byte[] buff = new byte[1024];
		int numread = istream.read(buff);
		while (numread > 0) {
			ostream.write(buff, 0, numread);
			numread = istream.read(buff);
		}
		ostream.flush();
		ostream.close();
	}

	/**
	 * Reads all data from the given reader and saves to the given file
	 */
	private void saveLOB(File file, Reader reader) throws IOException {
		OutputStreamWriter file_writer = new OutputStreamWriter(new FileOutputStream(file));
		BufferedWriter writer = new BufferedWriter(file_writer);

		char[] buff = new char[1024];
		int numread = reader.read(buff);
		while (numread > 0) {
			writer.write(buff, 0, numread);
			numread = reader.read(buff);
		}

		writer.flush();
		writer.close();
	}

	/**
	 * Generic error handler. Shows dialog with exception message
	 */
	protected void showError(Exception e) {
		SQLErrorDialog dlg = (SQLErrorDialog) TSGuiToolbox.createDialog(SQLErrorDialog.class, m_view, true);
		dlg.initialize(e, null, false);
		dlg.setSize(dlg.getPreferredSize());
		dlg.showCenter();
	}

}
