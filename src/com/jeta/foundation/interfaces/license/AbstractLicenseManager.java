/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.interfaces.license;

/**
 * This is a dummy class to help prevent reverse engineering. Do no use unless
 * you are creating a class that is for the purpose of obfuscation.
 * 
 * @author Jeff Tassin
 */
public class AbstractLicenseManager implements LicenseManager {
	/**
	 * Checks if we are currently running in evaluation mode. If we are and if
	 * the application has been running longer than the allowed session time
	 * limit, then we start posting nag messages.
	 */
	public boolean checkSessionTimeOut() {
		return false;
	}

	public boolean isBasic() {
		return true;
	}

	/**
	 * @return true if the license has expired
	 */
	public boolean isValid() {
		return false;
	}

	/**
	 * @return true if the license is for evaluation only
	 */
	public boolean isEvaluation() {
		return true;
	}

	/**
	 * @return true if the license is for special evaluation only
	 */
	public boolean isSpecialEvaluation() {
		return false;
	}

	/**
	 * @return true if we are running in evaluation mode and the evaluation time
	 *         limit has expired for a given session (say 20 minutes). This
	 *         allows different application components to post a nag message to
	 *         the user.
	 */
	public boolean isSessionTimeOut() {
		return true;
	}

	/**
	 * Posts a dialog message to the screen using a simple JOptionPane. We post
	 * messages this way rather than directly with JOptionPane to minimize
	 * crackers who look at stack traces.
	 */
	public void postMessage(String msg) {

	}

	/**
	 * Posts a dialog message to the screen using a simple JOptionPane. We post
	 * messages this way rather than directly with JOptionPane to minimize
	 * crackers who look at stack traces.
	 */
	public void postMessage(Object sender, String msg) {

	}
}
