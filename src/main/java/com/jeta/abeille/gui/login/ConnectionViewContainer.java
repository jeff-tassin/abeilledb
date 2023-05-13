package com.jeta.abeille.gui.login;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import com.jeta.abeille.database.model.Database;
import com.jeta.abeille.database.model.ConnectionInfo;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.utils.TSGuiToolbox;


import com.jeta.open.rules.JETARule;
import com.jeta.open.rules.RuleResult;

import com.jeta.foundation.utils.TSUtils;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.form.FormAccessor;

/**
 * This view allows the user to edit connection properties
 * 
 * @author Jeff Tassin
 */
public class ConnectionViewContainer extends TSPanel {
	/**
	 * The currently active view. (Either basic or advanced)
	 */
	private ConnectionView m_activeview;

	/**
	 * The validator for the currently active view.
	 */
	private JETARule m_activevalidator;

	private BasicConnectionView m_basicview;
	private ConnectionViewValidator m_basicviewvalidator;
	private AdvancedConnectionViewValidator m_advviewvalidator;
	private AdvancedConnectionView m_advancedview;

	/**
	 * The validator for this container. It simply delegates the validation to
	 * the active view
	 */
	private Validator m_validator;

	/**
	 * button used to display help. We change the help id on this button
	 * depending on whether the view is advanced or basic
	 */
	private JButton m_help_btn;

	private FormPanel m_form;

	/**
	 * Actionlisteners for the basic and advanced view database combo boxes. We
	 * need to remove the listeners when changing the view so we don't get
	 * events.
	 */
	private ActionListener m_basic_dbcombo_listener;
	private ActionListener m_adv_dbcombo_listener;
	private JComboBox m_basic_db_combo;
	private JComboBox m_adv_db_combo;

	/**
	 * ctor
	 */
	public ConnectionViewContainer(ConnectionInfo model) {
		this(model, null, true);
	}

	/**
	 * ctor
	 */
	public ConnectionViewContainer(ConnectionInfo model, boolean enabled) {
		this(model, null, enabled);
	}

	/**
	 * ctor
	 */
	public ConnectionViewContainer(ConnectionInfo model, JButton help_btn, boolean enabled) {
		m_help_btn = help_btn;
		setLayout(new BorderLayout());

		if (enabled) {
			m_form = new FormPanel("com/jeta/abeille/gui/login/connectionView.jfrm");
			add(m_form, BorderLayout.CENTER);
			createView(enabled);
		} else {
			add(createView(enabled), BorderLayout.NORTH);
		}
		setModel(model);
	}

	private BasicConnectionView createView(boolean enabled) {
		m_basicview = new BasicConnectionView(null);
		m_basicview.setName("connection.view");
		m_basicviewvalidator = new ConnectionViewValidator();
		m_advviewvalidator = new AdvancedConnectionViewValidator();
		m_advancedview = new AdvancedConnectionView(null);
		m_advancedview.setName("connection.view");

		setEnabled(enabled);
		if (isEnabled()) {
			JCheckBox basic_cbox = (JCheckBox) m_basicview.getComponentByName(BasicConnectionView.ID_ADVANCED_CHECK);
			final JCheckBox adv_cbox = (JCheckBox) m_advancedview
					.getComponentByName(AdvancedConnectionView.ID_ADVANCED_CHECK);

			basic_cbox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					setView(true);
				}
			});

			adv_cbox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					setView(false);
				}
			});

			m_basic_dbcombo_listener = new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					if (Database.GENERIC.equals(m_basicview.getDatabase())) {
						setView(true);
						adv_cbox.setEnabled(false);
					}
				}
			};

			m_basic_db_combo = (JComboBox) m_basicview.getComponentByName(BasicConnectionView.ID_DATABASE);
			m_basic_db_combo.addActionListener(m_basic_dbcombo_listener);

			m_adv_dbcombo_listener = new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					if (Database.GENERIC.equals(m_advancedview.getDatabase())) {
						adv_cbox.setEnabled(false);
					} else {
						if (m_advancedview.isEnabled()) {
							adv_cbox.setEnabled(true);
						}
					}
				}
			};

			m_adv_db_combo = (JComboBox) m_advancedview.getComponentByName(BasicConnectionView.ID_DATABASE);
			m_adv_db_combo.addActionListener(m_adv_dbcombo_listener);
		}

		return m_basicview;
	}

	/**
	 * Saves the information in the GUI components to the model
	 */
	public ConnectionInfo createConnectionModel() {
		return m_activeview.createConnectionModel();
	}

	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(8, 13);
	}

	public JETARule getValidator() {
		if (m_validator == null)
			m_validator = new Validator();

		return m_validator;
	}

	public boolean isAdvanced() {
		return (m_activeview == m_advancedview);
	}

	/**
	 * Enables/Disables the view
	 */
	public void setEnabled(boolean enable) {
		super.setEnabled(enable);
		m_basicview.setEnabled(enable);
		m_advancedview.setEnabled(enable);
	}

	/**
	 * Sets the model for the view
	 */
	public void setModel(ConnectionInfo model) {
		if (model == null) {
			setView(false);
		} else {
			if (m_activeview == null) {
				setView(model.isAdvanced());
				m_activeview.setModel(model);
			} else {
				m_activeview.setModel(model);
				setView(model.isAdvanced());
			}
		}
	}

	/**
	 * Sets the view to either advanced or basic
	 */
	private void setView(boolean advanced) {
		try {
			if (isEnabled()) {
				m_basic_db_combo.removeActionListener(m_basic_dbcombo_listener);
				m_adv_db_combo.removeActionListener(m_adv_dbcombo_listener);
			} else {
				removeAll();
			}

			/*
			try {
				if (m_help_btn != null) {
					if (advanced)
						HelpUtils.setHelpIDString(m_help_btn, "abeille.main.adv_connection");
					else
						HelpUtils.setHelpIDString(m_help_btn, "abeille.main.basic_connection");
				}
			} catch (Exception e) {
				TSUtils.printException(e);
			}
			 */

			JCheckBox basic_cbox = (JCheckBox) m_basicview.getComponentByName(BasicConnectionView.ID_ADVANCED_CHECK);
			JCheckBox adv_cbox = (JCheckBox) m_advancedview
					.getComponentByName(AdvancedConnectionView.ID_ADVANCED_CHECK);

			basic_cbox.setSelected(advanced);
			adv_cbox.setSelected(advanced);

			ConnectionInfo info = null;
			if (m_activeview != null)
				info = m_activeview.createConnectionModel();

			if (info != null)
				info.setAdvanced(advanced);

			if (advanced) {
				if (isEnabled()) {
					FormAccessor fa = m_form.getFormAccessor("connection.form");
					fa.replaceBean("connection.view", m_advancedview);
				} else {
					add(m_advancedview, BorderLayout.CENTER);
				}

				m_activeview = m_advancedview;
				m_activevalidator = m_advviewvalidator;
			} else {
				if (isEnabled()) {

					FormAccessor fa = m_form.getFormAccessor("connection.form");
					fa.replaceBean("connection.view", m_basicview);
				} else {
					add(m_basicview, BorderLayout.CENTER);
				}
				m_activeview = m_basicview;
				m_activevalidator = m_basicviewvalidator;
			}
			m_activeview.setModel(info);

			revalidate();
			repaint();
		} finally {
			if (isEnabled()) {
				m_basic_db_combo.addActionListener(m_basic_dbcombo_listener);
				m_adv_db_combo.addActionListener(m_adv_dbcombo_listener);
			}
		}
	}

	public class Validator implements JETARule {

		/**
		 * JETARule implementation. This rule expects a parameter array of at
		 * least 1 element. The first element must be a ConnectionViewContainer
		 * object.
		 * 
		 */
		public RuleResult check(Object[] params) {
			assert (params != null);
			assert (params.length > 0);
			assert (params[0] instanceof ConnectionViewContainer);
			ConnectionViewContainer cvc = (ConnectionViewContainer) params[0];

			if (m_activevalidator != null) {
				Object[] cv_params = new Object[1];
				cv_params[0] = m_activeview;
				return m_activevalidator.check(cv_params);
			} else {
				return RuleResult.SUCCESS;
			}
		}
	}

}
