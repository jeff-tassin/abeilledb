package com.jeta.abeille.licensemgr;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.interfaces.license.LicenseManager;

public class AbeilleLicenseUtils {
	public static boolean isBasic() {
		LicenseManager jlm = (LicenseManager) ComponentMgr.lookup(LicenseManager.COMPONENT_ID);
		if (jlm == null)
			return true;

		return !jlm.isValid();
	}

}
