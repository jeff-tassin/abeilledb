package com.jeta.abeille.gui.query;

import java.awt.*;
import java.util.*;
import javax.swing.*;

import com.jeta.foundation.gui.utils.*;

public class ConstraintObject {
	private ImageIcon m_icon;
	private String m_label;

	public ConstraintObject(String imageFile, String label) {
		m_icon = TSGuiToolbox.loadImage(imageFile);
		m_label = label;
	}

	public String getLabel() {
		return m_label;
	}

	public ImageIcon getIcon() {
		return m_icon;
	}

}
