package annotation;

import org.w3c.dom.Element;

import PamView.symbol.PamSymbolChooser;
import PamView.symbol.modifier.SymbolModifier;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelector;
import annotation.binary.AnnotationBinaryHandler;
import annotation.dataselect.AnnotationDataSelCreator;
import annotation.handler.AnnotationOptions;
import annotation.xml.AnnotationXMLWriter;
import annotation.xml.SQLXMLWriter;
import generalDatabase.SQLLoggingAddon;
import tethys.species.DataBlockSpeciesManager;

/**
 * Something that can tell us a little more about 
 * a type of DataAnnotation, such as how to store and display
 * and edit the data in that annotation. <p>This base type
 * is intended for annotations which are genuinely annotations 
 * of existing data units. Stand alone annotations, which create 
 * their own dataunit should base upon SoloAnnotationType
 * @author Doug Gillespie
 * @see SoloAnnotationType
 *
 */
public abstract class DataAnnotationType<TDataAnnotation extends DataAnnotation<?>> {

	public abstract String getAnnotationName();

	private AnnotationOptions annotationOptions;
	
	public static final int SHORTIDCODELENGTH = 4;
	
	private PamDataBlock targetDataBlock;

	private SQLXMLWriter annotationWriter;

	/**
	 * Get the annotation as a string for use in tables, tool tips, etc. 
	 * @param dataAnnotation Data Annotation
	 * @return String representation of the data
	 */
	public String toString(TDataAnnotation dataAnnotation) {
		return dataAnnotation.toString();
	}
	
	/**
	 * 
	 * @return Class type for the annotations
	 */
	public abstract Class getAnnotationClass();

	/**
	 * Find out whether or not a particular type of data can be annotated
	 * @param dataUnitType Class of a type of data unit. Can be null in which 
	 * case annotation can be stand alone. 
	 * @return true if the data unit Class can be annotated. 
	 */
	public abstract boolean canAnnotate(Class dataUnitType);

	/**
	 * 
	 * @return True if annotation can be automatic (i.e. calculated rather than input by user). 
	 */
	public boolean canAutoAnnotate() {
		return false;
	}

	/**
	 * Automatically annotate the data unit with this annotation. 
	 * @param pamDataUnit data unit to annotate
	 * @return the annotation (though this should have already been added to the data unit). 
	 */
	public TDataAnnotation autoAnnotate(PamDataUnit pamDataUnit) {
		return null;
	}

	/**
	 * Get an SQLLogging add on. For stand alone annotations, this will be used
	 * to generate a stand alone database table. For annotations of existing data
	 * this will be used to add additional columns to the existing data tables. 
	 * @return An SQLLoggingAddon or null if SQL logging unavailable for this type
	 * of annotation.
	 */
	public SQLLoggingAddon getSQLLoggingAddon() {
		return null;
	}
	
	/**
	 * Get something that can write an annotation as XML
	 * @return
	 */
	public AnnotationXMLWriter<TDataAnnotation> getXMLWriter() {
		/*
		 *  can do this automatically if we've got sqllogging, otherwise 
		 *  override and do something bespoke. 
		 */
		if (annotationWriter != null) {
			return annotationWriter;
		}
		SQLLoggingAddon sqlLogging = getSQLLoggingAddon();
		if (sqlLogging == null) {
			return null; 
		}
		annotationWriter = new SQLXMLWriter<>(this);
		return annotationWriter;
	}
	
	/**
	 * Get an optional AnnotationBinaryHandler which can be used to add the 
	 * annotation information for binary files and also read data back from 
	 * them. 
	 * @return handler for binary data i/o.
	 */
	public AnnotationBinaryHandler<TDataAnnotation> getBinaryHandler() {
		return null;
	}

	/**
	 * Get a dialog component that can be incorporated into a larger dialog. <p>
	 * Note that this is for setting the data for a specific annotation, NOT the dialog
	 * for setting options controlling how the annotation type works. 
	 * @return a dialog panel (contains a component = a few other functions). 
	 */
	public AnnotationDialogPanel getDialogPanel() {
		return null;
	}
	
	public AnnotationSettingsPanel getSettingsPanel() {
		return null;
	}
	
	public boolean hasSettingsPanel() {
		return false;
	}

	/**
	 * @return the annotationOptions
	 */
	public AnnotationOptions getAnnotationOptions() {
		return new AnnotationOptions(getAnnotationName());
	}

	/**
	 * @param annotationOptions the annotationOptions to set
	 */
	public void setAnnotationOptions(AnnotationOptions annotationOptions) {
	}

	/**
	 * A short identifying code which MUST be unique to the data annotation and MUST
	 * be four characters long. Classes can override this with something even more cryptic 
	 * so long as it remains unique.
	 * @return a four character id string. 
	 */
	public String getShortIdCode() {
		String str = getAnnotationName();
		if (str.length() == 4) {
			return str;
		}
		else if (str.length() < 4) {
			while (str.length() < 4) {
				str += '_';
			}
			return str;
		}
		else {
			int len = str.length();
			return str.substring(0, 2) + str.substring(len-2, len);
		}
	}
	
	/**
	 * Some annotations may be able to set the type of symbol. This
	 * will be accessed from a datablocks SymbolManager
	 * @return
	 */
	public SymbolModifier getSymbolModifier(PamSymbolChooser symbolChooser) {
		return null;
	}

	@Override
	public String toString() {
		return getAnnotationName();
	}
	
	protected AnnotationDataSelCreator getDataSelectCreator(String selectorName, boolean allowScores) {
		return null;
	}
	
	/**
	 * Get a data selector specific to this annotation, which will merge seamlessly 
	 * into a master data selector combining data unit specific and annotation selections
	 * 
	 * @param pamDataBlock
	 * @param selectorName
	 * @param allowScores
	 * @return data selector
	 */
	public DataSelector getDataSelector(PamDataBlock pamDataBlock, String selectorName, boolean allowScores, String selectorType) {
		AnnotationDataSelCreator<TDataAnnotation> dataSelCreator = getDataSelectCreator(selectorName, allowScores);
		if (dataSelCreator == null) {
			return null;
		}
		return dataSelCreator.getDataSelector(selectorName, allowScores, selectorType);
	}

	/**
	 * Is this type annotating a particular datablock. 
	 * @param pamDataBlock
	 * @return
	 */
	public boolean isAnnotating(PamDataBlock pamDataBlock) {
		return (targetDataBlock == pamDataBlock);
	}

	/**
	 * @return the targetDataBlock
	 */
	public PamDataBlock getTargetDataBlock() {
		return targetDataBlock;
	}

	/**
	 * @param targetDataBlock the targetDataBlock to set
	 */
	public void setTargetDataBlock(PamDataBlock targetDataBlock) {
		this.targetDataBlock = targetDataBlock;
	}
	
	/**
	 * Annotations may have a species manager. Most won't. 
	 * @return
	 */
	public DataBlockSpeciesManager getDataBlockSpeciesManager() {
		return null;
	}
	

}
