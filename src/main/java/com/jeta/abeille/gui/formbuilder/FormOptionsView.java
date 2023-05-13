package com.jeta.abeille.gui.formbuilder;

import com.jeta.forms.components.panel.FormPanel;

import com.jeta.foundation.utils.TSUtils;

public class FormOptionsView extends FormPanel {
	public static final String ID_FORM_NAME = "form.name"; // javax.swing.JTextField

	public FormOptionsView(FormModel model) {
		super("com/jeta/abeille/gui/formbuilder/formOptions.jfrm");
		setText(ID_FORM_NAME, model.getName());
	}

	public String getFormName() {
		return TSUtils.fastTrim(getText(ID_FORM_NAME));
	}

}
