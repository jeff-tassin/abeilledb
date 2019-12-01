package com.jeta.abeille.gui.importer;

import java.awt.BorderLayout;
import java.awt.Dimension;

import java.util.Collection;
import java.util.LinkedList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JToolBar;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.model.ModelerEventHandler;
import com.jeta.abeille.gui.model.ModelerModel;
import com.jeta.abeille.gui.model.ModelView;
import com.jeta.abeille.gui.model.ModelViewNames;
import com.jeta.abeille.gui.model.ViewGetter;

import com.jeta.foundation.gui.components.TSCell;
import com.jeta.foundation.gui.components.TSComponentNames;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.MenuDefinition;
import com.jeta.foundation.gui.components.MenuTemplate;
import com.jeta.foundation.gui.components.TSStatusBar;
import com.jeta.foundation.gui.components.TSToolBarTemplate;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * This is the frame window that contains a graphical view of tables and joins
 * used for building an import
 * 
 * @author Jeff Tassin
 */
public class ImportBuilderFrame extends TSInternalFrame implements ViewGetter {
	/** the database connection */
	private TSConnection m_connection; // the database connection

	/** the view of tables used to build the form */
	private ImportBuilderView m_view;

	/** the data model for the form */
	private ImportBuilderModel m_importermodel;

	private TSStatusBar m_statusbar;
	private TSCell m_anchorcell;

	/** the frame icon for this frame */
	static ImageIcon m_frameicon;

	static {
		m_frameicon = TSGuiToolbox.loadImage("general/Import16.gif");
	}

	/**
	 * Default ctor
	 */
	public ImportBuilderFrame() {
		super("");
		setFrameIcon(m_frameicon);
	}

	/**
	 * Creates and initizes the menu for this frame
	 */
	protected void createMenu() {
		MenuTemplate template = this.getMenuTemplate();

		MenuDefinition menu = new MenuDefinition(I18N.getLocalizedMessage("Window"));
		menu.add(i18n_createMenuItem("Close", ImportBuilderNames.ID_CLOSE, null));
		template.add(menu);

		menu = new MenuDefinition(I18N.getLocalizedMessage("Edit"));
		menu.add(i18n_createMenuItem("Cut", TSComponentNames.ID_CUT, null));
		menu.add(i18n_createMenuItem("Copy", TSComponentNames.ID_COPY, null));
		menu.add(i18n_createMenuItem("Paste", TSComponentNames.ID_PASTE, null));
		menu.add(i18n_createMenuItem("Select All", ModelViewNames.ID_SELECT_ALL, null));
		template.add(menu);

	}

	/**
	 * Creates the toolbar for the frame
	 */
	protected void createToolBar() {
		TSToolBarTemplate template = this.getToolBarTemplate();

		template.add(i18n_createToolBarButton(ImportBuilderNames.ID_OPEN_IMPORT, "general/Open16.gif", "Open"));
		template.add(i18n_createToolBarButton(ImportBuilderNames.ID_SAVE_IMPORT, "general/Save16.gif", "Save"));
		template.add(i18n_createToolBarButton(ImportBuilderNames.ID_ADD_TABLE, "general/Add16.gif", "Add Table"));
		template.add(i18n_createToolBarButton(ImportBuilderNames.ID_REMOVE_TABLE, "general/Delete16.gif",
				"Delete From View"));
		template.add(i18n_createToolBarButton(ImportBuilderNames.ID_START_IMPORT, "general/Import16.gif", null));
		template.addSeparator();

		template.add(Box.createHorizontalStrut(16));

		ButtonGroup group = new ButtonGroup();
		JRadioButton btn = new JRadioButton(TSGuiToolbox.loadImage("mouse16.gif"));
		btn.setToolTipText(I18N.getLocalizedMessage("Mouse Tool"));
		btn.setSelectedIcon(TSGuiToolbox.loadImage("mouse_sel16.gif"));
		btn.setSelected(true);
		btn.setContentAreaFilled(false);

		setCommandHandler(btn, ImportBuilderNames.ID_MOUSE_TOOL);
		template.add(btn);
		group.add(btn);

		btn = new JRadioButton(TSGuiToolbox.loadImage("link16.gif"));
		btn.setToolTipText(I18N.getLocalizedMessage("Link_Tool_Tip"));
		btn.setSelectedIcon(TSGuiToolbox.loadImage("link_sel16.gif"));
		btn.setContentAreaFilled(false);

		setCommandHandler(btn, ImportBuilderNames.ID_LINK_TOOL);
		group.add(btn);
		template.add(btn);

		template.addSeparator();
		template.add(i18n_createToolBarButton(ImportBuilderNames.ID_TARGET, "anchor16.gif", null));
		template.add(Box.createHorizontalStrut(5));
		JLabel m_anchorlabel = new JLabel(I18N.getLocalizedDialogLabel("Target") + " ");
		template.add(m_anchorlabel);

		m_statusbar = new TSStatusBar();
		m_anchorcell = new TSCell(ImportBuilderNames.ID_TARGET_CELL, "############  Table Name ############");
		m_anchorcell.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		m_statusbar.addCell(m_anchorcell);
		template.add(m_statusbar);

		JToolBar toolbar = getToolBar();
		ToolBarLayout layout = new ToolBarLayout(toolbar);
		toolbar.setLayout(layout);
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
	public ImportBuilderView getImportBuilderView() {
		return m_view;
	}

	/**
	 * @return the underlying data model
	 */
	public ImportBuilderModel getModel() {
		return m_importermodel;
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
	 *            ImportBuilderModel object that we wish to edit.
	 */
	public void initializeModel(Object[] params) {
		m_connection = (TSConnection) params[0];

		String title = I18N.getLocalizedMessage("Import");
		setShortTitle(title);
		setTitle(title + " - " + m_connection.getShortId());

		createToolBar();
		createMenu();

		try {
			// ModelerModel modeler = ModelerModel.createInstance( m_connection
			// );
			// modeler.addListener( new ModelerEventHandler( this ) );
			// @todo remove listener from modeler

			setImportBuilderModel(new ImportBuilderModel(m_connection));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void setImportBuilderModel(ImportBuilderModel importmodel) {
		try {
			if (m_view != null) {
				getContentPane().remove(m_view);
			}

			// ModelerModel modeler = ModelerModel.createInstance( m_connection
			// );
			m_importermodel = importmodel;
			// m_view = new ImportBuilderView( m_importermodel, modeler );
			// getContentPane().add( m_view, BorderLayout.CENTER );
		} catch (Exception e) {
			e.printStackTrace();
		}

		setController(new ImportBuilderFrameController(this));

	}

	/**
	 * Saves the current frame state (and all views) to the data model
	 */
	public void saveState() {
		m_view.saveState();
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

			Dimension d = m_statusbar.getSize();
			Dimension pd = m_statusbar.getPreferredSize();
			d.height = pd.height;

			Dimension toolbard = target.getSize();
			d.width = toolbard.width - m_statusbar.getX() - 10;
			m_statusbar.setSize(d);

			java.awt.Point pt = m_statusbar.getLocation();
			pt.y = (toolbard.height - d.height) / 2;
			m_statusbar.setLocation(pt);
		}
	}

}
