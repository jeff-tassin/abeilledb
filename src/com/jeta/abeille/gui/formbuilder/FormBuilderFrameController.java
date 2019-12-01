package com.jeta.abeille.gui.formbuilder;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;

import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TableMetaData;
import com.jeta.abeille.database.model.TSConnection;

import com.jeta.abeille.gui.common.DefaultTableSelectorModel;
import com.jeta.abeille.gui.common.TableSelectorModel;
import com.jeta.abeille.gui.model.AddTableAction;
import com.jeta.abeille.gui.model.ModelView;
import com.jeta.abeille.gui.model.ModelViewController;
import com.jeta.abeille.gui.model.ModelViewDelegateAction;
import com.jeta.abeille.gui.model.ModelViewNames;
import com.jeta.abeille.gui.model.QueryTableAction;
import com.jeta.abeille.gui.update.ShowInstanceFrameAction;
import com.jeta.abeille.gui.update.TableInstanceViewBuilder;
import com.jeta.abeille.gui.update.InstanceOptionsView;

import com.jeta.foundation.gui.components.TSComponentNames;
import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSErrorDialog;
import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

import com.jeta.foundation.interfaces.app.ObjectStore;

import com.jeta.open.gui.framework.JETAController;
import com.jeta.open.gui.framework.UIDirector;

/**
 * This is the controller for the form builder frame window
 * 
 * @author Jeff Tassin
 */
public class FormBuilderFrameController extends JETAController {
	/** the frame window that we are controlling */
	private FormBuilderFrame m_frame;

	/** the underlying database connection */
	private TSConnection m_connection;

	/**
	 * ctor
	 */
	public FormBuilderFrameController(FormBuilderFrame frame, TSConnection connection) {
		super(frame);
		m_frame = frame;
		m_connection = connection;

		assignAction(ModelViewNames.ID_PRINT, new com.jeta.abeille.gui.model.common.PrintAction(m_frame));
		assignAction(ModelViewNames.ID_PRINT_PREVIEW, new com.jeta.abeille.gui.model.common.PrintPreviewAction(m_frame));
		assignAction(ModelViewNames.ID_PAGE_SETUP, new com.jeta.abeille.gui.model.common.PageSetupAction());
		assignAction(ModelViewNames.ID_SAVE_AS_SVG, new com.jeta.abeille.gui.model.export.SaveAsImageAction(m_frame));
		assignAction(ModelViewNames.ID_COPY_JOINS,
				new com.jeta.abeille.gui.model.common.CopyJoinsAction(m_frame, false));
		assignAction(ModelViewNames.ID_COPY_JOINS_QUALIFIED, new com.jeta.abeille.gui.model.common.CopyJoinsAction(
				m_frame, true));

		assignAction(FormNames.ID_SAVE_MODEL, new SaveModelAction());
		assignAction(FormNames.ID_ADD_TABLE, new FormAddAction());
		assignAction(FormNames.ID_ANCHOR, new SetAnchorAction());
		assignAction(FormNames.ID_REMOVE_TABLE, new ModelViewController.RemoveFromViewAction(m_frame));
		assignAction(FormNames.ID_UPDATE_TABLE, new UpdateTableAction());
		assignAction(FormNames.ID_QUERY_TABLE, new QueryTable());
		assignAction(FormNames.ID_SHOW_FORM, new ShowFormAction());
		assignAction(FormNames.ID_SHOW_QUERY_SQL, new ShowQuerySQL());
		assignAction(FormNames.ID_OPTIONS, new FormOptionsAction());

		assignAction(FormNames.ID_MOUSE_TOOL, new SelectMouseToolAction());
		assignAction(FormNames.ID_LINK_TOOL, new SelectLinkToolAction());

		assignAction(FormNames.ID_ADD_COLUMN, new ColumnsDelegateAction(FormNames.ID_ADD_COLUMN));
		assignAction(FormNames.ID_REMOVE_COLUMN, new ColumnsDelegateAction(FormNames.ID_REMOVE_COLUMN));
		assignAction(InstanceOptionsView.ID_EDIT_COLUMN, new ColumnsDelegateAction(InstanceOptionsView.ID_EDIT_COLUMN));
		assignAction(InstanceOptionsView.ID_MOVE_UP, new ColumnsDelegateAction(InstanceOptionsView.ID_MOVE_UP));
		assignAction(InstanceOptionsView.ID_MOVE_DOWN, new ColumnsDelegateAction(InstanceOptionsView.ID_MOVE_DOWN));
		assignAction(InstanceOptionsView.ID_RESET_DEFAULTS, new ColumnsDelegateAction(
				InstanceOptionsView.ID_RESET_DEFAULTS));

		assignAction(TSComponentNames.ID_CUT, new ModelViewDelegateAction(m_frame, TSComponentNames.ID_CUT));
		assignAction(TSComponentNames.ID_COPY, new ModelViewDelegateAction(m_frame, TSComponentNames.ID_COPY));
		assignAction(TSComponentNames.ID_PASTE, new ModelViewDelegateAction(m_frame, TSComponentNames.ID_PASTE));
		assignAction(ModelViewNames.ID_SELECT_ALL, new ModelViewDelegateAction(m_frame, ModelViewNames.ID_SELECT_ALL));

		FormBuilderFrameUIDirector director = new FormBuilderFrameUIDirector(frame);
		frame.setUIDirector(director);
		director.updateComponents(null);
	}

	/**
	 * @return the underlying database connection for this controller
	 */
	public TSConnection getConnection() {
		return m_connection;
	}

	/**
	 * Adds a column to the columns view
	 */
	public class ColumnsDelegateAction implements ActionListener {
		private String m_actionname;

		public ColumnsDelegateAction(String aname) {
			m_actionname = aname;
		}

		public void actionPerformed(ActionEvent evt) {
			FormView view = m_frame.getFormView();
			ColumnsView cview = view.getColumnsView();
			ColumnsViewController controller = (ColumnsViewController) cview.getController();
			controller.invokeAction(m_actionname);
		}
	}

	/**
	 * Closes the frame window
	 */
	public class CloseAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			invokeAction(FormNames.ID_SAVE_MODEL);
			TSWorkspaceFrame.getInstance().closeWindow(m_frame);
		}
	}

	/**
	 * Adds a table to the view
	 */
	public class FormAddAction extends AddTableAction {
		public FormAddAction() {
			super(m_frame.getParentFrame(), getConnection());
		}

		/**
		 * AddTableAction implementation override
		 */
		protected ModelView getView() {
			return m_frame.getModelView();
		}

		/**
		 * AddTableAction implementation override
		 */
		protected TableSelectorModel getTableSelectorModel() {
			return new DefaultTableSelectorModel(getConnection());
		}
	}

	/**
	 * Queries all the rows in the table and opens the SQL results frame.
	 */
	public class QueryTable implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			FormView fview = m_frame.getFormView();
			TableMetaData tmd = fview.getSelectedTable();
			if (tmd != null) {
				QueryTableAction.invoke(m_connection, tmd.getTableId());
			}
		}
	}

	/**
    */
	public class FormOptionsAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			FormModel model = m_frame.getModel();
			FormOptionsView view = new FormOptionsView(model);
			TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, m_frame, true);
			dlg.setTitle(I18N.getLocalizedMessage("Form Options"));
			dlg.setPrimaryPanel(view);
			dlg.setSize(dlg.getPreferredSize());
			dlg.showCenter();
			if (dlg.isOk()) {
				String name = view.getFormName();
				if (!name.equals(model.getName())) {
					model.setName(name);
				}
			}

		}
	}

	/**
	 * Saves the model to the object store and then sends a flush message to the
	 * object store to save now.
	 */
	public class SaveModelAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			try {
				m_frame.saveFrame();
				FormView view = m_frame.getFormView();
				FormModel formmodel = view.getModel();
				ObjectStore os = m_connection.getObjectStore();
				os.flush(formmodel.getStoreKey());
			} catch (IOException ioe) {
				TSErrorDialog dlg = (TSErrorDialog) TSGuiToolbox.createDialog(TSErrorDialog.class, m_frame, true);
				dlg.initialize(null, ioe);
				dlg.setSize(dlg.getPreferredSize());
				dlg.showCenter();
			}
		}
	}

	/**
	 * Selects the mouse tool
	 */
	public class SelectMouseToolAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ModelView view = m_frame.getModelView();
			view.setCursor(Cursor.getDefaultCursor());
			view.enableLinkTool(false);
		}
	}

	/**
	 * Selects the link tool which allows the user to draw/delete links
	 */
	public class SelectLinkToolAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {

			ModelView view = m_frame.getModelView();
			view.enableLinkTool(true);
		}
	}

	/**
	 * Sets the anchor table
	 */
	public class SetAnchorAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			FormView fview = m_frame.getFormView();
			TableMetaData tmd = fview.getSelectedTable();
			if (tmd != null) {
				FormModel model = m_frame.getModel();
				model.setAnchorTable(tmd.getTableId());

				UIDirector director = m_frame.getUIDirector();
				director.updateComponents(null);
			}
		}
	}

	/**
	 * Action handler that shows the built form in the instance viewn
	 */
	public class ShowFormAction extends ShowInstanceFrameAction {
		public void actionPerformed(ActionEvent evt) {
			FormModel model = m_frame.getModel();
			TableId tableid = model.getAnchorTable();
			if (tableid != null) {
				FormInstanceViewBuilder builder = new FormInstanceViewBuilder(model);
				showFrame(m_connection, builder);
			}
		}
	}

	/**
	 * Action handler that shows the query sql
	 */
	public class ShowQuerySQL extends ShowInstanceFrameAction {
		public void actionPerformed(ActionEvent evt) {

		}
	}

	/**
	 * Action handler that shows the instance view for a selected table
	 */
	public class UpdateTableAction extends ShowInstanceFrameAction {
		public void actionPerformed(ActionEvent evt) {
			FormView fview = m_frame.getFormView();
			TableMetaData tmd = fview.getSelectedTable();
			if (tmd != null) {
				TableInstanceViewBuilder builder = new TableInstanceViewBuilder(m_connection, tmd.getTableId());
				showFrame(m_connection, builder);
			}
		}
	}

}
