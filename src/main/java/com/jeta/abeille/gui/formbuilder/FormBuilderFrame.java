package com.jeta.abeille.gui.formbuilder;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;

import java.util.Collection;
import java.util.LinkedList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import com.jeta.abeille.database.model.Catalog;
import com.jeta.abeille.database.model.Schema;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.abeille.gui.model.ModelerModel;
import com.jeta.abeille.gui.model.ModelerEventHandler;
import com.jeta.abeille.gui.model.ModelView;
import com.jeta.abeille.gui.model.ModelViewModelEvent;
import com.jeta.abeille.gui.model.ModelViewModelListener;
import com.jeta.abeille.gui.model.ModelViewNames;
import com.jeta.abeille.gui.model.ViewGetter;

import com.jeta.abeille.gui.query.BuilderModelerEventHandler;

import com.jeta.abeille.gui.update.InstanceNames;
import com.jeta.abeille.gui.update.InstanceOptionsView;

import com.jeta.foundation.gui.components.TSCell;
import com.jeta.foundation.gui.components.TSComponentNames;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.MenuDefinition;
import com.jeta.foundation.gui.components.MenuTemplate;
import com.jeta.foundation.gui.components.TSStatusBar;
import com.jeta.foundation.gui.components.TSToolBarTemplate;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * This is the frame window that contains a graphical view of tables and joins
 * used for a query
 * 
 * @author Jeff Tassin
 */
public class FormBuilderFrame extends TSInternalFrame implements ViewGetter, ModelViewModelListener {
	/** the database connection */
	private TSConnection m_connection; // the database connection

	/** the view of tables used to build the form */
	private FormView m_view;

	/** the data model for the form */
	private FormModel m_formmodel;

	// private TSStatusBar m_statusbar;
	private JTextField m_anchorfield;

	private ModelerEventHandler m_modelerlistener = new BuilderModelerEventHandler(this);

	/** the frame icon for this frame */
	static ImageIcon m_frameicon;

	public static final String ID_ANCHOR_CELL = "status.anchor.cell";

	static {
		m_frameicon = TSGuiToolbox.loadImage("incors/16x16/form_green.png");
	}

	/**
	 * Default ctor
	 */
	public FormBuilderFrame() {
		super("");
		setFrameIcon(m_frameicon);
		String title = I18N.getLocalizedMessage("Form Builder");
		setShortTitle(title);
		setTitle(title);
	}

	/**
	 * Creates and initizes the menu for this frame
	 */
	protected void createMenu() {
		MenuTemplate template = this.getMenuTemplate();

		MenuDefinition menu = new MenuDefinition(I18N.getLocalizedMessage("File"));
		menu.setName("file");

		menu.add(i18n_createMenuItem("Show Form", FormNames.ID_SHOW_FORM, null));
		menu.add(i18n_createMenuItem("Save Form State", FormNames.ID_SAVE_MODEL,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK, false)));
		menu.addSeparator();
		menu.add(i18n_createMenuItem("Page Setup", ModelViewNames.ID_PAGE_SETUP, null));
		menu.add(i18n_createMenuItem("Print Preview", ModelViewNames.ID_PRINT_PREVIEW, null));
		menu.add(i18n_createMenuItem("Print", ModelViewNames.ID_PRINT, null));
		menu.addSeparator();
		menu.add(i18n_createMenuItem("Save As Image", ModelViewNames.ID_SAVE_AS_SVG, null));
		template.add(menu);

		menu = new MenuDefinition(I18N.getLocalizedMessage("Edit"));
		menu.add(i18n_createMenuItem("Cut", TSComponentNames.ID_CUT, null));
		menu.add(i18n_createMenuItem("Copy", TSComponentNames.ID_COPY, null));
		menu.add(i18n_createMenuItem("Copy Joins", ModelViewNames.ID_COPY_JOINS, null));
		menu.add(i18n_createMenuItem("Copy Joins Qualified", ModelViewNames.ID_COPY_JOINS_QUALIFIED, null));
		menu.add(i18n_createMenuItem("Paste", TSComponentNames.ID_PASTE, null));
		menu.add(i18n_createMenuItem("Select All", ModelViewNames.ID_SELECT_ALL, null));
		template.add(menu);

		menu = new MenuDefinition(I18N.getLocalizedMessage("Table"));
		menu.add(i18n_createMenuItem("Add Table", FormNames.ID_ADD_TABLE, null));
		menu.add(i18n_createMenuItem("Remove Table", FormNames.ID_REMOVE_TABLE, null));

		template.add(menu);

		// columns menu
		menu = new MenuDefinition(I18N.getLocalizedMessage("Columns"));
		menu.add(i18n_createMenuItem("Add Column", FormNames.ID_ADD_COLUMN, null));
		menu.add(i18n_createMenuItem("Remove Column", FormNames.ID_REMOVE_COLUMN, null));

		template.add(menu);

	}

	/**
	 * Creates the toolbar for the frame
	 */
	protected void createToolBar() {
		TSToolBarTemplate template = this.getToolBarTemplate();

		template.add(i18n_createToolBarButton(FormNames.ID_ADD_TABLE, "incors/16x16/table_sql_add.png", "Add Table"));
		template.add(i18n_createToolBarButton(FormNames.ID_REMOVE_TABLE, "incors/16x16/table_sql_delete.png",
				"Delete From View"));
		template.addSeparator();

		template.add(createToolBarButton(FormNames.ID_UPDATE_TABLE, "incors/16x16/form_blue.png",
				InstanceNames.ID_SHOW_INSTANCE_VIEW));
		template.add(i18n_createToolBarButton(FormNames.ID_QUERY_TABLE, "incors/16x16/table_sql_view.png",
				"select_star"));

		template.add(i18n_createToolBarButton(FormNames.ID_SHOW_FORM, "incors/16x16/form_green.png", "Show Form"));

		template.addSeparator();
		template.add(i18n_createToolBarButton(FormNames.ID_ADD_COLUMN, "add_column16.gif", "Add Column"));
		template.add(i18n_createToolBarButton(InstanceOptionsView.ID_EDIT_COLUMN, "incors/16x16/document_edit.png",
				"Edit Column Settings"));
		template.add(i18n_createToolBarButton(InstanceOptionsView.ID_MOVE_UP, "incors/16x16/navigate_up.png", "Move Up"));
		template.add(i18n_createToolBarButton(InstanceOptionsView.ID_MOVE_DOWN, "incors/16x16/navigate_down.png",
				"Move Down"));
		template.add(i18n_createToolBarButton(InstanceOptionsView.ID_RESET_DEFAULTS, "incors/16x16/refresh.png",
				"Defaults"));
		template.add(Box.createHorizontalStrut(16));

		ButtonGroup group = new ButtonGroup();
		JRadioButton btn = new JRadioButton(TSGuiToolbox.loadImage("mouse16.gif"));
		btn.setToolTipText(I18N.getLocalizedMessage("Mouse Tool"));
		btn.setSelectedIcon(TSGuiToolbox.loadImage("mouse_sel16.gif"));
		btn.setSelected(true);
		btn.setContentAreaFilled(false);

		setCommandHandler(btn, FormNames.ID_MOUSE_TOOL);
		template.add(btn);
		group.add(btn);

		btn = new JRadioButton(TSGuiToolbox.loadImage("link16.gif"));
		btn.setToolTipText(I18N.getLocalizedMessage("Link_Tool_Tip"));
		btn.setSelectedIcon(TSGuiToolbox.loadImage("link_sel16.gif"));
		btn.setContentAreaFilled(false);

		setCommandHandler(btn, FormNames.ID_LINK_TOOL);
		group.add(btn);
		template.add(btn);

		template.addSeparator();
		template.add(i18n_createToolBarButton(FormNames.ID_ANCHOR, "anchor16.gif", "anchor_tooltip"));
		template.add(Box.createHorizontalStrut(5));
		JLabel m_anchorlabel = new JLabel(I18N.getLocalizedDialogLabel("Anchor") + " ");
		template.add(m_anchorlabel);

		m_anchorfield = new JTextField(10);
		m_anchorfield.setEnabled(false);
		Dimension d = m_anchorfield.getPreferredSize();
		m_anchorfield.setName(ID_ANCHOR_CELL);
		m_anchorfield.setPreferredSize(d);
		m_anchorfield.setMaximumSize(d);
		m_anchorfield.setMinimumSize(d);
		template.add(m_anchorfield);

		template.add(Box.createHorizontalStrut(20));
		template.add(i18n_createToolBarButton(FormNames.ID_OPTIONS, "incors/16x16/preferences.png", "Options"));

		// ToolBarLayout layout = new ToolBarLayout( toolbar );
		// toolbar.setLayout( layout );

		// template.add( Box.createHorizontalStrut(10) );
		// javax.swing.JButton hbtn = i18n_createToolBarButton(
		// FormNames.ID_HELP, "general/Help16.gif", "Help" );
		// com.jeta.foundation.help.HelpUtils.enableHelpOnButton( hbtn,
		// FormNames.ID_HELP );
		// template.add( hbtn );

	}

	/**
	 * Override dispose so we can remove reference to the controller to allow
	 * for proper clean up
	 */
	public void dispose() {
		saveFrame(); // save frame state
		if (m_formmodel != null) {
			m_formmodel.removeListener(this);
		}

		try {
			// ModelerModel modeler = ModelerModel.createInstance( m_connection
			// );
			// modeler.removeListener( m_modelerlistener );
		} catch (Exception e) {

		}

		// TSController controller = getController();
		// controller.setUIDirector( null );
		super.dispose();
	}

	/**
	 * ModelViewModelListener implementation. Used when the model name changes.
	 */
	public void eventFired(ModelViewModelEvent evt) {
		if (evt.getID() == FormModel.MODEL_NAME_CHANGED) {
			setFormName((String) evt.getParameter(0));
		}
	}

	/**
	 * @return the underlying database connection
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * @return the form view
	 */
	public FormView getFormView() {
		return m_view;
	}

	/**
	 * @return the underlying data model
	 */
	public FormModel getModel() {
		return m_formmodel;
	}

	/**
	 * ViewGetter implementation
	 * 
	 * @return the one and only ModelView component for this frame
	 */
	public ModelView getModelView() {
		return m_view.getModelView();
	}

	/**
	 * ViewGetter implementation
	 * 
	 * @return the one and only ModelView component for this frame
	 */
	public Collection getViews() {
		LinkedList list = new LinkedList();
		list.add(m_view.getModelView());
		return list;
	}

	/**
	 * Sets the connection needed by this frame.
	 * 
	 * @param params
	 *            a 2 length array. The first element must contain the
	 *            TSConnection object. The second element must contain the
	 *            FormModel object that we wish to edit.
	 */
	public void initializeModel(Object[] params) {
		m_connection = (TSConnection) params[0];
		m_formmodel = (FormModel) params[1];

		createToolBar();
		createMenu();

		try {
			// ModelerModel modeler = ModelerModel.createInstance( m_connection
			// );
			// modeler.addListener( m_modelerlistener );

			m_view = new FormView(m_formmodel, null);
			getContentPane().add(m_view, BorderLayout.CENTER);
			setFormName(m_formmodel.getName());
			FormBuilderFrameController controller = new FormBuilderFrameController(this, m_connection);
			addController(controller);

			ColumnsView cview = m_view.getColumnsView();
			cview.setController(new ColumnsViewController(this, cview));

			m_formmodel.addListener(this);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Saves the frame state to the application store
	 */
	void saveFrame() {
		m_formmodel.save();
	}

	/**
	 * Sets the name of the form
	 */
	public void setFormName(String newFormName) {
		String qualified_name = DbUtils.getQualifiedName(m_formmodel.getCatalog(), m_formmodel.getSchema(),
				m_formmodel.getName());

		// String title = I18N.getLocalizedMessage("Form Builder" );
		// setShortTitle( title );
		// setTitle( title + " - " + m_connection.getShortId() );
	}

	/**
	 * LayoutManager for our toolbar. This is mainly to resize the status bar on
	 * the toolbar
	 */
	public class ToolBarLayout extends BoxLayout {
		ToolBarLayout(JToolBar toolBar) {
			super(toolBar, BoxLayout.X_AXIS);
		}

		public void layoutContainer(java.awt.Container target) {
			super.layoutContainer(target);

			// Dimension d = m_statusbar.getSize();
			// Dimension pd = m_statusbar.getPreferredSize();
			// d.height = pd.height;

			// Dimension toolbard = target.getSize();
			// d.width = toolbard.width - m_statusbar.getX() - 10;
			// m_statusbar.setSize( d );

			// java.awt.Point pt = m_statusbar.getLocation();
			// pt.y = (toolbard.height - d.height )/2;
			// m_statusbar.setLocation( pt );
		}
	}

}
