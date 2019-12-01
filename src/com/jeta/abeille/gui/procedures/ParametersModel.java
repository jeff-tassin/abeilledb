package com.jeta.abeille.gui.procedures;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.procedures.ParameterDirection;
import com.jeta.abeille.database.procedures.ProcedureParameter;
import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * This class manages the procedure parameters for a given stored procedure
 * 
 * @author Jeff Tassin
 */
class ParametersModel extends AbstractTableModel {
	/** an array of ProcedureParameter objects */
	private ArrayList m_data;

	/** an array of column names for this TableModel */
	private String[] m_colnames;

	/** this is the types of columns for this TableModel */
	private Class[] m_coltypes;

	/** The database connection */
	private TSConnection m_connection;

	/**
	 * The return type for the procedure
	 */
	private String m_returntype;

	/** column definitions */
	static final int VENDOR_TYPE_COLUMN = 0;
	static final int JDBC_TYPE_COLUMN = 1;

	private static final String RETURN_MSG = I18N.getLocalizedMessage("Return");

	/**
	 * ctor.
	 * 
	 * @param returnType
	 *            the return type for the procedure
	 * @param parameters
	 *            a collection of ProcedureParameter objects
	 */
	public ParametersModel() {
		String[] values = { I18N.getLocalizedMessage("Type"), I18N.getLocalizedMessage("JDBC Type") };
		m_colnames = values;
		Class[] types = { String.class, String.class };
		m_coltypes = types;
		m_data = new ArrayList();
	}

	/**
	 * ctor.
	 * 
	 * @param returnType
	 *            the return type for the procedure
	 * @param parameters
	 *            a collection of ProcedureParameter objects
	 */
	public ParametersModel(String returnType, Collection parameters) {
		this();

		m_returntype = returnType;
		if (parameters != null) {
			Iterator iter = parameters.iterator();
			while (iter.hasNext()) {
				Object obj = iter.next();
				assert (obj instanceof ProcedureParameter);
				ProcedureParameter param = (ProcedureParameter) obj;
				m_data.add(param);
			}
		}

	}

	/**
	 * Adds a parameter to the model
	 */
	public void addParameter(ProcedureParameter param) {
		if (m_data == null)
			m_data = new ArrayList();
		m_data.add(param);
		fireTableRowsInserted(m_data.size() - 1, m_data.size() - 1);
	}

	/**
	 * Deletes the procedure parameter at the given row
	 */
	public void deleteRow(int row) {
		if (row >= 0 && row < m_data.size()) {
			m_data.remove(row);
			fireTableChanged(new TableModelEvent(this));
		}

	}

	/**
	 * @return the number of columns in this model
	 */
	public int getColumnCount() {
		return m_colnames.length;
	}

	/**
	 * @return the name of a column at a given index
	 */
	public String getColumnName(int column) {
		return m_colnames[column];
	}

	/**
	 * @return the type of column at a given index
	 */
	public Class getColumnClass(int column) {
		return m_coltypes[column];
	}

	/**
	 * @return the set of ProcedureParameter objects in this model
	 */
	public Collection getParameters() {
		return m_data;
	}

	/**
	 * @return the return type for the procedure.
	 */
	public String getReturnType() {
		return m_returntype;
	}

	/**
	 * @return the object at the given row in the model
	 */
	public ProcedureParameter getRow(int row) {
		return (ProcedureParameter) m_data.get(row);
	}

	/**
	 * @return the number of parameters in this model
	 */
	public int getRowCount() {
		return m_data.size();
	}

	/**
	 * @return the ProcedureParameter at the given row (regardless of the
	 *         column)
	 */
	public Object getValueAt(int row, int column) {
		ProcedureParameter param = getRow(row);
		if (column == VENDOR_TYPE_COLUMN) {
			return param.getVendorType();
		} else if (column == JDBC_TYPE_COLUMN) {
			return DbUtils.getJDBCTypeName(param.getType());
		} else
			return "";
	}

	/**
    */
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	/**
	 * Remove all data items from this model
	 */
	public void removeAll() {
		m_data.clear();
	}

	/**
	 * Replaces an existing parameter with a new one
	 */
	public void replaceParameter(ProcedureParameter newParam, ProcedureParameter oldparam) {
		for (int index = 0; index < m_data.size(); index++) {
			ProcedureParameter param = getRow(index);
			if (param == oldparam) {
				m_data.set(index, newParam);
				fireTableRowsUpdated(index, index);
				break;
			}
		}

	}

	/**
	 * Sets the procedure parameters in this model
	 */
	public void setParameters(Collection params) {
		m_data.clear();
		Iterator iter = params.iterator();
		while (iter.hasNext()) {
			ProcedureParameter param = (ProcedureParameter) iter.next();
			m_data.add(param);
		}
		fireTableDataChanged();
	}

	/**
	 * Sets the return type for the procedure.
	 */
	public void setReturnType(String rettype) {
		m_returntype = rettype;
	}

}
