package com.jeta.abeille.gui.model.options;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JColorChooser;
import javax.swing.JComponent;

import com.jeta.abeille.gui.model.ModelViewSettings;
import com.jeta.foundation.gui.components.JETAInkWell;
import com.jeta.foundation.gui.components.TSController;

public class ModelViewPreferencesController extends TSController {
	/** the view we are handling events for */
	private ModelViewPreferencesView m_view;

	public ModelViewPreferencesController(ModelViewPreferencesView view) {
		super(view);
		m_view = view;
		assignAction(ModelViewSettings.ID_TABLE_FOREGROUND, new ColorAction(ModelViewSettings.ID_TABLE_FOREGROUND));
		assignAction(ModelViewSettings.ID_TABLE_BACKGROUND, new ColorAction(ModelViewSettings.ID_TABLE_BACKGROUND));
		assignAction(ModelViewSettings.ID_PROTOTYPE_FOREGROUND, new ColorAction(
				ModelViewSettings.ID_PROTOTYPE_FOREGROUND));
		assignAction(ModelViewSettings.ID_PROTOTYPE_BACKGROUND, new ColorAction(
				ModelViewSettings.ID_PROTOTYPE_BACKGROUND));
		assignAction(ModelViewSettings.ID_PRIMARY_KEY_FOREGROUND, new ColorAction(
				ModelViewSettings.ID_PRIMARY_KEY_FOREGROUND));
		assignAction(ModelViewSettings.ID_FOREIGN_KEY_FOREGROUND, new ColorAction(
				ModelViewSettings.ID_FOREIGN_KEY_FOREGROUND));
	}

	public class ColorAction implements ActionListener {
		private String m_id;

		public ColorAction(String id) {
			m_id = id;
		}

		public void actionPerformed(ActionEvent evt) {
			JETAInkWell comp = (JETAInkWell) evt.getSource();
			Color c = JColorChooser.showDialog(null, "Color Chooser", comp.getColor());
			if (c != null) {
				comp.setColor(c);
			}
		}
	}

}
