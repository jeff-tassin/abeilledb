/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.licensemgr;

import java.io.BufferedReader;
import java.io.File;

import java.net.URI;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

import java.util.Calendar;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.componentmgr.TSEvent;
import com.jeta.foundation.componentmgr.TSNotifier;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.gui.license.LicenseReporter;

import com.jeta.foundation.interfaces.license.LicenseManager;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This is the main license manager for the application. It attempts to load the
 * jeta.license file from the classpath and application path. If it is found,
 * the file is parsed and license information is extracted. The license
 * information, if valid, is put into a hashmap and registered with the
 * component manager. If any problems are encounted, a message is sent to the
 * TSNotifier. The LicenseReporter is a class that listens for messages from the
 * TSNotifier and pops up a message if an error occurs.
 * 
 * @author Jeff Tassin
 */
public class JETALicenseManager implements LicenseManager {

	/** flag indicating if the license is valid */
	private boolean m_valid = false;

	private LicenseReporter m_reporter;

	private SerialNumber m_serialnumber;

	/** the time (in millisecs)when the application was started */
	private long m_starttime;

	// CODES
	public static int VALID_LICENSE = 1;
	public static int EVALUATION_EXPIRED = -1;
	public static int INVALID_FORMAT = -2;
	public static int FILE_NOT_FOUND = -3;
	public static int SESSION_TIMEOUT = 2;
	public static int DELTA_GRACE_PERIOD_DAYS = 15;

	/**
	 * ctor
	 */
	public JETALicenseManager() {
		initialize();

		AppLicenseSettings als = (AppLicenseSettings) ComponentMgr.lookup(AppLicenseSettings.COMPONENT_ID);

		try {
			// String LICENSE_FILE = "jeta.license";
			String LICENSE_FILE = als.getLicenseFile();

			Calendar cal = Calendar.getInstance();
			m_starttime = cal.getTimeInMillis();

			Collection c = getPaths();
			Iterator iter = c.iterator();
			while (iter.hasNext()) {
				String path = (String) iter.next();
				String license_file = path + LICENSE_FILE;
				// System.out.println( "JETALicenseManager.path = " +
				// license_file );
				File f = new File(license_file);
				if (!f.isFile()) {
					license_file += ".txt";
					f = new File(license_file); // try .txt because Internet
												// Explorer will rename files it
												// downloads
				}

				if (f.isFile()) {
					// System.out.println( "***** found license file: " +
					// license_file );

					int result = checkLicenseFile(license_file);

					if (result == EVALUATION_EXPIRED) {
						// System.out.println(
						// "JETALicenseManager. evaluation expired" );
						break;
					} else if (result == INVALID_FORMAT) {
						// System.out.println(
						// "JETALicenseManager invalid format" );

						break;
					} else if (result == VALID_LICENSE) {
						// System.out.println(
						// "JETALicenseManager valid license" );

						HashMap lminfo = (HashMap) ComponentMgr.lookup(LicenseManager.LICENSE_INFO);
						m_serialnumber = (SerialNumber) lminfo.get(LicenseManager.SERIAL_NUMBER_OBJECT);
						m_valid = true;
						return;
					}
				}
			}

		} catch (Exception e) {
			// default to evaluation mode
		}

		// License not found, so enter into free mode
		// SerialNumber sn = new SerialNumber( Abeille.MAJOR_VERSION,
		// Abeille.MINOR_VERSION, Abeille.SUBMINOR_VERSION,
		// LicenseInfo.getEvalCode(), 1, (short)1 );

		assert (als != null);
		SerialNumber sn = new SerialNumber(als.getEvaluationProductCode(), als.getMajorVersion(),
				als.getMinorVersion(), als.getSubMinorVersion(), LicenseInfo.getEvalCode(), 1, (short) 1);

		// System.out.println( "JETALicenseManager.running evaluation version"
		// );
		HashMap lminfo = new HashMap();
		lminfo.put(LicenseManager.LICENSEE, als.getDefaultLicense());
		lminfo.put(LicenseManager.SERIAL_NUMBER_OBJECT, sn);
		lminfo.put(LicenseManager.SERIAL_NO, sn.toString());
		lminfo.put(LicenseManager.EXPIRE_DATE, Calendar.getInstance());
		lminfo.put(LicenseManager.EVAL_CODE, new Character(sn.getLicenseCode()));
		ComponentMgr.registerComponent(LicenseManager.LICENSE_INFO, lminfo);
		m_serialnumber = sn;
		m_valid = true;
	}

	/**
	 * Loads and validates the license file
	 */
	public static int checkLicenseFile(String path) {
		try {

			File file = new File(path);
			if (!file.isFile())
				return FILE_NOT_FOUND;

			LicenseReader reader = new LicenseReader(path);
			LicenseInfo info = reader.getLicenseInfo();
			if (validate(info)) {
				Calendar expcal = Calendar.getInstance();
				SerialNumber serialnumber = SerialNumberParser.parse2(info.getSerialNumber());

				if (serialnumber.isEvaluation()) {
					SimpleDateFormat format = new SimpleDateFormat(LicenseInfo.DATE_FORMAT);
					Date expdate = format.parse(info.getExpires());
					// System.out.println( "JETALicenseManager  exp date = " +
					// info.getExpires() );
					expcal.setTime(expdate);
					Calendar current = Calendar.getInstance();
					if (current.after(expcal)) {
						return EVALUATION_EXPIRED;
					}
				}

				HashMap lminfo = new HashMap();
				lminfo.put(LicenseManager.LICENSEE, info.getName());
				lminfo.put(LicenseManager.SERIAL_NUMBER_OBJECT, serialnumber);
				lminfo.put(LicenseManager.SERIAL_NO, serialnumber.toString());
				lminfo.put(LicenseManager.EXPIRE_DATE, expcal);
				lminfo.put(LicenseManager.EVAL_CODE, new Character(info.getLicenseType()));
				ComponentMgr.registerComponent(LicenseManager.LICENSE_INFO, lminfo);
				return VALID_LICENSE;
			} else {
				return INVALID_FORMAT;
			}
		} catch (Exception e) {
			// eat it
		}
		return INVALID_FORMAT;
	}

	/**
	 * Checks if we are currently running in evaluation mode. If we are and if
	 * the application has been running longer than the allowed session time
	 * limit, then we start posting nag messages.
	 */
	public boolean checkSessionTimeOut() {
		return true;
	}

	/**
	 * Fires an error to any listeners
	 */
	private void fireError(String msg) {
		// System.out.println( "JETALicenseManager: " + msg );
		TSNotifier tn = TSNotifier.getInstance();
		TSEvent evt = new TSEvent(null, LicenseManager.MSG_GROUP, LicenseManager.LICENSE_ERROR, msg);
		tn.fireEvent(evt);
	}

	/**
	 * Look for the location of the settings.properties file.
	 * 
	 */
	private String getAppDirectory() {
		String result = null;
		try {
			java.net.URL url = ClassLoader.getSystemResource("settings.properties");
			if (url != null) {
				URI uri = new URI(url.getPath());
				File f = new File(uri);
				String filepath = f.getPath();

				// System.out.println( "Settings.props path: " + filepath );
				int sppos = filepath.lastIndexOf("settings.properties");
				if (sppos >= 0) {
					result = filepath.substring(0, sppos);
					AppLicenseSettings als = (AppLicenseSettings) ComponentMgr.lookup(AppLicenseSettings.COMPONENT_ID);
					assert (als != null);
					String app_jar = als.getApplicationJAR();
					// app_jar = "jeta.jar"; for Abeille
					int jpos = result.indexOf(app_jar);
					if (jpos >= 0) {
						result = filepath.substring(0, jpos);
					}

					char lastchar = result.charAt(result.length() - 1);
					if (lastchar != '\\' && lastchar != '/')
						result = result + File.separatorChar;

					int fpos = result.indexOf("file:");
					if (fpos == 0) {
						result = result.substring(fpos + "file:".length(), result.length());
					}
				}
				// System.out.println( "Settings.props result: " + result );
			}
		} catch (Exception e) {
			// eat it, since we don't want stack traces in the license manager
		}
		return result;

	}

	/**
	 * Parsed the class path and gets all directories that are declared in the
	 * classpath. If a directory is found, it is extracted and a trailing
	 * backslash is added if not already there. It is then added to a collection
	 * of found paths and returned.
	 */
	private Collection getPaths() {
		LinkedList result = new LinkedList();
		String cp = System.getProperty("java.class.path");
		// System.out.println( "got class path: " + cp );

		String delimiter = ":;";
		if (TSUtils.isWindows()) {
			delimiter = ";";
			// System.out.println( "JETALicenseManager running windows" );
		}

		StringTokenizer st = new StringTokenizer(cp, delimiter);
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			File f = new File(token);
			if (f.isDirectory()) {
				char lastchar = token.charAt(token.length() - 1);
				if (lastchar != '\\' && lastchar != '/')
					result.add(token + File.separatorChar);
				else
					result.add(token);
			}
		}

		String apppath = getAppDirectory();
		if (apppath != null)
			result.add(apppath);
		return result;
	}

	/**
	 * Initializes the license reporter
	 */
	private void initialize() {
		TSNotifier tn = TSNotifier.getInstance();
		m_reporter = new LicenseReporter();
		tn.registerInterest(m_reporter, LicenseManager.MSG_GROUP);
	}

	/**
	 * NOTE: Never call this method when determining if the evaluation has
	 * expired. Call the SerialNumber class directly.
	 * 
	 * @return true if the license is for evaluation only.
	 */
	public boolean isEvaluation() {
		if (true)
			return false;

		if (m_serialnumber != null) {
			// special evaluations are those that still time out but are fully
			// functional
			if (m_serialnumber.isSpecialEvaluation())
				return false;
			else
				return m_serialnumber.isEvaluation();
		} else {
			return true;
		}
	}

	/**
	 * @return true if the license is for special evaluation only
	 */
	public boolean isSpecialEvaluation() {
		if (m_serialnumber != null) {
			// special evaluations are those that still time out but are fully
			// functional
			return m_serialnumber.isSpecialEvaluation();
		} else
			return false;
	}

	/**
	 * The grace period is a 2 week period where you can use the product without
	 * nag screens. After the 2 weeks is up, you have about 25 minutes to use
	 * the product, then nag screens will appear.
	 */
	private boolean isGracePeriod() {
		boolean bresult = true;
		if (isEvaluation()) {
			// this is temporary for the beta
			if (true)
				return true;

			HashMap lminfo = (HashMap) ComponentMgr.lookup(LicenseManager.LICENSE_INFO);
			if (lminfo != null) {
				Calendar c = (Calendar) lminfo.get(LicenseManager.EXPIRE_DATE);
				c = (Calendar) c.clone();
				c.add(Calendar.DATE, -DELTA_GRACE_PERIOD_DAYS);
				Calendar current = Calendar.getInstance();
				if (current.after(c))
					bresult = false;
			}
		}
		return bresult;
	}

	/**
	 * @return true if we are running in evaluation mode and the evaluation time
	 *         limit has expired for a given session (say 25 minutes). This
	 *         allows different application components to post a nag message to
	 *         the user.
	 */
	public boolean isSessionTimeOut() {
		if (isEvaluation()) {
			/**
			 * The grace period is a 2 week period where you can use the product
			 * without nag screens. After the 2 weeks is up, you have about 25
			 * minutes to use the product, then nag screens will appear.
			 */
			if (isGracePeriod()) {
				return false;
			} else {
				// Calendar c = Calendar.getInstance();
				// long currtime = c.getTimeInMillis();
				// long delta = currtime - m_starttime;
				// long minutes = delta/60000;
				// if ( minutes >= SESSION_TIMEOUT )
				// {
				// return true;
				// }
				// else
				// {
				return false;
				// }
			}
		} else {
			return false;
		}
	}

	/**
	 * @return true if the license is valid
	 */
	public boolean isValid() {
		return m_valid;
	}

	/**
	 * Posts a dialog message to the screen using a simple JOptionPane. We post
	 * messages this way rather than directly with JOptionPane to minimize
	 * crackers who look at stack traces.
	 */
	public void postMessage(String msg) {
		final String fmsg = msg;
		Runnable gui_update = new Runnable() {
			public void run() {
				TSNotifier tn = TSNotifier.getInstance();
				TSEvent tnevt = new TSEvent(null, LicenseManager.MSG_GROUP, LicenseManager.EVALUATION_MESSAGE, fmsg);
				tn.fireEvent(tnevt);
			}
		};
		javax.swing.SwingUtilities.invokeLater(gui_update);
	}

	/**
	 * Posts a dialog message to the screen using a simple JOptionPane. We post
	 * messages this way rather than directly with JOptionPane to minimize
	 * crackers who look at stack traces.
	 */
	public void postMessage(Object sender, String msg) {
		final String fmsg = msg;
		final Object window = sender;
		Runnable gui_update = new Runnable() {
			public void run() {
				TSNotifier tn = TSNotifier.getInstance();
				TSEvent tnevt = new TSEvent(window, LicenseManager.MSG_GROUP, LicenseManager.EVALUATION_MESSAGE, fmsg);
				tn.fireEvent(tnevt);
			}
		};
		javax.swing.SwingUtilities.invokeLater(gui_update);
	}

	private static boolean validate(LicenseInfo info) {
		if (info == null) {
			// System.out.println( "validate info = null" );
			return false;
		}

		boolean bresult = true;
		try {
			SimpleDateFormat format = new SimpleDateFormat(LicenseInfo.DATE_FORMAT);
			Date expdate = format.parse(info.getExpires());
		} catch (Exception e) {
			// e.printStackTrace();
			bresult = false;
		}
		return bresult;
	}

}
