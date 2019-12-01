/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.interfaces.license;

public interface LicenseManager {
	public static final String COMPONENT_ID = "JETA.LicenseManager";
	public static final String MSG_GROUP = "JETA.LM.group";
	public static final String LICENSE_ERROR = "JETA.LM.Error";
	public static final String LICENSE_INFO = "JETA.LM.Info";
	public static final String LICENSEE = "JETA.LM.Licensee";
	public static final String SERIAL_NO = "JETA.LM.serialno";
	public static final String SERIAL_NUMBER_OBJECT = "JETA.LM.serialno.obj";
	public static final String EXPIRE_DATE = "JETA.LM.expiredate";
	public static final String EVAL_CODE = "JETA.LM.evalcode";
	public static final String LICENSE_TIMEOUT_MESSAGE = "JETA.LM.timeoutmsg";
	public static final String EVALUATION_MESSAGE = "JETA.LM.evalmsg";
	public static final String LICENSE_ERROR_VERBOSE = "JETA.LM.verbose";

	public static final String OPEN_WINDOW_LIMIT_MESSAGE = "JETA.LM.open.window.limit";
	public static final String PRODUCT_CODES = "JETA.LM.productcodes";
	public static final String APPLICATION_JAR = "JETA.LM.application.jar";

	/**
	 * Checks if we are currently running in evaluation mode. If we are and if
	 * the application has been running longer than the allowed session time
	 * limit, then we start posting nag messages.
	 */
	public boolean checkSessionTimeOut();

	/**
	 * @return true if the license has expired
	 */
	public boolean isValid();

	/**
	 * @return true if the license is for evaluation only
	 */
	public boolean isEvaluation();

	/**
	 * @return true if the license is for special evaluation only
	 */
	public boolean isSpecialEvaluation();

	/**
	 * @return true if we are running in evaluation mode and the evaluation time
	 *         limit has expired for a given session (say 20 minutes). This
	 *         allows different application components to post a nag message to
	 *         the user.
	 */
	public boolean isSessionTimeOut();

	/**
	 * Posts a dialog message to the screen using a simple JOptionPane. We post
	 * messages this way rather than directly with JOptionPane to minimize
	 * crackers who look at stack traces.
	 */
	public void postMessage(String msg);

	/**
	 * Posts a dialog message to the screen using a simple JOptionPane. We post
	 * messages this way rather than directly with JOptionPane to minimize
	 * crackers who look at stack traces.
	 */
	public void postMessage(Object windowOwner, String msg);

}
