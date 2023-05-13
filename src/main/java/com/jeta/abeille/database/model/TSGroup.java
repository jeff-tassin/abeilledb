package com.jeta.abeille.database.model;

import java.util.LinkedList;
import java.util.Collection;
import java.util.Iterator;

import com.jeta.foundation.i18n.I18N;

/**
 * This class defines a group in the database
 * 
 * @author Jeff Tassin
 */
public class TSGroup extends TSUser implements Cloneable {
	public static final String COMPONENT_ID = "jeta.TSGroup";

	private LinkedList m_users; // a list of users (String objects) that belong
								// to this group

	/**
	 * ctor
	 */
	public TSGroup(String name) {
		super(name);
		m_users = new LinkedList();
	}

	/**
	 * Adds the user name to this group
	 */
	public void addUser(String uname) {
		m_users.add(uname);
	}

	/**
	 * @return a copy of this object
	 */
	public Object clone() {
		TSGroup group = new TSGroup(getName());
		group.m_users = (LinkedList) m_users.clone();
		return group;
	}

	/**
	 * @param name
	 *            the name of the user to search for
	 * @return true if the group contains the given user name
	 */
	public boolean contains(String name) {
		Iterator iter = m_users.iterator();
		while (iter.hasNext()) {
			if (I18N.equals(name, (String) iter.next()))
				return true;
		}
		return false;
	}

	/**
	 * @return the users (as String objects) that belong to this group
	 */
	public Collection getUsers() {
		return m_users;
	}

	/**
	 * Removes the given user from this group
	 * 
	 * @param name
	 *            the name of the user to remove
	 */
	public void removeUser(String name) {
		Iterator iter = m_users.iterator();
		while (iter.hasNext()) {
			if (I18N.equals(name, (String) iter.next())) {
				iter.remove();
				break;
			}
		}
	}

}
