/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.java;

import javax.swing.Icon;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * This interface is used to describe some attributes for a Java primitive or
 * object used in the ObjectView and ObjectModel
 * 
 * @author Jeff Tassin
 */
public interface JavaWrapper {
	/**
	 * invokes a wrapper specific action to edit the wrapper value. For most,
	 * this invokes a dialog
	 */
	public void edit();

	/** @return the icon representing this type */
	public Icon getIcon();

	/** @return the name for this wrapper */
	public String getName();

	/** @return the string value that will be displayed in the tree */
	public String getDisplayValue();

	/** @return the actual object managed by the wrapper */
	public Object getObject();

	/** @return the type name for this type */
	public String getTypeName();

	/** @return true if this wrapper currently has children */
	public boolean hasChildren();

	/**
	 * @return true if this wrapper has children that need to be loaded by the
	 *         wrapper This allows the tree to show the expand node
	 */
	public boolean isLoaded();

	/** @return true if this wrapper is readonly. No in place editing */
	public boolean isReadOnly();

	/**
	 * Loads the child objects for this wrapper
	 */
	public void loadChildren(DefaultMutableTreeNode parent);

	/**
	 * Sets the value for this object
	 */
	public void setValue(Object ojb) throws IllegalAccessException;

}
