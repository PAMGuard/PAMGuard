package annotation.handler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import annotation.AnnotationDialog;
import annotation.DataAnnotation;
import annotation.DataAnnotationType;
import annotationMark.MarkDataBlock;
import generalDatabase.SQLLogging;
import mapgrouplocaliser.MarkGroupSQLLogging;

/**
 * Annotation handler that provides functionality for the programmer to set things
 * up so that the user can chose which types of annotation to use. 
 * @author Doug
 *
 */
public abstract class AnnotationChoiceHandler extends AnnotationHandler {

	
	public AnnotationChoiceHandler(PamDataBlock<PamDataUnit> pamDataBlock) {
		super(pamDataBlock);
	}

	public abstract AnnotationChoices getAnnotationChoices();

	/**
	 * 
	 * @return a dialog panel that can be used in Swing dialogs. 
	 */
	public AnnotationsSelectionPanel getSelectionPanel() {
		return new AnnotationsSelectionPanel(this);
	}

	@Override
	public int getNumUsedAnnotationTypes() {
		int n = 0;
		List<DataAnnotationType<?>> availableAnnotationTypes = getAvailableAnnotationTypes();
		AnnotationChoices choices = getAnnotationChoices();
		for (int i = 0; i < availableAnnotationTypes.size(); i++) {
			AnnotationOptions choice = choices.getAnnotationOptions(availableAnnotationTypes.get(i).getAnnotationName());
			if (choice != null && choice.isIsSelected()) {
				n++;
			}
		}
		return n;
	}

	@Override
	public List<DataAnnotationType<?>> getUsedAnnotationTypes() {
		ArrayList<DataAnnotationType<?>> usedTypes = new ArrayList<>();
		List<DataAnnotationType<?>> availableAnnotationTypes = getAvailableAnnotationTypes();
		AnnotationChoices choices = getAnnotationChoices();
		for (int i = 0; i < availableAnnotationTypes.size(); i++) {
			AnnotationOptions choice = choices.getAnnotationOptions(availableAnnotationTypes.get(i).getAnnotationName());
			if (choice != null && choice.isIsSelected()) {
				usedTypes.add(availableAnnotationTypes.get(i));
			}
		}
		return usedTypes;
	}
	
	/**
	 * Call this after settings have been loaded to load individual settings for the various 
	 * annotation types. 
	 */
	public void loadAnnotationChoices() {
		AnnotationChoices choices = getAnnotationChoices();
		List<DataAnnotationType<?>> anTypes = getAvailableAnnotationTypes();
		if (anTypes == null) return;
		for (DataAnnotationType aType:anTypes) {
			AnnotationOptions anOption = choices.getAnnotationOptions(aType.getAnnotationName());
			if (anOption != null) {
				aType.setAnnotationOptions(anOption);
			}
		}
	}

	public void addSQLLogging(MarkDataBlock dataBlock) {
		if (dataBlock == null) {
			return;
		}
		SQLLogging sqlLogging = dataBlock.getLogging();
		if (sqlLogging == null) {
			return;
		}
		this.addAnnotationSqlAddons(sqlLogging);
		
	}

	/**
	 * Create a menu for editing annotations for a data unit. 
	 * @param dataUnit Existing data unit
	 * @return menu item. 
	 */
	public JMenuItem createAnnotationEditMenu(PamDataUnit dataUnit) {
		JMenu menuItem = new JMenu("Edit/Update annotations ...");
		List<DataAnnotationType<?>> usedAnnotations = getUsedAnnotationTypes();
		
		for (DataAnnotationType an:usedAnnotations) {
			JMenuItem subItem = new JMenuItem(an.getAnnotationName());
			subItem.addActionListener(new EditAnnotationHandler(dataUnit, an));
			menuItem.add(subItem);
		}
		
		return menuItem;
	}
	
	/**
	 * Menu handler for editing annotations. 
	 * @author dg50
	 *
	 */
	private class EditAnnotationHandler implements ActionListener {
		private DataAnnotationType annotationType;
		private PamDataUnit pamDataUnit;
		public EditAnnotationHandler(PamDataUnit pamDataUnit, DataAnnotationType annotationType) {
			super();
			this.pamDataUnit = pamDataUnit;
			this.annotationType = annotationType;
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			updateAnnotation(pamDataUnit, annotationType);
		}
		
		
	}

	/**
	 * Update an existing annotation - or create a blank one if
	 * there isn't already one in the data unit. 
	 * @param pamDataUnit
	 * @param annotationType
	 */
	public boolean updateAnnotation(PamDataUnit pamDataUnit, DataAnnotationType annotationType) {
		/* 
		 * need to check this is actually the right data unit, which matches the datablock of 
		 * this annotatoin handler. Gets very confused when dealing with superdetections 
		 */
		if (pamDataUnit.getParentDataBlock() != this.getPamDataBlock()) {
			pamDataUnit = pamDataUnit.getSuperDetection(getPamDataBlock(), true);
			if (pamDataUnit == null) {
				return false;
			}
		}
		
		DataAnnotation existingAnnotation = pamDataUnit.findDataAnnotation(annotationType.getAnnotationClass(), annotationType.getAnnotationName());
		boolean changed = false;
		if (annotationType.canAutoAnnotate()) {
			int ans = WarnOnce.showWarning(null, "Annotation update", "Automatically update data annotation", WarnOnce.OK_CANCEL_OPTION);
			if (ans == WarnOnce.CANCEL_OPTION) {
				return false;
			}
			DataAnnotation newAnnotation = annotationType.autoAnnotate(pamDataUnit);
			pamDataUnit.removeDataAnnotation(existingAnnotation);
			if (newAnnotation != null) {
				pamDataUnit.addDataAnnotation(newAnnotation);
				changed = true;
			}
		}
		else {
			if (AnnotationDialog.showDialog(PamController.getMainFrame(), annotationType, pamDataUnit, null)) {
				pamDataUnit.removeDataAnnotation(existingAnnotation);
				changed = true;
			}
//			annotationType.get
		}
		if (changed) {
			getPamDataBlock().updatePamData(pamDataUnit, PamCalendar.getTimeInMillis());
		}
		return changed;
	}
	
}

