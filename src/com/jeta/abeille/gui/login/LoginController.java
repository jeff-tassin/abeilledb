package com.jeta.abeille.gui.login;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import com.jeta.abeille.database.model.ConnectionInfo;
import com.jeta.abeille.database.model.ConnectionTag;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.TSConnectionMgr;

import com.jeta.abeille.licensemgr.AbeilleLicenseUtils;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.componentmgr.ComponentNames;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSErrorDialog;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.license.LicenseManager;
import com.jeta.foundation.interfaces.resources.ResourceLoader;

import com.jeta.foundation.interfaces.app.ObjectStore;
import com.jeta.foundation.utils.TSUtils;

/**
 * This is the controller for the LoginView. All button/component events are
 * handled here
 * 
 * @author Jeff Tassin
 */
public class LoginController extends TSController {
	/**
	 * The view we are controlling
	 */
	private LoginView m_view;

	/**
	 * ctor
	 */
	public LoginController(LoginView view) {
		super(view);
		m_view = view;
	}

	/**
	 * Loads the driver and attempts to login
	 */
	private void loadBootstrap(ConnectionInfo info) throws MalformedURLException, ClassNotFoundException,
			IllegalAccessException, InstantiationException, SQLException {
		TSUtils.printMessage("LoginController.loadBootstrap.....");
		ArrayList urls = new ArrayList();
		Collection jars = info.getJars();
		Iterator iter = jars.iterator();
		while (iter.hasNext()) {
			String jar = (String) iter.next();
			assert (jar != null);
			assert (jar.length() > 0);
			File f = new File(jar);
			if (f.isFile()) {
				urls.add(f.toURL());
				TSUtils.printMessage("LoginController.loadBootstrap  jar file:" + jar);
			} else {
				TSUtils.printMessage("LoginController.loadBootstrap  jar file not found: " + jar);
				assert (false);
			}
		}

		if (urls.size() == 0) {
			TSUtils.printMessage("LoginController.loadBootstrap  urls size is zero");
			assert (false);
		}

		java.net.URLClassLoader classloader = new java.net.URLClassLoader((URL[]) urls.toArray(new URL[0]));
		info.setClassLoader(classloader);

		Logger logger = Logger.getLogger(ComponentNames.APPLICATION_LOGGER);

		// if the connection is successfull, a message will be posted to the
		// main frame
		TSConnection connection = TSConnectionMgr.createConnection(info);

		iter = urls.iterator();
		while (iter.hasNext()) {
			URL url = (URL) iter.next();
			logger.fine(I18N.getLocalizedDialogLabel("JDBC Jar File") + "  " + url);
		}
	}

	/**
	 * Checks the serial number of the last saved connection. They must match
	 * (currently)
	 */
	private void checkSerialNumber(ConnectionInfo info) throws SQLException, IOException {
		ObjectStore os = TSConnectionMgr.getObjectStore(info.getConnectionId());
		if (os == null) {
			ResourceLoader loader = (ResourceLoader) ComponentMgr.lookup(ResourceLoader.COMPONENT_ID);
			HashMap lminfo = (HashMap) ComponentMgr.lookup(LicenseManager.LICENSE_INFO);
			String serialno = (String) lminfo.get(LicenseManager.SERIAL_NO);
		}
	}

	/**
	 * Called by the LoginAction. Check that the given connection is not
	 * currently opened in the main frame
	 */
	protected boolean validate(ConnectionInfo info) {
		return true;
	}

	boolean login() {
		boolean result = false;
		ConnectionInfo info = m_view.getSelectedConnection();
		if (validate(info)) {
			String username = m_view.getUsername();
			String password = m_view.getPassword();
			try {
				// now try to create the login bootstrap
				info.setUserName(username);
				info.setPassword(password);

				checkSerialNumber(info);
				assert (info.getUrl() != null);

				loadBootstrap(info);

				LoginModel lmodel = m_view.getModel();
				lmodel.setLastConnection(info);
				lmodel.setLastUser(info, username);

				/** saves the preferences */
				lmodel.save();
				result = true;
			} catch (ClassNotFoundException cnf) {
				TSUtils.printStackTrace(cnf);

				TSErrorDialog dlg = (TSErrorDialog) TSGuiToolbox.createDialog(TSErrorDialog.class, m_view, true);
				StringBuffer msg = new StringBuffer();
				msg.append("ClassNotFoundException: ");
				msg.append(cnf.getLocalizedMessage());
				msg.append("\n");
				msg.append(I18N.getLocalizedMessage("check_driver_in_path"));
				msg.append("\n");
				msg.append(formatConnectionInfo(info));
				dlg.initialize(msg.toString());
				dlg.showErrorIcon(I18N.getLocalizedMessage("Login Error"));
				dlg.setSize(dlg.getPreferredSize());
				dlg.showCenter();
			} catch (Throwable e) {
				TSUtils.printStackTrace(e);
				TSErrorDialog dlg = (TSErrorDialog) TSGuiToolbox.createDialog(TSErrorDialog.class, m_view, true);
				StringBuffer msg = new StringBuffer();
				if (e instanceof NullPointerException) {
					/**
					 * this can occur when using DB2 and the URL has invalid
					 * characters
					 */
					msg.append(I18N.getLocalizedMessage("Unexpected Error"));
					msg.append("\n");
					msg.append(I18N.getLocalizedMessage("Check your URL"));
					msg.append("\n");
					msg.append("\n");
					msg.append(formatConnectionInfo(info));
					dlg.initialize(msg.toString());
				} else {
					String emsg = e.getMessage();
					if (emsg != null) {
						msg.append(emsg);
					}
					msg.append("\n");
					msg.append("\n");
					msg.append(formatConnectionInfo(info));
					dlg.initialize(msg.toString());
				}
				dlg.showErrorIcon(I18N.getLocalizedMessage("Login Error"));
				dlg.setSize(dlg.getPreferredSize());
				dlg.showCenter();
			}
		}
		return result;
	}

	public static String formatConnectionInfo(ConnectionInfo cinfo) {
		if (cinfo == null) {
			return "Unexpected error.  ConnectionInfo was null.";
		}

		StringBuffer msg = new StringBuffer();
		if (cinfo.isBasic()) {
			msg.append(I18N.format("UserName_1", cinfo.getUserName()));
			msg.append("\n");
			msg.append(I18N.format("Name_1", cinfo.getName()));
			msg.append("\n");
			msg.append(I18N.format("Server_1", cinfo.getServer()));
			msg.append("\n");
			msg.append(I18N.format("Port_1", String.valueOf(cinfo.getPort())));
			msg.append("\n");
			msg.append(I18N.format("URL_1", cinfo.getUrl()));
			msg.append("\n");
			msg.append(I18N.format("Driver_1", cinfo.getDriver()));
			msg.append("\n");
			msg.append(I18N.format("JAR_1", cinfo.getJDBCJar()));
			return msg.toString();
		} else {
			msg.append(I18N.format("UserName_1", cinfo.getUserName()));
			msg.append("\n");
			msg.append(I18N.format("URL_1", cinfo.getUrl()));
			msg.append("\n");
			msg.append(I18N.format("Driver_1", cinfo.getDriver()));
			msg.append("\n");
			Collection jars = cinfo.getJars();
			Iterator iter = jars.iterator();
			if (jars.size() == 1) {
				msg.append(I18N.getLocalizedDialogLabel("JDBC Jar Files"));
				msg.append(" ");
				msg.append((String) iter.next());
			} else {
				msg.append(I18N.getLocalizedDialogLabel("JDBC Jar Files"));
				msg.append("\n");
				while (iter.hasNext()) {
					msg.append((String) iter.next());
					msg.append("\n");
				}
			}
			return msg.toString();
		}
	}
}
