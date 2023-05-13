package com.jeta.abeille.gui.update.image;

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

import com.jeta.abeille.gui.update.DefaultColumnHandler;
import com.jeta.abeille.gui.update.InstanceComponent;
import com.jeta.abeille.gui.update.InstanceView;

import com.jeta.foundation.common.JETAExternalizable;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * This column handler is used to work with Image binary objects.
 * 
 * @author Jeff Tassin
 */
public class ImageObjectHandler extends DefaultColumnHandler implements JETAExternalizable {
	static final long serialVersionUID = 7183689808285980365L;

	public static int VERSION = 1;

	public static final String HANDLER_NAME = I18N.getLocalizedMessage("Blob_Image");
	// ////////////////////////////////////////////////////////////////////////////
	// defined handlers

	private transient TSPanel m_optionspanel; // this is the panel that contains
												// all the gui components for
												// the
												// selected handler type
	private String m_application; // the path and name of the application to
									// launch

	/**
	 * ctor
	 */
	public ImageObjectHandler() {

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
		ImageComponent comp = new ImageComponent(cmd.getColumnName(), cmd.getType(), view);
		comp.setController(new ImageController(view, comp));
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
		String msg = I18N.getLocalizedMessage("Allow_to_view_image_objects");
		JTextArea msglabel = new JTextArea();
		msglabel.setText(msg);
		msglabel.setEditable(false);
		msglabel.setLineWrap(true);
		msglabel.setWrapStyleWord(true);
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
		m_application = (String) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(VERSION);
		out.writeObject(m_application);
	}

}
