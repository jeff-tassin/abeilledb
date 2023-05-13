package com.jeta.abeille.gui.update;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import java.sql.SQLException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;


import com.jeta.abeille.database.model.ColumnMetaData;
import com.jeta.abeille.database.utils.DbUtils;
import com.jeta.abeille.database.utils.SQLFormatter;
import com.jeta.abeille.query.ConstraintNode;
import com.jeta.abeille.query.Expression;
import com.jeta.abeille.query.LogicalConnective;
import com.jeta.abeille.query.Operator;
import com.jeta.abeille.query.QueryConstraint;
import com.jeta.abeille.query.Reportable;

import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;

/**
 * This window shows the fields (columns) for a database table. It allows the
 * user to query the table and view and *edit* the results (1 row at a time). It
 * has enhanced gui capabilities over and above the general query results window
 * 
 * We currently support the following operators >,>=,<,<=,!=,~
 * 
 * @author Jeff Tassin
 */
public class InstanceView extends TSPanel {
	/**
	 * the view contains all of the data components It is a layer in the
	 * JLayered pane of this view
	 */
	private InstanceComponentContainer m_view;

	/**
	 * the overlay is used to render the null icons over each component. It is a
	 * layer above the m_view
	 */
	private OverlayPanel m_overlay;

	/**
	 * The layered pane for this view
	 */
	private JLayeredPane m_layered_pane;

	private HashMap m_fieldcontrols = new HashMap();
	private static final int TEXTFIELDX = 26;
	private int m_maxlabelLength = FieldElement.MAX_FIELD_NAME_LENGTH;

	private InstanceModel m_instancemodel; // when we are in query mode, this is
											// the result set

	private static ImageIcon m_noconstrainticon;
	private ConstraintPopup m_constraintPopup = new ConstraintPopup();

	// for receiving action events from this view
	// see addActionListener
	private LinkedList m_listeners = new LinkedList();

	private BrowserPopup m_browserPopup = new BrowserPopup();

	// /////////////////////////////////////////////////////////////////////////////////
	// constraint
	public static final String EQUAL = "=";
	public static final String NOTEQUAL = "!=";
	public static final String LESSTHAN = "<";
	public static final String LESSTHANEQUAL = "<=";
	public static final String GREATERTHAN = ">";
	public static final String GREATERTHANEQUAL = ">=";
	public static final String LIKE = "LIKE";
	public static final String UNSELECTED = "";

	// this is the command string of the action event that occurs when the user
	// clicks a
	// constraint button
	public static final String ID_CONSTRAINT_ACTION = "instanceview.contraintaction";

	static {
		m_noconstrainticon = TSGuiToolbox.loadImage("update_unselected16.gif");
	}

	public InstanceView(InstanceModel model) {
		m_instancemodel = model;
		initialize();
	}

	/**
	 * Adds a listener to receive events for the actions that occur on this
	 * view. Currently, only one type of action event is supported - the
	 * constraint action. This event occurs when the user clicks a constraint
	 * button for a column. The event is sent with the following information:
	 * source - the field metadata object (not the button that fired the event)
	 * id - not used command - this is set to ID_CONSTRAINT_ACTION Upon
	 * receiving the event, the caller can retrieve the fieldmetadata object.
	 * This object can be used to retrieve other properties from this view
	 * 
	 * @param listener
	 *            the action listener that will receive the events
	 */
	public void addActionListener(ActionListener listener) {
		m_listeners.add(listener);
	}

	/**
	 * Clears all fields in the form. Sets to null all fields.
	 */
	public void clear() {
		Iterator iter = m_fieldcontrols.keySet().iterator();
		while (iter.hasNext()) {
			ColumnMetaData cmd = (ColumnMetaData) iter.next();
			FieldElement e = (FieldElement) m_fieldcontrols.get(cmd);
			e.fieldcomponent.clear();
			e.fieldcomponent.setModified(false);
			ConstraintButton cbtn = (ConstraintButton) e.getFilterButton();
			if (cbtn != null) {
				cbtn.setConstraint(UNSELECTED);
				cbtn.setIcon(m_constraintPopup.getIcon(UNSELECTED));
			}
		}
	}

	/**
	 * Clears the components on the form. Sets to null all fields. Does not
	 * clear the constraint buttons though.
	 */
	public void clearComponents() {
		Iterator iter = m_fieldcontrols.keySet().iterator();
		while (iter.hasNext()) {
			ColumnMetaData cmd = (ColumnMetaData) iter.next();
			FieldElement e = (FieldElement) m_fieldcontrols.get(cmd);
			e.fieldcomponent.clear();
			e.fieldcomponent.setModified(false);
		}
	}

	/**
	 * Clears all constraints.
	 */
	public void clearConstraints() {
		Iterator iter = m_fieldcontrols.keySet().iterator();
		while (iter.hasNext()) {
			ColumnMetaData cmd = (ColumnMetaData) iter.next();
			FieldElement e = (FieldElement) m_fieldcontrols.get(cmd);
			ConstraintButton cbtn = (ConstraintButton) e.getFilterButton();
			if (cbtn != null) {
				cbtn.setConstraint(UNSELECTED);
				cbtn.setIcon(m_constraintPopup.getIcon(UNSELECTED));
			}
		}
	}

	/**
	 * Clears any fields that are not constrained
	 */
	public void clearUnconstrained() {
		Iterator iter = m_fieldcontrols.keySet().iterator();
		while (iter.hasNext()) {
			ColumnMetaData cmd = (ColumnMetaData) iter.next();
			if (!isConstrained(cmd)) {
				FieldElement e = (FieldElement) m_fieldcontrols.get(cmd);
				e.fieldcomponent.clear();
				e.fieldcomponent.setModified(false);
			}
		}

	}

	/**
	 * Creates a field component that handles the specified type
	 * 
	 * @param cmd
	 *            the field metadata to create the component for
	 * @return the newly instantiated field component
	 */
	InstanceComponent createFieldComponent(ColumnMetaData cmd) {
		String columnname = cmd.getColumnName();
		ColumnSettings info = m_instancemodel.getColumnSettings(cmd);
		if (info.getColumnHandler() == null)
			info.setColumnHandler(new DefaultColumnHandler());
		return info.getColumnHandler().createComponent(cmd, this);
	}

	/**
	 * Sets the label color and adds any lable icons to the field element
	 * depending on the column
	 */
	public void decorate(FieldElement fe) {
		InstanceMetaData imd = m_instancemodel.getMetaData();
		ColumnMetaData cmd = fe.getColumnMetaData();
		JLabel label = fe.getLabel();
		label.setForeground(Color.black);
		JButton iconbtn = (JButton) fe.getIconButton();
		iconbtn.setIcon(null);
		if (m_instancemodel.isPrimaryKey(cmd)) {
			label.setForeground(new Color(0, 0, 96));
		} else if (imd.isLink(cmd)) {
			label.setForeground(new Color(96, 0, 0));
		}

	}

	public void doLayout() {
		super.doLayout();
		m_view.doLayout();
	}

	public BrowserPopup getBrowserPopup() {
		return m_browserPopup;
	}

	/**
	 * @param cmd
	 *            the field metadata object ot get the constraint for
	 * @return the selected constraint for the given filter element
	 */
	public String getConstraint(ColumnMetaData cmd) {
		String constraint = UNSELECTED;
		FieldElement e = (FieldElement) m_fieldcontrols.get(cmd);
		if (e != null) {
			ConstraintButton cbtn = (ConstraintButton) e.filterbtn;
			constraint = cbtn.getConstraint();
			if (constraint == null)
				constraint = UNSELECTED;
		}
		return constraint;
	}

	/**
	 * @return a collection of Expression objects that define the currently
	 *         constraint values in the view
	 * 
	 */
	public Collection getConstraintExpressions(SQLFormatter formatter) {
		LinkedList results = new LinkedList();
		boolean bfirst = true;

		int count = m_instancemodel.getColumnCount();
		for (int index = 0; index < count; index++) {
			ColumnMetaData cmd = (ColumnMetaData) m_instancemodel.getColumn(index);
			if (isConstrained(cmd)) {
				String constraint = getConstraint(cmd);
				String value = getSQLString(cmd, formatter);

				if (constraint != null && !constraint.equals(InstanceView.UNSELECTED)) {
					Expression expr = new Expression(cmd, Operator.fromString(constraint), value);
					results.add(expr);
				}

			}
		}
		return results;
	}

	/**
	 * This method returns a collection of ColumnMetaData objects. There is one
	 * object per filtered column.
	 */
	public Collection getConstraintColumns() {
		LinkedList results = new LinkedList();
		int count = m_instancemodel.getColumnCount();
		for (int index = 0; index < count; index++) {
			ColumnMetaData cmd = (ColumnMetaData) m_instancemodel.getColumn(index);
			if (isConstrained(cmd)) {
				results.add(cmd);
			}
		}
		return results;
	}

	/**
	 * @return the icon for the given constraint (i.e. >, <, ==, .. )
	 */
	public static ImageIcon getConstraintIcon(String op) {
		if (InstanceView.EQUAL.equals(op))
			return TSGuiToolbox.loadImage("update_equal16.gif");
		else if (InstanceView.NOTEQUAL.equals(op))
			return TSGuiToolbox.loadImage("update_notequal16.gif");
		else if (InstanceView.LESSTHAN.equals(op))
			return TSGuiToolbox.loadImage("update_lessthan16.gif");
		else if (InstanceView.LESSTHANEQUAL.equals(op))
			return TSGuiToolbox.loadImage("update_lessthanequal16.gif");
		else if (InstanceView.GREATERTHAN.equals(op))
			return TSGuiToolbox.loadImage("update_greaterthan16.gif");
		else if (InstanceView.GREATERTHANEQUAL.equals(op))
			return TSGuiToolbox.loadImage("update_greaterthanequal16.gif");
		else if (InstanceView.LIKE.equals(op))
			return TSGuiToolbox.loadImage("update_like16.gif");
		else
			return TSGuiToolbox.loadImage("update_unselected16.gif");
	}

	/**
	 * This method returns a collection of QueryConstraint objects. There is one
	 * object per filtered column. The AND logical connective is automatically
	 * set here.
	 */
	public Collection getConstraints(SQLFormatter formatter) {
		LinkedList results = new LinkedList();
		boolean bfirst = true;
		Collection expressions = getConstraintExpressions(formatter);
		Iterator iter = expressions.iterator();
		while (iter.hasNext()) {
			Expression expr = (Expression) iter.next();
			QueryConstraint constraint = null;
			if (bfirst) {
				constraint = new QueryConstraint(null, new ConstraintNode(expr));
				bfirst = false;
			} else {
				constraint = new QueryConstraint(LogicalConnective.AND, new ConstraintNode(expr));
			}
			results.add(constraint);
		}
		return results;
	}

	/**
	 * @param cmd
	 *            the field metadata object ot get the constraint for
	 * @return the selected constraint for the given filter element
	 */
	public Operator getOperator(ColumnMetaData cmd) {
		String constraint = getConstraint(cmd);
		if (constraint == UNSELECTED)
			return null;
		else
			return Operator.fromString(constraint);
	}

	/**
	 * @return the list of all columns in this view as reportable objects
	 * 
	 */
	public Collection getReportables() {
		LinkedList results = new LinkedList();
		for (int index = 0; index < m_instancemodel.getColumnCount(); index++) {
			Reportable r = new Reportable(m_instancemodel.getColumn(index));
			results.add(r);
		}
		return results;
	}

	/**
	 * @param cmd
	 *            the field metadata object to get the SQL string for
	 * @return a query expression object for the value associated with the given
	 *         column
	 */
	public String getSQLString(ColumnMetaData cmd, SQLFormatter formatter) {
		FieldElement e = (FieldElement) m_fieldcontrols.get(cmd);
		return e.fieldcomponent.toSQLString(formatter);
	}

	/**
	 * @param the
	 *            field metadata object
	 * @return the field element that corresponds to the given field metadata
	 *         object
	 */
	public FieldElement getFieldElement(ColumnMetaData cmd) {
		return (FieldElement) m_fieldcontrols.get(cmd);
	}

	/**
	 * @return the field element that contains the given label
	 */
	public FieldElement getFieldElement(JLabel label) {
		Collection fes = m_fieldcontrols.values();
		Iterator iter = fes.iterator();
		while (iter.hasNext()) {
			FieldElement fe = (FieldElement) iter.next();
			if (fe.getLabel() == label)
				return fe;
		}
		return null;
	}

	/**
	 * @param the
	 *            field metadata object
	 * @return the field element that corresponds to the given field metadata
	 *         object
	 */
	public InstanceComponent getInstanceComponent(ColumnMetaData cmd) {
		FieldElement fe = getFieldElement(cmd);
		if (fe == null) {
			// System.out.println( "null field element for: " +
			// cmd.getQualifiedName() );
			// System.out.println( "dumping field elements" );
			/*
			 * Iterator iter = m_fieldcontrols.keySet().iterator(); while(
			 * iter.hasNext() ) { ColumnMetaData fcmd =
			 * (ColumnMetaData)iter.next(); boolean bequals = cmd.equals( fcmd
			 * ); System.out.println( "   column: " + fcmd.getQualifiedName() +
			 * "  equals = " + bequals + "  fieldelement = " +
			 * m_fieldcontrols.get(fcmd) );
			 * 
			 * }
			 */
			return null;
		} else
			return fe.fieldcomponent;
	}

	/**
	 * @return the model used by this view
	 */
	public InstanceModel getModel() {
		return m_instancemodel;
	}

	/**
	 * @return the preferred size for this view
	 */
	public Dimension getPreferredSize() {
		int colcount = m_instancemodel.getColumnCount();
		if (colcount > 0) {
			ColumnMetaData cmd = m_instancemodel.getColumn(colcount - 1);
			FieldElement e = (FieldElement) m_fieldcontrols.get(cmd);
			int width = InstanceView.this.getWidth();
			int height = 300;
			if (e != null) {
				height = e.label.getY() + 32;
				InstanceComponent comp = e.getInstanceComponent();
				if (comp != null)
					height += comp.getHeight();

				if (height < 300)
					height = 300;
			}

			return new Dimension(width, height);
		} else
			return new Dimension(400, 400);
	}

	/**
	 * @return the value for the given column metadata object. Null is returned
	 *         if an error occurs
	 */
	public Object getValue(ColumnMetaData cmd) {
		Object result = null;
		InstanceComponent ic = getInstanceComponent(cmd);

		if (ic != null)
			result = ic.getValue();

		return result;
	}

	/**
	 * Creates and initializes the controls on this frame
	 */
	public void initialize() {
		m_constraintPopup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ConstraintButton constraintbutton = (ConstraintButton) m_constraintPopup.getSourceObject();
				constraintbutton.setIcon(m_constraintPopup.getIcon(e.getActionCommand()));
				constraintbutton.setConstraint(e.getActionCommand());
				m_constraintPopup.setVisible(false);
				ActionEvent evt = new ActionEvent(constraintbutton.getColumnMetaData(), 0, ID_CONSTRAINT_ACTION);
				notifyListeners(evt);
			}
		});

		m_view = new InstanceComponentContainer();
		m_view.setLayout(new UpdateFrameLayoutManager());

		m_layered_pane = new JLayeredPane();
		m_layered_pane.add(m_view, new Integer(0));

		// JRootPane root_pane = new JRootPane();
		// root_pane.setLayeredPane( m_layered_pane );

		m_overlay = new OverlayPanel();
		m_layered_pane.add(m_overlay, new Integer(1));

		// root_pane.setGlassPane( m_overlay );
		// m_overlay.setVisible(true);

		setLayout(new BorderLayout());
		// add( root_pane, BorderLayout.CENTER );
		add(m_layered_pane, BorderLayout.CENTER);

		reset();
	}

	/**
	 * @param cmd
	 *            the field metadata object to test if it is constrained or not
	 * @return if the constraint button has a valid constraint assigned for the
	 *         given field metadata
	 */
	public boolean isConstrained(ColumnMetaData cmd) {
		FieldElement e = (FieldElement) m_fieldcontrols.get(cmd);
		ConstraintButton btn = (ConstraintButton) e.filterbtn;
		return (btn.getConstraint() != null && !btn.getConstraint().equals(UNSELECTED));
	}

	/**
	 * @return true if the view has any constraint buttons selected
	 */
	boolean isViewConstrained() {
		int count = m_instancemodel.getColumnCount();
		for (int index = 0; index < count; index++) {
			ColumnMetaData cmd = m_instancemodel.getColumn(index);
			String constraint = getConstraint(cmd);
			if (constraint != null && !constraint.equals(InstanceView.UNSELECTED))
				return true;
		}
		return false;
	}

	/**
	 * Sends the action event to all listeners who are interested in receiving
	 * events from this view. Currently, we are only sending out constraint
	 * button events.
	 * 
	 * @param evt
	 *            the action event to send
	 */
	public void notifyListeners(ActionEvent evt) {
		Iterator iter = m_listeners.iterator();
		while (iter.hasNext()) {
			ActionListener listener = (ActionListener) iter.next();
			listener.actionPerformed(evt);
		}
	}

	/**
	 * This method takes a value entered by the user and converts it to SQL
	 * form. For example, if the field type is a VARCHAR, then we automatically
	 * put quotes around the value. If the field type is a date, then we put
	 * to_date around the value.
	 * 
	 * @param cmd
	 *            the field meta data object that the value corresponds to
	 * @param value
	 *            the value to prepare for SQL
	 * @return the value converted to a form usable by SQL
	 */
	private String prepareValue(ColumnMetaData cmd, String value) {
		if (DbUtils.isAlpha(cmd)) {
			StringBuffer buff = new StringBuffer();
			buff.append('\'');
			for (int index = 0; index < value.length(); index++) {
				char c = value.charAt(index);
				if (c == '\'') {
					buff.append("\'\'");
				} else
					buff.append(c);
			}
			buff.append('\'');

			return buff.toString();
		} else
			return value;
	}

	/**
	 * Resets all the controls on the view
	 */
	public void reset() {
		repaint();

		m_view.removeAll();
		m_overlay.removeAll();
		m_fieldcontrols.clear();

		m_view.setSize(1200, 2400);
		if (m_overlay != null) {
			m_overlay.setSize(1200, 2400);
		}
		InstanceMetaData imd = m_instancemodel.getMetaData();
		int colcount = m_instancemodel.getColumnCount();
		int y = 10;
		for (int index = 0; index < colcount; index++) {
			ColumnMetaData cmd = m_instancemodel.getColumn(index);

			FieldElement e = new FieldElement(cmd, m_instancemodel.getColumnSettings(cmd), imd.isLink(cmd));
			JLabel label = e.getLabel();

			label.setLocation(TEXTFIELDX, y);
			e.filterbtn = new ConstraintButton(m_noconstrainticon, cmd);
			e.filterbtn.setFocusPainted(false);
			e.filterbtn.setBorderPainted(false);
			e.filterbtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JButton btn = (JButton) e.getSource();
					showPopup(btn, btn.getX(), btn.getY());
				}
			});
			e.filterbtn.setFocusable(false);

			decorate(e);

			InstanceComponent icomp = createFieldComponent(cmd);
			e.setColumnComponent(icomp);

			m_fieldcontrols.put(cmd, e);

			if (e.getIconButton() != null)
				m_view.add(e.getIconButton());

			// m_view.add( e.getLabel() );
			m_overlay.add(e.getLabel());

			m_view.add(e.getInstanceComponent());
			m_view.add(e.getFilterButton());

			y += 20;
		}

		revalidate();
		doLayout();

	}

	/**
	 * @param cmd
	 *            the field metadata object to test if it is constrained or not
	 * @param constraint
	 *            the constraint to set
	 * @return if the constraint button has a valid constraint assigned for the
	 *         given field metadata
	 */
	public void setConstraint(ColumnMetaData cmd, String constraint) {
		FieldElement e = (FieldElement) m_fieldcontrols.get(cmd);
		if (e != null) {
			ConstraintButton cbtn = (ConstraintButton) e.filterbtn;
			if (cbtn != null) {
				cbtn.setConstraint(constraint);
				cbtn.setIcon(m_constraintPopup.getIcon(constraint));
			}
		}
	}

	/**
	 * sets the value for the given column metadata object.
	 */
	public void setValue(ColumnMetaData cmd, Object obj) {
		Object result = null;
		InstanceComponent ic = getInstanceComponent(cmd);
		if (ic != null)
			ic.setValue(obj);
	}

	/**
	 * Displays the browser popup
	 */
	public void showBrowserLinkPopup(Collection browser_targets, int x, int y) {
		// m_constraintPopup.setPreferredSize( d );
		// m_constraintPopup.setMaximumSize( d );
		// m_constraintPopup.setSize(d);
		// m_constraintPopup.setSourceObject( sourceBtn );
		m_browserPopup.addBrowserTargets(browser_targets);
		m_browserPopup.show(this, x, y);
	}

	/**
	 * Shows the constraint popup
	 */
	public void showPopup(JButton sourceBtn, int x, int y) {
		Dimension d = new Dimension(37, 175);
		m_constraintPopup.setPreferredSize(d);
		m_constraintPopup.setMaximumSize(d);
		m_constraintPopup.setSize(d);
		m_constraintPopup.setSourceObject(sourceBtn);
		m_constraintPopup.show(this, x - 8, y);
	}

	/**
	 * Updates the view with the lastest instance data
	 */
	public void updateView() throws SQLException {
		InstanceProxy instanceproxy = m_instancemodel.getInstanceProxy();
		Iterator iter = m_instancemodel.getColumns().iterator();
		while (iter.hasNext()) {
			ColumnMetaData cmd = (ColumnMetaData) iter.next();
			FieldElement e = (FieldElement) getFieldElement(cmd);

			InstanceComponent comp = e.getInstanceComponent();
			if (instanceproxy != null && !instanceproxy.isEmpty())
				instanceproxy.setValue(comp, cmd);

			e.fieldcomponent.setModified(false);
		}
	}

	/**
	 * This is the button used to constraint any fields for queries
	 */
	class ConstraintButton extends JButton {
		private String m_constraint; // the query constraint that is set by this
										// button
		private ColumnMetaData m_cmd; // the column that this button is
										// assoicated with

		ConstraintButton(ImageIcon icon, ColumnMetaData cmd) {
			super(icon);
			m_cmd = cmd;
			setOpaque(false);
			setContentAreaFilled(false);
		}

		public void setConstraint(String constraint) {
			m_constraint = constraint;
		}

		public String getConstraint() {
			return m_constraint;
		}

		public ColumnMetaData getColumnMetaData() {
			return m_cmd;
		}

	}

	/**
	 * This is the layout manager for the view. It handles sizing the fields and
	 * setting their positions
	 * 
	 */
	class UpdateFrameLayoutManager implements LayoutManager {
		public void addLayoutComponent(String name, Component comp) {
		}

		public void layoutContainer(Container parent) {
			int count = parent.getComponentCount();
			for (int index = 0; index < count; index++) {
				Component c = parent.getComponent(index);
				Dimension d = c.getPreferredSize();
				c.setSize(d);
			}

			count = m_overlay.getComponentCount();
			for (int index = 0; index < count; index++) {
				Component c = m_overlay.getComponent(index);
				Dimension d = c.getPreferredSize();
				c.setSize(d);
			}

			String maxlabel = "MMMMMMMMMMMMMMMM";

			// now calculate the longest field length
			// we truncate those fields with very long names so the controls
			// aren't too
			// spread out on the form
			m_maxlabelLength = 0;
			Iterator iter = m_fieldcontrols.keySet().iterator();
			while (iter.hasNext()) {

				ColumnMetaData cmd = (ColumnMetaData) iter.next();
				FieldElement e = (FieldElement) m_fieldcontrols.get(cmd);
				String txt = e.getLabelText();
				if (txt != null) {
					if (m_maxlabelLength < txt.length()) {
						m_maxlabelLength = txt.length();
						maxlabel = txt;
					}
				}
			}

			if (m_maxlabelLength < 8)
				m_maxlabelLength += 1;

			if (m_maxlabelLength > 20)
				m_maxlabelLength = 20;

			int y = 10;

			int colcount = m_instancemodel.getColumnCount();
			for (int index = 0; index < colcount; index++) {
				ColumnMetaData cmd = m_instancemodel.getColumn(index);
				FieldElement e = (FieldElement) m_fieldcontrols.get(cmd);
				if (e == null) {
					assert (false);
				}
				FontMetrics metrics = e.getLabel().getFontMetrics(e.getLabel().getFont());

				String labeltxt = e.getLabelText();
				if (labeltxt.length() > m_maxlabelLength) {
					labeltxt = labeltxt.substring(0, m_maxlabelLength - 3) + "...";
					e.setLabelText(labeltxt);
				}

				// ColumnSettings info = m_instancemodel.getColumnSettings(
				// cmd.getColumnName() );

				// center all components for a given field along a horizontal
				// axis
				int row_height = metrics.getHeight() * 3 / 2;
				int max_comp_height = getMaxComponentHeight(e);

				if (row_height < max_comp_height)
					row_height = max_comp_height;

				if (maxlabel.length() > m_maxlabelLength) {
					maxlabel = maxlabel.substring(0, m_maxlabelLength);
				}

				// System.out.println( "label width " + e.label.getWidth() +
				// "  stringwidth = " + metrics.stringWidth(maxlabel) );
				int size = e.cmd.getColumnSize();
				int xloc = TEXTFIELDX + metrics.stringWidth(maxlabel) + 16 + e.label.getIconTextGap() + 15;
				int rowwidth = getWidth();
				if (getParent() != null)
					rowwidth = getParent().getWidth() - xloc - 15;
				int fieldwidth = rowwidth;

				int min_width = metrics.stringWidth(I18N.getLocalizedMessage("-null-"));
				if (fieldwidth < min_width)
					fieldwidth = min_width;

				int comp_y = y + getVerticalOffset(e.fieldcomponent, row_height);
				e.fieldcomponent.setLocation(xloc, comp_y);
				e.label.setLocation(TEXTFIELDX, comp_y);

				javax.swing.AbstractButton iconbtn = e.getIconButton();
				if (iconbtn != null) {
					int iheight = iconbtn.getHeight();
					iconbtn.setLocation(4, (e.label.getHeight() - iheight) / 2 + comp_y);
				}

				e.filterbtn.setLocation(xloc - 20, comp_y);
				e.filterbtn.setSize(16, 16);

				// we set the width for those components that want to expand to
				// the full size of the container
				e.fieldcomponent.setWidth(fieldwidth);
				Dimension d = e.fieldcomponent.getPreferredSize();

				y += row_height;

				e.fieldcomponent.setSize(d);
				if (e.fieldcomponent instanceof InstanceUnknownComponent) {
					e.filterbtn.setVisible(false);
				}
			}

		}

		public int getMaxComponentHeight(FieldElement e) {
			int maxheight = e.label.getHeight();
			if (e.getIconButton() != null) {
				if (maxheight < e.getIconButton().getHeight())
					maxheight = e.getIconButton().getHeight();
			}

			if (maxheight < e.fieldcomponent.getHeight())
				maxheight = e.fieldcomponent.getHeight();

			if (maxheight < e.filterbtn.getHeight())
				maxheight = e.filterbtn.getHeight();

			return maxheight;
		}

		public int getVerticalOffset(JComponent comp, int maxHeight) {
			int result = (maxHeight - comp.getHeight()) / 2;
			return result;
		}

		public Dimension minimumLayoutSize(Container parent) {
			return new Dimension(100, 100);
		}

		public Dimension preferredLayoutSize(Container parent) {
			return new Dimension(800, 1200);
		}

		public void removeLayoutComponent(Component comp) {
		}

	}

	public class InstanceComponentContainer extends TSPanel {
		public void processMouseMotionEvent(MouseEvent evt) {
			super.processMouseMotionEvent(evt);
		}
	}

	private class OverlayPanel extends javax.swing.JComponent {
		private ImageIcon m_image;

		public OverlayPanel() {
			m_image = TSGuiToolbox.loadImage("null_icon.gif");
		}

		public void paint(Graphics g) {
			super.paint(g);
			java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;

			int colcount = m_instancemodel.getColumnCount();
			for (int index = 0; index < colcount; index++) {
				ColumnMetaData cmd = m_instancemodel.getColumn(index);
				FieldElement e = (FieldElement) m_fieldcontrols.get(cmd);
				InstanceComponent i_comp = e.fieldcomponent;
				JComponent data_comp = i_comp.getComponent();
				if (i_comp.isNull() && !(data_comp instanceof javax.swing.JToggleButton)) {
					Rectangle rect = data_comp.getBounds();
					int x = rect.width - 4 - m_image.getIconWidth();
					/**
					 * for date/time/timestamp, we need to handle spinner
					 * buttons
					 */
					if (data_comp instanceof javax.swing.JSpinner) {
						javax.swing.JSpinner sp = (javax.swing.JSpinner) data_comp;
						JComponent e_comp = sp.getEditor();
						Rectangle e_rect = e_comp.getBounds();
						x = e_rect.width - 4 - m_image.getIconWidth();
					}
					Point pt = javax.swing.SwingUtilities.convertPoint(data_comp, x, 4, InstanceView.this);
					g2.drawImage(m_image.getImage(), pt.x, pt.y, this);
				}
			}
		}
	}

}
