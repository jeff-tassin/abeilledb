/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.filechooser;

import java.awt.Component;
import javax.swing.JFileChooser;

/**
 * This class is used to configure the file chooser dialog. Parameters such as
 * initial directory, file filters, etc are set in this config class.
 * 
 * @author Jeff Tassin
 */
public class FileChooserConfig {

	/**
	 * This is used when loading files of a given type. It is not the file
	 * filter. Rather is is used to store the directory last accessed for this
	 * type. For example, if the type is an ".img", we want to store the
	 * directory last accessed for images. However, if the file is a ".txt", we
	 * don't want to go to the last images directory but rather the last .txt
	 * directory.
	 */
	private String m_file_type;

	/**
	 * The mode for the JFileChooser (e.g. FILES_ONLY )
	 */
	private int m_mode = JFileChooser.FILES_ONLY;

	/**
	 * The file filters.
	 */
	private TSFileFilter[] m_file_filter;

	/**
	 * The directory to open in the file chooser.
	 */
	private String m_initial_directory;

	private Component m_parent = null;

	/**
	 * ctor
	 */
	public FileChooserConfig(String initialDirectory, String type, int mode, TSFileFilter fileFilter) {
		m_initial_directory = initialDirectory;
		m_file_type = type;
		m_mode = mode;
		if (fileFilter != null)
			m_file_filter = new TSFileFilter[] { fileFilter };
	}

	/**
	 * ctor
	 */
	public FileChooserConfig(String type, int mode, TSFileFilter fileFilter) {
		m_file_type = type;
		m_mode = mode;
		if (fileFilter != null)
			m_file_filter = new TSFileFilter[] { fileFilter };
	}

	/**
	 * ctor
	 */
	public FileChooserConfig(String type, TSFileFilter fileFilter) {
		m_file_type = type;
		if (fileFilter != null)
			m_file_filter = new TSFileFilter[] { fileFilter };
	}

	/**
	 * ctor
	 */
	public FileChooserConfig(String initialDir, String type, TSFileFilter fileFilter) {
		m_initial_directory = initialDir;
		m_file_type = type;
		if (fileFilter != null)
			m_file_filter = new TSFileFilter[] { fileFilter };
	}

	/**
	 * ctor
	 */
	public FileChooserConfig(String type, int mode, TSFileFilter[] fileFilters) {
		m_file_type = type;
		m_mode = mode;
		m_file_filter = fileFilters;
	}

	/**
	 * ctor
	 */
	public FileChooserConfig(String initialDirectory, String type, int mode, TSFileFilter[] fileFilters) {
		m_initial_directory = initialDirectory;
		m_file_type = type;
		m_mode = mode;
		m_file_filter = fileFilters;
	}

	public String getFileType() {
		return m_file_type;
	}

	/**
	 * The mode for the JFileChooser (e.g. FILES_ONLY )
	 */
	public int getMode() {
		return m_mode;
	}

	/**
	 * The file filters.
	 */
	public TSFileFilter[] getFileFilters() {
		return m_file_filter;
	}

	/**
	 * The directory to open in the file chooser.
	 */
	public String getInitialDirectory() {
		return m_initial_directory;
	}

	public void setInitialDirectory(String dir) {
		m_initial_directory = dir;
	}

	public Component getParentComponent() {
		return m_parent;
	}

	public void setParentComponent(Component comp) {
		m_parent = comp;
	}
}
