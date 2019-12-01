/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.xml;

/**
 * The interface for objects that handle XML commands from the application
 * configuration file.
 * 
 * @author Jeff Tassin
 */
public interface XMLCommandHandler {
	public void run(org.w3c.dom.Element xmlElement);
}
