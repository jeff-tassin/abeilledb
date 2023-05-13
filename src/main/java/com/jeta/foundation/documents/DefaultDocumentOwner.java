/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.documents;

import java.io.File;
import java.io.Serializable;

public class DefaultDocumentOwner implements DocumentOwner {
	private File m_file;
	private Object m_doc_model;
	private boolean m_modified;

	public DefaultDocumentOwner(File file, Object docModel, boolean modified) {
		m_file = file;
		m_doc_model = docModel;
		m_modified = modified;
	}

	public File getCurrentFile() {
		return m_file;
	}

	public Serializable getCurrentDocument() {
		return (Serializable) m_doc_model;
	}

	public boolean isDocumentModified() {
		return m_modified;
	}

	public void setDocumentModified(boolean modified) {
		m_modified = modified;
	}

}
