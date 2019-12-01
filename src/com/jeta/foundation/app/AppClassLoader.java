/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.app;

import java.io.*;
import java.util.Enumeration;
import java.net.URL;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.interfaces.resources.ResourceLoader;

public class AppClassLoader extends ClassLoader {
	String m_altPath;

	public AppClassLoader() {

	}

	public AppClassLoader(String path) {
		m_altPath = path;
	}

	protected Class findClass(String name) throws ClassNotFoundException {

		// System.out.println( "findClass  " + name );
		return super.findClass(name);
	}

	protected String findLibrary(String libname) {

		// System.out.println( "findLibrary " + libname );
		return super.findLibrary(libname);
	}

	protected URL findResource(String name) {

		// System.out.println( "findResource " + name );
		return super.findResource(name);
	}

	protected Enumeration findResources(String name) throws IOException {

		// System.out.println( "findResources " + name );
		return super.findResources(name);
	}

	protected Package getPackage(String name) {

		// System.out.println( "getPackage " + name );
		return super.getPackage(name);
	}

	public URL getResource(String name) {
		if (m_altPath == null) {
			return super.getResource(name);
		} else {
			String urlstring = "file://localhost/" + m_altPath + "/" + name;
			try {
				return new URL(urlstring);
			} catch (Exception e) {
				return super.getResource(name);
			}
		}
	}

	public InputStream getResourceAsStream(String name) {
		try {
			if (m_altPath == null) {
				ResourceLoader loader = (ResourceLoader) ComponentMgr.lookup(ResourceLoader.COMPONENT_ID);
				return loader.getInputStream(name);
			} else {
				URL url = getResource(name);
				return url.openStream();
			}

		} catch (Exception e) {
			return super.getResourceAsStream(name);
		}
	}

	public Class loadClass(String name) throws ClassNotFoundException {

		// System.out.println( "loadClass " + name );
		return super.loadClass(name);
	}

	protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {

		// System.out.println( "loadClass resolve" + name );
		return super.loadClass(name, resolve);
	}

}
