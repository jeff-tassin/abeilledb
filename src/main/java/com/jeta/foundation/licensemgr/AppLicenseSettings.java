/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.licensemgr;

public class AppLicenseSettings {
	public static final String COMPONENT_ID = "app.license.settings";
	/**
	 * The main jar file for the application
	 */
	private String m_app_jar;

	/**
	 * The name of the license file
	 */
	private String m_license_file;

	/**
	 * An array of valid product codes for the application
	 */
	private char[] m_product_codes;

	/**
	 * The default license name if no license file is found ( e.g.
	 * "Free Version" or "Evaluation" )
	 */
	private String m_default_license;

	/**
	 * Product version numbers
	 */
	private int m_major_version;
	private int m_minor_version;
	private int m_subminor_version;

	/**
	 * ctor
	 */
	public AppLicenseSettings(String jar, String licensefile, char[] prodcodes, String defLicense, int major_ver,
			int minor_ver, int subminor_ver) {
		m_app_jar = jar;
		m_license_file = licensefile;
		m_product_codes = prodcodes;
		m_default_license = defLicense;
		m_major_version = major_ver;
		m_minor_version = minor_ver;
		m_subminor_version = subminor_ver;
	}

	public String getApplicationJAR() {
		return m_app_jar;
	}

	public String getDefaultLicense() {
		return m_default_license;
	}

	public char getEvaluationProductCode() {
		return m_product_codes[0];
	}

	public char[] getProductCodes() {
		return m_product_codes;
	}

	/**
	 * @return the name of the license file
	 */
	public String getLicenseFile() {
		return m_license_file;
	}

	/**
	 * The major version number of the product.
	 */
	public int getMajorVersion() {
		return m_major_version;
	}

	/**
	 * The minor version number of the product
	 */
	public int getMinorVersion() {
		return m_minor_version;
	}

	/**
	 * The sub minor version number of the product.
	 */
	public int getSubMinorVersion() {
		return m_subminor_version;
	}

}
