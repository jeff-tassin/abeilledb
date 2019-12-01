package com.jeta.abeille.query;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.model.TableId;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * This class represents a reportable object in a query
 * 
 * @author Jeff Tassin
 */
public class Reportable implements JETAExternalizable, Comparable {
	static final long serialVersionUID = 5417275118834346835L;

	public static int VERSION = 1;

	/**
	 * The column we wish to show in the query
	 */
	private ColumnMetaData m_cmd;

	/**
	 * The name used for the output. If null, then the default is to use the
	 * column name
	 */
	private String m_outputname;

	/**
	 * ctor for serialization
	 */
	public Reportable() {

	}

	public Reportable(ColumnMetaData cmd) {
		m_cmd = cmd;
	}

	/**
	 * Comparable interface
	 */
	public int compareTo(Object obj) {
		if (obj instanceof Reportable) {
			Reportable r = (Reportable) obj;
			return m_cmd.compareTo(r.m_cmd);
		} else {
			return -1;
		}
	}

	/**
	 * Equals
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Reportable) {
			return (compareTo(obj) == 0);
		} else
			return false;
	}

	/**
	 * Hash value for hash tables
	 */
	public int hashCode() {
		return m_cmd.hashCode();
	}

	/**
	 * @return the column for this reportable
	 */
	public ColumnMetaData getColumn() {
		return m_cmd;
	}

	/**
	 * @return the column name for this reportable
	 */
	public String getColumnName() {
		return m_cmd.getName();
	}

	/**
	 * @return the output name for the column
	 */
	public String getOutputName() {
		return m_outputname;
	}

	/**
	 * @return the table for this reportable
	 */
	public TableId getTableId() {
		return m_cmd.getTableId();
	}

	/**
	 * @return the table name for this reportable
	 */
	public String getTableName() {
		return m_cmd.getTableId().getTableName();
	}

	/**
	 * Prints this reportable to the console
	 */
	public void print() {
		System.out.println(" Reportable  cmd = " + m_cmd + "  output name = " + m_outputname);
	}

	public void setColumn(ColumnMetaData cmd) {
		m_cmd = cmd;
	}

	/**
	 * Sets the output name for the column
	 */
	public void setOutputName(String output) {
		m_outputname = output;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_cmd = (ColumnMetaData) in.readObject();
		m_outputname = (String) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_cmd);
		out.writeObject(m_outputname);
	}

}
