package com.jeta.abeille.gui.model;

/**
 * This is the controller for the ModelView window. This controller is for those
 * ModelViews that allow a read-only view of the model (e.g. FormBuilder and
 * QueryBuilder).
 * 
 * @author Jeff Tassin
 */
public class TableViewController extends ModelViewController {
	/**
	 * ctor
	 */
	public TableViewController(ModelView view, ModelerModel modeler) {
		super(view, modeler);
	}

	/**
	 * Initializes the drop listener for this controller
	 */
	protected ModelViewDropListener createDropListener() {
		// set up the drop listener for the view
		if (getDropListener() == null) {
			return new TableViewDropListener(getModelView());
		} else
			return getDropListener();
	}

}
