package com.jeta.abeille.gui.update;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import java.io.ObjectInput;
import java.io.ObjectOutput;
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

import com.jeta.foundation.common.JETAExternalizable;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.components.TSController;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * This column handler is used to work with binary objects as files.
 * 
 * @author Jeff Tassin
 */
public class FileObjectHandler extends DefaultColumnHandler implements JETAExternalizable {
	static final long serialVersionUID = 6472150433466287111L;

	public static int VERSION = 1;

	public static final String HANDLER_NAME = I18N.getLocalizedMessage("Blob_File");
	public static final String CLOB_HANDLER_NAME = I18N.getLocalizedMessage("Clob_File");
	// ////////////////////////////////////////////////////////////////////////////
	// defined handlers

	private transient TSPanel m_optionspanel; // this is the panel that contains
												// all the gui components for
												// the
												// selected handler type

	/**
	 * set to true if this handler is for clob types. This means that if a file
	 * is loaded into the form, a Reader object is used instead of a Stream
	 * object
	 */
	private boolean m_clob = false;

	/**
	 * ctor only for serialization
	 */
	public FileObjectHandler() {

	}

	/**
	 * ctor
	 */
	public FileObjectHandler(boolean clob) {
		m_clob = clob;
	}

	/**
	 * Creates a component to handle a specified column for the InstanceView
	 * 
	 * @param cmd
	 *            the column metadata object that specifies which type of
	 *            component to create
	 */
	public InstanceComponent createComponent(ColumnMetaData cmd, InstanceView view) {
		LOBComponent comp = new LOBComponent(cmd.getColumnName(), cmd.getType(), view,
				I18N.getLocalizedMessage("file object"));
		comp.setController(new FileObjectController(view, comp, m_clob));
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
		if (m_clob)
			return CLOB_HANDLER_NAME;
		else
			return HANDLER_NAME;

	}

	/**
	 * Initializes the handler
	 */
	private void initialize() {
		m_optionspanel = new TSPanel(new GridBagLayout());

		// configuration common to all handlers
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 5, 2, 5);
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 3;
		c.gridheight = 3;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		String msg = I18N.getLocalizedMessage("Allow_manually_store_load_object_as_file");
		JTextArea msglabel = new JTextArea();
		msglabel.setText(msg);
		msglabel.setEditable(false);
		msglabel.setLineWrap(true);
		msglabel.setWrapStyleWord(true);
		// msglabel.setForeground( handlerlabel.getForeground() );
		msglabel.setOpaque(false);
		m_optionspanel.add(msglabel, c);
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
	public void readExternal(ObjectInput in) throws ClassNotFoundException, IOException {
		super.readExternal(in);
		int version = in.readInt();
		m_clob = in.readBoolean();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(VERSION);
		out.writeBoolean(m_clob);
	}

}
