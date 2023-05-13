package com.jeta.abeille.gui.model.export;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.JScrollPane;

import com.jeta.abeille.gui.model.ModelView;
import com.jeta.abeille.gui.model.TableWidget;
import com.jeta.abeille.gui.model.ViewGetter;

import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSErrorDialog;
import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

import java.io.BufferedOutputStream;
import java.io.Writer;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.dom.GenericDOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;

/**
 * Action that saves a ModelView to a Windows Meta File
 * 
 * @author Jeff Tassin
 */
public class SaveAsImageAction implements ActionListener {
	private ViewGetter m_viewgetter;

	public SaveAsImageAction(ViewGetter viewgetter) {
		m_viewgetter = viewgetter;
	}

	public void actionPerformed(ActionEvent evt) {
		ModelView modelview = m_viewgetter.getModelView();
		if (modelview != null) {
			Collection widgets = modelview.getTableWidgets();
			if (widgets.size() == 0) {
				String title = I18N.getLocalizedMessage("Error");
				String msg = I18N.getLocalizedMessage("Nothing to save");
				javax.swing.JOptionPane.showMessageDialog(null, msg, title, javax.swing.JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		TSDialog dlg = (TSDialog) TSGuiToolbox.createDialog(TSDialog.class, modelview, true);
		SaveOptionsView view = new SaveOptionsView();
		dlg.setPrimaryPanel(view);
		TSGuiToolbox.setReasonableWindowSize(dlg, dlg.getPreferredSize());
		dlg.setTitle(I18N.getLocalizedMessage("Save Options"));
		dlg.addValidator(view, new SaveOptionsValidator());
		dlg.showCenter();
		if (dlg.isOk()) {
			try {
				view.saveSettings();
				saveImage(view);
			} catch (Exception e) {
				TSUtils.printException(e);
				TSErrorDialog edlg = TSErrorDialog.createDialog(e.getMessage());
				edlg.setSize(edlg.getPreferredSize());
				edlg.showCenter();
			}
		}
	}

	public void saveImage(SaveOptionsView options) throws Exception {
		ModelView modelview = m_viewgetter.getModelView();
		if (modelview != null) {
			modelview.deselectAll();

			Color old_background = modelview.getBackground();
			modelview.setDoubleBuffered(false);
			/**
			 * we need to turn off the scroll bars when repainting onto the SVG
			 * canvas. For some reason, turning off double-buffering causes
			 * exceptions to be thrown when repainting on SVGGraphics2D
			 */
			Collection widgets = modelview.getTableWidgets();
			Iterator iter = widgets.iterator();
			while (iter.hasNext()) {
				TableWidget tw = (TableWidget) iter.next();
				JScrollPane scroll = tw.getScrollPane();
				if (scroll != null) {
					scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
					scroll.revalidate();
					scroll.doLayout();
					scroll.repaint();
					tw.getJList().repaint();
				}
			}

			try {

				String path = options.getPath();
				TSUtils.printMessage("SaveAsImageAction saving image: " + path);
				/**
				 * get the minimum rectangle that encloses all
				 * widgets/components on the view Expand the rectangle so that
				 * we have a small amount of padding so the user has room to
				 * edit the image if needed
				 */
				Rectangle rect = modelview.getComponentBounds();
				rect.x -= 20;
				rect.y -= 20;
				if (rect.x < 0)
					rect.x = 0;
				if (rect.y < 0)
					rect.y = 0;

				rect.width += 40;
				rect.height += 40;

				if ((rect.x + rect.width) > modelview.getWidth())
					rect.width = modelview.getWidth() - rect.x;
				if ((rect.y + rect.height) > modelview.getHeight())
					rect.height = modelview.getHeight() - rect.y;

				// TSDialog dlg = (TSDialog)TSGuiToolbox.createDialog(
				// TSDialog.class, true );
				// com.jeta.foundation.gui.print.PagePreview preview = new
				// com.jeta.foundation.gui.print.PagePreview( rect.width,
				// rect.height, img );
				// dlg.setPrimaryPanel( preview );
				// dlg.setSize( new java.awt.Dimension(800,600) );
				// dlg.showCenter();

				if (options.isPNG() || options.isJPEG()) {
					Transcoder trans = null;
					if (options.isPNG())
						trans = new PNGTranscoder();
					else {
						trans = new JPEGTranscoder();
						trans.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(options.getQuality()));
					}

					BufferedImage img = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_RGB);
					Graphics2D g = (Graphics2D) img.getGraphics();
					g.setColor(Color.white);
					g.fillRect(0, 0, rect.width, rect.height);

					g.translate(-rect.x, -rect.y);
					g.setClip(rect.x, rect.y, rect.width, rect.height);
					modelview.repaint();

					modelview.setBackground(Color.white);
					modelview.paint(g);

					if (modelview.isEvaluation()) {
						modelview.paintEvaluationMessage(g, rect.x, rect.y, rect.width, rect.height);
					}

					// paint the buffer to the image
					OutputStream ostream = new BufferedOutputStream(new FileOutputStream(path));
					if (options.isPNG()) {
						((PNGTranscoder) trans).writeImage(img, new TranscoderOutput(ostream));
					} else {
						((JPEGTranscoder) trans).writeImage(img, new TranscoderOutput(ostream));
					}
					ostream.flush();
				} else {
					DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
					Document document = domImpl.createDocument(null, "svg", null);
					SVGGraphics2D g = new SVGGraphics2D(document);
					g.setSVGCanvasSize(new Dimension(rect.width, rect.height));
					g.translate(-rect.x, -rect.y);
					g.setClip(rect.x, rect.y, rect.width, rect.height);
					modelview.setBackground(Color.white);
					modelview.paint(g);
					if (modelview.isEvaluation()) {
						modelview.paintEvaluationMessage(g, rect.x, rect.y, rect.width, rect.height);
					}

					Writer out = new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
					g.stream(out, true);
					out.flush();
					out.close();
				}
			} finally {
				modelview.setBackground(old_background);
				modelview.setDoubleBuffered(true);
				iter = widgets.iterator();
				while (iter.hasNext()) {
					TableWidget tw = (TableWidget) iter.next();
					JScrollPane scroll = tw.getScrollPane();
					if (scroll != null) {
						scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
						scroll.revalidate();
						scroll.doLayout();
						scroll.repaint();
						tw.getJList().repaint();
					}
				}
			}
		}
	}

}
