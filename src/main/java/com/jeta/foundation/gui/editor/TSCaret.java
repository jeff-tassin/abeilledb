/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

/**
 * Specialization of Caret that adds the capability to handle mark-set type
 * actions
 * 
 * @author Jeff Tassin
 */
public interface TSCaret {
	public boolean isMarked();

	public void setDot(int dotPos);

	public void setMarked(boolean bMarked);

}
