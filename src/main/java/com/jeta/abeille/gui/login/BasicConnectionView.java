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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import java.io.File;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.ConnectionInfo;

import com.jeta.abeille.gui.downloader.DownloaderDialog;
import com.jeta.abeille.gui.login.help.ConnectionHelpView;

import com.jeta.foundation.componentmgr.ComponentMgr;

import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSPanel;
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
public class BasicConnectionView extends TSPanel implements ConnectionView {
	/**
	 * the name of the databases that the user can choose to log into Currently
	 * limited to Postgres and MySQL
	 */
	private JComboBox m_databasecombo;

	/** checkbox that changes the view to advanced */
	private JCheckBox m_advancedcheck;

	/** the text field for the url of the database */
	private JTextField m_url;

	/**
	 * the textfield name for this database This is the instance name for the
	 * database. That is, the value in the URL
	 */
	private JTextField m_name;

	/**
	 * The label next ot the name text field
	 */
	private JLabel m_namelabel;

	/** the textfield description for this database */
	private JTextField m_description;

	/** the textfield server name for this database */
	private JTextField m_server;

	/** the textfield port for this database */
	private JTextField m_port;

	/** the JDBC jar file path/name for this database */
	private JTextField m_jar;

	/** open jar file button */
	private JButton m_filebtn;

	/** download jar file button */
	// private JButton m_helpbtn;

	/** the uid of the model */
	private String m_uid;

	private ConnectionInfo m_model;

	/** listener for focus events on text fields so we can update the url field */
	private ComponentFocusListener m_focuslistener = new ComponentFocusListener();

	/** component ids */
	public static final String ID_DATABASE = "database.combo";
	public static final String ID_ADVANCED_CHECK = "avanced.view.check";
	public static final String ID_NAME = "database.name.field";
	public static final String ID_SERVER = "database.server.field";
	public static final String ID_DESCRIPTION = "database.description.field";
	public static final String ID_PORT = "database.port.field";
	public static final String ID_URL = "database.url.field";
	public static final String ID_DRIVER = "database.driver.field";
	public static final String ID_JDBC_JAR = "jdbc.jar.file";

	/**
	 * ctor
	 */
	public BasicConnectionView(ConnectionInfo model) {
		setLayout(new BorderLayout());
		add(createComponents(), BorderLayout.NORTH);
		setModel(model);

		String jdbc = TSUtils.fastTrim(getJDBCJar());
		if (jdbc.length() == 0) {
			setJDBC(getDatabase());
		}
	}

	/**
	 * Creates the panel that contains the gui components
	 */
	private JPanel createComponents() {
		ControlsAlignLayout layout = new ControlsAlignLayout();

		m_databasecombo = new JComboBox();
		m_databasecombo.setName(ID_DATABASE);

		Database def_database = null;
		Collection c = Database.getDatabases();
		Iterator iter = c.iterator();
		while (iter.hasNext()) {
			Database db = (Database) iter.next();
			m_databasecombo.addItem(db);
			if (def_database == null) {
				def_database = db;
			}
		}

		m_databasecombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				Database db = getDatabase();
				int port = ConnectionInfo.getDefaultPort(db);
				if (port > 0) {
					m_port.setText(String.valueOf(port));
				} else {
					m_port.setText("");
				}
				m_url.setText(ConnectionInfo.getUrl(getDatabase(), getServer(), getPort(), getInstanceName()));
				m_jar.setText("");
				setJDBC(db);
				if (isEnabled()) {
					// m_helpbtn.setEnabled( true );
				}

				if (db.equals(Database.MCKOI)) {
					m_namelabel.setText(I18N.getLocalizedDialogLabel("Schema"));
				} else if (db.equals(Database.ORACLE)) {
					m_namelabel.setText(I18N.getLocalizedDialogLabel("SID"));
				} else {
					m_namelabel.setText(I18N.getLocalizedDialogLabel("Instance"));
				}
			}
		});

		m_advancedcheck = new JCheckBox(I18N.getLocalizedMessage("Advanced"));
		m_advancedcheck.setName(ID_ADVANCED_CHECK);

		JPanel db_panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		db_panel.add(m_databasecombo);
		db_panel.add(Box.createHorizontalStrut(30));
		db_panel.add(m_advancedcheck);

		m_name = new JTextField();
		m_name.setName(ID_NAME);
		m_name.addFocusListener(m_focuslistener);

		m_description = new JTextField();
		m_description.setName(ID_DESCRIPTION);

		m_server = new JTextField();
		m_server.setName(ID_SERVER);
		m_server.addFocusListener(m_focuslistener);

		m_port = new JTextField();
		m_port.setName(ID_PORT);
		m_port.addFocusListener(m_focuslistener);

		if (def_database != null) {
			int port = ConnectionInfo.getDefaultPort(def_database);
			if (port > 0) {
				m_port.setText(String.valueOf(port));
			} else {
				m_port.setText("");
			}
		}

		m_url = new JTextField();
		m_url.setName(ID_URL);
		m_url.setEnabled(false);

		m_jar = new JTextField();
		m_jar.setName(ID_JDBC_JAR);
		m_filebtn = new JButton(TSGuiToolbox.loadImage("ellipsis16.gif"));
		m_filebtn.setToolTipText(I18N.getLocalizedMessage("Manually specify JDBC driver"));
		m_filebtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				editJARLocation();
			}
		});

		// m_helpbtn = new JButton( TSGuiToolbox.loadImage("general/Help16.gif"
		// ) );
		// m_helpbtn.setToolTipText( I18N.getLocalizedMessage( "Help" ) );

		/*
		 * m_helpbtn.addActionListener( new ActionListener() { public void
		 * actionPerformed( ActionEvent evt ) { showHelpDialog(); } });
		 */

		JPanel jarpanel = createTextFieldButtonPanel(m_jar, m_filebtn);

		JComponent[] controls = new JComponent[7];
		controls[0] = db_panel;
		controls[1] = m_name;
		controls[2] = m_description;
		controls[3] = m_server;
		controls[4] = m_port;
		controls[5] = jarpanel;
		controls[6] = m_url;

		m_namelabel = new JLabel(I18N.getLocalizedDialogLabel("Instance"));

		JLabel[] labels = new JLabel[7];
		labels[0] = new JLabel(I18N.getLocalizedDialogLabel("Database"));
		labels[1] = m_namelabel;
		labels[2] = new JLabel(I18N.getLocalizedDialogLabel("Description"));
		labels[3] = new JLabel(I18N.getLocalizedDialogLabel("Server"));
		labels[4] = new JLabel(I18N.getLocalizedDialogLabel("Port"));
		labels[5] = new JLabel(I18N.getLocalizedDialogLabel("JDBC Jar File"));
		labels[6] = new JLabel(I18N.getLocalizedDialogLabel("URL"));

		// layout.setMaxTextFieldWidth( m_databasecombo, 20 );
		layout.setMaxTextFieldWidth(m_name, 20);
		layout.setMaxTextFieldWidth(m_description, 20);
		layout.setMaxTextFieldWidth(m_server, 20);
		layout.setMaxTextFieldWidth(m_port, 10);

		JPanel panel = TSGuiToolbox.alignLabelTextRows(layout, labels, controls);

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
			m_model.setServer(getServer());
			m_model.setDescription(m_description.getText());
			m_model.setName(getInstanceName());
			m_model.setJar(m_jar.getText());
			m_model.setPort(getPort());
			m_model.setUID(m_uid);
			m_model.setAdvanced(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return m_model;
	}

	/**
	 * Creates a text field with a button to the right of the field
	 * 
	 * @return the field and button in a JPanel
	 */
	JPanel createTextFieldButtonPanel(JTextField txtField, JButton btn1) {
		/*
		 * Dimension d =
		 * com.jeta.foundation.gui.components.TextFieldwButtonPanel
		 * .getButtonDimension(); btn1.setBorderPainted(true);
		 * btn1.setFocusPainted(false); btn1.setSize( d ); btn1.setMaximumSize(
		 * d ); btn1.setPreferredSize( d );
		 * 
		 * btn2.setBorderPainted(true); btn2.setFocusPainted(false);
		 * btn2.setSize( d ); btn2.setMaximumSize( d ); btn2.setPreferredSize( d
		 * );
		 * 
		 * JPanel panel = new JPanel( new java.awt.GridLayout(1,2) ) { public
		 * Insets getInsets() { return new Insets(0,0,0,0); } };
		 * panel.setBorder( javax.swing.BorderFactory.createEmptyBorder(0,0,0,0)
		 * ); panel.add( btn1 ); panel.add( btn2 );
		 */
		return new com.jeta.foundation.gui.components.TextFieldwButtonPanel(txtField, btn1);
	}

	/**
	 * Invokes the download dialog
	 */
	private void downloadAction() {
		DownloaderDialog dlg = (DownloaderDialog) TSGuiToolbox.createDialog(DownloaderDialog.class, this, true);
		dlg.setSize(dlg.getPreferredSize());
		ResourceLoader loader = (ResourceLoader) ComponentMgr.lookup(ResourceLoader.COMPONENT_ID);

		String hostname = "";
		try {
			loader.createSubdirectories("jdbc");
			if (TSUtils.isDebug()) {
				hostname = java.net.InetAddress.getLocalHost().getHostName();
			}
		} catch (Exception e) {
			TSUtils.printException(e);
		}

		StringBuffer url = new StringBuffer();
		Database db = getDatabase();
		url.append("http://www.jetaware.com/getjdbcdescriptor13?database=");

		url.append(db.getName().toLowerCase());
		dlg.setUrl(url.toString(), loader.getHomeDirectory() + File.separatorChar + "jdbc");

		dlg.showCenter();
		if (dlg.isOk()) {
			String result = dlg.getResult();
			m_jar.setText(result);
			String propname = "jdbc." + db.getName().toLowerCase();
			TSUserProperties userprops = (TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID);
			userprops.setProperty(propname, result);
		}
	}

	/**
	 * Invokes the JFileChooser so the user can browse for a JAR file
	 */
	private void editJARLocation() {
		String current = m_jar.getText();
		if (current == null)
			current = "";

		JFileChooser chooser = null;
		if (current.length() > 0) {
			chooser = new JFileChooser(current);
			int returnVal = chooser.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File f = chooser.getSelectedFile();
				Database db = getDatabase();
				m_jar.setText(f.getPath());
			}
		} else {
			File f = com.jeta.foundation.gui.filechooser.TSFileChooserFactory.showOpenDialog();
			if (f != null) {
				Database db = getDatabase();
				m_jar.setText(f.getPath());
			}
		}

	}

	/**
	 * @return the selected database
	 */
	public Database getDatabase() {
		return (Database) m_databasecombo.getSelectedItem();
	}

	public String getJDBCJar() {
		return m_jar.getText();
	}

	public String getInstanceName() {
		return m_name.getText().trim();
	}

	/**
	 * @return the port number entered by the user
	 */
	public int getPort() {
		try {
			return Integer.parseInt(m_port.getText().trim());
		} catch (Exception e) {
			return 0;
		}
	}

	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(7, 13);
	}

	/**
	 * The server name
	 */
	public String getServer() {
		return m_server.getText().trim();
	}

	/**
	 * Initializes the gui components from the model
	 */
	private void loadModel(ConnectionInfo model) {
		if (model != null) {
			m_databasecombo.setSelectedItem(model.getDatabase());
			m_server.setText(model.getServer());
			m_description.setText(model.getDescription());
			m_port.setText(String.valueOf(model.getPort()));
			m_name.setText(model.getName());
			m_jar.setText(model.getJDBCJar());

			m_url.setText(ConnectionInfo.getUrl(getDatabase(), getServer(), getPort(), getInstanceName()));

			m_uid = model.getUID();
			m_model = model;
		}
	}

	/**
	 * Enables/Disables the view
	 */
	public void setEnabled(boolean enable) {
		super.setEnabled(enable);
		m_databasecombo.setEnabled(enable);
		m_advancedcheck.setEnabled(enable);
		// m_url.setEnabled( enable );
		m_name.setEnabled(enable);
		m_description.setEnabled(enable);
		m_server.setEnabled(enable);
		m_port.setEnabled(enable);
		m_jar.setEnabled(enable);
		m_filebtn.setEnabled(enable);
		// m_helpbtn.setEnabled( enable );
	}

	public void setJDBC(Database db) {
		if (db != null) {
			String propname = "jdbc." + db.getName().toLowerCase();
			TSUserProperties userprops = (TSUserProperties) ComponentMgr.lookup(TSUserProperties.COMPONENT_ID);
			String jar_name = TSUtils.fastTrim(userprops.getProperty(propname));
			if (jar_name != null && jar_name.length() > 0) {
				try {
					File f = new File(jar_name);
					if (f.exists() && f.isFile()) {
						m_jar.setText(jar_name);
					}
				} catch (Exception e) {
					TSUtils.printException(e);
				}
			}
		}
	}

	/**
	 * Sets the model for the view
	 */
	public void setModel(ConnectionInfo model) {
		loadModel(model);
	}

	/**
	 * Shows our own help dialog for configuring JDBC parameters.
	 */
	private void showHelpDialog() {
		TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, this, true);
		dlg.setTitle(I18N.getLocalizedMessage("Help"));
		dlg.setPrimaryPanel(new ConnectionHelpView("com/jeta/abeille/resources/help/db_basic_help.htm"));
		TSGuiToolbox.setReasonableWindowSize(dlg, dlg.getPreferredSize());
		dlg.showCenter();
	}

	/**
	 * Listen for focus events and update the url field accordingly
	 */
	public class ComponentFocusListener extends FocusAdapter {
		public void focusLost(FocusEvent e) {
			m_url.setText(ConnectionInfo.getUrl(getDatabase(), getServer(), getPort(), getInstanceName()));
		}
	}

}
