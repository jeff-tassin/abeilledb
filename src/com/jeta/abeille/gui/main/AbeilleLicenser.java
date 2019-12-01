package com.jeta.abeille.gui.main;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.licensemgr.AppLicenseSettings;
import com.jeta.foundation.licensemgr.InternalComponentInitializer;

/**
 * Initializes the license manager for Abeille Db Client
 * 
 * @author Jeff Tassin
 */
public class AbeilleLicenser {
	public static final String COMPONENT_ID = "abeille.cache.x.info";

	/**
	 * Creates the application specific licensing info and then lauches the
	 * license manager.
	 */
	public AbeilleLicenser() {
		AppLicenseSettings als = new AppLicenseSettings("dbclient.jar", "jeta.license", new char[] { 'A', 'B' },
				"Evaluation", 1, 0, 0);
		ComponentMgr.registerComponent(AppLicenseSettings.COMPONENT_ID, als);
		new InternalComponentInitializer();
	}
}
