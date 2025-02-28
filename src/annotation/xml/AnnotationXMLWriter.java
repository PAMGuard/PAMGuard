package annotation.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import PamguardMVC.PamDataUnit;
import annotation.DataAnnotation;

/**
 * Writer to convert an annotation into XML. 
 * @author dg50
 *
 */
public interface AnnotationXMLWriter<TDataAnnotation extends DataAnnotation<?>> {


	/**
	 * Write an annotation as an XML element into a document. 
	 * @param document
	 * @param parent
	 * @param pamDataUnit
	 * @param annotation
	 * @return
	 */
	public Element writeAnnotation(Document parent, PamDataUnit pamDataUnit, TDataAnnotation annotation);
}
