package com.jeta.abeille.database.procedures;

import java.io.ObjectStreamException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.DatabaseObject;
import com.jeta.abeille.database.model.DbObjectId;
import com.jeta.abeille.database.model.Schema;

import com.jeta.foundation.common.JETAExternalizable;
import com.jeta.foundation.i18n.I18N;

/**
 * This class represents a stored (SQL) procedure in the database
 * 
 * @author Jeff Tassin
 */
public class StoredProcedure implements JETAExternalizable, Comparable, Cloneable, DatabaseObject {
	static final long serialVersionUID = -5313618523563571232L;

	public static int VERSION = 1;

	/**
	 * This a key used to indentify the procedure. This is for cases where the
	 * procedure name is not unique such as postgres. This can be the same as
	 * the procedure name for RDBMS that require unique procedure names
	 */
	private Object m_key;

	/**
	 * The id (schema/name) of the procedure
	 */
	private DbObjectId m_id;

	/**
	 * The procedure source code
	 */
	private transient String m_source;

	/** the description of this procedure as found in the database */
	private transient String m_description;

	/**
	 * The procedure language (e.g. SQL, C )
	 */
	private ProcedureLanguage m_language;

	/**
	 * The procedure parameters as loaded from the database
	 */
	private transient ArrayList m_parameters;

	/**
	 * The return type for the procedure
	 */
	private ProcedureParameter m_returnparam;

	/** the signature for this procedure */
	private transient String m_signature;

	/**
	 * ctor
	 */
	public StoredProcedure() {
	}

	/**
	 * ctor
	 */
	public StoredProcedure(DbObjectId oid) {
		m_id = oid;
	}

	/**
	 * Adds a procedure parameter to the list of parameters for this procedure
	 */
	public void addParameter(ProcedureParameter param) {
		if (m_parameters == null)
			m_parameters = new ArrayList();

		m_parameters.add(param);
		m_signature = null;
	}

	/**
	 * Removes all parameters in the parameters list for this procedure
	 */
	public void clearParameters() {
		if (m_parameters != null)
			m_parameters.clear();

		m_signature = null;

	}

	/**
	 * Cloneable implementation
	 */
	public Object clone() {
		StoredProcedure proc = new StoredProcedure();
		proc.m_key = m_key;
		proc.m_id = (DbObjectId) ((m_id == null) ? null : m_id.clone());
		proc.m_source = m_source;
		proc.m_description = m_description;
		proc.m_language = m_language;

		if (m_parameters != null) {
			proc.m_parameters = new ArrayList();
			Iterator iter = m_parameters.iterator();
			while (iter.hasNext()) {
				proc.m_parameters.add(iter.next());
			}
		}

		proc.m_returnparam = (ProcedureParameter) ((m_returnparam == null) ? null : m_returnparam.clone());
		proc.m_signature = m_signature;
		return proc;
	}

	/**
	 * Comparable implementation
	 */
	public int compareTo(Object o) {
		if (o == this)
			return 0;
		else if (o instanceof StoredProcedure) {
			StoredProcedure proc = (StoredProcedure) o;
			if (m_key != null && m_key.equals(proc.m_key))
				return 0;

			if (m_id == null)
				return -1;

			int result = m_id.compareTo(proc.m_id);
			if (result == 0) {
				if (m_key != null && proc.m_key != null) {
					if (m_key instanceof Comparable) {
						Comparable comp = (Comparable) m_key;
						result = comp.compareTo(proc.m_key);
					} else {
						assert (false);
					}
				}
			}
			return result;
		} else
			return -1;

	}

	/**
	 * Equals override
	 */
	public boolean equals(Object obj) {
		boolean bresult = false;
		if (obj instanceof StoredProcedure) {
			StoredProcedure proc = (StoredProcedure) obj;
			if (m_key != null)
				bresult = m_key.equals(proc.m_key);
		}

		return bresult;
	}

	/**
	 * @return the catalog for the procedure
	 */
	public Catalog getCatalog() {
		if (m_id == null) {
			return null;
		} else {
			return m_id.getCatalog();
		}
	}

	/**
	 * @return the description for this procedure
	 */
	public String getDescription() {
		return m_description;
	}

	/**
	 * @return the name of the procedure
	 */
	public String getName() {
		if (m_id == null) {
			return null;
		} else {
			return m_id.getObjectName();
		}
	}

	/**
	 * @return the key used to identify the procedure for a particular database
	 *         vendor. This can be the same as the procedure name for RDBMS that
	 *         require unique procedure names
	 */
	public Object getKey() {
		return m_key;
	}

	/**
	 * @return the language of the procedure
	 */
	public ProcedureLanguage getLanguage() {
		return m_language;
	}

	/** @return the object id/type for this object */
	public DbObjectId getObjectId() {
		return m_id;
	}

	/**
	 * @return the procedure parameter at the given index
	 */
	public ProcedureParameter getParameter(int index) {
		if (m_parameters == null || index < 0 || index >= m_parameters.size())
			return null;
		else
			return (ProcedureParameter) m_parameters.get(index);
	}

	/**
	 * @return the number of parameters in the procedure
	 */
	public int getParameterCount() {
		if (m_parameters == null)
			return 0;
		else
			return m_parameters.size();
	}

	public ProcedureParameter getReturnParameter() {
		return m_returnparam;
	}

	/**
	 * @return the schema for the procedure
	 */
	public Schema getSchema() {
		if (m_id == null) {
			return null;
		} else {
			return m_id.getSchema();
		}
	}

	/**
	 * @return the schema qualified name for this procedure
	 */
	public String getFullyQualifiedName() {
		if (m_id == null) {
			return null;
		} else {
			return m_id.getFullyQualifiedName();
		}
	}

	/**
	 * @return the signature for this procedure
	 */
	public String getSignature() {
		if (m_signature == null) {
			StringBuffer args = new StringBuffer();
			args.append(m_id.getFullyQualifiedName());
			if (getParameterCount() == 0) {
				args.append("()");
			} else {
				args.append("(");
				for (int index = 0; index < getParameterCount(); index++) {
					ProcedureParameter param = getParameter(index);
					args.append(param.getVendorType());
					if ((index + 1) < getParameterCount()) {
						args.append(", ");
					}
				}
				args.append(")");
			}
			m_signature = args.toString();
		}

		return m_signature;
	}

	/**
	 * @return the procedure source code
	 */
	public String getSource() {
		return m_source;
	}

	/**
	 * @return the hash code for this procedure
	 */
	public int hashCode() {
		assert (m_key != null);
		return m_key.hashCode();
	}

	/**
	 * Print this class to console for debugging purposes
	 */
	public void print() {
		System.out.println("dumping procedure.... ");
		if (m_id == null)
			System.out.println("  id = null ");
		else {
			System.out.print("  id = ");
			m_id.print();
		}

		System.out.println("             key = " + m_key);
		System.out.println("             language = " + m_language.getLanguage());
		System.out.println("             signature = " + getSignature());
	}

	/**
	 * Sets the description for this procedure
	 */
	public void setDescription(String desc) {
		m_description = desc;
	}

	/**
	 * Sets the id (schema/name) for this procedure
	 */
	public void setId(DbObjectId oid) {
		m_id = oid;
		m_signature = null;
	}

	/**
	 * Sets the key used to identify the procedure for a particular database
	 * vendor. This can be the same as the procedure name for RDBMS that require
	 * unique procedure names
	 */
	public void setKey(Object key) {
		m_key = key;
	}

	/**
	 * Sets the language of the procedure
	 */
	public void setLanguage(ProcedureLanguage lang) {
		m_language = lang;
	}

	public void setReturnParameter(ProcedureParameter param) {
		m_returnparam = param;
	}

	/**
	 * Set the procedure source code
	 */
	public void setSource(String source) {
		m_source = source;
	}

	/**
	 * @return the name of this procedure
	 */
	public String toString() {
		return getName();
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_key = (Object) in.readObject();
		m_id = (DbObjectId) in.readObject();
		m_language = (ProcedureLanguage) in.readObject();
		m_returnparam = (ProcedureParameter) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_key);
		out.writeObject(m_id);
		out.writeObject(m_language);
		out.writeObject(m_returnparam);
	}

}
