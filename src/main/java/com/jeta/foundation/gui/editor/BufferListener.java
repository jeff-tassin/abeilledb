/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

/**
 * This interface is used to notify listeners of buffer change events
 * 
 * @author Jeff Tassin
 */
public interface BufferListener {
	public void bufferChanged(BufferEvent evt);
}
