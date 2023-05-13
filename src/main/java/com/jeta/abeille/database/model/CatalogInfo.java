package com.jeta.abeille.database.model;

/**
 * Associates a database metadata model with a given catalog
 * 
 * @author Jeff Tassin
 */
public class CatalogInfo {
	private Catalog m_catalog;
	private DbModel m_model;

	public CatalogInfo(Catalog cat, DbModel model) {
		m_catalog = cat;
		m_model = model;
		assert (m_model != null);
	}

	public DbModel getModel() {
		return m_model;
	}

	/**
	 * @return true if the model associated with this catalog is currently being
	 *         loaded
	 */
	public boolean isLoading(Schema schema) {
		if (m_model == null)
			return true;

		return m_model.isLoading(schema);
	}

}
