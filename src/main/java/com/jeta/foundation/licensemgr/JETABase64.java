/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.licensemgr;

import java.lang.reflect.Method;
import com.jeta.foundation.interfaces.utils.Base64;

/**
 * Implementation of Base64 encoder
 * 
 * @author Jeff Tassin
 */
public class JETABase64 implements Base64 {
	private static final String ENCODER = "sun.misc.BASE64Encoder";
	private static final String DECODER = "sun.misc.BASE64Decoder";

	public String encode(String str) {
		return encode(str.getBytes());
	}

	public String encode(byte[] data) {
		try {
			Class c = Class.forName(ENCODER);
			Object encoder = c.newInstance();
			Class[] paramtypes = new Class[1];
			paramtypes[0] = byte[].class;
			Method m = c.getMethod("encode", paramtypes);
			Object[] params = new Object[1];
			params[0] = data;
			String result = (String) m.invoke(encoder, params);
			return result;
			// return b64enc.encode( data );
		} catch (Exception e) {
			System.out.println("Unable to launch sun.misc.BASE64Encoder");
		}
		return null;
	}

	public byte[] decode(String buff) throws java.io.IOException {
		try {
			Class c = Class.forName(DECODER);
			Object decoder = c.newInstance();
			Class[] paramtypes = new Class[1];
			paramtypes[0] = String.class;
			Method m = c.getMethod("decodeBuffer", paramtypes);
			Object[] params = new Object[1];
			params[0] = buff;
			byte[] result = (byte[]) m.invoke(decoder, params);
			return result;
			// return b64dec.decodeBuffer( buff );
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unable to launch sun.misc.BASE64Decoder");
		}
		return null;
	}

}
