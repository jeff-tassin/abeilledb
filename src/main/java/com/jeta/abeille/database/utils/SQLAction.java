package com.jeta.abeille.database.utils;

/**
 * This class defines the 3 common actions to modify data in a database We
 * define these actions using the standard 'enum' pattern because they are used
 * frequently in the application
 * 
 * @author Jeff Tassin
 */
public class SQLAction {
	private final String m_name;

	public static final SQLAction SELECT = new SQLAction("select");
	public static final SQLAction UPDATE = new SQLAction("update");
	public static final SQLAction INSERT = new SQLAction("insert");
	public static final SQLAction DELETE = new SQLAction("delete");

	/**
	 * Private ctor per the enum pattern
	 */
	private SQLAction(String name) {
		m_name = name;
	}

	public String toString() {
		return m_name;
	}
}
