package com.jeta.abeille.gui.update;

import java.awt.BorderLayout;

import java.io.ObjectStreamException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.foundation.common.JETAExternalizable;

import com.jeta.foundation.gui.components.TSPanel;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * Default implementation of ColumnHandler. This creates a column component for
 * the InstanceView based on the column type.
 * 
 * @author Jeff Tassin
 */
public class DefaultColumnHandler implements ColumnHandler, JETAExternalizable {
	static final long serialVersionUID = 4776191275106621036L;

	public static int VERSION = 1;

	public static String HANDLER_NAME = I18N.getLocalizedMessage("Default");

	public static String INTEGER_HANDLER = I18N.getLocalizedMessage("Integer");
	public static String NUMERIC_HANDLER = I18N.getLocalizedMessage("Numeric");
	public static String VARCHAR_HANDLER = "Varchar";

	/**
	 * The user can override the default handler to a standard type such as
	 * integer, boolean, short, etc. In this case, we set the m_type to the
	 * java.sql.Type. Otherwise, if the m_type is zero, we just use the type
	 * found in the columnmetadata to determine the handler.
	 */
	private int m_type;
	private String m_handlername;

	public DefaultColumnHandler() {

	}

	DefaultColumnHandler(int type, String handlerName) {
		m_type = type;
		m_handlername = handlerName;
	}

	/**
	 * Creates a component to handle a specified column for the InstanceView The
	 * type of component created depends on the type of column. For example, if
	 * the column is a integer, then an IntegralComponent is returned.
	 * 
	 * @param cmd
	 *            the column metadata object that specifies which type of
	 *            component to create
	 */
	public InstanceComponent createComponent(ColumnMetaData cmd, InstanceView view) {
		InstanceComponent result = null;

		int data_type = (m_type == 0) ? cmd.getType() : m_type;
		String columnname = cmd.getColumnName();

		if (data_type == java.sql.Types.DATE) {
			result = new UpdateDateComponent(columnname, data_type);
		} else if (data_type == java.sql.Types.TIME) {
			result = new UpdateTimeComponent(columnname, data_type);
		} else if (data_type == java.sql.Types.TIMESTAMP) {
			result = new UpdateTimeStampComponent(columnname, data_type);
		} else if (DbUtils.isIntegral(data_type)) {
			result = new IntegralComponent(columnname, data_type, 20);
		} else if (DbUtils.isBinary(data_type)) {
			LOBComponent comp = new LOBComponent(columnname, data_type, view, I18N.getLocalizedMessage("file object"));
			comp.setController(new FileObjectController(view, comp, false));
			result = comp;
		} else if (data_type == java.sql.Types.BIT) {
			result = new UpdateBooleanComponent(columnname, data_type);
		} else if (DbUtils.isAlpha(data_type)) {
			result = new EditorComponent(columnname, data_type, 1);
		} else if (data_type == java.sql.Types.DECIMAL || data_type == java.sql.Types.NUMERIC) {
			result = new DecimalComponent(columnname, data_type, 30);
		} else if (DbUtils.isReal(data_type)) {
			result = new RealComponent(columnname, data_type, 30);
		} else {
			result = new InstanceUnknownComponent(columnname, data_type);
		}

		return result;
	}

	/**
	 * Creates the panel/controller that is used to configure this handler.
	 * 
	 * @return the panel with the correct layout and controller
	 */
	public TSPanel createConfigurationPanel() {
		TSPanel panel = new TSPanel(new BorderLayout());
		panel.add(new JLabel(I18N.getLocalizedMessage("No Options")), BorderLayout.WEST);
		return panel;
	}

	/**
	 * @return the name or this handler
	 */
	public String getName() {
		if (m_type == 0)
			return HANDLER_NAME;
		else
			return m_handlername;

	}

	/**
	 * Reads the user input from the given panel. This panel should be the one
	 * created by the call to createConfigurationPanel().
	 */
	public void readInput(TSPanel panel) {
		// no implementation for this one
	}

	/**
	 * @return the name of this handler
	 */
	public String toString() {
		return getName();
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_type = in.readInt();
		m_handlername = (String) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeInt(m_type);
		out.writeObject(m_handlername);
	}

}
