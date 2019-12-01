package com.jeta.abeille.database.security;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

/**
 * Defines a privilege in the database. This is used in GRANT/REVOKE statements
 * 
 * @author Jeff Tassin
 */
public class Privilege {
	/**
	 * The bitwise value for this privilege. We use mask operations to determine
	 * the grants for a given object
	 */
	private int m_mask;

	/** the name for this privilege */
	private String m_name;

	/** the collection of defined privileges */
	private static LinkedList m_definitions = new LinkedList();
	private static TreeMap m_lookup;

	public static final Privilege SELECT = new Privilege("SELECT", 0x1);
	public static final Privilege UPDATE = new Privilege("UPDATE", 0x2);
	public static final Privilege INSERT = new Privilege("INSERT", 0x4);
	public static final Privilege DELETE = new Privilege("DELETE", 0x8);
	public static final Privilege RULE = new Privilege("RULE", 0x10);
	public static final Privilege REFERENCES = new Privilege("REFERENCES", 0x20);
	public static final Privilege TRIGGER = new Privilege("TRIGGER", 0x40);
	public static final Privilege EXECUTE = new Privilege("EXECUTE", 0x80);
	public static final Privilege USAGE = new Privilege("USAGE", 0x100);
	public static final Privilege CREATE = new Privilege("CREATE", 0x200);
	public static final Privilege TEMPORARY = new Privilege("TEMPORARY", 0x400);
	public static final Privilege OWN = new Privilege("OWN", 0x800);
	public static final Privilege ALTER = new Privilege("ALTER", 0x1000);
	public static final Privilege DROP = new Privilege("DROP", 0x2000);
	public static final Privilege GRANT = new Privilege("GRANT", 0x4000);

	/**
	 * ctor
	 */
	protected Privilege(String name, int mask) {
		m_name = name;
		m_mask = mask;

		m_definitions.add(this);
		m_lookup = null;
	}

	/**
	 * @return the mask for this privilege
	 */
	public int getMask() {
		return m_mask;
	}

	/**
	 * @return the set of Privilege definitions
	 */
	public static Collection getDefinitions() {
		return m_definitions;
	}

	/**
	 * @return the name for this privilege
	 */
	public String getName() {
		return m_name;
	}

	public static Privilege lookup(String privName) {
		if (m_lookup == null) {
			m_lookup = new TreeMap(String.CASE_INSENSITIVE_ORDER);
			Iterator iter = m_definitions.iterator();
			while (iter.hasNext()) {
				Privilege p = (Privilege) iter.next();
				m_lookup.put(p.getName(), p);
			}
		}

		return (Privilege) m_lookup.get(privName);
	}

	public String toString() {
		return getName();
	}
}
