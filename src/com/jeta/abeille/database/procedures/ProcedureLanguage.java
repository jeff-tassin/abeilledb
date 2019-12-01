package com.jeta.abeille.database.procedures;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.foundation.common.JETAExternalizable;

public class ProcedureLanguage implements JETAExternalizable {
	static final long serialVersionUID = 55414533594262739L;

	public static int VERSION = 1;

	private String m_language;

	/**
	 * ctor for serialization
	 */
	public ProcedureLanguage() {

	}

	public ProcedureLanguage(String lang) {
		m_language = lang;
	}

	public String getLanguage() {
		return m_language;
	}

	public String toString() {
		return m_language;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_language = (String) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_language);
	}

}
