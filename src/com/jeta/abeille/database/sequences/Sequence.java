package com.jeta.abeille.database.sequences;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.DatabaseObject;
import com.jeta.abeille.database.model.DbObjectId;
import com.jeta.abeille.database.model.Schema;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * This is the definition for a sequence in the database We use objects instead
 * of primitives (e.g. Long instead of long) so we can distinguish between null
 * values and zero values
 * 
 * @author Jeff Tassin
 */
public class Sequence implements Comparable, DatabaseObject, JETAExternalizable {
	static final long serialVersionUID = -1938800654230750551L;

	public static int VERSION = 1;

	/** the id for this sequence (schema.name) */
	private DbObjectId m_id;

	/**
	 * a key that uniquely identifies this sequence. This is not necessarily the
	 * schema.name. In PostgreSQL its an integer PK
	 */
	private Object m_key;

	/**
	 * This flag indicates if the sequence should wrap around if the max value
	 * is exceeded
	 */
	private Boolean m_cycle;

	/** This flag indicates if the sequence is temporary */
	private Boolean m_temporary;

	/**
	 * The cache value for the sequence. This defines if sequence numbers should
	 * be pre-allocated and cached for faster access. The min value for this is
	 * one.
	 */
	private Long m_cache;

	/** The increment value for this sequence */
	private Long m_increment;

	/** The last known value for this sequence */
	private Long m_lastvalue;

	/** The max value for this sequence */
	private Long m_maxvalue;

	/** The max value for this sequence */
	private Long m_minvalue;

	/** The start value for this sequence */
	private Long m_start;

	/**
	 * ctor only for serialization
	 */
	public Sequence() {

	}

	/**
	 * ctor
	 * 
	 * @param key
	 *            a unique ( vendor specified ) key that identifies this
	 *            sequence in the database
	 */
	public Sequence(Object key) {
		m_key = key;
	}

	/**
	 * Comparable implementation
	 */
	public int compareTo(Object obj) {
		if (obj == this)
			return 0;

		if (obj instanceof Sequence) {
			Sequence seq = (Sequence) obj;
			if (m_id == null || seq.m_id == null)
				return -1;
			else
				return m_id.compareTo(seq.m_id);
		} else
			return -1;
	}

	/**
	 * Comparable implementation
	 */
	public boolean equals(Object obj) {
		return (compareTo(obj) == 0);
	}

	/**
	 * @return the cache value
	 */
	public Long getCache() {
		return m_cache;
	}

	/**
	 * @return the underlying catalog
	 */
	public Catalog getCatalog() {
		return m_id.getCatalog();
	}

	/**
	 * @return the id for the sequence ( schema.name )
	 */
	public DbObjectId getId() {
		return m_id;
	}

	/**
	 * @return the increment for this sequence
	 */
	public Long getIncrement() {
		return m_increment;
	}

	/**
	 * @return the last known value for this sequence
	 */
	public Long getLastValue() {
		return m_lastvalue;
	}

	/**
	 * @return the maximum value
	 */
	public Long getMax() {
		return m_maxvalue;
	}

	/**
	 * @return the minimum value
	 */
	public Long getMin() {
		return m_minvalue;
	}

	/**
	 * @return the name of the sequence
	 */
	public String getName() {
		return m_id.getObjectName();
	}

	/** @return the object id/type for this object */
	public DbObjectId getObjectId() {
		return m_id;
	}

	/**
	 * @return the schema that contains this sequence
	 */
	public Schema getSchema() {
		return m_id.getSchema();
	}

	/**
	 * @return the name of the sequence
	 */
	public String getFullyQualifiedName() {
		return m_id.getFullyQualifiedName();
	}

	/**
	 * @return the starting value for the sequence
	 */
	public Long getStart() {
		return m_start;
	}

	/**
	 * @return the cycle flag. This indicates if the sequence should wrap around
	 *         if the max value is exceeded.
	 */
	public Boolean isCycle() {
		return m_cycle;
	}

	/**
	 * @return the temporary flag. This indicates if this sequence is valid only
	 *         for this session
	 */
	public Boolean isTemporary() {
		return m_temporary;
	}

	/**
	 * Sets the cache value
	 */
	public void setCache(Long cache) {
		m_cache = cache;
	}

	/**
	 * Sets the cycle flag. This indicates if the sequence should wrap around if
	 * the max value is exceeded.
	 */
	public void setCycle(Boolean cycle) {
		m_cycle = cycle;
	}

	/**
	 * Sets the id for this sequence
	 */
	public void setId(DbObjectId id) {
		m_id = id;
	}

	/**
	 * Sets the increment for this sequence
	 */
	public void setIncrement(Long increment) {
		m_increment = increment;
	}

	/**
	 * Sets the last known value for this sequence
	 */
	public void setLastValue(Long last_value) {
		m_lastvalue = last_value;
	}

	/**
	 * Sets the maximum value
	 */
	public void setMax(Long max) {
		m_maxvalue = max;
	}

	/**
	 * Sets the minimum value
	 */
	public void setMin(Long min) {
		m_minvalue = min;
	}

	/**
	 * Sets the start value for this sequence
	 */
	public void setStart(Long start) {
		m_start = start;
	}

	/**
	 * Sets the temporary flag for this sequence
	 */
	public void setTemporary(Boolean temp) {
		m_temporary = temp;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_id = (DbObjectId) in.readObject();
		m_key = in.readObject();
		m_cycle = (Boolean) in.readObject();
		m_temporary = (Boolean) in.readObject();
		m_cache = (Long) in.readObject();
		m_increment = (Long) in.readObject();
		m_lastvalue = (Long) in.readObject();
		m_maxvalue = (Long) in.readObject();
		m_minvalue = (Long) in.readObject();
		m_start = (Long) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_id);
		out.writeObject(m_key);
		out.writeObject(m_cycle);
		out.writeObject(m_temporary);
		out.writeObject(m_cache);
		out.writeObject(m_increment);
		out.writeObject(m_lastvalue);
		out.writeObject(m_maxvalue);
		out.writeObject(m_minvalue);
		out.writeObject(m_start);
	}

}
