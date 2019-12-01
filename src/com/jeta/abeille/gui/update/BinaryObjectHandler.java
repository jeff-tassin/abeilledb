package com.jeta.abeille.gui.update;

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

import com.jeta.foundation.common.JETAExternalizable;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.components.TSController;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * This column handler is used to work with binary objects. There are many ways
 * of handling binary objects, depending on the type of data. For example, the
 * user might want to launch an application to work with the object (e.g.
 * spreadsheet or image ) or the object could be a Java object (the application
 * could instantiate the object and introspect its attributes - assuming the
 * class path is set up correctly. This handler has support for these and other
 * scenarios
 * 
 * @author Jeff Tassin
 */
public class BinaryObjectHandler extends DefaultColumnHandler implements JETAExternalizable {
	static final long serialVersionUID = -1050076967074031047L;

	public static int VERSION = 1;

	public static final String HANDLER_NAME = I18N.getLocalizedMessage("Binary Object");
	// ////////////////////////////////////////////////////////////////////////////
	// defined handlers
	public static final String STANDARD_FILE = I18N.getLocalizedMessage("File");
	public static final String APPLICATION_LAUNCHER = I18N.getLocalizedMessage("Application Launcher");
	public static final String JAVA_OBJECT = I18N.getLocalizedMessage("Java Object");

	private transient TSPanel m_optionspanel; // this is the panel that contains
												// all the gui components for
												// the
												// selected handler type
	private transient JComboBox m_handlercombo;

	private String m_handlertype; // the selected type (either STANDARD_FILE,
									// APPLICATION_LAUNCHER, JAVA_OBJECT)
	private String m_application; // the path and name of the application to
									// launch

	/**
	 * ctor
	 */
	public BinaryObjectHandler() {
		m_handlertype = STANDARD_FILE;
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
		if (m_handlertype == null)
			m_handlertype = STANDARD_FILE;

		if (m_handlertype.equals(APPLICATION_LAUNCHER)) {
			LOBComponent comp = new LOBComponent(cmd.getColumnName(), cmd.getType(), view,
					I18N.getLocalizedMessage("file object"));
			comp.setController(new ApplicationLauncherController(view, comp, m_application));
			return comp;
		} else if (m_handlertype.equals(JAVA_OBJECT)) {
			LOBComponent comp = new LOBComponent(cmd.getColumnName(), cmd.getType(), view,
					I18N.getLocalizedMessage("java object"));
			return comp;
		} else {
			LOBComponent comp = new LOBComponent(cmd.getColumnName(), cmd.getType(), view,
					I18N.getLocalizedMessage("file object"));
			comp.setController(new FileObjectController(view, comp, false));
			return comp;
		}
	}

	/**
	 * Creates the panel/controller that is used to configure this handler.
	 * 
	 * @return the panel with the correct layout and controller
	 */
	public TSPanel createConfigurationPanel() {
		if (m_optionspanel == null)
			initialize(m_handlertype);

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
	private void initialize(String handlerType) {
		m_optionspanel = new TSPanel(new GridBagLayout());
		m_handlercombo = new JComboBox();
		m_handlercombo.addItem(STANDARD_FILE);
		m_handlercombo.addItem(APPLICATION_LAUNCHER);
		m_handlercombo.addItem(JAVA_OBJECT);

		setHandler(handlerType);

		m_handlercombo.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				setHandler((String) m_handlercombo.getSelectedItem());
			}
		});

	}

	/**
	 * Reads the user input from the given panel. This panel should be the one
	 * created by the call to createConfigurationPanel().
	 */
	public void readInput(TSPanel panel) {

	}

	/**
	 * Override serialize so we can initalize our handler properly
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
	}

	/**
	 * Override serialize so we can initalize our handler properly
	 */
	private void writeObject(java.io.ObjectOutputStream out) throws IOException, ClassNotFoundException {
		out.defaultWriteObject();
	}

	/**
	 * Sets the type of handler for the selected binary object. (e.g.
	 * STANDARD_FILE, APPLICATION_LAUNCHER, JAVA_OBJECT ) See the defined list
	 * of handlers for valid values.
	 */
	public void setHandler(String handler) {
		m_handlertype = handler;
		m_optionspanel.repaint(); // invalidate the entire panel
		m_optionspanel.removeAll();

		// configuration common to all handlers
		JLabel handlerlabel = new JLabel(I18N.getLocalizedDialogLabel("Handler"));
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.insets = new Insets(2, 5, 2, 5);
		c.anchor = GridBagConstraints.WEST;

		m_optionspanel.add(handlerlabel, c);

		c.gridx = 1;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		m_optionspanel.add(m_handlercombo, c);

		if (handler == APPLICATION_LAUNCHER) {
			JButton filebtn = new JButton(TSGuiToolbox.loadImage("openfile16.gif"));
			filebtn.setPreferredSize(new Dimension(16, 16));
			filebtn.setMaximumSize(new Dimension(16, 16));
			filebtn.setFocusPainted(false);
			filebtn.setBorderPainted(false);

			JLabel commandlabel = new JLabel(I18N.getLocalizedDialogLabel("Command"));

			c.gridx = 0;
			c.gridy = 1;
			c.weightx = 0.0;
			c.anchor = GridBagConstraints.WEST;
			c.fill = GridBagConstraints.NONE;
			m_optionspanel.add(commandlabel, c);

			c.gridx = 1;
			c.weightx = 1.0;
			c.fill = GridBagConstraints.HORIZONTAL;
			m_optionspanel.add(new JTextField(), c);

			c.gridx = 2;
			c.weightx = 0.0;
			c.weighty = 0.0;
			c.insets = new Insets(2, 0, 2, 5);
			c.fill = GridBagConstraints.NONE;
			c.anchor = GridBagConstraints.WEST;
			m_optionspanel.add(filebtn, c);

			c.gridx = 0;
			c.gridy = 2;
			c.gridwidth = 3;
			c.gridheight = 3;
			c.fill = GridBagConstraints.BOTH;
			c.weightx = 1.0;
			String msg = "The application launcher allows you to specify an application to handle the given object.  The object will be passed to the application by a temporary file.  This file is specified with $1 in the command field.  For example, if your application requires a -f <filename> when launching on command line, then you would enter [application path] -f $1";
			JTextArea msglabel = new JTextArea();
			msglabel.setText(msg);
			msglabel.setEditable(false);
			msglabel.setLineWrap(true);
			msglabel.setWrapStyleWord(true);
			msglabel.setForeground(handlerlabel.getForeground());
			msglabel.setOpaque(false);
			m_optionspanel.add(msglabel, c);

		} else if (handler == JAVA_OBJECT) {
			c.gridx = 0;
			c.gridy = 1;
			c.gridwidth = 3;
			c.gridheight = 3;
			c.fill = GridBagConstraints.BOTH;
			c.weightx = 1.0;
			String msg = "Allows you to manipulate a Java object with this application.  Note that the classpath for this application must include any classes that are required by the Java object.";
			JTextArea msglabel = new JTextArea();
			msglabel.setText(msg);
			msglabel.setEditable(false);
			msglabel.setLineWrap(true);
			msglabel.setWrapStyleWord(true);
			msglabel.setForeground(handlerlabel.getForeground());
			msglabel.setOpaque(false);
			m_optionspanel.add(msglabel, c);
		} else // default to standard file
		{
			c.gridx = 0;
			c.gridy = 1;
			c.gridwidth = 3;
			c.gridheight = 3;
			c.fill = GridBagConstraints.BOTH;
			c.weightx = 1.0;
			String msg = "Allows you to manually store/load the binary object as a file.";
			JTextArea msglabel = new JTextArea();
			msglabel.setText(msg);
			msglabel.setEditable(false);
			msglabel.setLineWrap(true);
			msglabel.setWrapStyleWord(true);
			msglabel.setForeground(handlerlabel.getForeground());
			msglabel.setOpaque(false);
			m_optionspanel.add(msglabel, c);
		}

		System.out.println("*************************** handler combo set item: " + handler);
		m_handlercombo.setSelectedItem(handler);

		// got to call revalidate or else the components won't be positioned
		// correctly
		m_optionspanel.revalidate();
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
		m_handlertype = (String) in.readObject();
		m_application = (String) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(VERSION);
		out.writeObject(m_handlertype);
		out.writeObject(m_application);
	}

}
