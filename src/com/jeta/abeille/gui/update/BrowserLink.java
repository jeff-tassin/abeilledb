package com.jeta.abeille.gui.update;

import com.jeta.abeille.database.model.Link;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;

/**
 * This class is used in the InstanceView when the user wishes to browser to
 * another table that is linked to the curren table. We store the current table
 * metadata and the target table id in this class. For multiple targets (i.e. a
 * primary key that is linked to multiple foreign keys), we store these objects
 * in JMenuItems and display in a popup. The user can then select a target.
 * 
 * @author Jeff Tassin
 */
public class BrowserLink implements Comparable {
	/**
	 * the TableMetaData of the table that is the source for the link to the
	 * table we wish to browse.
	 */
	private TableMetaData m_sourcetmd;

	/**
	 * The underling link
	 */
	private Link m_link;

	/**
	 * Flag controls whether all columns for all links between the source and
	 * destination table are initialzed/loaded when the destination form is
	 * launched. If set to false, only the linked column that is clicked is
	 * loaded in the new form. If set to true, all linked columns are loaded.
	 */
	private boolean m_loadAllColumns;

	/**
	 * Flag that indicates if the launcher should launch a new frame window even
	 * if one is found in the cache.
	 */
	public boolean m_newframe;

	/**
	 * ctor
	 */
	public BrowserLink(TableMetaData sourcetmd, String srcCol, TableId targetid, String targetcol,
			boolean loadAllColumns, boolean showNew) {
		m_sourcetmd = sourcetmd;
		m_link = new Link(sourcetmd.getTableId(), srcCol, targetid, targetcol);
		m_newframe = showNew;
		m_loadAllColumns = loadAllColumns;
	}

	/**
	 * Comparable implementation
	 */
	public int compareTo(Object o) {
		if (o == this)
			return 0;
		else if (o instanceof BrowserLink) {
			BrowserLink blink = (BrowserLink) o;
			return m_link.compareTo(blink.m_link);
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
	 * @return the TableMetaData of the table that is the source for the link to
	 *         the table we wish to browse.
	 */
	public TableMetaData getSourceTableMetaData() {
		return m_sourcetmd;
	}

	public String getSourceColumn() {
		return m_link.getSourceColumn();
	}

	public String getTargetColumn() {
		return m_link.getDestinationColumn();
	}

	/**
	 * @return the id of the table that we wish to browse
	 */
	public TableId getTargetTableId() {
		return m_link.getDestinationTableId();
	}

	public boolean isLoadAllColumns() {
		return m_loadAllColumns;
	}

	public boolean isNewFrame() {
		return m_newframe;
	}
}
