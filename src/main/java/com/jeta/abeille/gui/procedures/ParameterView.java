package com.jeta.abeille.gui.procedures;

import java.awt.BorderLayout;
import java.awt.Dimension;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import com.jeta.abeille.database.procedures.ParameterDirection;
import com.jeta.abeille.database.procedures.ProcedureParameter;

import com.jeta.abeille.database.utils.DbUtils;

import com.jeta.foundation.gui.components.TSComboBox;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * This view allows the user to edit/view the attributes for a single procedure
 * parameter
 * 
 * @author Jeff Tassin
 */
public class ParameterView extends TSPanel {

	/** component ids */
	public static final String ID_NAME_COMPONENT = "name.component";
	public static final String ID_IN_BUTTON = "in.button";
	public static final String ID_OUT_BUTTON = "out.button";
	public static final String ID_INOUT_BUTTON = "in.out.button";
	public static final String ID_RETURN_BUTTON = "return.button";
	public static final String ID_DATATYPE_COMPONENT = "data.type.component";

	/**
	 * ctor Call when creating a new parameter
	 */
	public ParameterView() {
		initialize();
	}

	/**
	 * ctor
	 */
	public ParameterView(ProcedureParameter param) {
		initialize();

		JTextComponent comp = (JTextComponent) getComponentByName(ID_NAME_COMPONENT);
		comp.setText(param.getName());

		ParameterDirection direction = param.getDirection();
		if (direction == ParameterDirection.OUT) {
			JRadioButton btn = (JRadioButton) getComponentByName(ID_OUT_BUTTON);
			btn.setSelected(true);
		} else if (direction == ParameterDirection.INOUT) {
			JRadioButton btn = (JRadioButton) getComponentByName(ID_INOUT_BUTTON);
			btn.setSelected(true);
		} else {
			JRadioButton btn = (JRadioButton) getComponentByName(ID_IN_BUTTON);
			btn.setSelected(true);
		}

		TSComboBox datatype = (TSComboBox) getComponentByName(ID_DATATYPE_COMPONENT);
		datatype.setSelectedItem(DbUtils.getJDBCTypeName(param.getType()));
	}

	/**
	 * creates the controls for this panel
	 */
	private JPanel createControlsPanel() {
		JTextField paramname = new JTextField();
		paramname.setName(ID_NAME_COMPONENT);

		ButtonGroup group = new ButtonGroup();
		JRadioButton inbtn = new JRadioButton("IN");
		inbtn.setName(ID_IN_BUTTON);
		inbtn.setSelected(true);
		inbtn.setEnabled(false);
		group.add(inbtn);
		JRadioButton outbtn = new JRadioButton("OUT");
		outbtn.setName(ID_OUT_BUTTON);
		outbtn.setEnabled(false);

		group.add(outbtn);
		JRadioButton inoutbtn = new JRadioButton("INOUT");
		inoutbtn.setName(ID_INOUT_BUTTON);
		inoutbtn.setEnabled(false);
		group.add(inoutbtn);
		// JRadioButton returnbtn = new JRadioButton(
		// I18N.getLocalizedMessage("Return") );
		// returnbtn.setName( ID_RETURN_BUTTON );
		// group.add( returnbtn );

		assert (false);
		TSComboBox datatype = com.jeta.abeille.gui.common.DbGuiUtils.createDataTypeCombo(null);
		datatype.setName(ID_DATATYPE_COMPONENT);

		JComponent[] controls = new JComponent[5];
		controls[0] = paramname;
		controls[1] = inbtn;
		controls[2] = outbtn;
		controls[3] = inoutbtn;
		controls[4] = datatype;

		JLabel[] labels = new JLabel[5];
		labels[0] = new JLabel(I18N.getLocalizedDialogLabel("Name"));
		labels[1] = new JLabel("INOUT");
		labels[2] = new JLabel();
		labels[3] = new JLabel();
		labels[4] = new JLabel(I18N.getLocalizedDialogLabel("Type"));

		return TSGuiToolbox.alignLabelTextRows(labels, controls);
	}

	/**
	 * @return the parameter direction entered by the user
	 */
	public ParameterDirection getDirection() {
		JRadioButton btn = (JRadioButton) getComponentByName(ID_IN_BUTTON);
		if (btn.isSelected()) {
			return ParameterDirection.IN;
		}

		btn = (JRadioButton) getComponentByName(ID_OUT_BUTTON);
		if (btn.isSelected()) {
			return ParameterDirection.OUT;
		}

		btn = (JRadioButton) getComponentByName(ID_INOUT_BUTTON);
		if (btn.isSelected()) {
			return ParameterDirection.INOUT;
		}

		// we should never be here!
		return ParameterDirection.IN;

	}

	/**
	 * @return the name entered by the user in the name field
	 */
	public String getName() {
		JTextComponent comp = (JTextComponent) getComponentByName(ID_NAME_COMPONENT);
		return comp.getText();
	}

	/**
	 * @return the procedure parameter defined by the user
	 */
	public ProcedureParameter getParameter() {
		String name = getName();
		ParameterDirection direction = getDirection();
		int type = getType();

		return new ProcedureParameter();
	}

	/**
	 * @return the preferred size for this panel
	 */
	public Dimension getPreferredSize() {
		return new Dimension(300, 200);
	}

	/**
	 * @return the datatype entered by the user
	 */
	public int getType() {
		TSComboBox datatype = (TSComboBox) getComponentByName(ID_DATATYPE_COMPONENT);
		String txt = datatype.getText();
		// return DbUtils.getDataType( txt );
		assert (false);
		return 0;
	}

	/** initializes the components for the view */
	private void initialize() {
		setLayout(new BorderLayout());
		add(createControlsPanel(), BorderLayout.CENTER);
	}

}
