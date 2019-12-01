package com.jeta.abeille.main;

import java.io.File;
import java.util.Locale;

import com.jeta.foundation.app.AppResourceLoader;
import com.jeta.foundation.app.ApplicationStateStore;
import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.componentmgr.ComponentNames;
import com.jeta.foundation.interfaces.resources.ResourceLoader;
import com.jeta.foundation.i18n.I18NHelper;
import com.jeta.foundation.utils.TSUtils;

/**
 * Class that performs common initialization for the application
 * 
 * @author Jeff Tassin
 */
public class JETAInitializer {
	/**
	 * Checks that the home directory is present. If not, tries to create it.
	 */
	private boolean checkHome(String home) {
		boolean bresult = false;
		try {
			File f = new File(home);
			f.mkdir();
			bresult = true;
		} catch (Exception e) {

		}
		return bresult;
	}

	/**
	 * Initializes the application
	 */
	public void initialize(String[] args) {
		// now try to load properties
		try {
			// System.out.println( "Starting program: classpath  " +
			// System.getProperty( "java.class.path") );
			// System.out.println( "Starting program: appdirectory  " +
			// getAppDirectory() );

			String tshome = null;
			String language = null;
			String country = null;
			if (args != null) {
				for (int x = 0; x < args.length; x++) {
					try {
						if (args[x].equals("-language"))
							language = args[x + 1];
						else if (args[x].equals("-country"))
							country = args[x + 1];
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			tshome = System.getProperty("JETA_HOME");
			if (tshome == null) {
				tshome = System.getProperty("user.home");
				if (tshome != null && tshome.length() > 0) {
					char c = tshome.charAt(tshome.length() - 1);
					if (c != '\\' && c != '/')
						tshome = tshome + File.separatorChar + ".abeilledb20";
					else
						tshome = tshome + ".abeilledb20";
				}
			}

			if (checkHome(tshome)) {
				AppResourceLoader loader = new AppResourceLoader(tshome);
				ComponentMgr.registerComponent(ResourceLoader.COMPONENT_ID, loader);

				initializeLocale(language, country);

				// load global application user settings
				ApplicationStateStore appstore = new ApplicationStateStore("application");
				ComponentMgr.registerComponent(ComponentNames.APPLICATION_STATE_STORE, appstore);

				System.setProperty("abeille.home", tshome);
				System.setProperty("abeille.version", com.jeta.abeille.common.Abeille.getVersionEx());


			} else {
				System.out.println("Unable to establish home directory: " + tshome);
				System.exit(0);
			}

			/**
			 * This is needed for Webstart applications. The reason is that
			 * eventhough the application JAR file is signed, it needs to use
			 * JDBC drivers from JAR files that are unsigned. These drivers
			 * needed file access(for embedded databases) and network access
			 * (for client/server). Without this statement, the JDBC JAR file
			 * would be unable to make file or network requests. When running as
			 * an application, the SecurityManager is null anyway, so this has
			 * no effect
			 */
			// System.setSecurityManager( null );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the language and country from the command line and sets as the
	 * default locale
	 */
	void initializeLocale(String language, String country) {
		Locale locale = null;
		if (language != null && country != null)
			locale = new Locale(language, country);
		else
			locale = Locale.getDefault();

		I18NHelper i18n = I18NHelper.getInstance();

		i18n.setLocale(locale);
		i18n.loadBundle("com.jeta.abeille.resources.MessagesBundle");
		i18n.loadBundle("com.jeta.foundation.resources.MessagesBundle");
		i18n.loadBundle("com.jeta.foundation.resources.LocalizedPlugins");
	}

}
