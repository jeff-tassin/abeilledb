/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.componentmgr;

/**
 * This class defines component names that are used by the application
 * 
 * @author Jeff Tassin
 */
public class ComponentNames {
	public static final String APPLICATION_STATE_STORE = "jeta.interfaces.app.objectstore.appstate";
	public static final String APPLICATION_LOGGER = "jeta.application.logger";

	public static final String APPLICATION_HELP_FACTORY = "jeta.help.factory";

	/**
	 * this is used during debugging. If an exception is thrown by the
	 * application, we put it in the ComponentMgr so the main toolbar can
	 * display an indication
	 */
	public static final String ID_DEBUG_EXCEPTION_FLAG = "jeta.debug.exception.flag";
}
