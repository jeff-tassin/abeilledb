package com.jeta.abeille.gui.rules.postgres;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.sql.SQLException;

import com.jeta.abeille.gui.utils.DropDialog;
import com.jeta.abeille.gui.utils.SQLCommandDialog;
import com.jeta.abeille.gui.utils.SQLDialogListener;
import com.jeta.abeille.gui.utils.SQLErrorDialog;

import com.jeta.foundation.gui.components.TSController;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * @author Jeff Tassin
 */
public class RulesViewController extends TSController {
	/** the view we are controlling */
	private RulesView m_view;

	public RulesViewController(RulesView view) {
		super(view);
		m_view = view;
		assignAction(RulesView.ID_CREATE_RULE, new CreateRule());
		assignAction(RulesView.ID_EDIT_RULE, new EditRule());
		assignAction(RulesView.ID_DROP_RULE, new DropRule());

		view.setUIDirector(new RulesViewUIDirector(view));
	}

	/**
	 * Action handler to create a new check
	 */
	public class CreateRule implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			final RulesModel model = m_view.getModel();

			SQLCommandDialog dlg = SQLCommandDialog.createDialog(model.getConnection(), m_view, true);
			String dlgmsg = I18N.getLocalizedMessage("Create Rule");
			dlg.setMessage(dlgmsg);

			final AdvancedRuleView ruleview = new AdvancedRuleView(model.getConnection(), model.getTableId());
			dlg.setPrimaryPanel(ruleview);
			dlg.addValidator(ruleview);
			TSGuiToolbox.setReasonableWindowSize(dlg, dlg.getPreferredSize());
			dlg.addDialogListener(new SQLDialogListener() {
				public boolean cmdOk() throws SQLException {
					Rule newrule = ruleview.createRule();
					model.createRule(newrule);
					return true;
				}
			});

			dlg.showCenter();
			if (dlg.isOk()) {
				model.reload();
			}
		}
	}

	/**
	 * Action Handler for dropping a rule
	 */
	public class DropRule implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			final Rule rule = m_view.getSelectedRule();
			if (rule != null) {
				String msg = I18N.format("Drop_1", rule.getName());
				final DropDialog dlg = DropDialog.createDropDialog(m_view.getModel().getConnection(), m_view, true);
				if (!m_view.getModel().getConnection().supportsSchemas())
					dlg.setCascadeEnabled(false);

				dlg.setMessage(msg);
				dlg.addDialogListener(new SQLDialogListener() {
					public boolean cmdOk() throws SQLException {
						RulesModel model = m_view.getModel();
						model.dropRule(rule, dlg.isCascade());
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
	public class EditRule implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			final Rule oldrule = m_view.getSelectedRule();
			if (oldrule != null) {
				final RulesModel model = m_view.getModel();

				SQLCommandDialog dlg = SQLCommandDialog.createDialog(model.getConnection(), m_view, true);
				String dlgmsg = I18N.getLocalizedMessage("Modify Rule");
				dlg.setMessage(dlgmsg);

				final RuleViewFactory vfactory = new RuleViewFactory(model.getConnection(), oldrule, model.getTableId());
				dlg.setPrimaryPanel(vfactory.getView());
				dlg.addValidator(vfactory);
				TSGuiToolbox.setReasonableWindowSize(dlg, dlg.getPreferredSize());
				dlg.addDialogListener(new SQLDialogListener() {
					public boolean cmdOk() throws SQLException {
						Rule newrule = vfactory.createRule();
						model.modifyRule(newrule, oldrule);
						return true;
					}
				});
				dlg.showCenter();
				if (dlg.isOk()) {
					model.reload();
				}
			}

		}
	}
}
