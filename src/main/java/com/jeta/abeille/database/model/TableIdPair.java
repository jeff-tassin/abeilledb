package com.jeta.abeille.database.model;

public class TableIdPair {
	private TableId m_newid;
	private TableId m_oldid;

	public TableIdPair(TableId newId, TableId oldId) {
		m_newid = newId;
		m_oldid = oldId;
	}

	public TableId getNewId() {
		return m_newid;
	}

	public TableId getOldId() {
		return m_oldid;
	}

}
