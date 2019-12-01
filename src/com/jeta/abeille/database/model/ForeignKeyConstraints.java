package com.jeta.abeille.database.model;

import java.io.ObjectStreamException;
import java.io.IOException;

import com.jeta.foundation.common.JETAExternalizable;

/**
 * This class defines constraints for a foreign key in Abeille 1. Actions - on
 * update and on delete 2. Deferrable
 * 
 * @author Jeff Tassin
 */
public class ForeignKeyConstraints implements JETAExternalizable {
	static final long serialVersionUID = 282908756648488457L;

	public static int VERSION = 1;

	/** defines when the foreign key constraint will be checked */
	private boolean m_deferrable;
	private boolean m_initiallydeferred;

	/** actions */
	private int m_onupdate;
	private int m_ondelete;

	/** action constants */

	public static final int NO_ACTION = java.sql.DatabaseMetaData.importedKeyNoAction;
	public static final int CASCADE = java.sql.DatabaseMetaData.importedKeyCascade;
	public static final int SET_NULL = java.sql.DatabaseMetaData.importedKeySetNull;
	public static final int SET_DEFAULT = java.sql.DatabaseMetaData.importedKeySetDefault;
	public static final int RESTRICT = java.sql.DatabaseMetaData.importedKeyRestrict;

	/**
	 * ctor
	 */
	public ForeignKeyConstraints() {

	}

	/**
	 * @return the action constant the defines the behavior of the foreign key
	 *         when related data is updated
	 */
	public int getUpdateAction() {
		return m_onupdate;
	}

	/**
	 * @return the action constant the defines the behavior of the foreign key
	 *         when related data is delete
	 */
	public int getDeleteAction() {
		return m_ondelete;
	}

	/**
	 * @return true if this foreign key is deferrable
	 */
	public boolean isDeferrable() {
		return m_deferrable;
	}

	/**
	 * @return true if this foreign key is initially deferred
	 */
	public boolean isInitiallyDeferred() {
		return m_initiallydeferred;
	}

	/**
	 * Sets if the transaction type is deferrable
	 */
	public void setDeferrable(boolean deferrable) {
		m_deferrable = deferrable;
	}

	/**
	 * Sets the delete action
	 */
	public void setDeleteAction(int actioncode) {
		// if ( actioncode == java.sql.DatabaseMetaData.importedKeyRestrict )
		// actioncode = NO_ACTION;
		m_ondelete = actioncode;
	}

	/**
	 * Sets if the initially deferred flag
	 */
	public void setInitiallyDeferred(boolean initially) {
		m_initiallydeferred = initially;
	}

	/**
	 * Sets the update action
	 */
	public void setUpdateAction(int actioncode) {
		// if ( actioncode == java.sql.DatabaseMetaData.importedKeyRestrict )
		// actioncode = NO_ACTION;

		m_onupdate = actioncode;
	}

	public static String toActionSQL(int action) {
		switch (action) {
		case CASCADE:
			return "CASCADE";

		case SET_NULL:
			return "SET NULL";

		case SET_DEFAULT:
			return "SET DEFAULT";

		case RESTRICT:
			return "RESTRICT";

		default:
			return "NO ACTION";
		}
	}

	public static int fromString(String actionName) {
		if (actionName == null)
			return NO_ACTION;
		else if (actionName.equalsIgnoreCase("CASCADE"))
			return CASCADE;
		else if (actionName.equalsIgnoreCase("SET NULL"))
			return SET_NULL;
		else if (actionName.equalsIgnoreCase("SET DEFAULT"))
			return SET_DEFAULT;
		else if (actionName.equalsIgnoreCase("RESTRICT"))
			return RESTRICT;
		else
			return NO_ACTION;

	}

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		int version = in.readInt();
		m_deferrable = in.readBoolean();
		m_initiallydeferred = in.readBoolean();
		m_onupdate = in.readInt();
		m_ondelete = in.readInt();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		out.writeInt(VERSION);
		out.writeBoolean(m_deferrable);
		out.writeBoolean(m_initiallydeferred);
		out.writeInt(m_onupdate);
		out.writeInt(m_ondelete);
	}

}
