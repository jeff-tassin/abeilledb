/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.licensemgr;

import java.io.*;
import java.lang.reflect.Method;

import com.jeta.foundation.componentmgr.ComponentMgr;
import com.jeta.foundation.interfaces.utils.Base64;

/**
 * This class is responsible for loading Licenses are of the form:
 * name#company#email#serialno#licensetype#expdate#signature where expdate is
 * formatted as: MM-DD-YYYY if any field contains a # char, then that char is
 * replaced by a @
 * 
 * @author Jeff Tassin
 */
public class LicenseReader {
	private static final int USERNAME_TOKEN = 0;
	private static final int COMPANY_TOKEN = 1;
	private static final int EMAIL_TOKEN = 2;
	private static final int LICENSETYPE_TOKEN = 3;
	private static final int SERIALNO_TOKEN = 4;
	private static final int EXPIRES_TOKEN = 5;
	private static final int SIGNATURE_TOKEN = 6;

	private LicenseInfo m_licenseinfo;

	/**
	 * ctor
	 */
	public LicenseReader(String filename) throws IOException, ClassNotFoundException {
		File f = new File(filename);
		FileInputStream fis = new FileInputStream(f);
		InputStreamReader ir = new InputStreamReader(fis);
		BufferedReader reader = new BufferedReader(ir);

		String result = reader.readLine();
		while (result != null) {
			int pos = result.indexOf(LicenseInfo.LICENSE_DELIMITER);
			if (pos >= 0) {
				parseLicenseCode(result);
				break;
			}
			result = reader.readLine();
		}

	}

	/**
	 * @return an implementation of a Base64 encoder/decoder
	 */
	public Base64 getBase64() {
		return (Base64) ComponentMgr.lookup(Base64.COMPONENT_ID);
	}

	LicenseInfo getLicenseInfo() {
		return m_licenseinfo;
	}

	/**
	 * Parse the license code
	 */
	private void parseLicenseCode(String lcode) throws ClassNotFoundException {
		String username = null;
		String company = null;
		String email = null;
		String licensetype = null;
		String serialno = null;
		String signature = null;
		String expires = null;

		String validate_str = null;

		int last_delim = 0;
		int token_counter = USERNAME_TOKEN;
		for (int index = 0; index < lcode.length(); index++) {
			char c = lcode.charAt(index);
			if (c == LicenseInfo.LICENSE_DELIMITER) {
				if (token_counter == USERNAME_TOKEN) {
					username = lcode.substring(last_delim, index);
				} else if (token_counter == COMPANY_TOKEN) {
					company = lcode.substring(last_delim, index);
				} else if (token_counter == EMAIL_TOKEN) {
					email = lcode.substring(last_delim, index);
				} else if (token_counter == LICENSETYPE_TOKEN) {
					licensetype = lcode.substring(last_delim, index);
					licensetype = licensetype.trim();
				} else if (token_counter == SERIALNO_TOKEN) {
					serialno = lcode.substring(last_delim, index);
				} else if (token_counter == EXPIRES_TOKEN) {
					expires = lcode.substring(last_delim, index);
					signature = lcode.substring(index + 1, lcode.length());
					validate_str = lcode.substring(0, index);
				}
				token_counter++;
				last_delim = index + 1;
			}
		}

		// now try to validate
		if (validate_str != null && signature != null) {
			// System.out.println( "LicenseReader   validate_str = " +
			// validate_str + "  sig = " + signature );
			if (verify(validate_str, signature)) {
				// System.out.println( "LicenseReader signature ok" );
				m_licenseinfo = new LicenseInfo(username, company, email, licensetype.charAt(0), serialno, expires);
			} else {
				// System.out.println(
				// "LicenseReader failed signature verification" );
			}
		} else {
			// System.out.println(
			// "LicenseReader unable to obtain signature from file" );
		}
	}

	/**
	 * Verifies the file against its signature. We use reflection here to
	 * provide some protection against code crackers.
	 * 
	 */
	private boolean verify(String vStr, String sigStr) throws ClassNotFoundException {
		try {
			/*
			 * create a Signature object to use for signing and verifying
			 */

			Class sigclass = Class.forName("java.security.Signature");
			Class[] mtypes = new Class[1];
			mtypes[0] = String.class;
			Method method = sigclass.getDeclaredMethod("getInstance", mtypes);
			Object[] params = new Object[1];
			params[0] = "SHA/DSA";
			Object sigobj = method.invoke(null, params);

			// Signature dsa = Signature.getInstance("SHA/DSA");

			Base64 decoder = getBase64();
			byte[] sig = decoder.decode(sigStr);

			/* Verify the signature */
			/* Initialize the Signature object for verification */
			Object pubkey = getPublicKey();

			mtypes = new Class[1];
			mtypes[0] = Class.forName("java.security.PublicKey");
			method = sigclass.getDeclaredMethod("initVerify", mtypes);
			params = new Object[1];
			params[0] = pubkey;
			method.invoke(sigobj, params);
			// dsa.initVerify(pub);

			mtypes = new Class[1];
			mtypes[0] = byte[].class;
			method = sigclass.getDeclaredMethod("update", mtypes);
			params = new Object[1];

			/* Update and verify the data */
			byte[] license_bytes = vStr.getBytes();
			params[0] = license_bytes;
			method.invoke(sigobj, params);

			// byte[] license_bytes = vStr.getBytes();
			// for( int index=0; index < license_bytes.length; index++ )
			// {
			// dsa.update( license_bytes[index] );
			// }

			mtypes = new Class[1];
			mtypes[0] = byte[].class;
			method = sigclass.getDeclaredMethod("verify", mtypes);
			params[0] = sig;
			Boolean bresult = (Boolean) method.invoke(sigobj, params);

			return bresult.booleanValue();
			// return dsa.verify(sig);

		} catch (Exception e) {
			// e.printStackTrace();
			return false;
		}
	}

	private Object getPublicKey() throws IOException, ClassNotFoundException {
		Base64 decoder = getBase64();
		byte[] sig = decoder.decode(JETA_PUBLIC_KEY);
		ByteArrayInputStream bis = new ByteArrayInputStream(sig);
		ObjectInputStream ois = new ObjectInputStream(bis);
		return ois.readObject();
	}

	/** public key for JETA. 12-09-2002 */
	private static final String JETA_PUBLIC_KEY = "rO0ABXNyACJzdW4uc2VjdXJpdHkucHJvdmlkZXIuRFNBUHVibGljS2V51nJ9DQQZ63sCAAFMAAF5"
			+ "dAAWTGphdmEvbWF0aC9CaWdJbnRlZ2VyO3hyABlzdW4uc2VjdXJpdHkueDUwOS5YNTA5S2V5taAd"
			+ "vmSacqYDAAVJAAp1bnVzZWRCaXRzTAAFYWxnaWR0AB9Mc3VuL3NlY3VyaXR5L3g1MDkvQWxnb3Jp"
			+ "dGhtSWQ7TAAMYml0U3RyaW5nS2V5dAAcTHN1bi9zZWN1cml0eS91dGlsL0JpdEFycmF5O1sACmVu"
			+ "Y29kZWRLZXl0AAJbQlsAA2tleXEAfgAFeHB39DCB8TCBqAYHKoZIzjgEATCBnAJBAPymgs6OEsq6"
			+ "Ju/M9xEOUm2weLBe3svNHrSiCPOuFheuAfNbkaR+bfY0E8XhLtCJm80TKs1Q2ZFRvcQ+5zdZLhcC"
			+ "FQCWLt3MNpy6jrsmDua2oSbZNG44xQJAZ4Rxsnqc9E7pGknFFH2xqaryRPBaQ01khpMdLRQnG541"
			+ "Awtx/XPaF5Bpsy4pNWMOHCBiNU0NogpsQW5QvnlMpANEAAJBAIJBJUZOWRWs+04FAMKZJaHYPdlB"
			+ "f51aZ78p9nKuecELNahvJwOLiRcSv/9CMIQEXOUCPHDBXoj38ukcw++VMKt4c3IAFGphdmEubWF0"
			+ "aC5CaWdJbnRlZ2VyjPyfH6k7+x0DAAZJAAhiaXRDb3VudEkACWJpdExlbmd0aEkAE2ZpcnN0Tm9u"
			+ "emVyb0J5dGVOdW1JAAxsb3dlc3RTZXRCaXRJAAZzaWdudW1bAAltYWduaXR1ZGVxAH4ABXhyABBq"
			+ "YXZhLmxhbmcuTnVtYmVyhqyVHQuU4IsCAAB4cP///////////////v////4AAAABdXIAAltCrPMX"
			+ "+AYIVOACAAB4cAAAAECCQSVGTlkVrPtOBQDCmSWh2D3ZQX+dWme/KfZyrnnBCzWobycDi4kXEr//"
			+ "QjCEBFzlAjxwwV6I9/LpHMPvlTCreA==";
}
