package com.jeta.abeille.gui.security;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.DbObjectType;
import com.jeta.abeille.database.model.DbObjectId;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.database.security.AbstractUser;
import com.jeta.abeille.database.security.GrantDefinition;
import com.jeta.abeille.database.security.Privilege;
import com.jeta.abeille.database.security.SecurityService;

import com.jeta.foundation.gui.table.JETATableModel;
import com.jeta.foundation.utils.TSUtils;

/**
 * This table model displays the grants for a database object. It is an abstract
 * class because the grants for tables, sequences, and views vary slightly from
 * those of functions
 * 
 * @author Jeff Tassin
 */
public abstract class GrantsModel extends JETATableModel {
	/** The database connection */
	private TSConnection m_connection;

	/** the catalog the current GrantDefinitions are from */
	private Catalog m_catalog;

	/** the schema the current GrantDefinitions are from */
	private Schema m_schema;

	/** the user associated with the current GrantDefinitions in the model */
	private AbstractUser m_user;

	/**
	 * the object type associated with the current GrantDefinitions in the model
	 */
	private DbObjectType m_objtype;

	/**
	 * hash map of ColumnGrantDefinition objects (values) to column index
	 * (Integer objects -keys )
	 */
	private HashMap m_columndefs = new HashMap();

	/**
	 * a regular expression that allows the user to filter the list of displayed
	 * objects in the model
	 */
	private String m_regexfilter;

	/** column definitions */
	static final int NAME_COLUMN = 0;

	/**
	 * ctor.
	 * 
	 * @param connection
	 *            the underlying database connection
	 */
	public GrantsModel(TSConnection connection) {
		m_connection = connection;
	}

	/**
	 * Adds the given collection of GrantDefinition objects to the model. If a
	 * regular expression is passed, the grantdef object name is checked against
	 * this expression for a match. If it does not match, it is not added.
	 * 
	 * @param grants
	 *            a collection of GrantDefinition objects to add
	 * @param regex
	 *            a regular expression that is used to filter which objects get
	 *            added. If null, then all objects are added.
	 * 
	 */
	public void addGrants(Collection grants, String regex) {
		Pattern pattern = null;
		try {
			if (regex != null) {
				regex = regex.trim();
				if (regex.length() > 0)
					pattern = Pattern.compile(regex);
			}
		} catch (Exception pe) {
			TSUtils.printException(pe);
		}

		Iterator iter = grants.iterator();
		while (iter.hasNext()) {
			GrantDefinition gdef = (GrantDefinition) iter.next();
			if (pattern != null) {
				Matcher matcher = pattern.matcher(gdef.getObjectId().getObjectName());
				if (matcher.find()) {
					addRow(new GrantDefinitionWrapper(gdef));
				}
			} else {
				addRow(new GrantDefinitionWrapper(gdef));
			}
		}
	}

	/**
	 * @return the Catalog that is associated with the data in this model
	 */
	public Catalog getCatalog() {
		return m_catalog;
	}

	/**
	 * @return the column grant definition for the given column
	 */
	protected ColumnGrantDefinition getColumnGrantDefinition(int column) {
		Integer ival = TSUtils.getInteger(column);
		return (ColumnGrantDefinition) m_columndefs.get(ival);
	}

	/**
	 * @return the database connection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return the filter used to display the objects in the view
	 */
	public String getFilter() {
		return m_regexfilter;
	}

	/**
	 * @return the GrantDefinition at the given row
	 */
	private GrantDefinition getGrantDefinition(int row) {
		GrantDefinitionWrapper wrapper = (GrantDefinitionWrapper) getRow(row);
		if (wrapper != null) {
			return wrapper.getGrantDefinition();
		} else {
			return null;
		}
	}

	/**
	 * @return the type of object that is associated with the grant data in this
	 *         model
	 */
	public DbObjectType getObjectType() {
		return m_objtype;
	}

	/**
	 * @return the Schema that is associated with the data in this model
	 */
	public Schema getSchema() {
		return m_schema;
	}

	/**
	 * @return the user that is associated with the data in this model
	 */
	public AbstractUser getUser() {
		return m_user;
	}

	/**
	 * @return the column value at the given row
	 */
	public Object getValueAt(int row, int column) {
		/** "Object Name", "Own", "Grants...." */
		GrantDefinitionWrapper wrapper = (GrantDefinitionWrapper) getRow(row);
		if (column == NAME_COLUMN) {
			return wrapper.getName();
		} else {
			ColumnGrantDefinition cdef = getColumnGrantDefinition(column);
			if (cdef != null) {
				return Boolean.valueOf(wrapper.isGranted(cdef.getPrivilege()));
			} else {
				assert (false);
			}
		}
		return "";
	}

	/**
	 * @return true if the given cell is editable
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		ColumnGrantDefinition cdef = getColumnGrantDefinition(columnIndex);
		if (cdef != null)
			return cdef.isEditable();
		else
			return false;
	}

	/**
	 * Tries to load the data from the security service
	 * 
	 * @param objType
	 *            the type of object to load (e.g. TABLE, VIEW, FUNCTION, etc )
	 * @param schema
	 *            the schema that contains the objects to load
	 * @param user
	 *            the user or group that we wish to load permissions for
	 * @param regex
	 *            a regular expression to match the object name for a given
	 *            object type
	 */
	public void loadData(DbObjectType objType, Catalog catalog, Schema schema, AbstractUser user, String regex) {
		try {
			SecurityService srv = (SecurityService) m_connection.getImplementation(SecurityService.COMPONENT_ID);
			assert (srv != null);
			Collection grants = srv.getGrants(objType, schema, user);
			addGrants(grants, regex);
			m_catalog = catalog;
			m_schema = schema;
			m_user = user;
			m_objtype = objType;
			m_regexfilter = regex;
		} catch (Exception e) {
			TSUtils.printException(e);
		}
	}

	/**
	 * Modifies the given grant in the database
	 */
	public void modifyGrant(GrantDefinition newdef, GrantDefinition olddef) throws SQLException {
		SecurityService srv = (SecurityService) m_connection.getImplementation(SecurityService.COMPONENT_ID);
		srv.modifyGrant(newdef, olddef);
	}

	/**
	 * Reloads the model with the current settings
	 * 
	 */
	public void reload() {
		reload(getObjectType(), getCatalog(), getSchema(), getUser(), getFilter());
	}

	/**
	 * Reloads the model
	 * 
	 * @param objType
	 *            the type of object to load (e.g. TABLE, VIEW, FUNCTION, etc )
	 * @param schema
	 *            the schema that contains the objects to load
	 * @param user
	 *            the user or group that we wish to load permissions for
	 * @param regex
	 *            a regular expression to match the object name for a given
	 *            object type
	 */
	public void reload(DbObjectType objType, Catalog catalog, Schema schema, AbstractUser user, String regex) {
		removeAll();
		loadData(objType, catalog, schema, user, regex);
		fireTableDataChanged();
	}

	/**
	 * Sets the Catalog that is associated with the data in this model
	 */
	protected void setCatalog(Catalog catalog) {
		m_catalog = catalog;
	}

	/**
	 * Sets the column grant definition for the given column
	 */
	protected void setColumnGrantDefinition(int column, ColumnGrantDefinition cdef) {
		Integer ival = TSUtils.getInteger(column);
		m_columndefs.put(ival, cdef);
	}

	/**
	 * @return the filter used to display the objects in the view
	 */
	protected void setFilter(String filter) {
		m_regexfilter = filter;
	}

	protected void setObjectType(DbObjectType objtype) {
		m_objtype = objtype;
	}

	/**
	 * Sets the Schema that is associated with the data in this model
	 */
	protected void setSchema(Schema schema) {
		m_schema = schema;
	}

	protected void setUser(AbstractUser user) {
		m_user = user;
	}

	/**
	 * Sets the value at the given row and column.
	 */
	public void setValueAt(Object aValue, int row, int column) {
		GrantDefinitionWrapper wrapper = (GrantDefinitionWrapper) getRow(row);
		ColumnGrantDefinition cdef = getColumnGrantDefinition(column);
		if (cdef != null && cdef.isEditable()) {
			Boolean bval = (Boolean) aValue;
			if (bval.booleanValue()) {
				wrapper.addPrivilege(cdef.getPrivilege());
			} else {
				wrapper.removePrivilege(cdef.getPrivilege());
			}
			fireTableRowsUpdated(row, row);
		}
	}

	/**
	 * Associates a privilege with a given column.
	 */
	public static class ColumnGrantDefinition {
		private Privilege m_priv;
		private boolean m_editable = true;

		public ColumnGrantDefinition(Privilege priv) {
			m_priv = priv;
		}

		public ColumnGrantDefinition(Privilege priv, boolean beditable) {
			m_priv = priv;
			m_editable = beditable;
		}

		public Privilege getPrivilege() {
			return m_priv;
		}

		public boolean isEditable() {
			return m_editable;
		}
	}
}
