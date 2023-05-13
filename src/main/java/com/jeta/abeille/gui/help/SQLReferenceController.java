package com.jeta.abeille.gui.help;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.jeta.open.gui.framework.JETAContainer;
import com.jeta.open.gui.framework.JETAController;

import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.utils.TSUtils;

public class SQLReferenceController extends TSController {
	private SQLReferenceFrame m_frame;

	public SQLReferenceController(JETAContainer view, SQLReferenceFrame frame) {
		super(view);
		m_frame = frame;

		assignAction(SQLReferenceNames.ID_SHOW_TOPIC, new ShowTopicAction());
	}

	public class ShowTopicAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			TSComboBox cbox = (TSComboBox) getView().getComponentByName(SQLReferenceNames.ID_TOPIC_COMBO);
			m_frame.showReference(m_frame.lookupSQLReference(TSUtils.fastTrim(cbox.getText())));
		}
	}
}
