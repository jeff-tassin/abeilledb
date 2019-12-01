package com.jeta.abeille.database.model;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.foundation.common.JETAExternalizable;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class defines a link between the columns of two tables ( table1( some
 * column) ------ > table(some column) )
 * 
 * @author Jeff Tassin
 */
public class Link implements Cloneable, Comparable, JETAExternalizable {
	/** serialize id */
	static final long serialVersionUID = -5337061122018154243L;

	public static int VERSION = 1;

	private TableId m_sourceTableId;
	private String m_sourceColumn;

	private TableId m_destinationTableId;
	private String m_destinationColumn;

	/** a string id that we use for the Comparable interface */
	private String m_sid;

	/**
	 * Flag that indicates if this link is user defined (as opposed to a foreign
	 * key link)
	 */
	private boolean m_userdefined;

	/**
	 * ctor only for serialization
	 */
	public Link() {

	}

	/**
	 * ctor
	 */
	public Link(TableId srcTable, String srcCol, TableId destTable, String destCol) {
		m_sourceTableId = srcTable;
		m_sourceColumn = srcCol;

		m_destinationTableId = destTable;
		m_destinationColumn = destCol;

		m_userdefined = false;
		recalcid();
	}

	/**
	 * Cloneable implementation
	 */
	public Object clone() {
		Link link = new Link(m_sourceTableId, m_sourceColumn, m_destinationTableId, m_destinationColumn);
		link.m_userdefined = m_userdefined;
		return link;
	}

	/**
	 * Comparable implementation
	 */
	public int compareTo(Object o) {
		if (o == this)
			return 0;
		else if (o instanceof Link) {
			Link link = (Link) o;
			return m_sid.compareTo(link.m_sid);
		} else
			return -1;

	}

	public boolean contains(TableId tableid) {
		if (tableid.equals(m_sourceTableId)) {
			return true;
		} else if (tableid.equals(m_destinationTableId)) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * @return true if this link has the given a table/column pair
	 */
	public boolean contains(TableId tableid, String colName) {
		if (colName == null || tableid == null)
			return false;

		/** be careful to handle self-referencing tables */
		if (tableid.equals(m_sourceTableId)) {
			if (colName.equalsIgnoreCase(m_sourceColumn))
				return true;
		}

		if (tableid.equals(m_destinationTableId)) {
			if (colName.equalsIgnoreCase(m_destinationColumn))
				return true;
		}

		return false;
	}

	/**
	 * global link factory
	 */
	public static Link createUserDefinedLink(TableId srcTable, String srcCol, TableId destTable, String destCol) {
		Link link = new Link(srcTable, srcCol, destTable, destCol);
		link.m_userdefined = true;
		return link;
	}

	/**
	 * Override Object implementation so we can test against our string id value
	 */
	public boolean equals(Object obj) {
		return (compareTo(obj) == 0);
	}

	/**
	 * Return the column that is associated with the given table. Note: this
	 * method has no meaning if the link is self-referencing.
	 */
	public String getColumn(TableId tableid) {
		if (tableid == null)
			return null;

		assert (!isSelfReference());
		if (tableid.equals(m_sourceTableId)) {
			return m_sourceColumn;
		}

		if (tableid.equals(m_destinationTableId)) {
			return m_destinationColumn;
		}

		return null;
	}

	/**
	 * Return the column that is linked to the given table/colName. We need to
	 * pass both the tableid and the colName to handle links on self-referencing
	 * tables.
	 * 
	 * @return the linked column assocated with the given table/col in this link
	 */
	public String getLinkedColumn(TableId tableid, String colName) {
		if (tableid == null) {
			return null;
		}

		if (tableid.equals(m_sourceTableId)) {
			if (I18N.equalsIgnoreCase(colName, m_sourceColumn)) {
				return m_destinationColumn;
			}
		}

		if (tableid.equals(m_destinationTableId)) {
			if (I18N.equalsIgnoreCase(colName, m_destinationColumn)) {
				return m_sourceColumn;
			}
		}

		if (TSUtils.isDebug()) {
			TSUtils.printMessage("Link.getLinkedColumn  failed:  tableid: " + tableid + "  colName: " + colName);
			print();
			System.out.println();
			assert (false);
		}
		return null;
	}

	public TableId getDestinationTableId() {
		return m_destinationTableId;
	}

	/**
	 * @return the column name that is the destination for this link
	 */
	public String getDestinationColumn() {
		return m_destinationColumn;
	}

	/**
	 * @return the name of the table that is the destination for this link
	 */
	public String getDestinationTableName() {
		return m_destinationTableId.getTableName();
	}

	public TableId getLinkedTable(TableId tableid) {
		if (tableid == null) {
			assert (false);
			return null;
		} else if (tableid.equals(m_sourceTableId))
			return m_destinationTableId;
		else if (tableid.equals(m_destinationTableId))
			return m_sourceTableId;
		else
			return null;

	}

	/**
	 * @return the name of the table that is the source for this link
	 */
	public String getSourceTableName() {
		return m_sourceTableId.getTableName();
	}

	/**
	 * @return the id of the table that is the source for this link
	 */
	public TableId getSourceTableId() {
		return m_sourceTableId;
	}

	/**
	 * @return the column name that is the source for this link
	 */
	public String getSourceColumn() {
		return m_sourceColumn;
	}

	/**
	 * @return a string identifier for this link This value is used mostly for
	 *         comparisons with other links
	 */
	protected String getStringId() {
		return m_sid;
	}

	/**
	 * @return true if the given link is self referencing (i.e. the source table
	 *         equals the destination table)
	 */
	public boolean isSelfReference() {
		return m_sourceTableId.equals(m_destinationTableId);
	}

	/**
	 * @return the flag that indicates this link is global to the application.
	 *         So, anywhere tables are displayed with relationships
	 *         (formbuilder, querybuilder, modeler, instanceview), these links
	 *         will be defined. Global links can be both user defined and
	 *         foreign key links.
	 */
	public boolean isUserDefined() {
		return m_userdefined;
	}

	/**
	 * For testing purposes
	 */
	public void print() {
		System.out.print(m_sourceTableId + "." + m_sourceColumn + " -> " + m_destinationTableId + "."
				+ m_destinationColumn);
	}

	/**
	 * Recalulates the id used for comparisons to other link objects
	 */
	private void recalcid() {
		// generate the id so for the compareTo method to use
		StringBuffer sid = new StringBuffer();
		if (m_sourceTableId != null)
			sid.append(m_sourceTableId.toString() + m_sourceColumn);

		if (m_destinationTableId != null)
			sid.append(m_destinationTableId.toString() + m_destinationColumn);

		m_sid = sid.toString();
	}

	/**
	 * @return true if this link references the given table id in any way.
	 */
	public boolean references(TableId tableId) {
		if (tableId.equals(m_sourceTableId))
			return true;
		else if (tableId.equals(m_destinationTableId))
			return true;
		else
			return false;
	}

	/**
	 * Set the id of the table that is to be the destination for this link
	 */
	public void setDestinationTableId(TableId destId) {
		m_destinationTableId = destId;
		recalcid();
	}

	/**
	 * Set the name of the column in the table that is to be the destination for
	 * this link
	 */
	public void setDestinationColumn(String colName) {
		m_destinationColumn = colName;
		recalcid();

	}

	/**
	 * Set the id of the table that is to be the source for this link
	 */
	public void setSourceTableId(TableId srcId) {
		m_sourceTableId = srcId;
		recalcid();

	}

	/**
	 * Set the name of the column in the table that is to be the source for this
	 * link
	 */
	public void setSourceColumn(String colName) {
		m_sourceColumn = colName;
		recalcid();

	}

	/**
	 * @return a string identifier for this link This value is used mostly for
	 *         comparisons with other links
	 */
	protected void setStringId(String sid) {
		m_sid = sid;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_sourceTableId = (TableId) in.readObject();
		m_sourceColumn = (String) in.readObject();
		m_destinationTableId = (TableId) in.readObject();
		m_destinationColumn = (String) in.readObject();
		m_sid = (String) in.readObject();
		m_userdefined = in.readBoolean();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeObject(m_sourceTableId);
		out.writeObject(m_sourceColumn);
		out.writeObject(m_destinationTableId);
		out.writeObject(m_destinationColumn);
		out.writeObject(m_sid);
		out.writeBoolean(m_userdefined);
	}

}
