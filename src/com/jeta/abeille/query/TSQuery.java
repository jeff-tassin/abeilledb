package com.jeta.abeille.query;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * This class describes a query that is built with the application query builder
 * 
 * @author Jeff Tassin
 */
public class TSQuery implements JETAExternalizable {
	static final long serialVersionUID = 1893842616607337137L;

	public static int VERSION = 1;

	private String m_name; // the user defined name for this query

	/**
	 * ctor for serialization
	 */
	public TSQuery() {

	}

	/**
	 * ctor
	 * 
	 * @param name
	 *            the name for this query
	 */
	public TSQuery(String name) {
		m_name = name;
	}

	/**
	 * Adds a constraint to this reportable
	 */
	public void addConstraint(QueryConstraint c) {

	}

	/**
	 * Adds a reportable to this query
	 */
	public void addReportable(Reportable reportable) {
	}

	/**
	 * @return the user defined name for this query
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * Removes all constraint objects from this query
	 */
	public void removeConstraints() {

	}

	/**
	 * Removes all reportable objects from this query
	 */
	public void removeReportables() {

	}

	/**
	 * Sets this query's name
	 * 
	 * @param name
	 *            the name of the query to set
	 */
	public void setName(String name) {
		m_name = name;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_name = (String) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_name);
	}

}
