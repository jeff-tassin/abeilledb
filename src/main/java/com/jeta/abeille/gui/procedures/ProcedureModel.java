package com.jeta.abeille.gui.procedures;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.procedures.ProcedureLanguage;
import com.jeta.abeille.database.procedures.ProcedureParameter;
import com.jeta.abeille.database.procedures.StoredProcedure;
import com.jeta.abeille.database.procedures.StoredProcedureService;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.interfaces.app.ObjectStore;

/**
 * This is the stored procedure model for the query view/editor
 * 
 * @author Jeff Tassin
 */
public class ProcedureModel {
	/**
	 * The database connection
	 */
	private transient TSConnection m_connection;

	/**
	 * The underlying stored procedure for this model
	 */
	private StoredProcedure m_procedure;

	/**
	 * The parameters (ProcedureParameter objects) for this procedure
	 */
	private ArrayList m_parameters = new ArrayList();

	/**
	 * ctor - serialization
	 */
	public ProcedureModel(TSConnection conn) {
		m_connection = conn;
		assert (m_connection != null);
	}

	/**
	 * ctor call create(...) to create a ProcedureModel instance
	 */
	public ProcedureModel(TSConnection conn, StoredProcedure proc) {
		this(conn);
		setProcedure(proc);
		// load default parameters into model
		m_parameters = new ArrayList();
	}

	/**
	 * @return the underlying database connection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return the language of the procedure
	 */
	public ProcedureLanguage getLanguage() {
		if (m_procedure == null)
			return null;
		else
			return m_procedure.getLanguage();
	}

	/**
	 * @return the name of the procedure
	 */
	public String getName() {
		if (m_procedure == null)
			return "";
		else
			return m_procedure.getName();
	}

	/**
	 * @return the number of parameters in the procedure
	 */
	public int getParameterCount() {
		return m_parameters.size();
	}

	/**
	 * @return the procedure parameter at the given index
	 */
	public ProcedureParameter getParameter(int index) {
		return (ProcedureParameter) m_parameters.get(index);
	}

	/**
	 * @return the parameters (ProcedureParameter) for this model
	 */
	public Collection getParameters() {
		return m_parameters;
	}

	/**
	 * @return the stored procedure instance
	 */
	public StoredProcedure getProcedure() {
		return m_procedure;
	}

	/**
	 * @return the key used to identify the procedure in the object store.
	 */
	static String getProcedureKey(StoredProcedure procedure) {
		StringBuffer keybuff = new StringBuffer();
		keybuff.append("stored.procedure.settings.");
		keybuff.append(procedure.getSchema());
		keybuff.append(".");
		keybuff.append(procedure.getName());
		keybuff.append(".");
		keybuff.append(procedure.getKey());
		return keybuff.toString();
	}

	/**
	 * @return the return type for the procedure.
	 */
	public String getReturnType() {
		if (m_procedure == null) {
			return "";
		} else {
			ProcedureParameter param = m_procedure.getReturnParameter();
			if (param == null)
				return "";
			else
				return param.getVendorType();
		}
	}

	/**
	 * @return the source code for hte procedure
	 */
	public String getSource() {
		if (m_procedure == null)
			return "";
		else
			return m_procedure.getSource();
	}

	/**
	 * Gets the latest procedure parameters from the database
	 */
	public static StoredProcedure loadProcedure(TSConnection connection, StoredProcedure proc) throws SQLException {
		if (proc != null) {
			StoredProcedureService tsprocs = (StoredProcedureService) connection
					.getImplementation(StoredProcedureService.COMPONENT_ID);
			return tsprocs.load(proc);
		} else
			return null;
	}

	/**
	 * Load the latest procedure information from the database
	 */
	void setProcedure(StoredProcedure proc) {
		try {
			m_procedure = proc;
			m_parameters.clear();
			if (proc != null) {
				if (m_procedure != null) {
					for (int index = 0; index < m_procedure.getParameterCount(); index++) {
						ProcedureParameter param = m_procedure.getParameter(index);
						String pname = param.getName();
						if (pname == null || pname.length() == 0) {
							param.setName(I18N.format("procedure_arg_1", new Integer(index + 1)));
						}
						m_parameters.add(param);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Sets the parameters (ProcedureParameter) for this model
	 */
	public void setParameters(ParametersModel model) {
		m_parameters = new ArrayList(model.getParameters());
	}

}
