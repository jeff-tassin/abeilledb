package com.jeta.abeille.database.model;

import java.util.ArrayList;

import com.jeta.foundation.utils.TSUtils;

/**
 * This event is for changes to the database model that are actually written to
 * the database
 * 
 * @author Jeff Tassin
 */
public class DbModelEvent {
	public final static int TABLE_CHANGED = 1;
	public final static int TABLE_RENAMED = 2;
	/** fired when a table is dropped from the model */
	public final static int TABLE_DELETED = 3;
	public final static int TABLE_CREATED = 4;

	/** fired when we explicitly know a view has changed */
	public final static int VIEW_CHANGED = 6;
	/** fired when we explicitly know a view has changed */
	public final static int VIEW_CREATED = 7;

	/**
	 * fired when it is okay to update any database gui components that indicate
	 * database status on a periodic basis. For example, the current schema
	 * needs to be periodically updated.
	 */
	public final static int STATUS_UPDATE = 8;

	/**
	 * fired when the catalog or schema is finished (re)loading. If the database
	 * does not support catalogs, the SCHEMA_LOADED message is sent when the
	 * given catalog is finished loading
	 */
	public final static int SCHEMA_LOADED = 51;

	/** fired when the model is finished (re)loading */
	public final static int MODEL_START_RELOAD = 52;

	/**
	 * the id of the event
	 */
	private int m_eventid;

	/** the id of the table that the event refers to */
	private TableId m_tableid;

	/** the list of parameters for this event */
	private ArrayList m_params = new ArrayList();

	/** the model associated with this event */
	private DbModel m_model;

	/** the schema associated with this event */
	private Schema m_schema;

	/**
	 * ctor
	 */
	public DbModelEvent(DbModel model, int id) {
		m_eventid = id;
		setModel(model);
	}

	/**
	 * ctor
	 */
	public DbModelEvent(DbModel model, int id, TableId tableId) {
		m_eventid = id;
		m_tableid = tableId;
		setModel(model);
		addParameter(tableId);
		if (tableId != null) {
			setSchema(tableId.getSchema());
		}
	}

	/**
	 * ctor (rename event)
	 */
	public DbModelEvent(DbModel model, TableId newId, TableId oldId) {
		m_eventid = TABLE_RENAMED;
		m_tableid = oldId;
		setModel(model);
		addParameter(oldId);
		addParameter(newId);
		if (newId != null) {
			setSchema(newId.getSchema());
		}
	}

	/**
	 * @return the event id
	 */
	public int getID() {
		return m_eventid;
	}

	public Schema getSchema() {
		return m_schema;
	}

	/**
	 * @return the tableid
	 */
	public TableId getTableId() {
		return m_tableid;
	}

	/**
	 * Adds a parameter to the event
	 */
	public void addParameter(Object obj) {
		m_params.add(obj);
	}

	/**
	 * @return the catalog that is associated with this event
	 */
	public Catalog getCatalog() {
		return m_model.getCatalog();
	}

	/**
	 * @return the connection that is associated with this event
	 */
	public TSConnection getConnection() {
		return m_model.getConnection();
	}

	/**
	 * Get the parameter at the given (zero-based) index. When tableid is not
	 * null, the zero index is always the tableid
	 * 
	 * @return the parameter at the given index
	 */
	public Object getParameter(int index) {
		return m_params.get(index);
	}

	/**
	 * @return the number of parameters in this event.
	 */
	public int getParameterCount() {
		return m_params.size();
	}

	/**
	 * Sets the database connection
	 */
	public void setModel(DbModel model) {
		m_model = model;
	}

	/**
	 * Sets the schema associated with this event
	 */
	public void setSchema(Schema schema) {
		m_schema = schema;
		if (TSUtils.isDebug()) {
			TSConnection conn = getConnection();
			if (conn != null) {
				if (conn.supportsSchemas()) {
					assert (m_schema != Schema.VIRTUAL_SCHEMA);
				} else {
					assert (m_schema == Schema.VIRTUAL_SCHEMA);
				}
			}
		}
	}
}
