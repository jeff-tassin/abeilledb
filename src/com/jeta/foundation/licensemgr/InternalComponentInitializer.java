/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.licensemgr;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.interfaces.utils.Base64;
import com.jeta.foundation.utils.TSUtils;

/**
 * Initializes components that are outside of the JETA package hierarchy
 * 
 * @author Jeff Tassin
 */
public class InternalComponentInitializer {
	public InternalComponentInitializer() {
		ComponentMgr.registerComponent(Base64.COMPONENT_ID, new JETABase64());
		initializeLicense();
	}

	/**
	 * Initialize the license manager
	 * 
	 * @todo move from this method
	 */
	private void initializeLicense() {
		try {
			JETALicenseManager jlm = new JETALicenseManager();
			ComponentMgr.registerComponent(com.jeta.foundation.interfaces.license.LicenseManager.COMPONENT_ID, jlm);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
