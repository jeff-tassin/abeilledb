package com.jeta.abeille.gui.procedures;

import com.jeta.abeille.database.procedures.ProcedureParameter;
import com.jeta.abeille.database.procedures.StoredProcedure;

import com.jeta.foundation.i18n.I18N;

/**
 * This class is a wrapper for a stored procedure. It is used mainly for the
 * args combo box in the ProcedureView. It displays the different function
 * arguments so the user can choose between different procedures/functions with
 * the same name.
 * 
 * @author Jeff Tassin
 */
public class ProcedureWrapper {
	private StoredProcedure m_proc;

	/**
	 * A string delimited list of parameters
	 */
	private String m_args;

	/**
	 * ctor
	 */
	public ProcedureWrapper(StoredProcedure proc) {
		m_proc = proc;
	}

	/**
	 * @return the procedure we are a wrapper for
	 */
	public StoredProcedure getProcedure() {
		return m_proc;
	}

	/**
	 * @return the args for this procedure
	 */
	public String toString() {
		if (m_proc == null) {
			m_args = "";
		} else if (m_args == null) {
			if (m_proc.getParameterCount() == 0) {
				m_args = I18N.getLocalizedMessage("None");
			} else {
				StringBuffer args = new StringBuffer();
				for (int index = 0; index < m_proc.getParameterCount(); index++) {
					ProcedureParameter param = m_proc.getParameter(index);
					args.append(param.getVendorType());
					if ((index + 1) < m_proc.getParameterCount()) {
						args.append(", ");
					}
				}
				m_args = args.toString();
			}
		}
		return m_args;
	}
}
