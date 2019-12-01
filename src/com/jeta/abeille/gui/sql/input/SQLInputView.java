package com.jeta.abeille.gui.sql.input;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.LayoutManager;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JViewport;
import javax.swing.text.JTextComponent;

import com.jeta.foundation.gui.components.TSPanel;

import com.jeta.foundation.i18n.I18N;

/**
 * This class is used to build a set of text fields that the user can enter
 * values for contraints in a SQL statement. If the user types a SQL statement
 * similar to a prepared statement, the SQL parser will extract the ? tokens and
 * then launch a dialog with this panel. The user can then type the constraints
 * for the query.
 * 
 * @author Jeff Tassin
 */
public class SQLInputView extends TSPanel {
	/** the data model */
	private SQLInputModel m_model;

	private final static int LABEL_X_OFFSET = 10;

	/**
	 * ctor
	 */
	public SQLInputView(SQLInputModel model) {
		m_model = model;
		setLayout(new BorderLayout());
		TSPanel panel = createView();
		add(panel, BorderLayout.CENTER);
	}

	/**
	 * @return the preferred size for this view
	 */
	public Dimension getPreferredSize() {
		return new Dimension(400, 200);
	}

	/**
	 * Utility method that gets the viewport for the given container. The
	 * container will always be an instance of this view. AND, this view must
	 * always be inside a JScrollPane
	 */
	private JViewport getViewport(Container container) {
		Component comp = container;
		while (comp != null) {
			if (comp instanceof JViewport)
				return (JViewport) comp;

			comp = comp.getParent();
		}
		return null;
	}

	/**
	 * Builds the view from the model
	 */
	private TSPanel createView() {
		TSPanel panel = new TSPanel();
		panel.setLayout(new SQLInputLayoutManager());

		for (int index = 0; index < m_model.getInputCount(); index++) {
			InputField ifield = m_model.getInput(index);
			panel.add(ifield.getLabel());
			panel.add(ifield.getOperatorComponent());
			panel.add(ifield.getInputComponent());
		}

		return panel;
	}

	/**
	 * @returns the first component on the view that handles an input. This
	 *          method is mainly used so we can set the focus to that component
	 *          when the view is first displayed.
	 */
	JComponent getFirstEntryComponent() {
		if (m_model.getInputCount() > 0) {
			InputField ifield = m_model.getInput(0);
			return ifield.getInputComponent();
		}
		return null;
	}

	/**
	 * This is the layout manager for the view. It handles sizing the text
	 * fields and setting their positions
	 * 
	 */
	class SQLInputLayoutManager implements LayoutManager {
		public void addLayoutComponent(String name, Component comp) {
		}

		/**
		 * do the layout
		 */
		public void layoutContainer(Container parent) {
			// now calculate the longest label length
			// we truncate those fields with very long names so the controls
			// aren't too
			// spread out on the form
			int max_label_length = 0;

			for (int index = 0; index < m_model.getInputCount(); index++) {
				InputField ifield = m_model.getInput(index);
				JLabel label = ifield.getLabel();
				String txt = ifield.getName();
				if (max_label_length < txt.length())
					max_label_length = txt.length();

				JComponent op_comp = ifield.getOperatorComponent();
				op_comp.setSize(op_comp.getPreferredSize());

				JTextComponent inputcomp = ifield.getInputComponent();
				inputcomp.setSize(inputcomp.getPreferredSize());
			}

			if (max_label_length == 0 || max_label_length > 20) {
				max_label_length = 20;
			}

			if (max_label_length < 8)
				max_label_length += 1;

			// gets the viewport for the container. Note, this view must be
			// put in a scroll pane
			JViewport viewport = getViewport(parent);
			assert (viewport != null);

			int y = 10;
			for (int index = 0; index < m_model.getInputCount(); index++) {
				InputField ifield = m_model.getInput(index);
				JLabel label = ifield.getLabel();
				FontMetrics metrics = label.getFontMetrics(label.getFont());

				String labeltxt = ifield.getName();
				if (labeltxt.length() > max_label_length) {
					labeltxt = labeltxt.substring(0, max_label_length - 3) + "...";
					StringBuffer lbuff = new StringBuffer();
					lbuff.append("<html><body><b>");
					lbuff.append(labeltxt);
					lbuff.append("</b></body></html>");
					label.setText(lbuff.toString());
				}

				int row_height = metrics.getHeight() * 3 / 2;

				JTextComponent inputcomp = ifield.getInputComponent();
				if (row_height < inputcomp.getHeight())
					row_height = inputcomp.getHeight();

				int xloc = LABEL_X_OFFSET + metrics.stringWidth("M") * max_label_length + 10;

				label.setLocation(LABEL_X_OFFSET, y);
				JComponent op_comp = ifield.getOperatorComponent();
				op_comp.setLocation(xloc, y);
				xloc += 30;
				inputcomp.setLocation(xloc, y);

				int row_width = viewport.getWidth() - xloc - 20;
				int min_width = metrics.stringWidth(I18N.getLocalizedMessage("null"));

				if (row_width < min_width)
					row_width = min_width;

				Dimension d = inputcomp.getPreferredSize();
				d.width = row_width;
				inputcomp.setSize(d);

				label.setSize(label.getPreferredSize());

				y += row_height;
			}
		}

		public Dimension minimumLayoutSize(Container parent) {
			return new Dimension(100, 100);
		}

		public Dimension preferredLayoutSize(Container parent) {
			return new Dimension(800, 800);
		}

		public void removeLayoutComponent(Component comp) {

		}
	}

}
