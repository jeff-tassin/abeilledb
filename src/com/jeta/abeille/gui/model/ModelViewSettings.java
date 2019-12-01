package com.jeta.abeille.gui.model;

import java.awt.Color;
import java.util.HashMap;

public class ModelViewSettings {
	private static HashMap m_colors = new HashMap();

	public static final String ID_TABLE_FOREGROUND = "table.foreground.color";
	public static final String ID_TABLE_BACKGROUND = "table.background.color";
	public static final String ID_PROTOTYPE_FOREGROUND = "prototype.foreground.color";
	public static final String ID_PROTOTYPE_BACKGROUND = "prototype.background.color";
	public static final String ID_PRIMARY_KEY_FOREGROUND = "primary.key.foreground";
	public static final String ID_FOREIGN_KEY_FOREGROUND = "foreign.key.foreground";
	public static final String ID_TABLE_PAINT_GRADIENT = "table.paint.gradient";

	public static final String ID_AUTO_LOAD_MODEL = "auto.load.model";
	public static final String ID_AUTO_SAVE_MODEL = "auto.save.model";

	static {
		m_colors.put(ID_TABLE_FOREGROUND, Color.black);
		m_colors.put(ID_TABLE_BACKGROUND, new Color(153, 204, 255));
		m_colors.put(ID_PROTOTYPE_FOREGROUND, Color.black);
		m_colors.put(ID_PROTOTYPE_BACKGROUND, new Color(255, 206, 108));
		m_colors.put(ID_PRIMARY_KEY_FOREGROUND, new Color(0, 68, 255));
		m_colors.put(ID_FOREIGN_KEY_FOREGROUND, new Color(204, 102, 0));
	}

	public static Color getDefaultColor(String prop_name) {
		return (Color) m_colors.get(prop_name);
	}
}
