/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.filechooser;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;

/**
 * Components we add to view
 */
class FileComponent extends JLabel implements Comparable {
	private boolean m_bdir; // set to true if this component represents a
							// directory

	/**
    * 
    */
	FileComponent(String label, Font f, boolean bdirectory) {
		if (bdirectory) {
			setText(label + java.io.File.separatorChar);
			setForeground(Color.blue);
		} else {
			setText(label);
			setForeground(Color.black);
		}
		m_bdir = bdirectory;

		setFont(f);

	}

	public int compareTo(Object o) {
		if (o instanceof FileComponent) {
			FileComponent fobj = (FileComponent) o;
			return toString().compareTo(fobj.toString());
		} else
			return -1;
	}

	boolean isDirectory() {
		return m_bdir;
	}

	public String toString() {
		return getText();
	}
}
