package com.jeta.abeille.gui.importer;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.Collection;
import java.util.LinkedList;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.model.ModelViewModel;

import com.jeta.abeille.gui.queryresults.QueryResultSet;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * This is the model for the form builder
 * 
 * @author Jeff Tassin
 */
public class ImportBuilderModel extends ModelViewModel implements JETAExternalizable {
	static final long serialVersionUID = 4496172996650475038L;

	public static int VERSION = 1;

	/** the table that is the target of the import */
	private TableId m_targetid;

	/** a collection of TargetColumnInfo objects */
	private Collection m_targetcolumns;

	/**
	 * The source data for the import
	 */
	private QueryResultSet m_rset;

	/**
	 * default ctor for serialization
	 */
	public ImportBuilderModel() {

	}

	/**
	 * Standard constructor
	 * 
	 * @param connection
	 *            this is the database connection. We need this to load the
	 *            metadata and links for tables we are working with
	 */
	ImportBuilderModel(TSConnection connection) {
		super("import.builder", null, null);
	}

	/**
	 * @return the source data for the import
	 */
	public QueryResultSet getQueryResults() {
		return m_rset;
	}

	/**
	 * @return the collection of target columns (TargetColumnInfo objects) for
	 *         the import
	 */
	public Collection getTargetColumns() {
		return m_targetcolumns;
	}

	/**
	 * @return the id of the target table for the import
	 */
	public TableId getTargetTable() {
		return m_targetid;
	}

	/**
	 * @return the source data for the import
	 */
	public void setQueryResults(QueryResultSet rset) {
		m_rset = rset;
	}

	/**
	 * Sets the collection of target columns (TargetColumnInfo objects) for the
	 * import
	 */
	public void setTargetColumns(Collection cols) {
		m_targetcolumns = cols;
	}

	public void setTargetTable(TableId tableid) {
		m_targetid = tableid;
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		int version = in.readInt();
		m_targetid = (TableId) in.readObject();
		m_targetcolumns = (Collection) in.readObject();
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(VERSION);
		out.writeObject(m_targetid);
		out.writeObject(m_targetcolumns);
	}
}
