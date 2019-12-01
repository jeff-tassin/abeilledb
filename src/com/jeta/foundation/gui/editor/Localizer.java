/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

import java.util.ResourceBundle;

import org.netbeans.editor.*;
import org.netbeans.editor.ext.*;

/**
 * 
 * @author Jeff Tassin
 */

class Localizer implements LocaleSupport.Localizer {
	ResourceBundle bundle;

	public Localizer(String bundleName) {
		bundle = ResourceBundle.getBundle(bundleName);
	}

	public String getString(String key) {
		return bundle.getString(key);
	}

}
