package PamguardMVC;

/**
 * Class for providing annotations. 
 * Will probably only ever get used by PamProcess. 
 * @author Doug Gillespie
 * @see ProcessAnnotation
 * @see PamProcess
 *
 */
public interface ProcessAnnotator {

	/**
	 * @param pamDataBlock Annotated datablock
	 * @return the number of annotations
	 */
	public int getNumAnnotations(PamDataBlock pamDataBlock);
	
	/**
	 * Get an Annotation
	 * @param pamDataBlock Annotated datablock
	 * @param iAnnotation annotation number
	 * @return Annotation
	 */
	public ProcessAnnotation getAnnotation(PamDataBlock pamDataBlock, int iAnnotation);
}
