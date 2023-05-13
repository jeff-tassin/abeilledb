package com.jeta.abeille.gui.downloader;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.jeta.foundation.gui.components.JETAComponentCleanser;
import com.jeta.foundation.gui.components.TSDialog;
import com.jeta.foundation.gui.components.TSUsernameDialog;

import com.jeta.foundation.gui.utils.TSGuiToolbox;
import com.jeta.foundation.i18n.I18N;
import com.jeta.foundation.utils.TSUtils;

/**
 * Displays the download status view
 * 
 * @author Jeff Tassin
 */
public class DownloaderDialog extends TSDialog implements ActionListener {
	/** the main view for the dialog */
	private DownloaderView m_view;

	/** the main url */
	private String m_url;
	/** the main destiation directory */
	private String m_dest_dir;

	/**
	 * Downloading a resource requires 2 steps. First, we contact
	 * www.jetaware.com to get the download descriptor file for a given
	 * resource. This xml file tells the application where the actual resource
	 * is on the net
	 */
	// private AbstractDownloader m_jetadownloader;

	private TaskGroup m_downloadaction;

	private boolean m_canceled = false;

	private JButton m_settingsbtn;

	private SettingsView m_settingsview;

	private String m_result;

	public DownloaderDialog(java.awt.Dialog parent, boolean bmodal) {
		super(parent, bmodal);
		createView();
	}

	public DownloaderDialog(java.awt.Frame parent, boolean bmodal) {
		super(parent, bmodal);
		createView();
	}

	/**
	 * DownloaderListener implementation
	 */
	public void actionPerformed(ActionEvent aevt) {
		if (!isCanceled()) {
			if (aevt instanceof DownloadEvent) {
				DownloadEvent evt = (DownloadEvent) aevt;
				// System.out.println( "DownloaderDialog.downloadEvent..." );
				// evt.print();
				if (evt.getID() == DownloadEvent.COMPLETED && evt.getSource() == m_downloadaction) {
					cmdOk();
				} else if (evt.getID() == DownloadEvent.STATUS) {
					m_view.updateStatus(evt.getMessage(), evt.getCurrentTotal(), evt.getContentLength());
				} else if (evt.getID() == DownloadEvent.ERROR) {
					String msg = I18N.getLocalizedMessage("jdbc_download_error");
					String title = I18N.getLocalizedMessage("Error");
					JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
					TSUtils.printException(evt.getException());
					cmdCancel();
				}
			}
		}
	}

	public boolean isCanceled() {
		return m_canceled;
	}

	/**
	 * Stops the current download
	 */
	public void cmdCancel() {
		m_canceled = true;
		if (m_downloadaction != null)
			m_downloadaction.cancel();

		super.cmdCancel();
	}

	/**
	 * Creates the components for the dialog
	 */
	private final void createView() {
		m_view = new DownloaderView();
		m_settingsview = new SettingsView();

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(m_view, BorderLayout.NORTH);
		panel.add(m_settingsview, BorderLayout.CENTER);
		setPrimaryPanel(panel);

		getCloseButton().setText(I18N.getLocalizedMessage("Cancel"));
		// getCloseButton().setEnabled( false );
		setTitle(I18N.getLocalizedMessage("Download"));
		m_view.updateStatus(I18N.getLocalizedMessage("Ready"), 0, 0);

		JButton okbtn = getOkButton();
		okbtn.setText(I18N.getLocalizedMessage("Start"));
		JETAComponentCleanser cleanser = new JETAComponentCleanser();
		cleanser.removeJETAListeners(okbtn, java.awt.event.ActionListener.class, "removeActionListener");
		okbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				m_settingsview.save();
				m_settingsview.setEnabled(false);
				getOkButton().setEnabled(false);
				// m_settingsbtn.setEnabled( false );
				getCloseButton().setEnabled(true);
				setTitle(I18N.getLocalizedMessage("Download"));
				m_view.updateStatus(I18N.getLocalizedMessage("Connecting"), 0, 0);
				start(m_url, m_dest_dir);
			}
		});

		Authenticator.setDefault(new JETAAuthenticator());
	}

	public String getResult() {
		return m_result;
	}

	public void setUrl(String url, String dest_dir) {
		m_url = url;
		m_dest_dir = dest_dir;
	}

	/**
	 * Initializes and starts the download. You must call showCenter/show
	 * immediately after this call
	 */
	public void start(String download_url, String dest_dir) {
		m_downloadaction = new TaskGroup(this);
		m_downloadaction.add(new BufferedDownloader(download_url, "jdbc.descriptor", null));
		m_downloadaction.add(new TaskProxy("jdbc.descriptor", this));

		final String output_dir = dest_dir;
		Runnable action_runner = new Runnable() {
			public void run() {
				try {
					// sleep for half a second to allow main dialog to display
					// properly
					Thread.currentThread().sleep(1000);
					HashMap props = new HashMap();

					// props.put( "save_to_dir", "/home/jeff/tmp/test" );
					props.put("save_to_dir", output_dir);
					m_downloadaction.invoke(props);
					m_result = (String) props.get("final_result");
					m_downloadaction.updateListeners(new DownloadEvent(m_downloadaction, DownloadEvent.COMPLETED));
				} catch (Exception e) {
					m_downloadaction.updateListeners(new DownloadEvent(m_downloadaction, e));
				}
			}
		};

		Thread t = new Thread(action_runner);
		t.setDaemon(true);
		t.start();

	}

	/**
	 * Authenticator class for those proxy systems that need a username/password
	 */
	public class JETAAuthenticator extends Authenticator {

		private PasswordAuthentication m_result;
		private Object m_lock = new Object();

		protected PasswordAuthentication getPasswordAuthentication() {
			m_result = null;
			Runnable gui_method = new Runnable() {
				public void run() {
					TSUsernameDialog dlg = (TSUsernameDialog) TSGuiToolbox.createDialog(TSUsernameDialog.class,
							(java.awt.Frame) null, true);
					dlg.setTitle(I18N.getLocalizedMessage("Proxy Authentication"));
					dlg.setSize(dlg.getPreferredSize());
					dlg.showCenter();
					if (dlg.isOk()) {
						m_result = new PasswordAuthentication(dlg.getUsername(), dlg.getPassword());
					} else {
						cmdCancel();
					}
				}
			};

			try {
				m_result = null;
				SwingUtilities.invokeAndWait(gui_method);
			} catch (Exception e) {
			}
			return m_result;
		}
	}

}
