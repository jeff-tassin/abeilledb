package com.jeta.abeille.gui.query;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * This is an enum class that defines a query type (SQL or QUERY)
 * 
 * @author Jeff Tassin
 */
public class QueryType implements JETAExternalizable {
	static final long serialVersionUID = 1027294587568867351L;

	public static int VERSION = 1;

	private String m_name;

	public static final QueryType SQL = new QueryType("sql");
	public static final QueryType QUERY = new QueryType("query");

	/**
	 * ctor only for initialization
	 */
	public QueryType() {

	}

	private QueryType(String name) {
		this.m_name = name;
	}

	public String toString() {
		return m_name;
	}

	public static QueryType fromString(String qtype) {
		if (qtype.equals(QueryType.SQL.toString()))
			return QueryType.SQL;
		else
			return QueryType.QUERY;
	}

	/**
	 * You absolutely need this when deserializing enum classes
	 */
	private Object readResolve() throws ObjectStreamException {
		return fromString(m_name);
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
