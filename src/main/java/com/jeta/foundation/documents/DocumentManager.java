/* 
 * Copyright 2004 JETA Software, Inc. All rights reserved.
 * JETA SOFTWARE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.jeta.foundation.documents;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Manages documents that can be shared among frames
 * 
 * @author Jeff Tassin
 */
public class DocumentManager {
	public static final String COMPONENT_ID = "document.manager";

	private HashMap m_documents = new HashMap();

	public Serializable cloneDocument(Serializable document) throws Exception {
		class MyByteArrayOutputStream extends ByteArrayOutputStream {
			public byte[] getBytes() {
				return buf;
			}
		}
		;
		MyByteArrayOutputStream bos = new MyByteArrayOutputStream();

		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(document);
		oos.close();

		ByteArrayInputStream bis = new ByteArrayInputStream(bos.getBytes());
		ObjectInputStream ois = new ObjectInputStream(bis);
		Serializable result = (Serializable) ois.readObject();
		ois.close();
		return result;
	}

	/**
	 * Returns the current owner of the document
	 */
	public DocumentOwner getCurrentOwner(File file) {
		DocumentInfo docinfo = getDocumentInfo(file);
		if (docinfo != null)
			return docinfo.getCurrentOwner();
		else
			return null;
	}

	private DocumentInfo getDocumentInfo(File file) {
		if (file == null)
			return null;
		else
			return (DocumentInfo) m_documents.get(file);
	}

	public boolean isDocumentRegistered(File file) {
		return (getDocumentInfo(file) != null);
	}

	public boolean isDocumentModified(File file) {
		DocumentInfo docinfo = getDocumentInfo(file);
		if (docinfo != null) {
			DocumentOwner doc_owner = docinfo.getCurrentOwner();
			return (doc_owner.isDocumentModified() || docinfo.isDocumentModified());
		}
		return false;
	}

	/**
	 * Registers the document and makes the specified owner the current owner
	 */
	public void registerDocument(File file, DocumentOwner owner) {
		DocumentInfo docinfo = getDocumentInfo(file);
		if (docinfo == null) {
			docinfo = new DocumentInfo(file, owner);
			m_documents.put(file, docinfo);
		} else {
			docinfo.addReference(owner);
		}
		setCurrentOwner(file, owner);
	}

	/**
	 * Used when shutting down. Removes all document references.
	 */
	public void removeAllReferences(File file) {
		m_documents.remove(file);
	}

	public void removeReference(File file, DocumentOwner owner) {
		DocumentInfo docinfo = getDocumentInfo(file);
		if (docinfo != null) {
			docinfo.removeReference(owner);
		}
	}

	public void setCurrentOwner(File file, DocumentOwner owner) {
		DocumentInfo docinfo = getDocumentInfo(file);
		if (docinfo != null)
			docinfo.setCurrentOwner(owner);
	}

	public void setDocumentModified(File file, boolean modified) {
		DocumentInfo docinfo = getDocumentInfo(file);
		if (docinfo != null) {
			docinfo.setDocumentModified(modified);
		}
	}

	private static class DocumentInfo {
		private File m_file;
		private LinkedList m_owners = new LinkedList();
		private boolean m_modified = false;

		DocumentInfo(File file, DocumentOwner owner) {
			m_file = file;
			m_owners.add(owner);
		}

		void addReference(DocumentOwner owner) {
			Iterator iter = m_owners.iterator();
			while (iter.hasNext()) {
				DocumentOwner regowner = (DocumentOwner) iter.next();
				if (owner == regowner)
					return;
			}
			m_owners.add(owner);
		}

		void removeReference(DocumentOwner owner) {
			Iterator iter = m_owners.iterator();
			while (iter.hasNext()) {
				DocumentOwner regowner = (DocumentOwner) iter.next();
				if (owner == regowner)
					iter.remove();
			}
		}

		DocumentOwner getCurrentOwner() {
			if (m_owners.size() > 0)
				return (DocumentOwner) m_owners.getFirst();
			else
				return null;
		}

		boolean isDocumentModified() {
			return m_modified;
		}

		void setCurrentOwner(DocumentOwner owner) {
			removeReference(owner);
			m_owners.addFirst(owner);
		}

		void setDocumentModified(boolean modified) {
			m_modified = modified;
			Iterator iter = m_owners.iterator();
			while (iter.hasNext()) {
				DocumentOwner regowner = (DocumentOwner) iter.next();
				regowner.setDocumentModified(modified);
			}
		}
	}
}
