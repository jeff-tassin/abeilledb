/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.app;

import java.io.*;
import java.util.*;

import com.jeta.foundation.interfaces.userprops.*;
import com.jeta.foundation.interfaces.resources.*;
import com.jeta.foundation.componentmgr.*;

/**
 * @author Jeff Tassin
 */
public class UserPropertiesStore implements TSUserProperties, TSComponent {
	private static final String PROPS_RESOURCE_NAME = "userdata.properties"; // the
																				// name
																				// of
																				// the
																				// 'file'
																				// we
																				// store
																				// to
	private Properties m_props = new Properties(); // the set of objects
	/**
	 * Flag that indicates if store should be read only. (Needed for webstart)
	 */
	private boolean m_readonly = false;

	public UserPropertiesStore() {

	}

	/**
	 * Reads the named property for the given keyname from the user properties
	 * store. This method does not throw any exceptions, it is assumed the
	 * implementor will log the exception somewhere
	 * 
	 * @param keyName
	 *            the unique key name to get the property for
	 * @return the property string. Null is returned if the string is not found
	 */
	public String getProperty(String keyName) {
		return m_props.getProperty(keyName);
	}

	/**
	 * Reads the named property for the given key from the user properties
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
	public String getProperty(String keyName, String defaultValue) {
		return m_props.getProperty(keyName, defaultValue);
	}

	/**
	 * TSComponent implementation. Get's called by ComponentMgr at startup
	 * 
	 * @see userdata.properties
	 * @see com.jeta.foundation.componentmgr
	 */
	public void startup() {
		try {
			ComponentMgr.registerComponent(TSUserProperties.COMPONENT_ID, this);

			if (m_readonly) {
				m_props = new Properties();
			} else {
				ResourceLoader loader = (ResourceLoader) ComponentMgr.lookup(ResourceLoader.COMPONENT_ID);
				InputStream reader = loader.getInputStream(PROPS_RESOURCE_NAME);
				m_props.load(reader);
				reader.close();
			}
		} catch (Exception e) {
			m_props = new Properties();
		}
	}

	/**
	 * TSComponent implementation. Get's called by ComponentMgr at shutdown
	 * 
	 * @see userdata.properties
	 * @see com.jeta.foundation.componentmgr
	 */
	public void shutdown() {
		try {
			if (!m_readonly) {
				ResourceLoader loader = (ResourceLoader) ComponentMgr.lookup(ResourceLoader.COMPONENT_ID);
				OutputStream writer = loader.getOutputStream(PROPS_RESOURCE_NAME);
				m_props.store(writer, null);
				writer.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Stores the named property for the given key to the user properties store.
	 * This method does not throw any exceptions, it is assumed the implementor
	 * will log the exception somewhere
	 */
	public void setProperty(String keyName, String value) {
		m_props.setProperty(keyName, value);
	}

	public void setReadOnly(boolean readonly) {
		m_readonly = readonly;
	}
}
