package com.jeta.foundation.gui.components.maskedtextfield;

/**
 * This is a component that makes up a TSMaskedTextField A mask can have an id
 * as well. This is important for fields like the date field. It uses the id to
 * distinguish between year, month, and day components.
 * 
 * @author Jeff Tassin
 */
abstract public class MaskComponent {
	private String m_id; // an identifier to distinguish this mask from others

	abstract public String toString();

	/**
	 * Sets the id for this component
	 */
	public void setId(String id) {
		m_id = id;
	}

	/**
	 * @return the id for this component
	 */
	public String getId() {
		if (m_id != null)
			return m_id;
		else
			return this.toString();
	}
}
