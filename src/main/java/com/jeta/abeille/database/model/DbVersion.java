package com.jeta.abeille.database.model;

import com.jeta.foundation.utils.TSUtils;

/**
 * This clas represents the version for a given database.
 * 
 * @author Jeff Tassin
 */
public class DbVersion {
	private String m_version;

	private int m_major;
	private int m_minor;
	private int m_sub;

	public DbVersion(String ver, int major, int minor, int sub) {
		m_version = ver;
		m_major = major;
		m_minor = minor;
		m_sub = sub;
	}

	public int getMajor() {
		return m_major;
	}

	public int getMinor() {
		return m_minor;
	}

	public int getSub() {
		return m_sub;
	}

	public String getVersion() {
		return m_version;
	}

	public void print() {
		if (TSUtils.isDebug()) {
			TSUtils.printMessage("----------- DbVersion ------- ");
			TSUtils.printMessage("version: " + getVersion());
			TSUtils.printMessage("major: " + getMajor());
			TSUtils.printMessage("minor: " + getMinor());
			TSUtils.printMessage("sub: " + getSub());
		}
	}
}
