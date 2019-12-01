package com.jeta.abeille.gui.model;

import java.util.Collection;

/**
 * Simple interface that allows the ModelViewFrameController and the
 * ModelViewController to share actions that act on views. We need this because
 * many shared actions act on a single view. The ModelViewFrameController
 * manages multiple views, so the interface would return the current view for
 * that case.
 * 
 * @author Jeff Tassin
 */
public interface ViewGetter {
	/**
	 * @return the current view (ModelView)
	 */
	public ModelView getModelView();

	/**
	 * @return the set of views (ModelView) if this getter is a frame with
	 *         multiple ModelView objects. For the single view case, the result
	 *         is simply a collection with one view.
	 */
	public Collection getViews();
}
