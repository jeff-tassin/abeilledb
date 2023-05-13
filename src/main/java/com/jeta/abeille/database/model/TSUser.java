package com.jeta.abeille.database.model;

/**
 * This class defines a user in the database
 * 
 * @author Jeff Tassin
 */
public class TSUser implements Cloneable {
	public static final String COMPONENT_ID = "jeta.TSUser";

	private String m_name; // the name of the user

	/**
	 * ctor
	 */
	public TSUser(String name) {
		m_name = name;
	}

	/**
	 * @return a copy of this object
	 */
	public Object clone() {
		TSUser user = new TSUser(m_name);
		return user;
	}

	/**
	 * @return the name of this user
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * Sets the name
	 */
	public void setName(String name) {
		m_name = name;
	}
}
