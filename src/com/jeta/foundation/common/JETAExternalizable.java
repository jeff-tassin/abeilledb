/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.common;

/**
 * This is used for those classes that are Serializable and are meant to persist
 * their state. We define this interface mainly for obfuscation. Our obfuscator
 * allows you to ignore classes with certain interfaces. We don't want to
 * obfuscate those classes that are stored in the application store, so we tag
 * them with this class. If we did not do this, the obfuscator would ignore
 * classes the have base classes that are serialiable such as those derived from
 * JFrame, JInternalFrame, and JPanel - which is what we don't want.
 * 
 * @author Jeff Tassin
 */
public interface JETAExternalizable extends java.io.Externalizable {
	public static final long jetaid = 7052937874686143827L;
}
