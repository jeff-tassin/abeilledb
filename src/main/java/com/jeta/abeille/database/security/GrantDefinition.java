package com.jeta.abeille.database.security;

import com.jeta.abeille.database.model.DbObjectId;

/**
 * This class defines the list of privileges given to a user for a database
 * object
 * 
 * @author Jeff Tassin
 */
public class GrantDefinition implements Comparable, Cloneable {
	/** a mask that defines the privileges */
	private int m_privmask = 0;

	/** the user/group that this definition applies to */
	private AbstractUser m_user;

	/** the object that this grant is assocated with */
	private DbObjectId m_objid;

	private boolean m_owner = false;

	/**
	 * Default ctor
	 */
	public GrantDefinition() {

	}

	/**
	 * ctor
	 */
	public GrantDefinition(DbObjectId objId) {
		m_objid = objId;
	}

	/**
	 * Add the given privilege for this definition
	 */
	public void addGrant(Privilege priv) {
		m_privmask |= priv.getMask();
	}

	/**
	 * Add the given privilege for this definition
	 */
	public void addPrivilege(Privilege priv) {
		addGrant(priv);
	}

	/**
	 * Cloneable implementation
	 */
	public Object clone() {
		GrantDefinition gdef = new GrantDefinition();
		gdef.m_privmask = m_privmask;
		gdef.m_user = m_user;
		gdef.m_objid = m_objid;
		gdef.m_owner = m_owner;
		return gdef;
	}

	/**
	 * Comparable interface
	 */
	public int compareTo(Object o) {
		if (o == this)
			return 0;
		else if (o instanceof GrantDefinition) {
			GrantDefinition gdef = (GrantDefinition) o;
			if (m_objid == null) {
				return -1;
			} else {
				int result = m_objid.compareTo(gdef.m_objid);
				if (result == 0) {
					if (m_user == null) {
						return -1;
					} else {
						return m_user.compareTo(gdef.m_user);
					}
				}
				return result;
			}
		} else
			return -1;

	}

	/**
	 * Comparable
	 */
	public boolean equals(Object obj) {
		return (compareTo(obj) == 0);
	}

	/**
	 * @return the object id
	 */
	public DbObjectId getObjectId() {
		return m_objid;
	}

	/**
	 * @returns the privilege mask for this definition. NOTE, this should be
	 *          used only in special cases where performance is critical since
	 *          the implementation may change in the future.
	 */
	public int getPrivilegeMask() {
		return m_privmask;
	}

	/**
	 * @return the user this grant is assocated with
	 */
	public AbstractUser getUser() {
		return m_user;
	}

	/**
	 * @return true if the given privilege is part of the grants
	 */
	public boolean isGranted(Privilege priv) {
		int result = priv.getMask() & m_privmask;
		return (result != 0);
	}

	public int hashCode() {
		return m_objid.hashCode();
	}

	/**
	 * @return true if the user is also the owner of the object
	 */
	public boolean isOwner() {
		return m_owner;
	}

	/**
	 * Removes the given privilege from this definition
	 */
	public void removeGrant(Privilege priv) {
		m_privmask &= (~priv.getMask());
	}

	/**
	 * Removes the given privilege from this definition
	 */
	public void removePrivilege(Privilege priv) {
		removeGrant(priv);
	}

	/**
	 * Sets the object id associated with this grant
	 */
	public void setObjectId(DbObjectId objid) {
		m_objid = objid;
	}

	/**
	 * Sets the user as the owner of the given object
	 */
	public void setOwner(boolean owner) {
		m_owner = owner;
	}

	/**
	 * Sets the privilege mask for this definition. NOTE, this should be used
	 * only in special cases where performance is critical since the
	 * implementation may change in the future.
	 */
	public void setPrivilegeMask(int mask) {
		m_privmask = mask;
	}

	/**
	 * Sets the user this grant is assocated with
	 */
	public void setUser(AbstractUser user) {
		m_user = user;
	}

}
