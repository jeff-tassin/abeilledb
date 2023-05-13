package com.jeta.abeille.gui.model.options;

import java.awt.Color;

import java.util.HashMap;
import java.util.Iterator;

import com.jeta.abeille.gui.model.ModelViewSettings;
import com.jeta.abeille.gui.model.TableWidget;
import com.jeta.abeille.gui.model.TableWidgetModel;
import com.jeta.foundation.interfaces.userprops.TSUserPropertiesUtils;

public class ModelViewPreferencesModel {
	/**
	 * flag that indicates whether to display the column icon in each table
	 * widget in the model view
	 */
	private boolean m_show_icon;

	/**
	 * flag that indicates whether to display the column type in each table
	 * widget in the model view
	 */
	private boolean m_show_type;

	/** hash map of property names (String) to colors ( Color objects) */
	private HashMap m_colors = new HashMap();

	/**
	 * flag that indicates if table widget caption should be painted with a
	 * gradient
	 */
	private boolean m_paint_gradient = true;

	/**
	 * Automatiically loads the last model
	 */
	private boolean m_auto_load = true;

	/**
	 * Automatically saves the model without prompting on exit
	 */
	private boolean m_auto_save = false;

	public ModelViewPreferencesModel() {
		m_show_type = TSUserPropertiesUtils.getBoolean(TableWidget.ID_SHOW_DATA_TYPE, true);
		m_show_icon = TSUserPropertiesUtils.getBoolean(TableWidget.ID_SHOW_ICON, true);
		m_paint_gradient = TSUserPropertiesUtils.getBoolean(ModelViewSettings.ID_TABLE_PAINT_GRADIENT, true);
		m_auto_load = TSUserPropertiesUtils.getBoolean(ModelViewSettings.ID_AUTO_LOAD_MODEL, true);
		m_auto_save = TSUserPropertiesUtils.getBoolean(ModelViewSettings.ID_AUTO_SAVE_MODEL, false);

		setColor(ModelViewSettings.ID_PROTOTYPE_BACKGROUND, TableWidgetModel.getPrototypeBackground());
		setColor(ModelViewSettings.ID_PROTOTYPE_FOREGROUND, TableWidgetModel.getPrototypeForeground());
		setColor(ModelViewSettings.ID_TABLE_BACKGROUND, TableWidgetModel.getTableBackground());
		setColor(ModelViewSettings.ID_TABLE_FOREGROUND, TableWidgetModel.getTableForeground());

		setColor(
				ModelViewSettings.ID_PRIMARY_KEY_FOREGROUND,
				TSUserPropertiesUtils.getColor(ModelViewSettings.ID_PRIMARY_KEY_FOREGROUND,
						ModelViewSettings.getDefaultColor(ModelViewSettings.ID_PRIMARY_KEY_FOREGROUND)));

		setColor(
				ModelViewSettings.ID_FOREIGN_KEY_FOREGROUND,
				TSUserPropertiesUtils.getColor(ModelViewSettings.ID_FOREIGN_KEY_FOREGROUND,
						ModelViewSettings.getDefaultColor(ModelViewSettings.ID_FOREIGN_KEY_FOREGROUND)));
	}

	public Color getColor(String propName) {
		return (Color) m_colors.get(propName);
	}

	public boolean isPaintGradient() {
		return m_paint_gradient;
	}

	public boolean isShowIcon() {
		return m_show_icon;
	}

	public boolean isShowType() {
		return m_show_type;
	}

	public boolean isAutoLoad() {
		return m_auto_load;
	}

	public boolean isAutoSave() {
		return m_auto_save;
	}

	public void save() {
		TSUserPropertiesUtils.setBoolean(TableWidget.ID_SHOW_DATA_TYPE, m_show_type);
		TSUserPropertiesUtils.setBoolean(TableWidget.ID_SHOW_ICON, m_show_icon);
		TSUserPropertiesUtils.setBoolean(ModelViewSettings.ID_AUTO_LOAD_MODEL, m_auto_load);
		TSUserPropertiesUtils.setBoolean(ModelViewSettings.ID_AUTO_SAVE_MODEL, m_auto_save);

		TSUserPropertiesUtils.setColor(ModelViewSettings.ID_PROTOTYPE_BACKGROUND,
				getColor(ModelViewSettings.ID_PROTOTYPE_BACKGROUND));
		TSUserPropertiesUtils.setColor(ModelViewSettings.ID_PROTOTYPE_FOREGROUND,
				getColor(ModelViewSettings.ID_PROTOTYPE_FOREGROUND));
		TSUserPropertiesUtils.setColor(ModelViewSettings.ID_TABLE_BACKGROUND,
				getColor(ModelViewSettings.ID_TABLE_BACKGROUND));
		TSUserPropertiesUtils.setColor(ModelViewSettings.ID_TABLE_FOREGROUND,
				getColor(ModelViewSettings.ID_TABLE_FOREGROUND));

		Color pkcol = getColor(ModelViewSettings.ID_PRIMARY_KEY_FOREGROUND);
		TSUserPropertiesUtils.setColor(ModelViewSettings.ID_PRIMARY_KEY_FOREGROUND, pkcol);
		TSUserPropertiesUtils.setColor(ModelViewSettings.ID_FOREIGN_KEY_FOREGROUND,
				getColor(ModelViewSettings.ID_FOREIGN_KEY_FOREGROUND));

		TSUserPropertiesUtils.setBoolean(ModelViewSettings.ID_TABLE_PAINT_GRADIENT, isPaintGradient());
	}

	public void setColor(String propName, Color color) {
		m_colors.put(propName, color);
	}

	public void setShowIcon(boolean display_icon) {
		m_show_icon = display_icon;
	}

	public void setShowType(boolean display_type) {
		m_show_type = display_type;
	}

	public void setPaintGradient(boolean paint_gradient) {
		m_paint_gradient = paint_gradient;
	}

	public void setAutoLoad(boolean auto_load) {
		m_auto_load = auto_load;
	}

	public void setAutoSave(boolean auto_save) {
		m_auto_save = auto_save;
	}

}
