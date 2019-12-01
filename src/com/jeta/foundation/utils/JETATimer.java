/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.utils;

import java.util.HashMap;

/**
 * Simple utility class for profiling code
 * 
 * @author Jeff Tassin
 */
public class JETATimer {
		   
	private long   m_time;
	
	public JETATimer() {
		m_time = System.currentTimeMillis();
	}
	
	/**
	 * Returns the elapsed time in seconds since the last call to this method (or the ctor)
	 * @return the time in seconds.
	 */
	public float getElapsedTime() {
		long endtm = System.currentTimeMillis();
		float delta = (float)(endtm-m_time)/1000.0f;
		m_time = System.currentTimeMillis();
		return delta;
	}
}

