package printscreen;

import java.io.Serializable;

import javax.imageio.ImageIO;

import PamController.PamFolders;
import annotation.handler.AnnotationChoices;

public class PrintScreenParameters implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;

	private String destFolder;
	
	public boolean datedSubFolders = true;
	
	public String imageType = ImageIO.getWriterFileSuffixes()[0];
	
	private AnnotationChoices annotationChoices;

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected PrintScreenParameters clone() {
		try {
			return (PrintScreenParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @return the annotationChoices
	 */
	public AnnotationChoices getAnnotationChoices() {
		if (annotationChoices == null) {
			annotationChoices = new AnnotationChoices();
		}
		return annotationChoices;
	}

	/**
	 * @param annotationChoices the annotationChoices to set
	 */
	public void setAnnotationChoices(AnnotationChoices annotationChoices) {
		this.annotationChoices = annotationChoices;
	}

	/**
	 * @return the destFolder
	 */
	public String getDestFolder() {
		if (destFolder == null) {
			destFolder = PamFolders.getDefaultProjectFolder();
		}
		return destFolder;
	}

	/**
	 * @param destFolder the destFolder to set
	 */
	public void setDestFolder(String destFolder) {
		this.destFolder = destFolder;
	}
	
	

}
