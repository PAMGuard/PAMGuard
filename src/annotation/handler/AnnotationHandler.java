package annotation.handler;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Window;
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
import PamguardMVC.datamenus.DataMenuParent;
import annotation.AnnotationDialog;
import annotation.AnnotationDialogPanel;
import annotation.AnnotationList;
import annotation.CentralAnnotationsList;
import annotation.DataAnnotation;
import annotation.DataAnnotationType;
import generalDatabase.SQLLogging;

/**
 * Manage annotation options for a datablock <p>
 * This contains a list of available annotations for the 
 * type of data in the datablock. It can provide a Swing panel
 * for selecting different annotation options and also interact with 
 * the AnnotationChoices class to save annotation options and selections. 
 * @author Doug Gillespie
 *
 */
public class AnnotationHandler {

	private PamDataBlock<PamDataUnit> pamDataBlock;

	private ArrayList<DataAnnotationType<?>> availableAnnotationTypes = new ArrayList<>();

	private AnnotationList codedList = new AnnotationList();

	/**
	 * @param pamDataBlock
	 */
	public AnnotationHandler(PamDataBlock<PamDataUnit> pamDataBlock) {
		super();
		this.pamDataBlock = pamDataBlock;
	}

	/**
	 * find an annotation type from it's 4 character code. Used when 
	 * recreating annotations read back from binary data.  
	 * @param code 4 character code
	 * @return
	 */
	public DataAnnotationType<?> findAnnotationTypeFromCode(String code) {
		DataAnnotationType<?> type = codedList.findTypeFromCode(code);
		if (type != null) {
			return type;
		}
		else {
			return CentralAnnotationsList.findTypeFromCode(code);
		}
	}

	/**
	 * Find an annotation type of a particular class. 
	 * @param annotationTypeClass class of annotation type
	 * @return Instance of that type or null
	 */
	public DataAnnotationType<?> findAnnotationType(Class annotationTypeClass) {
		for (DataAnnotationType<?> at : availableAnnotationTypes) {
			if (at.getClass() == annotationTypeClass) {
				return at;
			}
		}
		return null;
	}

	/**
	 * Add an annotation type to the list of available types for this datablock. 
	 * @param annotationType
	 */
	public void addAnnotationType(DataAnnotationType annotationType) {
		availableAnnotationTypes.add(annotationType);
		codedList.addAnnotationType(annotationType);
		CentralAnnotationsList.addAnnotationType(annotationType);
	}

	public boolean removeAnnotationType(DataAnnotationType annotationType) {
		return availableAnnotationTypes.remove(annotationType);
	}


	/**
	 * @return the pamDataBlock
	 */
	protected PamDataBlock<PamDataUnit> getPamDataBlock() {
		return pamDataBlock;
	}

	/**
	 * @return the availableAnnotationTypes
	 */
	public List<DataAnnotationType<?>> getAvailableAnnotationTypes() {
		return availableAnnotationTypes;
	}

	/**
	 * 
	 * @return a list of selected annotation types (which is the same
	 * as the list of available annotation types unless this is overridden in 
	 * a handler that offers choice). 
	 */
	public List<DataAnnotationType<?>> getUsedAnnotationTypes() {
		return availableAnnotationTypes;
	}

	/**
	 * 
	 * @return the number of used annotation types. 
	 */
	public int getNumUsedAnnotationTypes() {
		return availableAnnotationTypes.size();
	}

	/**
	 * Add all the SQLLogging addons to the database table. 
	 * Make sure the table is cleared of additions before calling this in case
	 * it gets called multiple times. 
	 * @param baseSQLLogging BAse SQLLogging (associated with a PamDataBlock) to add the extra fields to. 
	 */
	public int addAnnotationSqlAddons(SQLLogging baseSQLLogging) {
		List<DataAnnotationType<?>> usedAnnotations = getUsedAnnotationTypes();
		int nAdditions = 0;
		for (DataAnnotationType dt:usedAnnotations) {
			baseSQLLogging.addAddOn(dt.getSQLLoggingAddon());
			nAdditions++;
		}
		return nAdditions;
	}

	/**
	 * Add an annotation to a detection group. 
	 * @param anType
	 * @param pamDataUnit
	 * @return
	 */
	public boolean addAnnotation(DataAnnotationType anType, PamDataUnit pamDataUnit) {
		// see if it's an annotation with a dialog. 
		AnnotationDialogPanel dialogPanel = anType.getDialogPanel();
		if (dialogPanel == null) {
			anType.autoAnnotate(pamDataUnit);
			return true;
		}
		else {
			// make and show a dialog. show it close to the current mouse position
			PointerInfo mousePointerInfo = MouseInfo.getPointerInfo();
			Point locOnScreen = mousePointerInfo.getLocation();
			boolean ans = AnnotationDialog.showDialog(PamController.getMainFrame(), 
					anType, pamDataUnit, locOnScreen);
			//			System.out.println("Data unit has annotation " + ans);
			return ans;
		}
	}

	public boolean removeAnnotation(DataAnnotationType anType, PamDataUnit pamDataUnit) {
		DataAnnotation annotation = pamDataUnit.findDataAnnotation(anType.getAnnotationClass());
		if (annotation != null) {
			return pamDataUnit.removeDataAnnotation(annotation);
		}
		else {
			return false;
		}
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

	public boolean updateAnnotations(DataAnnotationType annotationType, PamDataUnit... pamDataUnits) {
		
		return addAnnotations(annotationType, pamDataUnits);
	}
	
	public boolean addAnnotations(DataAnnotationType annotationType, PamDataUnit... pamDataUnits) {
		boolean ok = false;
		if (pamDataUnits[0].findDataAnnotation(annotationType.getAnnotationClass()) != null) {
			ok = updateAnnotation(pamDataUnits[0], annotationType);
		}
		else {
			ok = addAnnotation(annotationType, pamDataUnits[0]);
		}
		if (ok) {
			String msg = String.format("Do you want to add this annotation to all %d data units?", pamDataUnits.length);
			int ans = WarnOnce.showWarning("Multi unit annotations", msg, WarnOnce.OK_CANCEL_OPTION);
			if (ans == WarnOnce.CANCEL_OPTION) {
				return false;
			}
			else {
				DataAnnotation newAnnot = pamDataUnits[0].findDataAnnotation(annotationType.getAnnotationClass());
				for (int i = 1; i < pamDataUnits.length; i++) {
					pamDataUnits[i].removeDataAnnotation(pamDataUnits[i].findDataAnnotation(annotationType.getAnnotationClass()));
					pamDataUnits[i].addDataAnnotation(newAnnot);
				}
			}
		}
		return true;
	}
	
	public boolean removeAnnotations(DataAnnotationType annotationType, PamDataUnit... pamDataUnits) {
		String msg = String.format("Are you sure you want to remove %s annotations from all %d data units?", 
				annotationType.getAnnotationName(), pamDataUnits.length);
		int ans = WarnOnce.showWarning("Multi unit annotations", msg, WarnOnce.OK_CANCEL_OPTION);
		if (ans == WarnOnce.CANCEL_OPTION) {
			return false;
		}
		for (int i = 1; i < pamDataUnits.length; i++) {
			pamDataUnits[i].removeDataAnnotation(pamDataUnits[i].findDataAnnotation(annotationType.getAnnotationClass()));
		}
		return true;
	}

	public boolean annotateDataUnit(Window parentFrame, PamDataUnit dataUnit) {
		boolean allOk = true;
		List<DataAnnotationType<?>> anTypes = getUsedAnnotationTypes();
		for (DataAnnotationType anType:anTypes) {
			if (anType.canAutoAnnotate()) {
				anType.autoAnnotate(dataUnit);
			}
			else {
				PointerInfo mousePointerInfo = MouseInfo.getPointerInfo();
				Point locOnScreen = mousePointerInfo.getLocation();
				boolean ok = AnnotationDialog.showDialog(parentFrame, anType, dataUnit, locOnScreen);
				if (ok == false) {
					allOk = false;
				}
			}
		}
		return allOk;
	}

	public List<JMenuItem> getAnnotationMenuItems(DataMenuParent menuParent, Point mousePosition, PamDataUnit ...dataUnits) {
		if (dataUnits != null && dataUnits.length == 1) {
			return getSingleUnitMenuItems(menuParent, mousePosition, dataUnits[0]);
		}
		else {
			return getMultiUnitMenuItems(menuParent, mousePosition, dataUnits);
		}
	}


	/**
	 * Create a menu for simultaneously annotating many data units at once. 
	 * @param menuParent
	 * @param mousePosition
	 * @param dataUnits
	 * @return
	 */
	private List<JMenuItem> getMultiUnitMenuItems(DataMenuParent menuParent, Point mousePosition,
			PamDataUnit[] dataUnits) {
		List<DataAnnotationType<?>> usedAnnots = getUsedAnnotationTypes();
		if (usedAnnots == null) {
			return null;
		}
		List<JMenuItem> items = new ArrayList<>();
		JMenuItem menuItem;
		for (DataAnnotationType<?> anType : usedAnnots) {
			String str = String.format("Edit %s on %d %s data units", anType.getAnnotationName(), dataUnits.length, dataUnits[0].getParentDataBlock().getDataName());
			menuItem = new JMenuItem(str);
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					updateAnnotations(anType, dataUnits);
				}
			});
			items.add(menuItem);
			str = String.format("Remove %s from %d %s data units", anType.getAnnotationName(), dataUnits.length, dataUnits[0].getParentDataBlock().getDataName());
			menuItem = new JMenuItem(str);
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					removeAnnotations(anType, dataUnits);
				}
			});
			items.add(menuItem);
			str = String.format("Add %s to %d %s data units", anType.getAnnotationName(), dataUnits.length, dataUnits[0].getParentDataBlock().getDataName());
			menuItem = new JMenuItem(str);
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					addAnnotations(anType, dataUnits);
				}
			});
			items.add(menuItem);
		}
		return items.size() > 0 ? items : null;
	}

	/**
	 * Create menu items to add, remove, or edit annotations from a data units. 
	 * @param menuParent
	 * @param mousePosition
	 * @param pamDataUnit
	 * @return
	 */
	private List<JMenuItem> getSingleUnitMenuItems(DataMenuParent menuParent, Point mousePosition, PamDataUnit pamDataUnit) {
		List<DataAnnotationType<?>> usedAnnots = getUsedAnnotationTypes();
		if (usedAnnots == null) {
			return null;
		}
		List<JMenuItem> items = new ArrayList<>();
		JMenuItem menuItem;
		for (DataAnnotationType<?> anType : usedAnnots) {
			DataAnnotation exAnnotation = pamDataUnit.findDataAnnotation(anType.getAnnotationClass());
			if (exAnnotation != null) {
				menuItem = new JMenuItem("Edit " + anType.getAnnotationName());
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						updateAnnotation(pamDataUnit, anType);
					}
				});
				items.add(menuItem);
				menuItem = new JMenuItem("Remove " + anType.getAnnotationName());
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						removeAnnotation(anType, pamDataUnit);
					}
				});
				items.add(menuItem);
			}
			else {
				menuItem = new JMenuItem("Add " + anType.getAnnotationName());
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						addAnnotation(anType, pamDataUnit);
					}
				});
				items.add(menuItem);
			}
		}
		return items.size() > 0 ? items : null;
	}

}
