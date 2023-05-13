/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.interfaces.utils;

/**
 * Interface for doing Base64 encoding/decoding
 * 
 * @author Jeff Tassin
 */
public interface Base64 {
	public static final String COMPONENT_ID = "Base64.component";

	public String encode(String str);

	public String encode(byte[] data);

	public byte[] decode(String buff) throws java.io.IOException;
}
