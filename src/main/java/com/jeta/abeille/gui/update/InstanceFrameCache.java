package com.jeta.abeille.gui.update;

import java.lang.ref.WeakReference;

import java.util.HashMap;

import com.jeta.abeille.database.model.TSConnection;

import com.jeta.foundation.gui.components.TSInternalFrame;
import com.jeta.foundation.gui.components.TSWorkspaceFrame;

/**
 * This class provides support for launching InstanceFrames for the application.
 * This is a common function in the app, so we provide a special class for it.
 * This class caches a frame once it is launched. If the user launches the frame
 * again, we can first look in the cache to see if it is already visible. This
 * class uses WeakReferences for the frames. This allows the frames to be
 * disposed and properly garbage collected even though we are holding a
 * (weak)reference to the frame.
 * 
 * @author Jeff Tassin
 */
public class InstanceFrameCache {
	/**
	 * this is a map of TSConnection objects (keys) to InstanceFrameCache
	 * objects (values)
	 */
	private static HashMap m_caches = new HashMap();

	/**
	 * this is a hash map builder UID (keys) to WeakReference objects (which
	 * wrap launched InstanceFrames)
	 */
	private HashMap m_frames = new HashMap();

	/**
	 * ctor This ctor allows clients to create instance caches that are specific
	 * to that client (rather than the connection as a whole ). An example is
	 * the foreign key launched frame in an instance form. We typically do not
	 * share that view with any other views
	 */
	public InstanceFrameCache() {

	}

	/**
	 * Creates and launches an instance frame. The newly created frame instance
	 * is put in the cache. This allows clients to share instance frames for the
	 * connection.
	 */
	public InstanceFrame createFrame(TSConnection connection, InstanceViewBuilder builder,
			InstanceFrameLauncher launcher) {
		return createFrame(connection, builder, builder.getID(), launcher);
	}

	/**
	 * Creates and launches an instance frame. The newly created frame instance
	 * is put in the cache. This allows clients to share instance frames for the
	 * connection.
	 */
	public InstanceFrame createFrame(TSConnection connection, InstanceViewBuilder builder, String id,
			InstanceFrameLauncher launcher) {
		TSInternalFrame frame = ShowInstanceFrameAction.launchFrame(connection, builder, launcher);
		if (frame == null) {
			return null;
		} else {
			WeakReference wref = new WeakReference(frame);
			m_frames.put(id, wref);
			return (InstanceFrame) frame;
		}
	}

	public InstanceFrame getFrame(String id) {
		InstanceFrame result = null;
		WeakReference wref = (WeakReference) m_frames.get(id);
		if (wref != null) {
			result = (InstanceFrame) wref.get();
		}
		return result;
	}

	/**
	 * Creates an instance frame cache for the given connection
	 */
	public static InstanceFrameCache getInstance(TSConnection connection) {
		synchronized (InstanceFrameCache.class) {
			InstanceFrameCache cache = (InstanceFrameCache) m_caches.get(connection);
			if (cache == null) {
				cache = new InstanceFrameCache();
				m_caches.put(connection, cache);
			}
			return cache;
		}
	}

	/**
	 * Launches the frame
	 */
	public synchronized InstanceFrame launchFrame(TSConnection connection, InstanceViewBuilder builder) {
		return launchFrame(connection, builder, null);
	}

	/**
	 * Launches the frame
	 */
	public synchronized InstanceFrame launchFrame(TSConnection connection, InstanceViewBuilder builder,
			InstanceFrameLauncher launcher) {
		InstanceFrame iframe = null;
		WeakReference weakref = (WeakReference) m_frames.get(builder.getID());
		if (weakref != null) {
			iframe = (InstanceFrame) weakref.get();
			if (iframe != null && iframe.isVisible()) {
				TSWorkspaceFrame wsframe = TSWorkspaceFrame.getInstance();
				wsframe.show(iframe);
			} else {
				// System.out.println(
				// "InstanceFrameCache found garbage collected frame:  " +
				// builder.getID() );
				m_frames.remove(builder.getID());
				iframe = null;
			}
		}

		if (iframe == null)
			iframe = createFrame(connection, builder, launcher);

		return iframe;
	}
}
