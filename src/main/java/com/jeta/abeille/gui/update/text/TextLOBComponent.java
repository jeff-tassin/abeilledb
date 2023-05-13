package com.jeta.abeille.gui.update.text;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.abeille.database.utils.SQLFormatter;

import com.jeta.abeille.gui.update.ButtonPopup;
import com.jeta.abeille.gui.update.EditorComponent;
import com.jeta.abeille.gui.update.InstanceComponent;
import com.jeta.abeille.gui.update.InstanceView;
import com.jeta.abeille.gui.update.InstanceModel;
import com.jeta.abeille.gui.update.InstanceProxy;
import com.jeta.abeille.gui.update.InstanceUtils;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class represents a text data that is stored as binary LOB or CLOB in the
 * database [icon][editor]
 * 
 * @author Jeff Tassin
 */
public class TextLOBComponent extends EditorComponent {
	private InstanceView m_view;

	/**
	 * flag to indicate if we are handleing a clob or a blob. set to true for
	 * clob
	 */
	private boolean m_clob = false;

	public TextLOBComponent(String fieldName, int dataType, InstanceView view, int rowsize) {
		this(fieldName, dataType, view, rowsize, false);
	}

	public TextLOBComponent(String fieldName, int dataType, InstanceView view, int rowsize, boolean clob) {
		super(fieldName, dataType, rowsize);
		m_view = view;
		m_clob = clob;
	}

	/**
	 * Sets the value represented by this component into the prepared statement
	 * 
	 * @param count
	 *            the parameter index to set
	 * @param pstmt
	 *            the prepared statement to act on
	 */
	public void prepareStatement(int count, PreparedStatement pstmt, SQLFormatter formatter) throws SQLException {
		TextLOBController controller = (TextLOBController) getController();
		assert (controller != null);
		if (controller != null) {
			controller.prepareStatement(count, pstmt);
		}
	}

	/**
	 * Sets the value displayed by this field
	 */
	public void setValue(Object value) {
		InstanceModel model = m_view.getModel();
		InstanceProxy proxy = (InstanceProxy) model.getInstanceProxy();
		if (value != null) {
			try {
				Object lob = InstanceUtils.getBinaryData(value, proxy, getFieldName());
				if (lob instanceof byte[]) {
					// use the default character set encoding for now
					String str = new String((byte[]) lob);
					super.setValue(str);
				} else {
					if (lob == null) {
						assert (false);
						super.setValue(lob);
					} else {
						super.setValue(lob.toString());
					}
				}
			} catch (Exception e) {
				// @todo handle better here
				TSUtils.printException(e);
			}
		} else {
			super.setValue(null);
		}
	}
}
