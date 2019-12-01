package com.jeta.abeille.gui.formbuilder;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.gui.model.ModelerModel;
import com.jeta.abeille.gui.model.ModelView;
import com.jeta.abeille.gui.model.TableViewController;
import com.jeta.abeille.gui.model.ModelViewNames;

import com.jeta.foundation.gui.components.BasicPopupMenu;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.split.CustomSplitPane;
import com.jeta.foundation.gui.split.SplitLayoutManager;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * This is the view window that contains a graphical view of tables and joins
 * used for the form builder
 * 
 * @author Jeff Tassin
 */
public class FormView extends TSPanel {
	/** the view of tables used to build the form */
	private ModelView m_tableview;

	/** the view of columns used to make up the form */
	private ColumnsView m_columnsview;

	private FormModel m_formmodel;

	private ModelerModel m_modeler;

	public static final String ID_MAIN_SPLIT = "main.splitter";

	/**
	 * ctor
	 */
	public FormView(FormModel model, ModelerModel modeler) {
		assert (model != null);
		m_modeler = modeler;
		m_formmodel = model;
		createComponents();
	}

	/**
	 * Creates the columns panel at the bottom pane in this frame
	 * 
	 */
	JPanel createBottomPanel() {
		m_columnsview = new ColumnsView(m_formmodel.getInstanceMetaData());
		m_columnsview.setMinimumSize(new Dimension(100, 32));
		return m_columnsview;
	}

	/**
	 * Initializes the components on this frame
	 */
	protected void createComponents() {
		m_tableview = new ModelView(m_formmodel);
		BasicPopupMenu popup = m_tableview.getPopup();
		popup.removeItem(ModelViewNames.ID_COPY_AS_NEW);

		m_tableview.setController(new TableViewController(m_tableview, m_modeler));

		JScrollPane scroller = new JScrollPane(m_tableview);

		CustomSplitPane split = new CustomSplitPane(JSplitPane.VERTICAL_SPLIT);
		split.setName(ID_MAIN_SPLIT);
		split.add(scroller);
		split.add(createBottomPanel());
		split.setDividerLocation(0.7f);
		split.setOneTouchExpandable(false);

		setLayout(new BorderLayout());
		// you must add the split's layered pane instead of the split itself
		// add( split.getLayeredPane(), BorderLayout.CENTER );
		add(split, BorderLayout.CENTER);
	}

	/**
	 * @return the view repsonsible for displaying the columns
	 */
	public ColumnsView getColumnsView() {
		return m_columnsview;
	}

	/**
	 * @return the currently selected table in the current view. If more than
	 *         one table is selected, the table with the current focus is
	 *         selected from the view. If no tables are selected, null is
	 *         returned.
	 */
	public TableMetaData getSelectedTable() {
		return m_tableview.getSelectedTable();
	}

	/**
	 * @return the form model
	 */
	public FormModel getModel() {
		return m_formmodel;
	}

	/**
	 * @return the one and only tables view component for this frame
	 */
	public ModelView getModelView() {
		return m_tableview;
	}

}
