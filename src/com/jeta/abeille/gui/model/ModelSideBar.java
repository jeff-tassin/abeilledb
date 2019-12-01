package com.jeta.abeille.gui.model;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

import java.lang.ref.WeakReference;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.jeta.abeille.gui.model.overview.CanvasOverview;

import com.jeta.foundation.gui.components.TSButtonBar;
import com.jeta.foundation.gui.components.TSPanel;
import com.jeta.foundation.gui.split.CustomSplitPane;

import com.jeta.foundation.gui.utils.TSGuiToolbox;

import com.jeta.foundation.i18n.I18N;

import com.jeta.forms.components.image.ImageComponent;
import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.form.FormAccessor;

/**
 * The side panel is displayed to the of the ModelViews in the ModelViewFrame.
 * It displays the ModelerTreeView as well as the CanvasOverview.
 * 
 * @author Jeff Tassin
 */
public class ModelSideBar extends TSPanel {
	/* reference to the modeler frame window */
	private WeakReference m_frameref;

	/** the button bar in the top view */
	private TSButtonBar m_buttonbar;

	/** the view of the ModelerModel tree */
	private ModelerView m_modelerview;

	private GlobalLinksView m_links_view;

	/** the overview window */
	private CanvasOverview m_overview;

	private CustomSplitPane m_splitpane;

	private ModelerModel m_modeler;

	public ModelSideBar(ModelViewFrame mframe, ModelerModel modeler) {
		m_frameref = new WeakReference(mframe);
		m_modeler = modeler;
		initialize();
	}

	void dispose() {
		if (m_modelerview != null)
			m_modelerview.dispose();

		if (m_links_view != null)
			m_links_view.dispose();
	}

	ModelViewFrame getFrame() {
		return (ModelViewFrame) m_frameref.get();
	}

	public CanvasOverview getOverview() {
		return m_overview;
	}

	/**
	 * Initializes the view
	 */
	private void initialize() {
		m_modelerview = new ModelerView(m_modeler);

		m_buttonbar = new TSButtonBar();
		m_buttonbar.addView(I18N.getLocalizedMessage("Prototypes"), m_modelerview,
				TSGuiToolbox.loadImage("incors/16x16/table_sql_create.png"));

		m_links_view = new GlobalLinksView(m_modeler);
		m_buttonbar.addView(I18N.getLocalizedMessage("User Links"), m_links_view, TSGuiToolbox.loadImage("link16.gif"));
		m_buttonbar.setMinimumSize(new Dimension(50, 50));
		m_buttonbar.updateView();

		m_splitpane = new CustomSplitPane(JSplitPane.VERTICAL_SPLIT);
		m_splitpane.setDividerLocation(0.75f);
		m_splitpane.setResizeWeight(1.);

		m_overview = new CanvasOverview(getFrame());
		m_overview.setBorder(BorderFactory.createLineBorder(Color.black));

		JPanel overviewpanel = new JPanel();
		overviewpanel.add(m_overview);
		overviewpanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.gray),
				BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		overviewpanel.setLayout(new OverviewLayout());
		overviewpanel.setOpaque(true);
		overviewpanel.setBackground(Color.white);

		m_splitpane.add(m_buttonbar);
		m_splitpane.add(overviewpanel);

		setLayout(new BorderLayout());

		FormPanel panel = new FormPanel("com/jeta/abeille/gui/model/modelerview.jfrm");
		FormAccessor faccessor = panel.getFormAccessor("main.form");
		faccessor.replaceBean("modeler.view", m_splitpane);
		m_buttonbar.setBorder(BorderFactory.createLineBorder(Color.gray));
		add(panel, BorderLayout.CENTER);
		ImageComponent close_btn = (ImageComponent) panel.getComponentByName("close.window");
		close_btn.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				getFrame().showPrototypes(false);
			}
		});
	}

	/**
	 * @return the modelermodel tree view
	 */
	public ModelerView getModelerView() {
		return m_modelerview;
	}

	public class OverviewLayout implements LayoutManager {
		public void addLayoutComponent(String name, Component comp) {
		}

		public void layoutContainer(Container parent) {
			ModelViewFrame mframe = getFrame();
			if (mframe != null) {
				Dimension d = mframe.getCanvasSize();

				Insets ins = parent.getInsets();
				int width = parent.getWidth() - (ins.left + ins.right);
				int height = parent.getHeight() - (ins.top + ins.bottom);

				double scale_x = (double) width / d.getWidth();
				double scale_y = (double) height / d.getHeight();

				double o_width;
				double o_height;

				if (scale_x <= scale_y) {
					o_width = width;
					o_height = o_width * d.getHeight() / d.getWidth();
				} else {
					o_height = height;
					o_width = o_height * d.getWidth() / d.getHeight();
				}

				int x = (width - (int) o_width) / 2 + ins.left;
				int y = (height - (int) o_height) / 2 + ins.top;
				m_overview.setBounds(x, y, (int) o_width, (int) o_height);
			}
		}

		public Dimension minimumLayoutSize(Container parent) {
			return new Dimension(100, 100);
		}

		public Dimension preferredLayoutSize(Container parent) {
			return new Dimension(100, 100);
		}

		public void removeLayoutComponent(Component comp) {
		}
	}

}
