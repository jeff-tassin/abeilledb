package com.jeta.foundation.gui.editor.options;

import java.awt.BorderLayout;
import java.awt.Dimension;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JLabel;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.components.TSController;

import com.jeta.foundation.gui.editor.KitSet;
import com.jeta.foundation.gui.layouts.TableLayout;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import org.netbeans.editor.*;

/**
 * This is a panel that allows the user to modify properties for the application
 * text editor This includes key bindings, abbreviations, tab settings, etc.
 * 
 * @author Jeff Tassin
 */
public class EditorOptionsView extends TSPanel {
	/** the model that stores all the persistent information */
	private EditorOptionsModel m_model;

	/** the combo box that displays the available key bindings */
	private JComboBox m_keybindingcombo;

	/** checkbox for showing line numbers */
	private JCheckBox m_linenumbersbox;

	/** command ids */
	public static final String ID_EDIT_BINDINGS = "jeta.editor.keyboard.editbindings";
	public static final String ID_CREATE_BINDINGS = "jeta.editor.keyboard.createbindings";
	public static final String ID_DELETE_BINDINGS = "jeta.editor.keyboard.deletebindings";
	public static final String ID_EDIT_MACROS = "jeta.editor.keyboard.editmacros";

	/**
	 * ctor Shows the key bindings/macros assigned to a given editor class
	 */
	public EditorOptionsView(EditorOptionsModel model) {
		m_model = model;
		initialize();
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		EditorOptionsUIDirector uidirector = new EditorOptionsUIDirector(this);
		setUIDirector(uidirector);
		uidirector.updateComponents(null);
	}

	/**
	 * Adds a key binding model to the combo box and the underlying
	 * EditorOptionsModel
	 */
	void addKeyBindings(KeyBindingModel model) {
		m_keybindingcombo.addItem(model.getBindingName());
		m_model.setKitKeyBindingModel(model.getBindingName(), model);
		m_keybindingcombo.setSelectedItem(model.getBindingName());
	}

	/**
	 * Creates the key binding combo box panel that allows the user to view and
	 * edit key bindings.
	 */
	TSPanel createKeyBindingComboPanel(KitSet kitSet) {
		TSPanel panel = new TSPanel();
		// panel.setLayout( new BorderLayout() );
		m_keybindingcombo = new JComboBox();

		// get names of all bindings
		Collection c = m_model.getBindingNames();
		Iterator iter = c.iterator();
		while (iter.hasNext()) {
			String name = (String) iter.next();
			m_keybindingcombo.addItem(name);
		}

		String activeeditor = m_model.getActiveBindings();
		m_keybindingcombo.setSelectedItem(activeeditor);

		JPanel btnpanel = new JPanel();

		JButton bindassignbtn = createButton("", ID_EDIT_BINDINGS);
		bindassignbtn.setIcon(TSGuiToolbox.loadImage("incors/16x16/document_edit.png"));
		bindassignbtn.setContentAreaFilled(false);
		bindassignbtn.setBorderPainted(false);
		bindassignbtn.setFocusPainted(false);
		bindassignbtn.setToolTipText(I18N.getLocalizedMessage("Edit selected mapping"));

		Dimension d = new Dimension(16, 16);
		bindassignbtn.setSize(d);
		bindassignbtn.setPreferredSize(d);
		bindassignbtn.setMaximumSize(d);

		JButton bindcreatebtn = createButton("", ID_CREATE_BINDINGS);
		bindcreatebtn.setIcon(TSGuiToolbox.loadImage("incors/16x16/document_add.png"));
		bindcreatebtn.setContentAreaFilled(false);
		bindcreatebtn.setBorderPainted(false);
		bindcreatebtn.setFocusPainted(false);
		bindcreatebtn.setSize(d);
		bindcreatebtn.setPreferredSize(d);
		bindcreatebtn.setMaximumSize(d);
		bindcreatebtn.setToolTipText(I18N.getLocalizedMessage("Create new mapping"));

		JButton binddeletebtn = createButton("", ID_DELETE_BINDINGS);
		binddeletebtn.setIcon(TSGuiToolbox.loadImage("incors/16x16/document_delete.png"));
		binddeletebtn.setContentAreaFilled(false);
		binddeletebtn.setBorderPainted(false);
		binddeletebtn.setFocusPainted(false);
		binddeletebtn.setSize(d);
		binddeletebtn.setPreferredSize(d);
		binddeletebtn.setMaximumSize(d);
		binddeletebtn.setToolTipText(I18N.getLocalizedMessage("Delete selected mapping"));

		btnpanel.setLayout(new BoxLayout(btnpanel, BoxLayout.X_AXIS));
		btnpanel.add(Box.createHorizontalStrut(5));
		btnpanel.add(bindassignbtn);
		btnpanel.add(bindcreatebtn);
		btnpanel.add(binddeletebtn);
		btnpanel.add(Box.createHorizontalStrut(5));

		/** 3 rows x 6 columns */
		double size[][] = {
				{ TSGuiToolbox.calculateAverageTextWidth(m_keybindingcombo, 25), 5, TableLayout.PREFERRED },
				{ TableLayout.PREFERRED } };

		panel.setLayout(new TableLayout(size));
		panel.add(m_keybindingcombo, "0,0");
		panel.add(btnpanel, "2,0");

		return panel;
	}

	/**
	 * Removes the bindings from the combo box and the data model
	 */
	void deleteBindings(String editor) {
		m_keybindingcombo.removeItem(editor);
		m_model.deleteBindings(editor);
	}

	/**
	 * @return the kit set for the editor we are working with
	 */
	public KitSet getKitSet() {
		return m_model.getKitSet();
	}

	/**
	 * @return the data model for this view
	 */
	public EditorOptionsModel getModel() {
		return m_model;
	}

	/**
	 * @return the preferred size for this panel
	 */
	public Dimension getPreferredSize() {
		return TSGuiToolbox.getWindowDimension(6, 7);
	}

	/**
	 * @return the name of the editor selected by the user
	 */
	public String getSelectedEditor() {
		return (String) m_keybindingcombo.getSelectedItem();
	}

	/**
	 * Creates the controls for the panel and gets the initial values from the
	 * model
	 */
	private void initialize() {
		CellConstraints cc = new CellConstraints();

		FormLayout layout = new FormLayout("pref, 4dlu, min, pref:grow", "pref, 4dlu, pref");

		setLayout(layout);

		JPanel btnpanel = new JPanel(new BorderLayout());
		btnpanel.add(i18n_createButton("Edit", ID_EDIT_MACROS, "incors/16x16/gears.png"), BorderLayout.WEST);

		add(new JLabel(I18N.getLocalizedDialogLabel("Key Bindings")), cc.xy(1, 1));
		add(new JLabel(I18N.getLocalizedDialogLabel("Macros")), cc.xy(1, 3));

		TSPanel keybindingpanel = createKeyBindingComboPanel(getKitSet());
		add(keybindingpanel, cc.xywh(3, 1, 2, 1));
		add(btnpanel, cc.xy(3, 3));
	}

	/**
	 * Saves the key settings that have been selected by the user
	 */
	public void saveToModel() {
		String newbindings = getSelectedEditor();
		m_model.setActiveBindings(newbindings);
	}

}
