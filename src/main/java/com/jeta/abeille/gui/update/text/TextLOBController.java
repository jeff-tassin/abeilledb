package com.jeta.abeille.gui.update.text;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.StringWriter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.jeta.abeille.gui.update.InstanceProxy;
import com.jeta.abeille.gui.update.ButtonPopup;
import com.jeta.abeille.gui.update.InstanceModel;
import com.jeta.abeille.gui.update.InstanceNames;
import com.jeta.abeille.gui.update.InstanceView;
import com.jeta.abeille.gui.update.LOBController;

import com.jeta.foundation.gui.components.TSErrorDialog;
import com.jeta.foundation.gui.filechooser.TSFileChooserFactory;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * Controller for handling columns that store text as binary objects
 * 
 * @author Jeff Tassin
 */
public class TextLOBController extends LOBController {
	/**
	 * flag that indicates if the controller is handling text as a clob or a
	 * blob
	 */
	private boolean m_clob;

	/**
	 * ctor
	 */
	public TextLOBController(InstanceView view, TextLOBComponent comp) {
		this(view, comp, false);
	}

	/**
	 * ctor
	 */
	public TextLOBController(InstanceView view, TextLOBComponent comp, boolean clob) {
		super(view, comp);
		m_clob = clob;
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
		Object obj = getComponent().getValue();
		if (getComponent().isNull()) {
			pstmt.setNull(count, getComponent().getDataType());
		} else if (obj instanceof String) {
			String str = (String) obj;
			pstmt.setString(count, str);
		} else {
			assert (!m_clob);
			if (!m_clob) {
				// then it must be a byte[]
				assert (obj instanceof byte[]);
				byte[] bytes = (byte[]) obj;
				ByteArrayInputStream istream = new ByteArrayInputStream(bytes);
				pstmt.setBinaryStream(count, istream, bytes.length);
			}
		}
	}

}
