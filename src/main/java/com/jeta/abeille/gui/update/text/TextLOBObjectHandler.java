package com.jeta.abeille.gui.update.text;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import java.io.ObjectStreamException;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.jeta.abeille.database.model.ColumnMetaData;

import com.jeta.abeille.gui.update.EditorHandler;
import com.jeta.abeille.gui.update.InstanceComponent;
import com.jeta.abeille.gui.update.InstanceView;

import com.jeta.foundation.common.JETAExternalizable;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * This column handler is used to work with text stored as binary objects.
 * 
 * @author Jeff Tassin
 */
public class TextLOBObjectHandler extends EditorHandler implements JETAExternalizable {
	static final long serialVersionUID = 770198750361121604L;

	public static int VERSION = 1;

	public static final String HANDLER_NAME = I18N.getLocalizedMessage("Blob_text");
	public static final String CLOB_HANDLER_NAME = I18N.getLocalizedMessage("Clob");
	// ////////////////////////////////////////////////////////////////////////////
	// defined handlers

	private transient TSPanel m_optionspanel; // this is the panel that contains
												// all the gui components for
												// the
												// selected handler type

	/** set to true if this handler is for clob types */
	private boolean m_clob;

	/**
	 * ctor
	 */
	public TextLOBObjectHandler() {
		// default to 5 lines of text as height
		setEditorSize(5);
	}

	/**
	 * ctor
	 */
	public TextLOBObjectHandler(boolean bclob) {
		this();
		m_clob = bclob;
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
		TextLOBComponent comp = new TextLOBComponent(cmd.getColumnName(), cmd.getType(), view, getEditorSize(), m_clob);
		comp.setController(new TextLOBController(view, comp, m_clob));
		return comp;
	}

	/**
	 * Creates the panel/controller that is used to configure this handler.
	 * 
	 * @return the panel with the correct layout and controller
	 */
	public TSPanel createConfigurationPanel() {
		if (m_optionspanel == null)
			m_optionspanel = super.createConfigurationPanel();

		return m_optionspanel;
	}

	/**
	 * @return the name of this handler
	 */
	public String getName() {
		if (m_clob)
			return CLOB_HANDLER_NAME;
		else
			return HANDLER_NAME;
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
		super.readExternal(in);
		int version = in.readInt();
		m_clob = in.readBoolean();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(VERSION);
		out.writeBoolean(m_clob);
	}

}
