package com.jeta.abeille.gui.checks.postgres;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.sql.SQLException;

import javax.swing.JOptionPane;

import com.jeta.abeille.gui.utils.DropDialog;
import com.jeta.abeille.gui.utils.SQLCommandDialog;
import com.jeta.abeille.gui.utils.SQLDialogListener;
import com.jeta.abeille.gui.utils.SQLErrorDialog;

import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSController;

import com.jeta.foundation.i18n.I18N;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.utils.TSUtils;

/**
 * @author Jeff Tassin
 */
public class ChecksViewController extends TSController {
	/** the view we are controlling */
	private ChecksView m_view;

	public ChecksViewController(ChecksView view) {
		super(view);
		m_view = view;
		assignAction(ChecksView.ID_CREATE_CHECK, new CreateCheck());
		assignAction(ChecksView.ID_EDIT_CHECK, new EditCheck());
		assignAction(ChecksView.ID_DELETE_CHECK, new DropCheckAction());

		m_view.setUIDirector(new ChecksViewUIDirector(m_view));
	}

	/**
	 * Action handler to create a new check
	 */
	public class CreateCheck implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			final ChecksModel model = m_view.getModel();

			SQLCommandDialog dlg = SQLCommandDialog.createDialog(model.getConnection(), m_view, true);
			String dlgmsg = I18N.getLocalizedMessage("Create Check");
			dlg.setMessage(dlgmsg);

			final CheckView cview = new CheckView(model.getConnection(), null);
			dlg.setPrimaryPanel(cview);
			dlg.addValidator(cview);
			TSGuiToolbox.setReasonableWindowSize(dlg, dlg.getPreferredSize());
			dlg.addDialogListener(new SQLDialogListener() {
				public boolean cmdOk() throws SQLException {
					CheckConstraint check = new CheckConstraint(model.getTableId(), cview.getName(), cview
							.getExpression());
					model.createCheck(check);
					return true;
				}
			});

			dlg.showCenter();
			try {
				if (dlg.isOk())
					model.reload();
			} catch (Exception e) {
				TSUtils.printStackTrace(e);
			}
		}
	}

	/**
	 * Action handler to drop the selected check
	 */
	public class DropCheckAction implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			final CheckConstraint cc = m_view.getSelectedCheck();
			if (cc != null) {
				String msg = I18N.format("Drop_1", cc.getName());
				final DropDialog dlg = DropDialog.createDropDialog(m_view.getModel().getConnection(), m_view, true);
				if (!m_view.getModel().getConnection().supportsSchemas())
					dlg.setCascadeEnabled(false);

				dlg.setMessage(msg);
				dlg.addDialogListener(new SQLDialogListener() {
					public boolean cmdOk() throws SQLException {
						ChecksModel model = m_view.getModel();
						model.dropCheck(cc, dlg.isCascade());
						return true;
					}
				});

				dlg.showCenter();
				if (dlg.isOk()) {
					m_view.getModel().reload();
				}
			}
		}
	}

	/**
	 * Action handler to edit the selected check
	 */
	public class EditCheck implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			final CheckConstraint cc = m_view.getSelectedCheck();
			if (cc != null) {
				final ChecksModel model = m_view.getModel();

				SQLCommandDialog dlg = SQLCommandDialog.createDialog(model.getConnection(), m_view, true);
				String dlgmsg = I18N.getLocalizedMessage("Modify Check");
				dlg.setMessage(dlgmsg);

				final CheckView cview = new CheckView(model.getConnection(), cc);
				dlg.setPrimaryPanel(cview);
				dlg.addValidator(cview);

				TSGuiToolbox.setReasonableWindowSize(dlg, dlg.getPreferredSize());
				dlg.addDialogListener(new SQLDialogListener() {
					public boolean cmdOk() throws SQLException {
						CheckConstraint check = new CheckConstraint(model.getTableId(), cview.getName(), cview
								.getExpression());
						model.modifyCheck(check);
						return true;
					}
				});

				dlg.showCenter();
				if (dlg.isOk()) {
					try {
						model.reload();
					} catch (Exception e) {
						TSUtils.printStackTrace(e);
					}
				}
			}
		}
	}

}
