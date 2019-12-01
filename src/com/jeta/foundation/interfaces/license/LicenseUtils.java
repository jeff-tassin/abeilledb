/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.interfaces.license;

import com.jeta.foundation.componentmgr.ComponentMgr;

public class LicenseUtils {
	/**
	 * @return true is this license is for the evaluation version
	 */
	public static boolean isEvaluation() {
		LicenseManager jlm = (LicenseManager) ComponentMgr.lookup(LicenseManager.COMPONENT_ID);
		return jlm.isEvaluation();
	}

}
