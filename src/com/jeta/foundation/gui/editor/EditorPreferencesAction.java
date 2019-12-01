/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.gui.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Collection;
import java.util.Iterator;

import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;

import com.jeta.foundation.gui.editor.options.EditorOptionsModel;
import com.jeta.foundation.gui.editor.options.EditorOptionsView;
import com.jeta.foundation.gui.editor.options.EditorOptionsController;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.open.gui.framework.JETAContainer;

import com.jeta.foundation.i18n.I18N;

/**
 * Launches the Editor Preferences dialog box. This includes the key bindings
 * and macros editors.
 * 
 * @author Jeff Tassin
 */
public class EditorPreferencesAction implements ActionListener {

	/** the owning frame */
	private JETAContainer m_frame;

	/** the buffer mgr */
	private BufferMgr m_buffermgr;

	private EditorOptionsView m_view = null;

	/**
	 * ctor
	 */
	public EditorPreferencesAction(JETAContainer window, BufferMgr buffmgr) {
		m_frame = window;
		m_buffermgr = buffmgr;
	}

	/**
	 * Creates the view
	 */
	protected EditorOptionsView createView() {
		Class kitclass = TSPlainKit.class;
		Buffer buff = m_buffermgr.getCurrentBuffer();
		if (buff != null)
			kitclass = buff.getEditor().getEditorKit().getClass();

		KitSet kset = new KitSet(kitclass, FrameKit.class);

		EditorOptionsModel optionsmodel = new EditorOptionsModel(kset);
		EditorOptionsView view = new EditorOptionsView(optionsmodel);
		EditorOptionsController controller = new EditorOptionsController(view);
		view.setController(controller);
		return view;
	}

	public EditorOptionsView getView() {
		if (m_view == null) {
			m_view = createView();
		}
		return m_view;
	}

	/**
	 * ActionListener implementation
	 */
	public void actionPerformed(ActionEvent evt) {
		if (invokeDialog()) {
			save();
		}
	}

	protected boolean invokeDialog() {
		TSDialog propsdlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, TSWorkspaceFrame.getInstance(), true);
		propsdlg.setTitle(I18N.getLocalizedMessage("Preferences"));

		propsdlg.setPrimaryPanel(getView());
		propsdlg.setSize(propsdlg.getPreferredSize());
		propsdlg.showCenter();
		return propsdlg.isOk();
	}

	public void save() {
		save(getView());
	}

	public void save(EditorOptionsView view) {
		if (view == null) {
			assert (false);
			return;
		}

		Buffer buff = m_buffermgr.getCurrentBuffer();
		// save the view settings to the options model

		view.saveToModel();

		// @todo tell TSKit to update any settings
		// e.g. line numbers set to visible

		// save the options model to the persistent store
		view.getModel().save();

		String newbindings = view.getModel().getActiveBindings();

		KeyBindingMgr.setActiveEditor(newbindings);
		// now set the keybindings in all the buffers
		Collection buffers = m_buffermgr.getBuffers();
		Iterator iter = buffers.iterator();
		while (iter.hasNext()) {
			Buffer buffer = (Buffer) iter.next();

			Class kitclass = buff.getEditor().getEditorKit().getClass();
			KitSet kset = new KitSet(kitclass, FrameKit.class);
			KeyBindingMgr.setKeyBindings(buffer.getEditor(), kset);
		}
	}

}
