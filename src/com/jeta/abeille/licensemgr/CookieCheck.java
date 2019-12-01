package com.jeta.abeille.licensemgr;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import java.util.HashMap;

import com.jeta.foundation.componentmgr.ComponentMgr;

import com.jeta.foundation.interfaces.resources.ResourceLoader;
import com.jeta.foundation.interfaces.license.LicenseManager;
import com.jeta.foundation.interfaces.utils.Base64;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class stores a file in the user's home directory. The idea is to still enforce an expired license if the user erases the
 * home and install directory, and then re-installs a new download with a new
 * license key. This only works on windows.
 * 
 * @author Jeff Tassin
 */
public class CookieCheck {

	public static int checkCookie() {
		String path = "abeille.data";
		return checkCookie(path);
	}

	/**
	 * Looks for the presence of the serial number cookie in the home directory.
	 * If it is found, determine if it belongs to a previous license. This is to
	 * check if the user is simply downloading new license keys everytime the
	 * old license expires.
	 */
	public static int checkCookie(String path) {
		try {
			ResourceLoader loader = (ResourceLoader) ComponentMgr.lookup(ResourceLoader.COMPONENT_ID);
			HashMap lminfo = (HashMap) ComponentMgr.lookup(LicenseManager.LICENSE_INFO);
			SerialNumber sn = (SerialNumber) lminfo.get(LicenseManager.SERIAL_NUMBER_OBJECT);
			if (sn.isEvaluation()) {
				if (loader.exists(path)) {

					InputStream istream = loader.getInputStream(path);
					ObjectInputStream ois = new ObjectInputStream(istream);
					ArrayList list = (ArrayList) ois.readObject();
					ois.close();

					String datestr = (String) list.get(0);
					Base64 base64 = (Base64) ComponentMgr.lookup(Base64.COMPONENT_ID);
					datestr = new String(base64.decode(datestr));

					SimpleDateFormat format = new SimpleDateFormat(LicenseInfo.DATE_FORMAT);
					Date expdate = format.parse(datestr);
					Calendar c = Calendar.getInstance();
					c.setTime(expdate);

					Calendar current = Calendar.getInstance();
					if (current.after(c)) {
						return JETALicenseManager.EVALUATION_EXPIRED;
					} else {
						return JETALicenseManager.VALID_LICENSE;
					}
				} else {
					storeCookie(path);
					return JETALicenseManager.VALID_LICENSE;
				}
			} else {
				// if it is a valid license, then we simply return here
				return JETALicenseManager.VALID_LICENSE;
			}
		} catch (Exception e) {
			// ignore
			if (TSUtils.isDebug()) {
				e.printStackTrace();
			}
		}

		storeCookie(path);
		// just return a valid license on error
		return JETALicenseManager.VALID_LICENSE;
	}

	private static void storeCookie(String path) {
		// if we are here, then we need to store the cookie
		try {
			HashMap lminfo = (HashMap) ComponentMgr.lookup(LicenseManager.LICENSE_INFO);
			SerialNumber sn = (SerialNumber) lminfo.get(LicenseManager.SERIAL_NUMBER_OBJECT);
			Calendar c = (Calendar) lminfo.get(LicenseManager.EXPIRE_DATE);
			SimpleDateFormat format = new SimpleDateFormat(LicenseInfo.DATE_FORMAT);

			ArrayList list = new ArrayList();
			Base64 base64 = (Base64) ComponentMgr.lookup(Base64.COMPONENT_ID);
			list.add(base64.encode(format.format(c.getTime())));
			list.add(base64.encode(sn.toString()));

			ResourceLoader loader = (ResourceLoader) ComponentMgr.lookup(ResourceLoader.COMPONENT_ID);
			loader.createResource(path);
			OutputStream os = loader.getOutputStream(path);
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(list);
			oos.flush();
			oos.close();

			/** store the file in a readable form as well so we can debug */
			if (TSUtils.isDebug()) {
				list = new ArrayList();
				list.add(format.format(c.getTime()));
				list.add(sn.toString());
				String debugpath = path + ".debug";
				loader.createResource(debugpath);
				os = loader.getOutputStream(debugpath);
				oos = new ObjectOutputStream(os);
				oos.writeObject(list);
				oos.flush();
				oos.close();
			}
		} catch (Exception e) {
			// ignore
			if (TSUtils.isDebug()) {
				e.printStackTrace();
			}
		}
	}
}
