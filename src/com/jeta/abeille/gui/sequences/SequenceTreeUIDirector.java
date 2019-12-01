package com.jeta.abeille.gui.sequences;

import com.jeta.open.gui.framework.UIDirector;
import com.jeta.abeille.database.sequences.Sequence;

/**
 * The UIDirector for the sequence tree
 */
public class SequenceTreeUIDirector implements UIDirector {
	private SequenceTree m_view;

	/**
	 * ctor
	 */
	public SequenceTreeUIDirector(SequenceTree view) {
		m_view = view;
	}

	/**
	 * UIDirector implementation
	 */
	public void updateComponents(java.util.EventObject evt) {
		Sequence seq = m_view.getSelectedSequence();
		boolean benable = (seq != null);
		m_view.enableComponent(SequenceNames.ID_EDIT_SEQUENCE, benable);
		m_view.enableComponent(SequenceNames.ID_DROP_SEQUENCE, benable);
	}
}
