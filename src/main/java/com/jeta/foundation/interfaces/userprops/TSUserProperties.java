/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.interfaces.userprops;

/**
 * This interface describes a service used to store/retrive user properties
 * 
 * @author Jeff Tassin
 */
public interface TSUserProperties {
	public static final String COMPONENT_ID = "jeta.TSUserProperties";

	/**
	 * Reads the named property for the given component from the user properties
	 * store. This method does not throw any exceptions, it is assumed the
	 * implementor will log the exception somewhere
	 * 
	 * @param keyName
	 *            the unique key name to get the property for
	 * @return the property string. Null is returned if the string is not found
	 */
	public String getProperty(String keyName);

	/**
	 * Reads the named property for the given key name from the user properties
	 * store. This method does not throw any exceptions, it is assumed the
	 * implementor will log the exception somewhere
	 * 
	 * @param keyName
	 *            the unique key name within that component to get the property
	 *            for
	 * @param defaultValue
	 *            if the key is not found, return this value
	 * @return the property string. Null is returned if the string is not found
	 */
	public String getProperty(String keyName, String defaultValue);

	/**
	 * Stores the named property for the given key. This method does not throw
	 * any exceptions, it is assumed the implementor will log the exception
	 * somewhere
	 */
	public void setProperty(String keyName, String value);

}
