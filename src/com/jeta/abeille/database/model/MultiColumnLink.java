package com.jeta.abeille.database.model;

import java.io.ObjectStreamException;
import java.io.IOException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Iterator;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * This class defines a referential integrity link between two tables ( foreign
 * key table 1 -> primary key table 2 )
 * 
 * @author Jeff Tassin
 */
public class MultiColumnLink extends Link implements JETAExternalizable {
	static final long serialVersionUID = -8313685270811262021L;

	public static int VERSION = 1;

	private String m_sourceKeyName;
	private String m_destinationKeyName;
	private DbForeignKey m_foreignkey;

	/**
	 * This is the set of links that make up this foreign key This really makes
	 * sense when a foreign key is composed of two or more columns.
	 */
	private LinkedList m_links = new LinkedList();

	/**
	 * ctor only for serialization
	 */
	public MultiColumnLink() {

	}

	/**
	 * ctor
	 */
	public MultiColumnLink(DbForeignKey fKey) {
		this(fKey, false);
	}

	/**
	 * ctor
	 */
	public MultiColumnLink(DbForeignKey fKey, boolean transposed) {
		if (transposed)
			initializeTransposed(fKey);
		else
			initialize(fKey);
	}

	/**
	 * Initializes the link
	 */
	private void initialize(DbForeignKey fKey) {
		m_foreignkey = fKey;

		m_sourceKeyName = m_foreignkey.getReferenceKeyName();
		m_destinationKeyName = m_foreignkey.getName();

		TableId srcid = m_foreignkey.getReferenceTableId();
		TableId destid = m_foreignkey.getLocalTableId();

		setSourceTableId(srcid);
		setDestinationTableId(destid);

		DbKey destpk = m_foreignkey.getLocalKey();
		Collection c = destpk.getColumns();
		Iterator iter = c.iterator();
		StringBuffer sid = new StringBuffer();
		while (iter.hasNext()) {
			String destcolname = (String) iter.next();
			String sourcecolname = m_foreignkey.getAssignedPrimaryKeyColumnName(destcolname);

			Link link = new Link(srcid, sourcecolname, destid, destcolname);
			if (m_links.size() == 0) {
				// set our link attributes to be the same as the first column in
				// the key
				setSourceColumn(sourcecolname);
				setDestinationColumn(destcolname);
			}
			sid.append(link.getStringId());
			m_links.add(link);
		}
		setStringId(sid.toString());
	}

	/**
	 * Initializes the link but transposes the source->destination attributes.
	 * 
	 */
	private void initializeTransposed(DbForeignKey fKey) {
		m_foreignkey = fKey;

		m_sourceKeyName = m_foreignkey.getName();
		m_destinationKeyName = m_foreignkey.getReferenceKeyName();

		TableId srcid = m_foreignkey.getReferenceTableId();
		TableId destid = m_foreignkey.getLocalTableId();

		setSourceTableId(destid);
		setDestinationTableId(srcid);

		DbKey destpk = m_foreignkey.getLocalKey();
		Collection c = destpk.getColumns();
		Iterator iter = c.iterator();
		StringBuffer sid = new StringBuffer();
		while (iter.hasNext()) {
			String destcolname = (String) iter.next();
			String sourcecolname = m_foreignkey.getAssignedPrimaryKeyColumnName(destcolname);

			Link link = new Link(destid, destcolname, srcid, sourcecolname);

			if (m_links.size() == 0) {
				// set our link attributes to be the same as the first column in
				// the key
				setSourceColumn(destcolname);
				setDestinationColumn(sourcecolname);
			}
			sid.append(link.getStringId());
			m_links.add(link);
		}
		setStringId(sid.toString());
	}

	/**
	 * Override Object implementation
	 */
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof MultiColumnLink) {
			MultiColumnLink fklink = (MultiColumnLink) obj;
			return m_foreignkey.equals(fklink.m_foreignkey);
		} else
			return false;
	}

	/**
	 * @return the foreign (destination) key for this link
	 */
	public DbForeignKey getForeignKey() {
		return m_foreignkey;
	}

	public String getDestinationKeyName() {
		return m_destinationKeyName;
	}

	/**
	 * @return the links that make up this key
	 */
	public Collection getLinks() {
		return m_links;
	}

	/**
	 * ForeignKey links by definition are global
	 */
	public boolean isUserDefinedGlobal() {
		return false;
	}

	/**
	 * ForeignKey links by definition are not user defined
	 */
	public boolean isUserDefinedLocal() {
		return false;
	}

	/**
	 * Prints the link information to the console
	 */
	public void print() {
		super.print();
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		super.readExternal(in);
		int version = in.readInt();
		m_sourceKeyName = (String) in.readObject();
		m_destinationKeyName = (String) in.readObject();
		m_foreignkey = (DbForeignKey) in.readObject();
		m_links = (LinkedList) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(VERSION);
		out.writeObject(m_sourceKeyName);
		out.writeObject(m_destinationKeyName);
		out.writeObject(m_foreignkey);
		out.writeObject(m_links);
	}

}
