package com.jeta.abeille.database.model;

/**
 * This is an enumeration of common database types
 * 
 * @author Jeff Tassin
 */
public class TSDatabaseObject {
	private final String m_element;

	private TSDatabaseObject(String element) {
		this.m_element = element;
	}

	public String toString() {
		return m_element;
	}

	public static final TSDatabaseObject FOREIGN_KEY = new TSDatabaseObject("foreign_key");
	public static final TSDatabaseObject INDEX = new TSDatabaseObject("index");
	public static final TSDatabaseObject PRIMARY_KEY = new TSDatabaseObject("primary_key");
	public static final TSDatabaseObject SEQUENCE = new TSDatabaseObject("sequence");
	public static final TSDatabaseObject TABLE = new TSDatabaseObject("table");
	public static final TSDatabaseObject USER = new TSDatabaseObject("user");
}
