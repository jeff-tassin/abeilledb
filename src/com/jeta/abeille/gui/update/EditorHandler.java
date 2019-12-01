package com.jeta.abeille.gui.update;

import java.awt.BorderLayout;

import java.io.ObjectStreamException;
import java.io.IOException;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jeta.abeille.database.model.ColumnMetaData;

import com.jeta.foundation.common.JETAExternalizable;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.components.TSController;

import com.jeta.foundation.gui.utils.ControlsAlignLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

import com.jeta.open.rules.RuleResult;

/**
 * This column handler uses a multiline editor to handler column data. It is
 * mainly used for columns that have large amounts of text or file objects such
 * as XML
 * 
 * @author Jeff Tassin
 */
public class EditorHandler extends DefaultColumnHandler implements JETAExternalizable {
	static final long serialVersionUID = 1116125323616215924L;

	public static int VERSION = 1;

	public static final String HANDLER_NAME = I18N.getLocalizedMessage("Multiline Editor_varchar");
	public static final int DEFAULT_EDITOR_SIZE = 5;

	private int m_editorsize;
	private boolean m_bscrollbars;

	/**
	 * ctor
	 */
	public EditorHandler() {
		m_editorsize = DEFAULT_EDITOR_SIZE;
	}

	/**
	 * Creates a component to handle a specified column for the InstanceView The
	 * type of component created depends on the type of column. For example, if
	 * the column is a integer, then an IntegralComponent is returned.
	 * 
	 * @param cmd
	 *            the column metadata object that specifies which type of
	 *            component to create
	 */
	public InstanceComponent createComponent(ColumnMetaData cmd, InstanceView view) {
		return new EditorComponent(cmd.getColumnName(), cmd.getType(), m_editorsize);
	}

	/**
	 * Creates the panel/controller that is used to configure this handler.
	 * 
	 * @return the panel with the correct layout and controller
	 */
	public TSPanel createConfigurationPanel() {
		JComponent[] controls = new JComponent[1];
		JTextField editorsizefield = new JTextField(String.valueOf(m_editorsize)); // default
																					// to
																					// two
																					// rows
		editorsizefield.setName(ID_EDITOR_SIZE_FIELD);

		// JCheckBox sbarscheck = new JCheckBox(
		// I18N.getLocalizedMessage("Show") );
		// sbarscheck.setName( ID_SCROLL_BARS_BOX );
		// sbarscheck.setSelected( m_bscrollbars );

		controls[0] = editorsizefield;
		// controls[1] = sbarscheck;

		JLabel[] labels = new JLabel[1];
		labels[0] = new JLabel(I18N.getLocalizedDialogLabel("Size"));
		// labels[1] = new JLabel(I18N.getLocalizedDialogLabel("Scroll Bars") );

		ControlsAlignLayout layout = new ControlsAlignLayout();
		layout.setMaxTextFieldWidth(editorsizefield, 4);

		final TSPanel panel = new TSPanel(new BorderLayout());
		panel.add(TSGuiToolbox.alignLabelTextRows(layout, labels, controls), BorderLayout.CENTER);
		panel.setController(new TSController(null) {
			public RuleResult check(Object[] params) {
				JTextField txtfield = (JTextField) panel.getComponentByName(ID_EDITOR_SIZE_FIELD);
				int editorsize = Integer.parseInt(txtfield.getText());
				if (editorsize <= 1 || editorsize > 10) {
					return new RuleResult(I18N.getLocalizedMessage("Invalid editor size"));
				} else {
					return RuleResult.SUCCESS;
				}
			}
		});

		return panel;
	}

	/**
	 * @return the size of the editor (number of lines)
	 */
	public int getEditorSize() {
		return m_editorsize;
	}

	/**
	 * @return the name of this handler
	 */
	public String getName() {
		return HANDLER_NAME;
	}

	/**
	 * Reads the user input from the given panel. This panel should be the one
	 * created by the call to createConfigurationPanel().
	 */
	public void readInput(TSPanel panel) {
		JTextField txtfield = (JTextField) panel.getComponentByName(ID_EDITOR_SIZE_FIELD);
		if (txtfield != null)
			m_editorsize = Integer.parseInt(txtfield.getText());

		// JCheckBox sbarscheck = (JCheckBox) panel.getComponentByName(
		// ID_SCROLL_BARS_BOX );
		// if ( sbarscheck != null )
		// m_bscrollbars = sbarscheck.isSelected();

	}

	/**
	 * Sets the size of the editor (number of lines)
	 */
	public void setEditorSize(int editorSize) {
		m_editorsize = editorSize;
	}

	/**
	 * @return the name of this handler
	 */
	public String toString() {
		return getName();
	}

	// /////////////////////////////////////////////////////////////////////////////
	// names used for the EditorHandler and supporting classes
	public static final String ID_EDITOR_SIZE_FIELD = "editorsizefield";
	public static final String ID_SCROLL_BARS_BOX = "scrollbarsbox";

	/**
	 * Externalizable Implementation
	 */
	public void readExternal(java.io.ObjectInput in) throws ClassNotFoundException, IOException {
		super.readExternal(in);
		int version = in.readInt();
		m_editorsize = in.readInt();
		m_bscrollbars = in.readBoolean();
	}

	/**
	 * Externalizable Implementation
	 */
	public void writeExternal(java.io.ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(VERSION);
		out.writeInt(m_editorsize);
		out.writeBoolean(m_bscrollbars);
	}

}
