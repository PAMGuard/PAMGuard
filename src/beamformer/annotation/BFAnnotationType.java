package beamformer.annotation;

import annotation.CentralAnnotationsList;
import annotation.DataAnnotationType;
import annotation.binary.AnnotationBinaryHandler;
import beamformer.localiser.BeamFormLocaliserControl;
import generalDatabase.SQLLoggingAddon;

public class BFAnnotationType extends DataAnnotationType<BeamFormerAnnotation> {

	public static final String ANNOTATIONNAME = "Beamformer";
	
	private BeamFormLocaliserControl beamFormerLocControl;

	private BFAnnotationBinary binaryHandler;
	
	private BFAnnotationLogging bfAnnotationLogging;
	
	public BFAnnotationType(BeamFormLocaliserControl beamFormerLocControl) {
		super();
		this.beamFormerLocControl = beamFormerLocControl;
		binaryHandler = new BFAnnotationBinary(this);
		bfAnnotationLogging = new BFAnnotationLogging(this);
		CentralAnnotationsList.addAnnotationType(this);
	}

	@Override
	public String getAnnotationName() {
		return ANNOTATIONNAME;
	}

	@Override
	public Class getAnnotationClass() {
		return BeamFormerAnnotation.class;
	}

	@Override
	public boolean canAnnotate(Class dataUnitType) {
		return true;
	}

	/* (non-Javadoc)
	 * @see annotation.DataAnnotationType#getBinaryHandler()
	 */
	@Override
	public AnnotationBinaryHandler<BeamFormerAnnotation> getBinaryHandler() {
		return binaryHandler;
	}

	/* (non-Javadoc)
	 * @see annotation.DataAnnotationType#getSQLLoggingAddon()
	 */
	@Override
	public SQLLoggingAddon getSQLLoggingAddon() {
		return bfAnnotationLogging;
	}

}
