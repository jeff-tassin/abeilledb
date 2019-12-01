package com.jeta.abeille.database.model;

import java.io.ObjectStreamException;
import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;

import com.jeta.foundation.common.JETAExternalizable;
import com.jeta.foundation.i18n.I18N;

/**
 * This class represents a foreign key in a table.
 * 
 * @author Jeff Tassin
 */
public class DbForeignKey implements Cloneable, Comparable, JETAExternalizable {
	/** serialize id */
	static final long serialVersionUID = 615923856081880611L;

	public static int VERSION = 1;

	private String m_keyName; // the name of the foreign key FK_NAME
	private TableId m_localTableId; // the table that contains the foreign key
									// FKTABLE_NAME
	private String m_referenceKeyName; // the table name the foreign key is
										// constrained to PKTABLE_NAME
	private TableId m_referenceTableId; // the table id the foreign key is
										// constrained to PKTABLE_NAME
	private DbKey m_localKey; // the columns in the local table that make up the
								// foreign key
	private HashMap m_assignments; // defines how columns in fk are mapped to pk
									// columns

	/**
	 * constraints on this foreign key. For example in PostgreSQL, we can define
	 * if a foreign key is deferrable or not. Also, we an define the type of
	 * action to take when a referred column is updated or deleted. This can be
	 * vendor specific Note: this object must be serializable
	 */
	private ForeignKeyConstraints m_constraints;

	/**
	 * ctor
	 */
	public DbForeignKey() {
		m_assignments = new HashMap();
		m_localKey = new DbKey();
	}

	/**
	 * Defines how a foreign key column is mapped to a primary key column. This
	 * is needed for compound keys. Adds the foreignKeyColumn to the m_localKey
	 * definition.
	 * 
	 * @param foreignKeyColumn
	 *            the name of a column in the foreign key
	 * @param primaryKeyColumn
	 *            the name of the corresponding column in the primary key
	 */
	public void assignForeignKeyColumn(String foreignKeyColumn, String primaryKeyColumn) {
		m_assignments.put(foreignKeyColumn, primaryKeyColumn);
		m_localKey.addField(foreignKeyColumn);
	}

	/**
	 * Clears any column assignments in the foreign key
	 */
	public void clearAssignments() {
		m_assignments.clear();
		m_localKey.clear();
	}

	/**
	 * Cloneable implementation
	 */
	public Object clone() {
		DbForeignKey fkey = new DbForeignKey();
		fkey.m_keyName = m_keyName;
		fkey.m_localTableId = (TableId) m_localTableId.clone();
		fkey.m_referenceKeyName = m_referenceKeyName;
		fkey.m_referenceTableId = (TableId) m_referenceTableId.clone();
		fkey.m_localKey = (DbKey) m_localKey.clone();
		fkey.m_assignments = (HashMap) m_assignments.clone();
		return fkey;
	}

	/**
	 * Comparable implementation
	 */
	public int compareTo(Object o) {
		if (o == this)
			return 0;

		if (o instanceof DbForeignKey) {
			DbForeignKey fkey = (DbForeignKey) o;
			// int result = m_keyName.compareTo( fkey.m_keyName );
			// if ( result == 0 )
			//
			if (m_localTableId == null || m_referenceTableId == null)
				return -1;

			int result = m_localTableId.compareTo(fkey.m_localTableId);
			if (result == 0) {
				// result = m_referenceKeyName.compareTo(
				// fkey.m_referenceKeyName );
				result = m_referenceTableId.compareTo(fkey.m_referenceTableId);
				if (result == 0) {
					if (m_assignments.size() == fkey.m_assignments.size()) {
						Iterator iter = m_assignments.keySet().iterator();
						while (iter.hasNext()) {
							String fk_col = (String) iter.next();
							String pk_col = (String) m_assignments.get(fk_col);

							if (pk_col == null) {
								return -1;
							} else {
								String fkey_pk_col = (String) fkey.m_assignments.get(fk_col);
								if (fkey_pk_col == null) {
									return -1;
								} else {
									result = I18N.compareToIgnoreCase(pk_col, fkey_pk_col);
									if (result != 0) {
										return result;
									}
								}
							}
						}
						result = 0;
					} else {
						if (m_assignments.size() < fkey.m_assignments.size())
							return -1;
						else
							return 1;
					}
				}
			}
			return result;
		} else
			return -1;

	}

	/**
	 * Test two foreign keys for equality
	 */
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		return (compareTo(obj) == 0);
	}

	/**
	 * @param primaryKeyColumn
	 *            the name of a column in the reference table's primary key
	 * @return the name of the foreign key column that is mapped to the given
	 *         primary key column
	 */
	public String getAssignedLocalKeyColumnName(String primaryKeyColumn) {
		Iterator iter = m_assignments.keySet().iterator();
		while (iter.hasNext()) {
			String fkey = (String) iter.next();
			String assignedpk = getAssignedPrimaryKeyColumnName(fkey);
			if (primaryKeyColumn.equals(assignedpk))
				return fkey;
		}

		// should not be here
		assert (false);
		return null;
	}

	/**
	 * @param foreignKeyColumn
	 *            the name of a column in the foreign key
	 * @return the name of the primary key column that is mapped to the given
	 *         foreign key column
	 */
	public String getAssignedPrimaryKeyColumnName(String foreignKeyColumn) {
		String pk = (String) m_assignments.get(foreignKeyColumn);
		return pk;
	}

	/**
	 * @return the foreign key constraints
	 */
	public ForeignKeyConstraints getConstraints() {
		return m_constraints;
	}

	/**
	 * @return the id of the table that contains the foreign key
	 */
	public TableId getDestinationTableId() {
		return getLocalTableId();
	}

	/**
	 * @return the columns that make up the foreign key
	 */
	public DbKey getLocalKey() {
		return m_localKey;
	}

	/**
	 * @return the name of the foreign key
	 */
	public String getName() {
		return m_keyName;
	}

	/**
	 * @return the name of the foreign key
	 * @deprecated
	 */
	public String getKeyName() {
		return m_keyName;
	}

	/**
	 * @return the id of the table that contains the foreign key
	 */
	public TableId getLocalTableId() {
		return m_localTableId;
	}

	/**
	 * This is the name of the primary key
	 */
	public String getReferenceKeyName() {
		return m_referenceKeyName;
	}

	/**
	 * @return the id of the table that the foreign key is constrained to
	 */
	public TableId getReferenceTableId() {
		return m_referenceTableId;
	}

	/**
	 * @return the name of the table that the foreign key is constrained to
	 */
	public String getReferenceTableName() {
		return m_referenceTableId.getTableName();
	}

	/**
	 * @return the id of the table that the foreign key is constrained to
	 */
	public TableId getSourceTableId() {
		return getReferenceTableId();
	}

	/**
	 * Prints this key to the console
	 */
	public void print() {

		System.out.println("");
		System.out.println("--------- dumping foreign key -----");
		System.out.println("     keyname = " + m_keyName);
		System.out.println("     localtable = " + m_localTableId);
		System.out.println("     reftable = " + m_referenceTableId);
		m_localKey.print();
		System.out.println("    ----- asignments ---");
		Iterator iter = m_assignments.keySet().iterator();
		while (iter.hasNext()) {
			String fkcol = (String) iter.next();
			String pkcol = (String) m_assignments.get(fkcol);
			System.out.println("  localkey = " + fkcol + "   refkey = " + pkcol);
		}
		System.out.println("");

	}

	/**
	 * Sets the foreign key constraints
	 */
	public void setConstraints(ForeignKeyConstraints obj) {
		m_constraints = obj;
	}

	public void setLocalKey(DbKey cKey) {
		m_localKey = cKey;
	}

	public void setLocalTableId(TableId localTableId) {
		m_localTableId = localTableId;
	}

	public void setDestinationTableId(TableId localTableId) {
		setLocalTableId(localTableId);
	}

	public void setReferenceKeyName(String referenceKey) {
		m_referenceKeyName = referenceKey;
	}

	/**
	 * Sets the reference table id
	 */
	public void setReferenceTableId(TableId refTableId) {
		m_referenceTableId = refTableId;
	}

	/**
	 * Sets the reference table id
	 */
	public void setSourceTableId(TableId refTableId) {
		setReferenceTableId(refTableId);
	}

	public void setKeyName(String keyName) {
		m_keyName = keyName;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();

		m_keyName = (String) in.readObject();
		m_localTableId = (TableId) in.readObject();
		m_referenceKeyName = (String) in.readObject();
		m_referenceTableId = (TableId) in.readObject();
		m_localKey = (DbKey) in.readObject();
		m_assignments = (HashMap) in.readObject();
		m_constraints = (ForeignKeyConstraints) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_keyName);
		out.writeObject(m_localTableId);
		out.writeObject(m_referenceKeyName);
		out.writeObject(m_referenceTableId);
		out.writeObject(m_localKey);
		out.writeObject(m_assignments);
		out.writeObject(m_constraints);
	}

}
