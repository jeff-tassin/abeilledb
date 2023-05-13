/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.documents;

import java.io.File;
import java.io.Serializable;

/**
 * Interface for those frames that support opening files
 */
public interface DocumentOwner {
	public File getCurrentFile();

	public Serializable getCurrentDocument();

	public boolean isDocumentModified();

	public void setDocumentModified(boolean bModified);
}
