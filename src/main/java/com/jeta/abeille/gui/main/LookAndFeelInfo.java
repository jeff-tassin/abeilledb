package com.jeta.abeille.gui.main;

public class LookAndFeelInfo {
	/**
	 * The class name of the look and feel.
	 */
	private String m_classname;

	/**
	 * The menu label
	 */
	private String m_name;

	public LookAndFeelInfo(String className, String name) {
		m_classname = className;
		m_name = name;
	}

	public String getName() {
		return m_name;
	}

	public String getClassName() {
		return m_classname;
	}
}
