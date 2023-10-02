package com.jeta.abeille.main;

import com.jeta.abeille.database.model.TSConnectionMgr;
import com.jeta.abeille.gui.main.AbeilleLicenser;
import com.jeta.abeille.gui.main.MainFrame;
import com.jeta.abeille.gui.main.Splash;
import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.componentmgr.ComponentNames;
import com.jeta.foundation.componentmgr.TSComponent;
import com.jeta.foundation.documents.DocumentManager;
import com.jeta.foundation.interfaces.app.ObjectStore;

import java.io.File;
import java.io.IOException;

/**
 * This is the main launcher class for the application.
 * 
 * @author Jeff Tassin
 */
public class Abeille implements TSComponent {
	private static Boolean m_intialized=false;

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
		ComponentMgr.setAppShutdown(this);
		try {
			if (checkLockFile()) {
				// this will load the components that actually launch any login
				// dialogs or
				// perform any automatic logins
				launchComponents(true);
			} else {
				System.exit(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void initialize()  throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		launchComponents(false);
	}

	/**
	 * Launched base components needed by the rest of the application
	 */
	private void launchComponents(boolean launchFrame) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		if ( m_intialized ) {
			return;
		}
		m_intialized = true;

		System.out.println("AbeilleDb-initializing...");

		String[] args = {};

		JETAInitializer ji = new JETAInitializer();
		ji.initialize(args);

		new com.jeta.abeille.logger.SystemConsoleLogger();
		new com.jeta.foundation.app.UserPropertiesStore();
		// new com.jeta.foundation.app.AppResourceLoader("jeta");

		ComponentMgr.registerComponent(DocumentManager.COMPONENT_ID, new DocumentManager());
		ComponentMgr.registerComponent(AbeilleLicenser.COMPONENT_ID, new AbeilleLicenser());
		com.jeta.foundation.app.UserPropertiesStore ups = new com.jeta.foundation.app.UserPropertiesStore();

		ups.startup();
		com.jeta.abeille.logger.DbLogger dblogger = new com.jeta.abeille.logger.DbLogger();
		dblogger.startup();

		com.jeta.foundation.gui.editor.KeyBindingMgr kmgr = new com.jeta.foundation.gui.editor.KeyBindingMgr();
		kmgr.startup();

		if (launchFrame) {
			m_splash = new Splash();
			try {
				// MainFrameController.setLookAndFeel();
			} catch (Exception e) {
				e.printStackTrace();
			}

			MainFrame mainframe = new MainFrame();
			m_splash.dispose();
			mainframe.show();
		}
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
				// eat it
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
