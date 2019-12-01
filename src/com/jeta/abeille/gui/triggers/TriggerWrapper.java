package com.jeta.abeille.gui.triggers;

import java.util.Collection;
import java.util.Iterator;

import com.jeta.abeille.database.procedures.ProcedureParameter;

import com.jeta.abeille.database.triggers.Trigger;

import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This class is used to hold information about a table trigger. It defines all
 * attributes of a Trigger as well as the StoredProcedure that is fired by the
 * trigger.
 * 
 * @author Jeff Tassin
 */
public class TriggerWrapper {
	/** the trigger */
	private Trigger m_trigger;

	/** the name of the procedure */
	private String m_procedureName;

	/**
	 * the list of parameters passed to the procedure when the trigger is fired.
	 * This is formatted here as a single, comma delimited string
	 */
	private String m_argstr;

	/** temporary holders */
	private String m_when;

	/** temporary holders */
	private String m_event;

	/**
	 * ctor
	 */
	public TriggerWrapper(Trigger trigger) {
		m_trigger = trigger;

		StringBuffer pbuff = new StringBuffer();
		Collection params = m_trigger.getParameters();
		Iterator iter = params.iterator();
		while (iter.hasNext()) {
			ProcedureParameter param = (ProcedureParameter) iter.next();
			pbuff.append(param.getValue());
			if (iter.hasNext())
				pbuff.append(", ");
		}

		m_argstr = pbuff.toString();
	}

	/**
	 * @return a comma delimited string of arguments
	 */
	public String getParametersString() {
		return m_argstr;
	}

	/**
	 * @return the string that indicates the type of event that fires the
	 *         trigger
	 */
	public String getEvent() {
		if (m_event == null) {
			String[] str = new String[3];
			if (m_trigger.isDeleteEvent())
				str[0] = I18N.getLocalizedMessage("Delete");

			if (m_trigger.isInsertEvent())
				str[1] = I18N.getLocalizedMessage("Insert");

			if (m_trigger.isUpdateEvent())
				str[2] = I18N.getLocalizedMessage("Update");

			m_event = I18N.generateCSVList(str);
		}
		return m_event;
	}

	/**
	 * @return the name of the trigger
	 */
	public String getName() {
		return m_trigger.getName();
	}

	/**
	 * @return the name of the procedure that is invoked by the trigger
	 */
	public String getProcedureName() {
		return m_procedureName;
	}

	/**
	 * @return the trigger we are wrapping
	 */
	public Trigger getTrigger() {
		return m_trigger;
	}

	/**
	 * @return the string that indicates when the trigger is fired
	 */
	public String getWhen() {
		if (m_when == null) {
			if (m_trigger.isBefore())
				m_when = I18N.getLocalizedMessage("Before");
			else
				m_when = I18N.getLocalizedMessage("After");
		}
		return m_when;
	}

	/**
	 * Sets the name of the procedure that is invoked by the trigger
	 */
	public void setProcedureName(String procName) {
		m_procedureName = procName;
	}

}
