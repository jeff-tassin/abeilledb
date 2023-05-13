/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.componentmgr;

import java.lang.reflect.Method;

import org.w3c.dom.Element;

import com.jeta.foundation.xml.XMLCommandHandler;
import com.jeta.foundation.xml.XMLUtils;

/**
 * This class is responsible for instantiating TSComponents from an XML config
 * file (typicall ts.components.xml ). This file has the following format: This
 * allows us to define singleton components that should be instantiated at
 * startup. It does not register the components with the componentmgr. The
 * components themselves are responsible for that.
 * 
 * @author Jeff Tassin
 */
public class ComponentLoader implements XMLCommandHandler {
	public ComponentLoader() {
	}

	public void run(Element cmdElement) {
		try {
			String componentname = XMLUtils.getChildText(cmdElement, "Name");
			// Class componentclass = Class.for Name( componentname );
			// TSComponent comp = (TSComponent)componentclass.newInstance();

			// set the register tag to true to automatically register with the
			// component mgr
			// String register = XMLUtils.getChildText( cmdElement, "Register"
			// );
			// if ( register != null && register.equalsIgnoreCase( "true" ) )
			// {
			// ComponentMgr.registerComponent( componentname, comp );
			// }

			// comp.startup();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
