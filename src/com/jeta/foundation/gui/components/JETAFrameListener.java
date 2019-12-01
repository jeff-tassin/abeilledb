/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.components;

public interface JETAFrameListener {
	public void jetaFrameActivated(JETAFrameEvent e);

	public void jetaFrameClosing(JETAFrameEvent e);

	public void jetaFrameClosed(JETAFrameEvent e);

	public void jetaFrameDeactivated(JETAFrameEvent e);

	public void jetaFrameDeiconified(JETAFrameEvent e);

	public void jetaFrameIconified(JETAFrameEvent e);

	public void jetaFrameOpened(JETAFrameEvent e);
}
