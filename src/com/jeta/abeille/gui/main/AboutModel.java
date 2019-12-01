package com.jeta.abeille.gui.main;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.HashMap;

import com.jeta.abeille.licensemgr.AbeilleLicenseUtils;

import com.jeta.foundation.componentmgr.ComponentMgr;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.license.LicenseManager;

/**
 * Data model for About Box
 * 
 * @author Jeff Tassin
 */
public class AboutModel {
	private String m_licensee;
	private String m_serialno;
	private String m_expires;
	private String m_evalcode;
	private String m_licensetype;

	/**
	 * ctor
	 */
	public AboutModel() {

		HashMap lminfo = (HashMap) ComponentMgr.lookup(LicenseManager.LICENSE_INFO);
		if (lminfo != null) {
			m_licensee = (String) lminfo.get(LicenseManager.LICENSEE);
			m_serialno = (String) lminfo.get(LicenseManager.SERIAL_NO);

			LicenseManager jlm = (LicenseManager) ComponentMgr.lookup(LicenseManager.COMPONENT_ID);
			if (jlm.isSpecialEvaluation() || jlm.isEvaluation()) {
				Calendar c = (Calendar) lminfo.get(LicenseManager.EXPIRE_DATE);
				SimpleDateFormat format = new SimpleDateFormat("MMMMM dd, yyyy");
				m_expires = format.format(c.getTime());

				m_licensetype = I18N.getLocalizedMessage("Evaluation");
			} else {
				m_licensetype = I18N.getLocalizedMessage("Standard");
			}
		}
	}

	/**
	 * @return a code that indicates if this is an evaluation version The letter
	 *         'E' marks this as an evaluation version;
	 */
	public String getLicenseType() {
		return m_licensetype;
	}

	/**
	 * @return a formatted date that shows the expiration date
	 */
	public String getExpires() {
		return m_expires;
	}

	/**
	 * @return the name of the licensee
	 */
	public String getLicensee() {
		return m_licensee;
	}

	/**
	 * @return the serial number
	 */
	public String getSerialNumber() {
		return m_serialno;
	}

	public String getVersion() {
		return com.jeta.abeille.common.Abeille.getVersion();
	}

}
