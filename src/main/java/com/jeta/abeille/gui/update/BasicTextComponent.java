package com.jeta.abeille.gui.update;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JTextField;

import com.jeta.abeille.database.utils.SQLFormatter;
import com.jeta.abeille.query.Expression;

import com.jeta.foundation.gui.editor.TSPlainKit;
import com.jeta.foundation.gui.editor.TSEditorUtils;
import com.jeta.foundation.gui.editor.TSTextDialog;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.gui.components.TSComponentNames;

/**
 * This class represents a text component in the InstanceFrame This component
 * has an icon and a JTextField [icon][JTextField]
 * 
 * @author Jeff Tassin
 */
public class BasicTextComponent extends TextComponentBase {
	int m_width = 0; // this component is resized according to the size of the
						// parent container, so we store the

	// desired width to use for our layout manager. I guess I could have used
	// setPreferredSize as well.

	/**
	 * Constructor for UpdateTextComponent
	 * 
	 * @param fieldName
	 *            the name of the column associated with this component
	 */
	public BasicTextComponent(String fieldName, int dataType) {
		this(fieldName, dataType, true);
	}

	public BasicTextComponent(String fieldName, int dataType, boolean createBtn) {
		super(fieldName, dataType);

		setIcon("general/Edit16.gif");
		setLayout(new BasicTextLayoutManager());

		JTextField field = new InstanceTextField(this);
		setComponent(field);

		if (createBtn) {
			JButton btn = getIconButton();
			btn.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent evt) {
					TSTextDialog dlg = (TSTextDialog) TSGuiToolbox.createDialog(TSTextDialog.class,
							BasicTextComponent.this, true);
					dlg.setTitle(getFieldName());
					TSGuiToolbox.centerFrame(dlg, 0.75f, 0.5f);
					dlg.initialize(com.jeta.abeille.gui.sql.SQLKit.class);
					dlg.setText(BasicTextComponent.this.toString());
					dlg.show();
					if (dlg.isOk()) {
						setValue(dlg.getText());
						setModified(true);
					}
				}
			});
		}
	}

	/**
	 * @return the preferred size of this component
	 */
	public Dimension getPreferredSize() {
		JComponent comp = getComponent();
		Dimension d = comp.getPreferredSize();
		d.width = d.width + getComponentX() + 2;
		return d;
	}

	/**
	 * @return the underlying text field
	 */
	public JTextField getTextField() {
		return (JTextField) getComponent();
	}

	/**
	 * Sets the value represented by this component into the prepared statement
	 * 
	 * @param count
	 *            the parameter index to set
	 * @param pstmt
	 *            the prepared statement to act on
	 */
	public void prepareStatement(int count, PreparedStatement pstmt, SQLFormatter sqlformatter) throws SQLException {
		if (isNull()) {
			pstmt.setNull(count, java.sql.Types.VARCHAR);
		} else {
			pstmt.setString(count, toString());
		}
	}

	/**
	 * @return a sql representation of this component value
	 */
	public String toSQLString(SQLFormatter formatter) {
		return formatter.formatString(toString());
	}

	/**
	 * Sets the width of this component.
	 */
	public void setWidth(int width) {
		JComponent comp = getComponent();
		Dimension d = comp.getPreferredSize();
		d.width = width - getComponentX();
		m_width = d.width;
		comp.setPreferredSize(d);
		comp.setSize(d);
	}

	class BasicTextLayoutManager implements LayoutManager {
		public void addLayoutComponent(String name, Component comp) {
		}

		public void layoutContainer(Container parent) {
			JComponent component = getComponent();
			JComponent iconbtn = getIconButton();
			Dimension d = component.getPreferredSize();
			d.width = m_width;
			component.setSize(d);
			component.setLocation(getComponentX(), 0);
			iconbtn.setLocation(0, 0);
		}

		public Dimension minimumLayoutSize(Container parent) {
			return new Dimension(12, 12);
		}

		public Dimension preferredLayoutSize(Container parent) {
			return getPreferredSize();
		}

		public void removeLayoutComponent(Component comp) {
		}

	}

}
