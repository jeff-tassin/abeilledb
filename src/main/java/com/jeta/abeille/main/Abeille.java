package com.jeta.abeille.main;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.awt.Toolkit;
import java.util.Locale;

import com.jeta.abeille.gui.main.*;
import com.jeta.abeille.database.model.TSConnectionMgr;

import com.jeta.foundation.app.AppResourceLoader;
import com.jeta.foundation.app.ApplicationStateStore;
import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.componentmgr.ComponentNames;
import com.jeta.foundation.componentmgr.TSComponent;

import com.jeta.foundation.documents.DocumentManager;

import com.jeta.foundation.gui.components.TSErrorDialog;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.interfaces.app.ObjectStore;
import com.jeta.foundation.interfaces.resources.ResourceLoader;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This is the main launcher class for the application.
 * 
 * @author Jeff Tassin
 */
public class Abeille implements TSComponent {
	private File m_lockfile;

	private Splash m_splash;

	public Abeille() {
	}

	/**
	 * Look for the presence of the abeille.lck file. Currently, we only support
	 * one instance at a time. If the lock file is found, we return false.
	 */
	private boolean checkLockFile() throws IOException {
		/*
		ResourceLoader loader = (ResourceLoader) ComponentMgr.lookup(ResourceLoader.COMPONENT_ID);
		String home = loader.getHomeDirectory();
		m_lockfile = new File(home + File.separatorChar + "abeille.lck");
		if (m_lockfile.isFile()) {
			String msg = I18N.format("Found_Abeille_lock_file_1", m_lockfile.getPath());
			TSErrorDialog dlg = (TSErrorDialog) TSGuiToolbox.createDialog(TSErrorDialog.class, (java.awt.Frame) null,
					true);
			dlg.initialize(msg);
			dlg.setSize(dlg.getPreferredSize());
			dlg.showCenter();
			return false;
		} else {
			m_lockfile.createNewFile();
			m_lockfile.deleteOnExit();
			return true;
		}
		*/
		return true;
	}

	public void launch() {
		m_splash = new Splash();

		ComponentMgr.setAppShutdown(this);

		String[] args = {};

		try {
			JETAInitializer ji = new JETAInitializer();
			ji.initialize(args);

			if (checkLockFile()) {

				// this will load the components that actually launch any login
				// dialogs or
				// perform any automatic logins
				launchComponents();
			} else {
				System.exit(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * Launched base components needed by the rest of the application
	 */
	private void launchComponents() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		// start..... create some dummy objects here just to throw off any code
		// snoopers
		new com.jeta.foundation.gui.utils.ControlsAlignLayout();
		new com.jeta.foundation.gui.utils.RegexFormatter();
		new com.jeta.abeille.logger.SystemConsoleLogger();
		new com.jeta.foundation.app.UserPropertiesStore();
		new com.jeta.foundation.app.AppResourceLoader("jeta");
		ComponentMgr.lookup("license");
		ComponentMgr.lookup("license2");
		ComponentMgr.lookup("license3");
		ComponentMgr.lookup("license4");

		ComponentMgr.registerComponent(DocumentManager.COMPONENT_ID, new DocumentManager());
		ComponentMgr.registerComponent(AbeilleLicenser.COMPONENT_ID, new AbeilleLicenser());
		// new com.jeta.abeille.licensemgr.InternalComponentInitializer();
		com.jeta.foundation.app.UserPropertiesStore ups = new com.jeta.foundation.app.UserPropertiesStore();

		ups.startup();
		com.jeta.abeille.logger.DbLogger dblogger = new com.jeta.abeille.logger.DbLogger();
		dblogger.startup();

		// System.setErr( new PrintStream(new
		// com.jeta.abeille.logger.SystemConsoleLogger()) );
		// System.setOut( new PrintStream(new
		// com.jeta.abeille.logger.SystemConsoleLogger()) );

		com.jeta.foundation.gui.editor.KeyBindingMgr kmgr = new com.jeta.foundation.gui.editor.KeyBindingMgr();
		kmgr.startup();

		try {
			MainFrameController.setLookAndFeel();
		} catch (Exception e) {
			e.printStackTrace();
		}

		MainFrame mainframe = new MainFrame();
		m_splash.dispose();
		mainframe.show();

	}

	/**
	 * TSComponent implementation
	 */
	public void startup() {
		// noop
	}

	/**
	 * TSComponent Implementation
	 */
	public void shutdown() {
		try {
			try {
				System.out.println("shutting down system");
				// save the connection specific state
				TSConnectionMgr.shutdown();
				// save the main application state
				ObjectStore os = (ObjectStore) ComponentMgr.lookup(ComponentNames.APPLICATION_STATE_STORE);
				os.flush();
			} catch (Exception e) {

			}

			try {
				if (m_lockfile != null)
					m_lockfile.delete();
			} catch (Exception e) {

			}

			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Trap all system.out
	 */
	private class EmptyStream extends java.io.OutputStream {
		public void write(byte[] b) throws java.io.IOException {
			// ignore
		}

		public void write(int ival) throws java.io.IOException {
			// ignore
		}
	}

}
