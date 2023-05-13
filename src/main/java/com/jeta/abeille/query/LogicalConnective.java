package com.jeta.abeille.query;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * This class is an 'enum' class that defines the AND/OR logic nodes for a
 * query.
 * 
 * @author Jeff Tassin
 */
public class LogicalConnective implements JETAExternalizable {
	static final long serialVersionUID = 8396881949237578424L;

	public static int VERSION = 1;

	private String m_element;

	/**
	 * ctor for serialization
	 */
	public LogicalConnective() {

	}

	LogicalConnective(String element) {
		this.m_element = element;
	}

	public String toString() {
		return m_element;
	}

	public static final LogicalConnective AND = new LogicalConnective("AND");
	public static final LogicalConnective OR = new LogicalConnective("OR");

	public static LogicalConnective fromString(String lcStr) {
		if (lcStr.equalsIgnoreCase(LogicalConnective.AND.toString()))
			return LogicalConnective.AND;
		else if (lcStr.equalsIgnoreCase(LogicalConnective.OR.toString()))
			return LogicalConnective.OR;
		else
			return null;
	}

	/**
	 * You absolutely need this when deserializing enumeration classes
	 */
	private Object readResolve() throws ObjectStreamException {
		if (m_element.equals(AND.m_element))
			return AND;
		else
			return OR;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_element = (String) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_element);
	}

}
