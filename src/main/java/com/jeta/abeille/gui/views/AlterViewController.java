package com.jeta.abeille.gui.views;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.sql.SQLException;

import com.jeta.abeille.database.model.DbModel;
import com.jeta.abeille.database.model.DbModelEvent;
import com.jeta.abeille.database.model.TableId;
import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.model.View;
import com.jeta.abeille.database.model.ViewService;

import com.jeta.abeille.gui.utils.SQLCommandDialog;
import com.jeta.abeille.gui.utils.SQLDialogListener;
import com.jeta.abeille.gui.utils.SQLErrorDialog;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * This is the controller for the ViewView. Here we allow the user to alter a
 * View in the database.
 * 
 * @author Jeff Tassin
 */
public class AlterViewController extends TSController {
	/** the view we are controlling */
	private ViewView m_viewview;

	/** a temporary place holder for the new view we create */
	private View m_newview;

	/**
	 * ctor
	 */
	public AlterViewController(ViewView viewview) {
		super(viewview);
		m_viewview = viewview;
		assignAction(ViewView.ID_MODIFY_VIEW, new ModifyViewAction());
	}

	/**
	 * Action handler that modifies the selected view
	 */
	public class ModifyViewAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			View view = m_viewview.getView();
			if (view != null) {
				TableId tableid = (TableId) view.getTableId();
				assert (tableid != null);
				if (tableid != null) {
					try {
						final ViewService viewsrv = (ViewService) m_viewview.getConnection().getImplementation(
								ViewService.COMPONENT_ID);
						final View oldview = viewsrv.getView(tableid);
						if (oldview != null) {
							String dlgmsg = null;
							dlgmsg = I18N.getLocalizedMessage("Modify View");
							SQLCommandDialog dlg = SQLCommandDialog.createDialog(m_viewview.getConnection(),
									m_viewview, true);
							dlg.setMessage(dlgmsg);

							final ViewView viewview = new ViewView(m_viewview.getConnection(), oldview);
							dlg.setPrimaryPanel(viewview);
							TSGuiToolbox.setReasonableWindowSize(dlg, dlg.getPreferredSize());
							dlg.addDialogListener(new SQLDialogListener() {
								public boolean cmdOk() throws SQLException {
									m_newview = viewview.createView();
									viewsrv.modifyView(m_newview, oldview);
									return true;
								}
							});
							dlg.showCenter();
							if (dlg.isOk()) {
								m_viewview.setView(m_newview);
								DbModel dbmodel = m_viewview.getConnection().getModel(tableid.getCatalog());
								dbmodel.reloadTable((TableId) tableid);
								dbmodel.fireEvent(new DbModelEvent(dbmodel, DbModelEvent.VIEW_CHANGED, tableid));
							}
						} else {
							TSGuiToolbox.showErrorDialog(I18N.getLocalizedMessage("Unable to find view"));
						}
					} catch (SQLException se) {
						SQLErrorDialog.showErrorDialog(m_viewview, se, null);
					}
				}
			}
		}
	}

}
