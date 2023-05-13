package com.jeta.abeille.gui.modeler;

import com.jeta.abeille.database.model.DbForeignKey;
import com.jeta.abeille.database.model.DbKey;
import com.jeta.abeille.database.model.TableId;

import com.jeta.foundation.i18n.I18N;

/**
 * This class represents a partial foreign key definition for the
 * ForeignKeyView/Model.
 * 
 * @author Jeff Tassin
 */
public class ForeignKeyWrapper {
	/** the foreign key we are wrapping */
	private DbForeignKey m_fkey;

	/** the CSV string representation of the columns in the fkey */
	private String m_colslist;

	/**
	 * ctor
	 * 
	 * @param name
	 *            the name of the foreign key
	 * @param refid
	 *            the reference table id. We don't store the local table id
	 *            because it is defined in another part of the dialog and is not
	 *            relevant anyway. Furthermore, the local table id can change
	 *            while the foreignkey view is visible
	 */
	public ForeignKeyWrapper(DbForeignKey fKey) {
		m_fkey = fKey;
	}

	/**
	 * @return a command separated list of column names from the local table
	 *         that make up this key
	 */
	public String getAssignedColumns() {
		if (m_colslist == null) {
			if (m_fkey != null) {
				DbKey lkey = m_fkey.getLocalKey();
				if (lkey != null) {
					m_colslist = I18N.generateCSVList(lkey.getColumns());
				}
			}
		}
		return m_colslist;
	}

	/**
	 * @return the underlying foreign key
	 */
	public DbForeignKey getForeignKey() {
		return m_fkey;
	}

	/**
	 * @return the name of this foreign key
	 */
	public String getName() {
		if (m_fkey == null)
			return null;
		else
			return m_fkey.getName();
	}

	/**
	 * @return the table id that this key is related to
	 */
	public TableId getReferenceTableId() {
		if (m_fkey == null)
			return null;
		else
			return m_fkey.getReferenceTableId();
	}

	/**
	 * Sets the foreign key for this wrapper
	 */
	public void setForeignKey(DbForeignKey fkey) {
		m_fkey = fkey;
		m_colslist = null;
	}

}
