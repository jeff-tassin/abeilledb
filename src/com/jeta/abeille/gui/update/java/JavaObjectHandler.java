package com.jeta.abeille.gui.update.java;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import java.io.ObjectStreamException;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.jeta.abeille.database.model.ColumnMetaData;

import com.jeta.abeille.gui.update.DefaultColumnHandler;
import com.jeta.abeille.gui.update.InstanceComponent;
import com.jeta.abeille.gui.update.InstanceView;

import com.jeta.foundation.common.JETAExternalizable;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * This column handler is used to work with Java binary objects. Allows the user
 * to see attributes of an object in the form
 * 
 * @author Jeff Tassin
 */
public class JavaObjectHandler extends DefaultColumnHandler implements JETAExternalizable {
	static final long serialVersionUID = -4872289382954798544L;

	public static int VERSION = 1;

	public static final String BLOB_HANDLER_NAME = I18N.getLocalizedMessage("Blob_Java_Object");
	public static final String HANDLER_NAME = I18N.getLocalizedMessage("Java Object");
	// ////////////////////////////////////////////////////////////////////////////
	// defined handlers

	/**
	 * this is the panel that contains all the gui components for the selecte
	 * handler type
	 */
	private transient TSPanel m_optionspanel;

	private transient JComboBox m_handlercombo;

	/**
	 * set this to true if the object is stored in the database as a blob. set
	 * to false if the object is stored and returned directly (i.e.
	 * resultset.getObject(..) returns the object itself
	 */
	private boolean m_blob = true;

	/**
	 * ctor only for serialization
	 */
	public JavaObjectHandler() {

	}

	/**
	 * ctor
	 */
	public JavaObjectHandler(boolean blob) {
		m_blob = blob;
	}

	/**
	 * Creates a component to handle a specified column for the InstanceView The
	 * type of component created depends on the type of column. For example, if
	 * the column is a integer, then an IntegralComponent is returned. In this
	 * case, we create a BinaryObjectHandler
	 * 
	 * @param cmd
	 *            the column metadata object that specifies which type of
	 *            component to create
	 */
	public InstanceComponent createComponent(ColumnMetaData cmd, InstanceView view) {
		JavaObjectComponent comp = new JavaObjectComponent(cmd.getColumnName(), cmd.getType(), m_blob, view);
		comp.setController(new JavaObjectController(view, comp));
		return comp;
	}

	/**
	 * Creates the panel/controller that is used to configure this handler.
	 * 
	 * @return the panel with the correct layout and controller
	 */
	public TSPanel createConfigurationPanel() {
		if (m_optionspanel == null)
			initialize();

		return m_optionspanel;
	}

	/**
	 * @return the name of this handler
	 */
	public String getName() {
		if (m_blob)
			return BLOB_HANDLER_NAME;
		else
			return HANDLER_NAME;
	}

	/**
	 * Initializes the handler
	 */
	private void initialize() {
		m_optionspanel = new TSPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 5, 2, 5);
		c.anchor = GridBagConstraints.WEST;

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		c.gridheight = 3;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;

		String msg = "";
		if (m_blob)
			msg = I18N.getLocalizedMessage("Allow_manually_store_load_as_java_object_blob");
		else
			msg = I18N.getLocalizedMessage("Allow_manually_store_load_as_java_object");

		JTextArea msglabel = new JTextArea();
		msglabel.setText(msg);
		msglabel.setEditable(false);
		msglabel.setLineWrap(true);
		msglabel.setWrapStyleWord(true);
		// msglabel.setForeground( handlerlabel.getForeground() );
		msglabel.setOpaque(false);
		m_optionspanel.add(msglabel, c);

		// got to call revalidate or else the components won't be positioned
		// correctly
		m_optionspanel.revalidate();
	}

	/**
	 * Reads the user input from the given panel. This panel should be the one
	 * created by the call to createConfigurationPanel().
	 */
	public void readInput(TSPanel panel) {

	}

	/**
	 * @return the name of this handler
	 */
	public String toString() {
		return getName();
	}

	// /////////////////////////////////////////////////////////////////////////////
	// names used for the BinaryObjectHandler and supporting classes

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		super.readExternal(in);
		int version = in.readInt();
		m_blob = in.readBoolean();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(VERSION);
		out.writeBoolean(m_blob);
	}

}
