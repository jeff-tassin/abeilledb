/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.componentmgr;

/**
 * This interface is used to receive events from the TSNotifier object. Objects
 * should implement this interface and then call registerInterest on the
 * TSNotifier.
 * 
 * @author Jeff Tassin
 */
public interface TSListener {
	public void tsNotify(TSEvent evt);
}