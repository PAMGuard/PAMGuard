package tethys;

import PamUtils.PamUtils;

/**
 * Basic information about a document that can be used to 
 * make document lists. 
 * @author dg50
 *
 */
public class DocumentInfo implements Comparable<DocumentInfo> {

	private Collection collection;
	private String documentName;
	private String documentId;
	
	/**
	 * flag to indicate that the document is associated with 
	 * this Tethys Project 
	 */
	private boolean thisTethysProject;
	/**
	 * Flag to indicate that the document is associated with the current
	 * PAMGuard dataset
	 */
	private boolean thisPAMGuardDataSet;

	/**
	 * @param collection
	 * @param documentName
	 * @param documentId
	 */
	public DocumentInfo(Collection collection, String documentName, String documentId) {
		this.collection = collection;
		this.documentName = PamUtils.trimString(documentName);
		this.documentId = PamUtils.trimString(documentId);
	}
	@Override
	public int compareTo(DocumentInfo o) {
		return this.documentName.compareTo(o.documentName);
	}
	/**
	 * @return the collection
	 */
	public Collection getCollection() {
		return collection;
	}
	/**
	 * @return the documentName
	 */
	public String getDocumentName() {
		return documentName;
	}
	/**
	 * @return the documentId
	 */
	public String getDocumentId() {
		return documentId;
	}
	/**
	 * Document is part of this Tethys Project
	 * @return the thisTethysProject
	 */
	public boolean isThisTethysProject() {
		return thisTethysProject;
	}
	/**
	 * Document is part of this Tethys Project
	 * @param thisTethysProject the thisTethysProject to set
	 */
	public void setThisTethysProject(boolean thisTethysProject) {
		this.thisTethysProject = thisTethysProject;
	}
	/**
	 * Document is part of current PAMGuard dataset
	 * @return the thisPAMGuardDataSet
	 */
	public boolean isThisPAMGuardDataSet() {
		return thisPAMGuardDataSet;
	}
	/**
	 * Document is part of current PAMGuard dataset
	 * @param thisPAMGuardDataSet the thisPAMGuardDataSet to set
	 */
	public void setThisPAMGuardDataSet(boolean thisPAMGuardDataSet) {
		this.thisPAMGuardDataSet = thisPAMGuardDataSet;
	}
	@Override
	public String toString() {
		return String.format("%s : %s", getDocumentName(), getDocumentId());
	}
	
	
}
