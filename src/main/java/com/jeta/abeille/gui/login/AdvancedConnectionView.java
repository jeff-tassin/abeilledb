package com.jeta.abeille.gui.login;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.ConnectionInfo;
import com.jeta.abeille.gui.login.help.ConnectionHelpView;

import com.jeta.foundation.gui.components.TextFieldwButtonPanel;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSPanel;

import com.jeta.foundation.gui.layouts.ColumnLayout;
import com.jeta.foundation.gui.utils.ControlsAlignLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.resources.ResourceLoader;
import com.jeta.foundation.interfaces.userprops.TSUserProperties;

import com.jeta.foundation.utils.TSUtils;

/**
 * This view allows the user to edit connection properties
 * 
 * @author Jeff Tassin
 */
public class AdvancedConnectionView extends TSPanel implements ConnectionView {
	/**
	 * the name of the databases that the user can choose to log into Currently
	 * limited to Postgres and MySQL
	 */
	private JComboBox m_databasecombo;

	/** checkbox that changes the view to advanced */
	private JCheckBox m_advancedcheck;

	/** the text field for the url of the database */
	private JComboBox m_url;

	/** the textfield description for this database */
	private JTextField m_description;

	/** the textfield description for this database */
	private JComboBox m_driver;

	/** List that displays the jar files for this connection */
	private JList m_jarslist;
	private JToolBar m_toolbar;
	private JButton m_addbtn;
	private JButton m_delbtn;
	// private JButton m_helpbtn;

	/** the uid of the model */
	private String m_uid;

	/** checkbox that sets the embedded option for daffodil databases */
	private JCheckBox m_embeddedcheck;

	/**
	 * text field that displays the location of the database directory for
	 * daffodil embedded
	 */
	private JTextField m_database_dir;
	private TextFieldwButtonPanel m_db_dir_panel;
	private JLabel m_db_dir_label;

	private ConnectionInfo m_model;

	/** component ids */
	public static final String ID_DATABASE = "database.combo";
	public static final String ID_ADVANCED_CHECK = "advanced.view.check";
	public static final String ID_EMBEDDED_CHECK = "embedded.check";
	public static final String ID_DESCRIPTION = "database.description.field";
	public static final String ID_URL = "database.url.field";
	public static final String ID_DRIVER = "database.driver.field";
	public static final String ID_JDBC_JAR = "jdbc.jar.file";

	/**
	 * ctor
	 */
	public AdvancedConnectionView(ConnectionInfo model) {
		setLayout(new BorderLayout());
		add(createComponents(), BorderLayout.CENTER);
		setModel(model);
	}

	/**
	 * Invokes the JFileChooser so the user can browse for a JAR file
	 */
	private void addJAR() {
		File f = com.jeta.foundation.gui.filechooser.TSFileChooserFactory.showOpenDialog();
		if (f != null) {
			if (f.isFile()) {
				String path = f.getPath();
				DefaultListModel lmodel = (DefaultListModel) m_jarslist.getModel();
				if (!lmodel.contains(path)) {
					lmodel.addElement(path);
				}
			}
		}
	}

	/**
	 * Creates the panel that contains the gui components
	 */
	private JPanel createComponents() {
		ControlsAlignLayout layout = new ControlsAlignLayout();

		m_databasecombo = new JComboBox();
		m_databasecombo.setName(ID_DATABASE);
		Collection databases = Database.getDatabases();
		Iterator iter = databases.iterator();
		while (iter.hasNext()) {
			Database db = (Database) iter.next();
			m_databasecombo.addItem(db);
		}
		m_databasecombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				updateView();
			}
		});

		m_advancedcheck = new JCheckBox(I18N.getLocalizedMessage("Advanced"));
		m_advancedcheck.setName(ID_ADVANCED_CHECK);
		m_advancedcheck.setSelected(true);

		JPanel db_panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		db_panel.add(m_databasecombo);
		db_panel.add(Box.createHorizontalStrut(30));
		db_panel.add(m_advancedcheck);
		db_panel.add(Box.createHorizontalStrut(10));

		m_embeddedcheck = new JCheckBox(I18N.getLocalizedMessage("Embedded"));
		m_embeddedcheck.setName(ID_EMBEDDED_CHECK);
		db_panel.add(m_embeddedcheck);
		m_embeddedcheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				m_db_dir_panel.setVisible(m_embeddedcheck.isSelected());
				m_db_dir_label.setVisible(m_embeddedcheck.isSelected());
			}
		});

		m_description = new JTextField(20);
		m_description.setName(ID_DESCRIPTION);

		m_driver = new JComboBox();
		m_driver.addItem("com.pointbase.jdbc.jdbcUniversalDriver");
		m_driver.addItem("in.co.daffodil.db.rmi.RmiDaffodilDBDriver");
		m_driver.addItem("in.co.daffodil.db.jdbc.DaffodilDBDriver");
		m_driver.addItem("org.postgresql.Driver");
		m_driver.addItem("com.mysql.jdbc.Driver");
		m_driver.addItem("org.hsqldb.jdbcDriver");
		m_driver.addItem("com.mckoi.JDBCDriver");
		m_driver.addItem("oracle.jdbc.driver.OracleDriver");
		m_driver.addItem("COM.ibm.db2.jdbc.net.DB2Driver");
		m_driver.addItem("com.sybase.jdbc2.jdbc.SybDriver");
		m_driver.setEditable(true);
		m_driver.setSelectedItem("");

		m_url = new JComboBox();
		m_url.addItem("jdbc:pointbase:embedded:<name>");
		m_url.addItem("jdbc:pointbase:server://<server>/<name>");
		m_url.addItem("jdbc:daffodilDB_embedded:<name>");
		m_url.addItem("jdbc:daffodilDB://<server>:<port>/<name>");
		m_url.addItem("jdbc:postgresql://<server>:<port>/<name>");
		m_url.addItem("jdbc:mysql://<server>:<port>/<name>");
		m_url.addItem("jdbc:sybase:Tds:<server>:<port>/<name>");
		m_url.addItem("jdbc:hsqldb:hsql://<server>:<port>");
		m_url.addItem("jdbc:mckoi://<server>:<port>");
		m_url.addItem("jdbc:oracle:thin:@<server>:<port>:<name>");
		m_url.addItem("jdbc:db2://<server>:<port>/<name>");
		m_url.addItem("jdbc:sybase:Tds:<server>:<port>/<name>");
		m_url.setEditable(true);
		m_url.setName(ID_URL);
		m_url.setSelectedItem("");

		JPanel jarpanel = createJARPanel();

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(5, 5, 0, 5);
		c.anchor = GridBagConstraints.WEST;
		panel.add(new JLabel(I18N.getLocalizedDialogLabel("Database")), c);

		c.gridy = 1;
		panel.add(new JLabel(I18N.getLocalizedDialogLabel("Description")), c);
		c.gridy = 2;
		panel.add(new JLabel(I18N.getLocalizedDialogLabel("Driver")), c);
		c.gridy = 3;
		panel.add(new JLabel(I18N.getLocalizedDialogLabel("URL")), c);

		/**
		 * we need to do this because the jar_label is noticeable higher than
		 * the toolbar buttons in the windows and metal look and feels
		 */
		JLabel jar_label = new JLabel(I18N.getLocalizedDialogLabel("JDBC Jar Files"));
		jar_label.setBorder(javax.swing.BorderFactory.createEmptyBorder(7, 0, 0, 0));

		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridy = 4;
		panel.add(jar_label, c);
		m_db_dir_label = new JLabel(I18N.getLocalizedDialogLabel("Database Directory"));
		c.gridy = 5;
		panel.add(m_db_dir_label, c);

		c.gridx = 1;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0f;
		c.weighty = 0.0f;
		panel.add(db_panel, c);

		JPanel des_panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		des_panel.add(m_description);

		// JPanel driver_panel = new JPanel( new FlowLayout( FlowLayout.LEFT, 0,
		// 0 ) );
		// driver_panel.add( m_driver );

		c.gridy = 1;
		panel.add(des_panel, c);
		c.gridy = 2;
		panel.add(m_driver, c);
		c.gridy = 3;
		panel.add(m_url, c);

		c.weighty = 1.0f;
		c.fill = GridBagConstraints.BOTH;
		c.gridy = 4;
		panel.add(jarpanel, c);

		c.weightx = 1.0f;
		c.weighty = 0.0f;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		m_db_dir_panel = new TextFieldwButtonPanel(TSGuiToolbox.loadImage("ellipsis16.gif"));
		m_database_dir = m_db_dir_panel.getTextField();
		c.gridy = 5;
		panel.add(m_db_dir_panel, c);

		JButton set_dir_btn = m_db_dir_panel.getButton();
		set_dir_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				setDatabaseDirectory();
			}
		});

		c.weighty = 0.0f;
		c.weightx = 0.0f;
		c.fill = GridBagConstraints.NONE;
		c.gridy = 6;
		// panel.add( Box.createVerticalStrut(10), c );

		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return panel;
	}

	/**
	 * Saves the information in the GUI components to the model
	 */
	public ConnectionInfo createConnectionModel() {
		try {
			if (m_model == null)
				m_model = new ConnectionInfo();

			m_model.setDatabase((Database) m_databasecombo.getSelectedItem());
			m_model.setDescription(m_description.getText());
			m_model.setDriver(getDriver());
			m_model.setUrl(getUrl());
			m_model.setJars(getJars());
			m_model.setUID(m_uid);
			m_model.setAdvanced(true);
			m_model.setEmbedded(isEmbedded());
			m_model.setParameter1(TSUtils.fastTrim(m_database_dir.getText()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return m_model;
	}

	JPanel createJARPanel() {
		m_toolbar = new javax.swing.JToolBar();
		m_toolbar.setFloatable(false);
		m_addbtn = TSGuiToolbox.createToolBarButton("incors/16x16/jar_add.png", "add.jar", "");
		m_addbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				addJAR();
			}
		});

		m_delbtn = TSGuiToolbox.createToolBarButton("incors/16x16/jar_delete.png", "delete.jar", "");
		m_delbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				deleteJAR();
			}
		});

		/*
		 * m_helpbtn = TSGuiToolbox.createToolBarButton( "general/Help16.gif",
		 * "help", "" ); m_helpbtn.addActionListener( new ActionListener() {
		 * public void actionPerformed( ActionEvent evt ) { showHelpDialog(); }
		 * });
		 */

		m_toolbar.add(m_addbtn);
		m_toolbar.add(m_delbtn);
		// m_toolbar.add( m_helpbtn );

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(m_toolbar, BorderLayout.NORTH);

		m_jarslist = new JList();
		m_jarslist.setModel(new DefaultListModel());
		javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(m_jarslist);
		panel.add(scroll, BorderLayout.CENTER);
		return panel;
	}

	/**
	 * Creates a text field with a button to the right of the field
	 * 
	 * @return the field and button in a JPanel
	 */
	JPanel createTextFieldButtonPanel(JTextField txtField, JButton btn1) {
		Dimension d = com.jeta.foundation.gui.components.TextFieldwButtonPanel.getButtonDimension();
		btn1.setBorderPainted(true);
		btn1.setFocusPainted(false);
		btn1.setSize(d);
		btn1.setMaximumSize(d);
		btn1.setPreferredSize(d);

		JPanel panel = new JPanel(new java.awt.GridLayout(1, 2)) {
			public Insets getInsets() {
				return new Insets(0, 0, 0, 0);
			}
		};
		panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		panel.add(btn1);

		return new com.jeta.foundation.gui.components.TextFieldwComponentPanel(txtField, panel);
	}

	/**
	 * Deletes the selected jar from the list
	 */
	private void deleteJAR() {
		int index = m_jarslist.getSelectedIndex();
		if (index >= 0) {
			DefaultListModel lmodel = (DefaultListModel) m_jarslist.getModel();
			lmodel.removeElementAt(index);
		}
	}

	/**
	 * @return the selected database
	 */
	public Database getDatabase() {
		return (Database) m_databasecombo.getSelectedItem();
	}

	/**
	 * @return the database path entered by the user. This is only used if the
	 *         database is embedded (e.g. Daffodil)
	 */
	public String getDatabasePath() {
		return TSUtils.fastTrim(m_database_dir.getText());
	}

	/**
	 * @return the driver entered by the user
	 */
	public String getDriver() {
		return (String) m_driver.getEditor().getItem();
	}

	/**
	 * @return a collection of String objects that represent paths to JDBC jars
	 *         files needed for this connection.
	 */
	public Collection getJars() {
		LinkedList jars = new LinkedList();
		DefaultListModel lmodel = (DefaultListModel) m_jarslist.getModel();
		for (int index = 0; index < lmodel.getSize(); index++) {
			String jar = TSUtils.fastTrim((String) lmodel.getElementAt(index));
			if (jar.length() > 0) {
				jars.add(jar);
			}
		}
		return jars;
	}

	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(7, 13);
	}

	/**
	 * @return the url entered by the user
	 */
	public String getUrl() {
		return (String) m_url.getEditor().getItem();
	}

	/**
	 * @return true if the embedded checkbox is selected
	 */
	public boolean isEmbedded() {
		return m_embeddedcheck.isSelected();
	}

	/**
	 * Initializes the gui components from the model
	 */
	private void loadModel(ConnectionInfo model) {
		if (model != null) {
			m_databasecombo.setSelectedItem(model.getDatabase());
			m_description.setText(model.getDescription());
			m_uid = model.getUID();
			m_url.setSelectedItem(model.getUrl());
			m_driver.setSelectedItem(model.getDriver());
			m_embeddedcheck.setSelected(model.isEmbedded());
			m_database_dir.setText(model.getParameter1());

			DefaultListModel lmodel = (DefaultListModel) m_jarslist.getModel();
			lmodel.removeAllElements();
			Collection jars = model.getJars();
			Iterator iter = jars.iterator();
			while (iter.hasNext()) {
				String jar = TSUtils.fastTrim((String) iter.next());
				if (jar.length() > 0) {
					lmodel.addElement(jar);
				}
			}

			m_model = model;
		}
	}

	/**
	 * Invokes the file chooser dialog. Allows the user to specify a directory
	 * for the location of an embedded database.
	 */
	private void setDatabaseDirectory() {
		File f = com.jeta.foundation.gui.filechooser.TSFileChooserFactory
				.showOpenDialog(javax.swing.JFileChooser.DIRECTORIES_ONLY);
		if (f != null) {
			if (f.isFile())
				f = f.getParentFile();

			String path = f.getPath();
			m_database_dir.setText(path);
		}
	}

	/**
	 * Enables/Disables the view
	 */
	public void setEnabled(boolean enable) {
		super.setEnabled(enable);
		m_databasecombo.setEnabled(enable);
		m_advancedcheck.setEnabled(enable);
		m_url.setEnabled(enable);
		m_description.setEnabled(enable);
		m_jarslist.setEnabled(enable);
		m_addbtn.setEnabled(enable);
		m_delbtn.setEnabled(enable);
		// m_helpbtn.setEnabled( enable );
		m_driver.setEnabled(enable);
		m_embeddedcheck.setEnabled(enable);
		m_db_dir_panel.setEnabled(enable);
	}

	/**
	 * Sets the model for the view
	 */
	public void setModel(ConnectionInfo model) {
		loadModel(model);
		updateView();
	}

	/**
	 * Shows our own help dialog for configuring JDBC parameters.
	 */
	private void showHelpDialog() {
		TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, this, true);
		dlg.setTitle(I18N.getLocalizedMessage("Help"));
		dlg.setPrimaryPanel(new ConnectionHelpView("com/jeta/abeille/resources/help/db_adv_help.htm"));
		TSGuiToolbox.setReasonableWindowSize(dlg, dlg.getPreferredSize());
		dlg.showCenter();
	}

	/**
	 * Displays or hides components on the view based on the currently selected
	 * database. Daffodil has an embedded check and text field that are not
	 * displayed with the other databases.
	 */
	private void updateView() {
		if (Database.DAFFODIL.equals(getDatabase()) || Database.POINTBASE.equals(getDatabase())) {
			m_embeddedcheck.setVisible(true);
			if (m_embeddedcheck.isSelected()) {
				m_db_dir_panel.setVisible(true);
				m_db_dir_label.setVisible(true);
			}
		} else {
			m_db_dir_panel.setVisible(false);
			m_embeddedcheck.setVisible(false);
			m_db_dir_label.setVisible(false);
		}
	}
}
