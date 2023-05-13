package com.jeta.abeille.database.procedures;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * Class defines directions for stored procedure parameters
 * 
 * @author Jeff Tassin
 */
public class ParameterDirection implements JETAExternalizable {
	static final long serialVersionUID = 4308408863508033343L;

	public static int VERSION = 1;

	/** the name for this direction */
	private String m_name;

	public static ParameterDirection IN = new ParameterDirection("IN");
	public static ParameterDirection INOUT = new ParameterDirection("INOUT");
	public static ParameterDirection OUT = new ParameterDirection("OUT");
	public static ParameterDirection RETURN = new ParameterDirection("Return");

	/**
	 * ctor for default serialization
	 */
	public ParameterDirection() {

	}

	/**
	 * ctor
	 */
	private ParameterDirection(String name) {
		m_name = name;
	}

	/**
	 * @return a predefined direction object from a string. Note that the case
	 *         must match.
	 */
	public static ParameterDirection fromString(String name) {
		if (name == null)
			return null;

		if (name.equals(IN.m_name))
			return IN;
		else if (name.equals(INOUT.m_name))
			return INOUT;
		else if (name.equals(OUT.m_name))
			return OUT;
		else if (name.equals(RETURN.m_name))
			return RETURN;
		else
			return null;
	}

	/**
	 * @return the database name
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * For comparable
	 */
	public int hashCode() {
		return m_name.hashCode();
	}

	/**
	 * You absolutely need this when deserializing this class classes
	 */
	private Object readResolve() throws ObjectStreamException {
		return fromString(m_name);
	}

	/**
	 * toString implementation
	 */
	public String toString() {
		return m_name;
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
