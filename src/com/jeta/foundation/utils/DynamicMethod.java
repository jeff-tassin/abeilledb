/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.utils;

import java.util.Iterator;
import java.lang.reflect.*;

public class DynamicMethod {
	public static void invoke(Iterator iter, String methodName, Object param) {
		while (iter.hasNext())
			DynamicMethod.invoke(iter.next(), methodName, param);
	}

	public static Object invoke(Object obj, String methodName, Object param) {
		if (obj == null)
			return null;

		Class c = obj.getClass();
		try {
			Class[] paramtypes = new Class[1];
			paramtypes[0] = param.getClass();
			Method m = c.getDeclaredMethod(methodName, paramtypes);
			if (Modifier.isPublic(m.getModifiers())) {
				Object[] params = new Object[1];
				params[0] = param;
				return m.invoke(obj, params);
			} else
				System.out.println("DynamicMethod invoke error.  method not public");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}