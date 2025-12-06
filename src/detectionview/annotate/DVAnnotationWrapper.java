package detectionview.annotate;

import java.awt.Point;
import java.awt.Window;
import java.util.List;

import javax.swing.JMenuItem;

import PamController.PamControlledUnitSettings;
import PamController.SettingsNameProvider;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.datamenus.DataMenuParent;
import annotation.DataAnnotationType;
import annotation.handler.AnnotationChoiceHandler;
import annotation.handler.AnnotationHandler;
import annotation.handler.AnnotationsSelectionPanel;
import annotation.handler.OneStopAnnotationHandler;
import annotation.userforms.UserFormAnnotationType;
import annotationMark.MarkDataBlock;
import detectionview.DVControl;
import generalDatabase.SQLLogging;

/**
 * Wrapper class (Decorator) of an existing annotation handler to add  / make sure we have all the
 * functionality needed for annotations in the DetectionsViewer. 
 * @author dg50
 *
 */
public class DVAnnotationWrapper extends OneStopAnnotationHandler {

	private AnnotationHandler exHandler;
	private DVControl dvControl;

	public DVAnnotationWrapper(AnnotationHandler exHandler, DVControl dvControl, PamDataBlock<PamDataUnit> pamDataBlock) {
		super(dvControl, pamDataBlock);
		this.dvControl = dvControl;
		if (exHandler == null) {
			exHandler = new AnnotationHandler(pamDataBlock);
		}
		this.exHandler = exHandler;
		createAnnotationTypes();
	}

	@Override
	public void createAnnotationTypes() {
		if (exHandler == null) {
			return;
		}
		if (exHandler.findAnnotationType(UserFormAnnotationType.class) == null) {
			exHandler.addAnnotationType(new UserFormAnnotationType(getPamDataBlock()));
		}
		
	}

	@Override
	public String getUnitType() {
		return super.getUnitType();
	}

	@Override
	public AnnotationsSelectionPanel getSelectionPanel() {
		return super.getSelectionPanel();
	}

	@Override
	public int getNumUsedAnnotationTypes() {
		return exHandler.getNumUsedAnnotationTypes();
	}

	@Override
	public List<DataAnnotationType<?>> getUsedAnnotationTypes() {
		// TODO Auto-generated method stub
		return exHandler.getUsedAnnotationTypes();
	}

	@Override
	public void loadAnnotationChoices() {
		// TODO Auto-generated method stub
		super.loadAnnotationChoices();
	}

	@Override
	public void addSQLLogging(MarkDataBlock dataBlock) {
		super.addSQLLogging(dataBlock);
	}

	@Override
	public JMenuItem createAnnotationEditMenu(PamDataUnit dataUnit) {
		return exHandler.createAnnotationEditMenu(dataUnit);
	}

	@Override
	public boolean updateAnnotation(PamDataUnit pamDataUnit, DataAnnotationType annotationType) {
		return exHandler.updateAnnotation(pamDataUnit, annotationType);
	}

	@Override
	public DataAnnotationType<?> findAnnotationTypeFromCode(String code) {
		return exHandler.findAnnotationTypeFromCode(code);
	}

	@Override
	public DataAnnotationType<?> findAnnotationType(Class annotationTypeClass) {
		return exHandler.findAnnotationType(annotationTypeClass);
	}

	@Override
	public void addAnnotationType(DataAnnotationType annotationType) {
		exHandler.addAnnotationType(annotationType);
	}

	@Override
	public boolean removeAnnotationType(DataAnnotationType annotationType) {
		return exHandler.removeAnnotationType(annotationType);
	}

	@Override
	public PamDataBlock<PamDataUnit> getPamDataBlock() {
		return super.getPamDataBlock();
	}

	@Override
	public List<DataAnnotationType<?>> getAvailableAnnotationTypes() {
		return exHandler.getAvailableAnnotationTypes();
	}

	@Override
	public int addAnnotationSqlAddons(SQLLogging baseSQLLogging) {
		return exHandler.addAnnotationSqlAddons(baseSQLLogging);
	}

	@Override
	public boolean addAnnotation(DataAnnotationType anType, PamDataUnit pamDataUnit) {
		return exHandler.addAnnotation(anType, pamDataUnit);
	}

	@Override
	public boolean removeAnnotation(DataAnnotationType anType, PamDataUnit pamDataUnit) {
		return exHandler.removeAnnotation(anType, pamDataUnit);
	}

	@Override
	public boolean updateAnnotations(DataAnnotationType annotationType, PamDataUnit... pamDataUnits) {
		return exHandler.updateAnnotations(annotationType, pamDataUnits);
	}

	@Override
	public boolean addAnnotations(DataAnnotationType annotationType, PamDataUnit... pamDataUnits) {
		return exHandler.addAnnotations(annotationType, pamDataUnits);
	}

	@Override
	public boolean removeAnnotations(DataAnnotationType annotationType, PamDataUnit... pamDataUnits) {
		return exHandler.removeAnnotations(annotationType, pamDataUnits);
	}

	@Override
	public boolean annotateDataUnit(Window parentFrame, PamDataUnit dataUnit) {
		return exHandler.annotateDataUnit(parentFrame, dataUnit);
	}

	@Override
	public List<JMenuItem> getAnnotationMenuItems(DataMenuParent menuParent, Point mousePosition,
			PamDataUnit... dataUnits) {
		return exHandler.getAnnotationMenuItems(menuParent, mousePosition, dataUnits);
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		// TODO Auto-generated method stub
		return super.restoreSettings(pamControlledUnitSettings);
	}


}
