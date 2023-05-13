package com.jeta.abeille.query;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * This is an enum class that defines query constraint operators
 * 
 * @author Jeff Tassin
 */
public class Operator implements JETAExternalizable {
	static final long serialVersionUID = 3903777917449904422L;

	public static int VERSION = 1;

	private String m_element;

	/**
	 * ctor for serialization
	 */
	public Operator() {

	}

	Operator(String element) {
		this.m_element = element;
	}

	public String toString() {
		return m_element;
	}

	public static final Operator EQUALS = new Operator("=");
	public static final Operator LESSTHAN = new Operator("<");
	public static final Operator GREATERTHAN = new Operator(">");
	public static final Operator LESSTHANEQUALS = new Operator("<=");
	public static final Operator GREATERTHANEQUALS = new Operator(">=");
	public static final Operator NOTEQUAL = new Operator("!=");
	public static final Operator NOTEQUAL2 = new Operator("<>");
	public static final Operator LIKE = new Operator("LIKE");
	public static final Operator IS_NULL = new Operator("IS NULL");

	public static Operator fromString(String opStr) {
		if (opStr.equals(Operator.EQUALS.toString()))
			return Operator.EQUALS;
		else if (opStr.equals(Operator.LESSTHAN.toString()))
			return Operator.LESSTHAN;
		else if (opStr.equals(Operator.GREATERTHAN.toString()))
			return Operator.GREATERTHAN;
		else if (opStr.equals(Operator.LESSTHANEQUALS.toString()))
			return Operator.LESSTHANEQUALS;
		else if (opStr.equals(Operator.GREATERTHANEQUALS.toString()))
			return Operator.GREATERTHANEQUALS;
		else if (opStr.equals(Operator.NOTEQUAL.toString()))
			return Operator.NOTEQUAL;
		else if (opStr.equals(Operator.NOTEQUAL2.toString()))
			return Operator.NOTEQUAL2;
		else if (opStr.equals(Operator.LIKE.toString()))
			return Operator.LIKE;
		else if (opStr.equals(Operator.IS_NULL.toString()))
			return Operator.IS_NULL;
		else
			return null;
	}

	/**
	 * You absolutely need this when deserializing enum classes
	 */
	private Object readResolve() throws ObjectStreamException {
		Object result = fromString(m_element);
		if (result == null)
			result = EQUALS;
		return result;
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
