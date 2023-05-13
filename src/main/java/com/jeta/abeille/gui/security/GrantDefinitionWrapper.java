package com.jeta.abeille.gui.security;

import com.jeta.abeille.database.model.DbObjectId;

import com.jeta.abeille.database.security.GrantDefinition;
import com.jeta.abeille.database.security.Privilege;

/**
 * This is a wrapper class for a GrantDefinition. It is used to keep track of a
 * single GrantDefinition as well as whether that definition has been modified.
 * 
 * @author Jeff Tassin
 */
public class GrantDefinitionWrapper {
	private GrantDefinition m_gdef;

	/** this the original grant mask */
	private int m_originalgrants;

	/**
	 * ctor
	 */
	public GrantDefinitionWrapper(GrantDefinition gdef) {
		m_gdef = gdef;
		if (gdef != null)
			m_originalgrants = gdef.getPrivilegeMask();
	}

	public void addPrivilege(Privilege priv) {
		assert (m_gdef != null);
		m_gdef.addPrivilege(priv);
	}

	/**
	 * @return the underlying grant defintion
	 */
	public GrantDefinition getGrantDefinition() {
		return m_gdef;
	}

	/**
	 * @return the name of the underlying object id
	 */
	public String getName() {
		DbObjectId objid = m_gdef.getObjectId();
		if (objid == null) {
			return "";
		} else {
			return objid.getObjectName();
		}
	}

	/**
	 * This is the original definition of the grant. The user may have altered
	 * the other in the GrantsView. We keep this value so we can compare the two
	 * when time to commit.
	 */
	public GrantDefinition getOriginalGrantDefinition() {
		GrantDefinition gdef = (GrantDefinition) m_gdef.clone();
		gdef.setPrivilegeMask(m_originalgrants);
		return gdef;
	}

	public boolean isGranted(Privilege priv) {
		if (priv == Privilege.OWN)
			return isOwner();
		else
			return m_gdef.isGranted(priv);
	}

	/**
	 * @return the flag that indicates if the GrantDefinition has been modified
	 */
	public boolean isModified() {
		return (m_originalgrants != m_gdef.getPrivilegeMask());
	}

	public boolean isOwner() {
		return m_gdef.isOwner();
	}

	public void removePrivilege(Privilege priv) {
		m_gdef.removePrivilege(priv);
	}

}
