/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.filechooser;

import java.io.File;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

import javax.swing.filechooser.FileFilter;

public class TSFileFilter extends FileFilter {
	/**
	 * A list of file extensions for this filter
	 */
	private LinkedList m_exts = new LinkedList();

	/**
	 * The description for this filter
	 */
	private String m_description;

	/**
	 * A comma separated list of file extensions without the . e.g.
	 * "txt,gif,jpeg"
	 */
	public TSFileFilter(String ext_tokens, String desc) {
		try {
			StringTokenizer st = new StringTokenizer(ext_tokens, ",");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				m_exts.add(token);
			}

			m_description = desc;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}

	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}

		if (m_exts.size() == 0)
			return true;

		String extension = getExtension(f);
		if (extension != null) {
			Iterator iter = m_exts.iterator();
			while (iter.hasNext()) {
				if (extension.equals(iter.next()))
					return true;
			}
		}
		return false;
	}

	/**
	 * The description for this filter
	 */
	public String getDescription() {
		return m_description;
	}
}
