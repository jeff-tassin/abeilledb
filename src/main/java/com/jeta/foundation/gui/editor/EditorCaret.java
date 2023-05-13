/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

import org.netbeans.editor.ext.ExtCaret;

/**
 * Specialization of ExtCaret that adds the capability to handle mark-set type
 * actions
 * 
 * @author Jeff Tassin
 */
public class EditorCaret extends ExtCaret implements TSCaret {
	private boolean m_bmarked;

	public EditorCaret() {

	}

	public boolean isMarked() {
		return m_bmarked;
	}

	public void setDot(int dotPos) {
		super.setDot(dotPos);
		setMarked(false);
	}

	public void setMarked(boolean bMarked) {
		m_bmarked = bMarked;
	}
}
