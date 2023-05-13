package com.jeta.abeille.gui.security;

import com.jeta.abeille.database.security.GrantDefinition;
import com.jeta.abeille.database.security.Privilege;

/**
 * This is a wrapper class for a GrantDefinition. It is used to keep track of a
 * single GrantDefinition as well as whether that definition has been modified.
 * 
 * @author Jeff Tassin
 */
public class OrthoGrantDefinitionWrapper extends GrantDefinitionWrapper {
	private Privilege m_priv;

	private boolean m_granted;
	private boolean m_originalgranted;

	/**
	 * ctor
	 */
	public OrthoGrantDefinitionWrapper(Privilege p, boolean bgranted) {
		super(null);
		m_priv = p;
		m_originalgranted = bgranted;
		m_granted = bgranted;
	}

	public void addPrivilege(Privilege priv) {
		assert (priv == Privilege.GRANT);
		m_granted = true;
	}

	/**
	 * @return the name of the underlying object id
	 */
	public String getName() {
		return m_priv.getName();
	}

	public boolean isGranted(Privilege priv) {
		assert (priv == Privilege.GRANT);
		return m_granted;
	}

	public Privilege getPrivilege() {
		return m_priv;
	}

	/**
	 * @return the flag that indicates if the GrantDefinition has been modified
	 */
	public boolean isModified() {
		return (m_originalgranted != m_granted);
	}

	public void removePrivilege(Privilege priv) {
		assert (priv == Privilege.GRANT);
		m_granted = false;
	}

}
