/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.interfaces.app;

import java.io.IOException;

/**
 * This interface describes a service used to store/retrive objects based on a
 * key name. Top level application services can use this to store information
 * such as user properties between program invocations. The keyName is unique,
 * so only one object per keyname. Of course, a single object can be composed of
 * many subobjects. Serialization is used as the storage mechanism, so the
 * objects must be serializable.
 * 
 * @author Jeff Tassin
 */
public interface ObjectStore {
	/**
	 * Deletes the object from the store cache as well as on disk
	 */
	public void delete(String keyName) throws IOException;

	/**
	 * Commits the entire store to disk
	 */
	public void flush() throws IOException;

	/**
	 * Commits the specified object to disk
	 */
	public void flush(String keyName) throws IOException;

	/**
	 * loads the named object from the store. This object is also deserialized
	 * at this point since the store only keeps track of serialized data
	 * 
	 * @param keyName
	 *            the unique name of the lobject to retrieve
	 * @return the instantiated object from the store
	 * @throws IOException
	 *             if an error occurs during deserialization
	 */
	public Object load(String keyName) throws IOException;

	/**
	 * loads the named object from the store. This object is also deserialized
	 * at this point since the store only keeps track of serialized data
	 * 
	 * @param keyName
	 *            the unique name of the lobject to retrieve
	 * @param defaultValue
	 *            the value to return if the name is not found in the store
	 * @return the instantiated object from the store. The default value is
	 *         returned if the name is not found.
	 * @throws IOException
	 *             if an error occurs during deserialization
	 */
	public Object load(String keyName, Object defaultValue) throws IOException;

	/**
	 * stores the named object to the store. The object is serialized here so
	 * and exception can be thrown.
	 */
	public void store(String keyName, Object obj) throws IOException;

}
