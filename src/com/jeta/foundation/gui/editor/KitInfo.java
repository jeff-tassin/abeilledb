/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

import java.util.List;

import java.net.URL;
import javax.swing.Icon;

import com.jeta.foundation.i18n.I18N;

/**
 * This class stores information for an editor kit
 * 
 * @author Jeff Tassin
 */
public class KitInfo {
	private String m_mimeType; // type of documents
	private String[] m_extensions; // file extensions used for documents
	private URL m_template;
	private Icon m_icon; // icon used to show documents in app
	private Class m_kitclass; // the class of the editor kit (derived from
								// TSKit)
	private String m_description; // description of this kit
	private ClassLoader m_loader; // class loader for kit class

	public KitInfo(String mimeType, Class kitClass) {
		m_mimeType = mimeType;
		m_kitclass = kitClass;
		m_loader = m_kitclass.getClassLoader();
		m_extensions = new String[0];
	}

	public KitInfo(String mimeType, Class kitClass, String ext) {
		m_mimeType = mimeType;
		m_kitclass = kitClass;
		m_loader = m_kitclass.getClassLoader();
		m_extensions = new String[1];
		m_extensions[0] = ext;
	}

	public KitInfo(String mimeType, List exts, URL template, Icon icon, String description, Class kitClass,
			ClassLoader loader) {
		m_mimeType = mimeType;
		m_extensions = (String[]) exts.toArray(new String[0]);
		m_template = template;
		m_icon = icon;
		m_description = description;
		m_kitclass = kitClass;
		m_loader = loader;
	}

	/**
	 * @param ext
	 *            the name of a file extension to test for ( include the . )
	 * @return true if this kit is responsible for handling files with the given
	 *         extension
	 */
	public boolean containsExtension(String ext) {
		for (int index = 0; index < m_extensions.length; index++) {
			if (I18N.equals(ext, m_extensions[index]))
				return true;
		}
		return false;
	}

	public ClassLoader getClassLoader() {
		return m_loader;
	}

	public String getDescription() {
		return m_description;
	}

	public Icon getIcon() {
		return m_icon;
	}

	public Class getKitClass() {
		return m_kitclass;
	}

	public String getMimeType() {
		return m_mimeType;
	}

	public URL getTemplate() {
		return m_template;
	}

	/**
	 * Sets the icon to display in the application when using this type of kit
	 */
	public void setIcon(Icon icon) {
		m_icon = icon;
	}

}
