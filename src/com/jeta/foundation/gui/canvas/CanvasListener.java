/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.canvas;

/**
 * Interface for clients that wish to get events when changes occur to the
 * ModelCanvas. This includes changes to size/position of components on the
 * canvas.
 * 
 * @author Jeff Tassin
 */
public interface CanvasListener {
	public void canvasEvent(CanvasEvent evt);
}
