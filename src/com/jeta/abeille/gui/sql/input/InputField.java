package com.jeta.abeille.gui.sql.input;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.netbeans.editor.TokenID;

import com.jeta.abeille.gui.sql.SQLTokenContext;
import com.jeta.abeille.gui.sql.TokenInfo;
import com.jeta.abeille.gui.update.InstanceView;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

/**
 * This class represents a single input field for a SQLInputView it is basically
 * a label with a text field. [label] [textfield]
 * 
 * @author Jeff Tassin
 */
public class InputField {
	/** the text label */
	private JLabel m_label;

	/** the text field */
	private JTextField m_textfield;

	/** the input definition */
	private SQLInput m_input;

	/** the constraint operator icon */
	private JLabel m_op_label;

	public InputField(SQLInput input) {
		StringBuffer lbuff = new StringBuffer();
		lbuff.append("<html><body><b>");
		lbuff.append(input.getName());
		lbuff.append("</b></body></html>");
		m_label = new JLabel(lbuff.toString());

		TokenInfo tinfo = input.getInputToken();
		TokenID token = tinfo.getToken();
		if (token == SQLTokenContext.AT) {
			m_label.setIcon(TSGuiToolbox.loadImage("sql_input_at16.gif"));
		} else {
			m_label.setIcon(TSGuiToolbox.loadImage("general/Empty16.gif"));
		}

		m_textfield = new JTextField();
		m_input = input;

		m_op_label = new JLabel();
		Dimension d = new Dimension(16, 16);
		m_op_label.setMaximumSize(d);
		m_op_label.setPreferredSize(d);
		m_op_label.setIcon(InstanceView.getConstraintIcon(m_input.getOperatorToken().getValue()));

	}

	/**
	 * @return the component used to input data
	 */
	public JTextComponent getInputComponent() {
		return m_textfield;
	}

	public JComponent getOperatorComponent() {
		return m_op_label;
	}

	public String getName() {
		return m_input.getName();
	}

	/**
	 * @return the label next to the component
	 */
	public JLabel getLabel() {
		return m_label;
	}

	public void save() {
		m_input.setValue(m_textfield.getText());
	}
}
