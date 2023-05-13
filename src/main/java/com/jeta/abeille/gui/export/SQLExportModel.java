package com.jeta.abeille.gui.export;

import java.io.ObjectStreamException;
import java.io.IOException;

import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.gui.queryresults.QueryResultsModel;

import com.jeta.foundation.common.JETAExternalizable;

import com.jeta.foundation.gui.table.TableSelection;
import com.jeta.foundation.gui.table.export.ExportModel;

/**
 * This class defines a model for exporting SQL results
 * 
 * @author Jeff Tassin
 */
public class SQLExportModel extends ExportModel implements JETAExternalizable {
	static final long serialVersionUID = 1551916214254255965L;

	public static int VERSION = 1;

	/** the table model that handles the column options */
	private ColumnOptionsModel m_comodel;

	/** the filename that we are exporting to */
	private String m_filename;

	/** get the data model that represents the data we will export */
	private QueryResultsModel m_datamodel;

	/** if we are explicitly exporting the results from a single table */
	private TableId m_tableid;

	/** flag that indicates if an export is currently active */
	private boolean m_exporting = false;

	/**
	 * Set to true if we should export text data as SQL text.
	 */
	private boolean m_sqltextdelimit = true;

	/**
	 * if we are exporting a selected area of a table rather than the entire
	 * results. The selected are can be discontinuous
	 */
	private TableSelection m_selection;

	/**
	 * ctor only for serialization
	 */
	public SQLExportModel() {

	}

	/**
	 * ctor
	 */
	SQLExportModel(QueryResultsModel model, TableId tableid) {
		m_datamodel = model;
		m_comodel = new ColumnOptionsModel(model);
		m_tableid = tableid;
		setColumnExportModel(m_comodel.getColumnExportModel());
	}

	/**
	 * ctor
	 */
	SQLExportModel(QueryResultsModel model, TableSelection selection) {
		m_datamodel = model;
		m_selection = selection;
		m_comodel = new ColumnOptionsModel(model, selection);
		setColumnExportModel(m_comodel.getColumnExportModel());
	}

	/**
	 * @return the underlying column options table model
	 */
	public ColumnOptionsModel getColumnOptionsModel() {
		return m_comodel;
	}

	/**
	 * @return the name of the file where we will be storing the output
	 */
	public String getFileName() {
		return m_filename;
	}

	/**
	 * @return the query model
	 */
	public QueryResultsModel getQueryModel() {
		return m_datamodel;
	}

	/**
	 * If the results belong to a single table, this will return a non-null
	 * table id. Otherwise the returned value will be null
	 */
	public TableId getTableId() {
		return m_tableid;
	}

	/**
	 * If we are exporting a selected area of the table, then this is it.
	 * Otherwise, this value will be the entire data model
	 */
	public TableModel getSelection() {
		if (m_selection == null)
			return getQueryModel();
		else
			return m_selection;
	}

	/**
	 * @return the flag that indicates if an export operation is currently
	 *         active
	 */
	public boolean isExporting() {
		return m_exporting;
	}

	/**
	 * @return the flag that indicates if a text delimiter is found in a text
	 *         value, then it should be repeated as is required in a SQL text
	 *         value.
	 */
	public boolean isSQLTextDelimit() {
		return m_sqltextdelimit;
	}

	/**
	 * Resets the model to the default settings
	 */
	public void reset() {
		for (int row = 0; row < m_comodel.getRowCount(); row++) {
			TableColumnExportSetting setting = m_comodel.getRow(row);
			setting.setSQLFormat(false);
			setting.setOutputExpression(ExportNames.VALUE_EXPRESSION);
		}
		setLineDecorator(ExportNames.LINE_EXPRESSION);
		m_comodel.fireTableChanged(new TableModelEvent(m_comodel));
	}

	/**
	 * Sets the flag that indicates if the export operation is currently active
	 */
	public void setExporting(boolean bexporting) {
		m_exporting = bexporting;
	}

	/**
	 * Sets the name of the file we are exporting to
	 */
	public void setFileName(String fName) {
		m_filename = fName;
	}

	/**
	 * Changes all the settings so that the data is exported in SQL format
	 */
	public void setSQLFormat() {
		for (int row = 0; row < m_comodel.getRowCount(); row++) {
			TableColumnExportSetting setting = m_comodel.getRow(row);
			setting.setSQLFormat(true);
		}

		StringBuffer ibuff = new StringBuffer();
		ibuff.append("INSERT INTO ");
		ibuff.append(m_tableid.getFullyQualifiedName());
		ibuff.append(" (");

		for (int row = 0; row < m_comodel.getRowCount(); row++) {
			TableColumnExportSetting setting = m_comodel.getRow(row);
			ibuff.append(setting.getColumnName());
			if ((row + 1) < m_comodel.getRowCount())
				ibuff.append(", ");
		}

		ibuff.append(") VALUES (");
		ibuff.append(ExportNames.LINE_EXPRESSION);
		ibuff.append(");");

		setLineDecorator(ibuff.toString());

		m_comodel.fireTableChanged(new TableModelEvent(m_comodel));
	}

	/**
	 * Sets a flag that indicates if a text delimiter is found in a text value,
	 * then it should be repeated as is required in a SQL text value.
	 */
	public void setSQLTextDelimit(boolean bsql) {
		m_sqltextdelimit = bsql;
	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		super.readExternal(in);
		int version = in.readInt();

		m_comodel = (ColumnOptionsModel) in.readObject();
		m_filename = (String) in.readObject();
		m_datamodel = (QueryResultsModel) in.readObject();
		m_tableid = (TableId) in.readObject();
		m_exporting = in.readBoolean();
		m_sqltextdelimit = in.readBoolean();
		m_selection = (TableSelection) in.readObject();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(VERSION);
		out.writeObject(m_comodel);
		out.writeObject(m_filename);
		out.writeObject(m_datamodel);
		out.writeObject(m_tableid);
		out.writeBoolean(m_exporting);
		out.writeBoolean(m_sqltextdelimit);
		out.writeObject(m_selection);
	}

}
