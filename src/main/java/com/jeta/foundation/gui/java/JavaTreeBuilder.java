/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.java;

import java.lang.reflect.Array;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * This class builds a tree node for a given java object
 */
public class JavaTreeBuilder {

	public static void buildNode(DefaultMutableTreeNode parent, Object obj, Class c) {
		if (obj == null)
			return;

		if (c == String.class) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode();
			StringWrapper wrapper = new StringWrapper(null, obj);
			node.setUserObject(wrapper);
			parent.add(node);
			return;
		}

		java.lang.reflect.Field[] fields = c.getDeclaredFields();

		for (int index = 0; index < fields.length; index++) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode();

			JavaWrapper wrapper = null;
			java.lang.reflect.Field f = fields[index];
			f.setAccessible(true);
			Class ftype = f.getType();

			if (ftype.isPrimitive()) {
				wrapper = new AttributeWrapper(f, obj);
			} else if (ftype == String.class) {
				wrapper = new StringWrapper(f, obj);
			} else if (ftype.isArray()) {
				wrapper = new ArrayWrapper(f, obj);
			} else if (ftype != Class.class && !ftype.isInterface()) {
				wrapper = new ObjectWrapper(f, obj);
			} else {

				System.out.println("JavaTreeBuilder.invalid ftype: " + ftype);
			}

			node.setUserObject(wrapper);

			if (wrapper != null && wrapper.hasChildren())
				node.add(new DefaultMutableTreeNode());

			if (node.getUserObject() != null)
				parent.add(node);
		}

	}
}
