package com.jeta.foundation.gui.editor.macros;

import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JComponent;
import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JEditorPane;
import javax.swing.text.JTextComponent;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.components.TSComponentUtils;

import com.jeta.foundation.gui.editor.KitSet;
import com.jeta.foundation.gui.editor.TSEditorMgr;
import com.jeta.foundation.gui.editor.TSEditorUtils;
import com.jeta.foundation.gui.editor.TSPlainKit;

import com.jeta.foundation.gui.utils.ControlsAlignLayout;
import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

/**
 * This is the panel for editing a macro. It allows the user to assing a
 * sequence of actions to execute
 * 
 * @author Jeff Tassin
 */
public class MacroPanel extends TSPanel {

	/** the description field for this macro */
	private JTextField m_namefield;

	/** the label for the description field */
	private JLabel m_namelabel;

	/** the editor pane for the macro command sequence */
	private JEditorPane m_editor;

	/** the editor kits that define the macros we are editing */
	private KitSet m_kitset;

	/** flag that indicates if this is a new macro */
	private boolean m_new;

	/**
	 * ctor
	 */
	public MacroPanel(KitSet kitSet, Macro macro) {
		m_kitset = kitSet;
		initialize();

		if (macro == null) {
			m_new = true;
		} else {
			m_new = false;
			setName(macro.getName());
			setCommand(macro.getCommand());
		}

	}

	/**
	 * Creates the macro list panel in the middle of this window
	 * 
	 * @return the panel containing the keymap list
	 */
	private Container createMacroPanel() {
		// description
		JComponent[] labels = new JComponent[1];
		m_namelabel = new JLabel(I18N.getLocalizedMessage("Description"));
		labels[0] = m_namelabel;
		JComponent[] comps = new JComponent[1];
		m_namefield = TSEditorUtils.createTextField();
		m_namefield.setName(ID_NAME);

		comps[0] = m_namefield;

		ControlsAlignLayout layout = new ControlsAlignLayout();
		layout.setMaxTextFieldWidth(m_namefield, 25);

		JPanel descpanel = TSGuiToolbox.alignLabelTextRows(layout, labels, comps);

		JPanel commandpanel = new JPanel(new BorderLayout());
		commandpanel.setBorder(BorderFactory.createTitledBorder("Command Sequence"));

		// text/plain2
		// m_editor = TSEditorUtils.createEditor( TSPlainKit.class, null );
		m_editor = TSEditorUtils.createEditor(new TSPlainKit());
		JComponent comp = TSEditorUtils.getExtComponent(m_editor);
		commandpanel.add(comp, BorderLayout.CENTER);

		JPanel main = new JPanel(new BorderLayout(8, 8));
		main.add(descpanel, BorderLayout.NORTH);
		main.add(commandpanel, BorderLayout.CENTER);
		main.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		return main;
	}

	/**
	 * @return the command string for this macro
	 */
	public String getCommand() {
		String result = "";
		try {
			Document doc = getEditorPane().getDocument();
			result = doc.getText(0, doc.getLength());
			if (result != null)
				result = result.trim();
		} catch (BadLocationException ble) {
			// hmm...
		}
		return result;
	}

	/**
	 * @return the editor pane that is used for the command sequence in this
	 *         panel
	 */
	public JEditorPane getEditorPane() {
		return m_editor;
	}

	/**
	 * @return the kit set for this panel
	 */
	public KitSet getKitSet() {
		return m_kitset;
	}

	/**
	 * @return the macro defined by this panel
	 */
	public Macro getMacro() {
		return new Macro(getName(), getCommand(), null);
	}

	/**
	 * @return the description for this panel
	 */
	public String getName() {
		return m_namefield.getText();
	}

	/**
	 * @return the text component used for the name field
	 */
	public JTextComponent getNameField() {
		return m_namefield;
	}

	/**
	 * @return the preferred size for this panel
	 */
	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(8, 10);
	}

	/**
	 * Creates and initializes the controls on this frame
	 */
	public void initialize() {
		setLayout(new BorderLayout());
		add(createMacroPanel(), BorderLayout.CENTER);
	}

	/**
	 * @return true if this view represents a new macro
	 */
	boolean isNew() {
		return m_new;
	}

	/**
	 * Sets the macro to be displayed by this panel
	 */
	public void setMacro(Macro macro) {
	}

	/**
	 * Sets the description (name) for this macro
	 * 
	 * @param description
	 *            the text to set for this macro's description
	 */
	public void setName(String description) {
		m_namefield.setText(description);
	}

	/**
	 * Sets the command for this macro
	 * 
	 * @param cmd
	 *            the command sequence to set
	 */
	public void setCommand(String cmd) {
		m_editor.setText(cmd);
	}

	// ////////////////////////////////////////////////////////////////////////////////
	// component ids
	public static final String ID_NAME = "jeta.keyboard.macropanel.description";
	public static final String ID_COMMAND = "jeta.keyboard.macropanel.command";

}
