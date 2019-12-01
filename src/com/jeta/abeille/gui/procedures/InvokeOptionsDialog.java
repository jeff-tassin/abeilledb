package com.jeta.abeille.gui.procedures;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.jeta.abeille.database.model.TSConnection;
import com.jeta.abeille.database.procedures.ProcedureParameter;
import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.i18n.I18N;

/**
 * We need a special dialog for the procedure options. An extra button is added
 * to the dialog's button panel to invoke the procedure from this dialog.
 * Furthermore, we change the text of the Ok button to save.
 * 
 * @author Jeff Tassin
 */
public class InvokeOptionsDialog extends TSDialog {
	/** the underlying database connection */
	private TSConnection m_connection;

	/** the view for this dialog */
	private ParametersView m_view;

	/** the procedure model */
	private ProcedureModel m_model;

	/**
	 * ctor
	 */
	public InvokeOptionsDialog(Frame owner, boolean bmodal) {
		super(owner, bmodal);
		setTitle(I18N.getLocalizedMessage("Procedure Options"));
	}

	/**
	 * ctor
	 */
	public InvokeOptionsDialog(Dialog owner, boolean bmodal) {
		super(owner, bmodal);
		setTitle(I18N.getLocalizedMessage("Procedure Options"));
	}

	/**
	 * Initializes the dialog
	 */
	public void initialize(TSConnection connection, ProcedureModel model, ParametersView view) {
		m_connection = connection;
		m_view = view;
		m_model = model;

		setPrimaryPanel(view);

		JButton okbtn = getOkButton();
		okbtn.setText(I18N.getLocalizedMessage("Save"));

		JButton invokebtn = new JButton(I18N.getLocalizedMessage("Invoke"));
		JPanel panel = getButtonPanel();
		panel.add(invokebtn, 0);

		invokebtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				// update the procedure model with the latest information from
				// the view
				m_view.saveToModel();
				m_model.setParameters(m_view.getModel());
				if (validateModel(m_model)) {
					ProcedureRunner runner = new ProcedureRunner(m_connection, m_model);
					if (runner.invokeProcedure())
						cmdCancel();
				}
			}
		});
	}

	/**
	 * This method looks at all of the ParameterTypes in the model and verifies
	 * that they are valid JDBC types and can be acceptably rendered in the
	 * InstanceView
	 */
	boolean validateModel(ProcedureModel model) {
		boolean bresult = true;

		for (int index = 0; index < model.getParameterCount(); index++) {
			ProcedureParameter param = model.getParameter(index);
			if (!DbUtils.isJDBCType(param.getType())) {
				String msg = I18N.format("parameter_unknown_type_1", param.getName());
				String error = I18N.getLocalizedMessage("Error");
				JOptionPane.showMessageDialog(null, msg, error, JOptionPane.ERROR_MESSAGE);
				bresult = false;
				break;
			}
		}
		return bresult;
	}

}
