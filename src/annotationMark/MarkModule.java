package annotationMark;

import java.awt.Window;
import java.util.List;

import javax.swing.JOptionPane;

import annotation.DataAnnotationType;
import annotation.handler.AnnotationHandler;
import generalDatabase.SQLLoggingAddon;


public abstract class MarkModule extends PamController.PamControlledUnit {

	public static String unitType = "Annotations";
	
//	private DataAnnotationType soloAnnotationType;
	
	private MarkProcess annotationProcess;
		
	public MarkModule(String unitName) {
		super(unitType, unitName);
		addPamProcess(annotationProcess = new MarkProcess(this));
	}

	/**
	 * This must be called from the concrete super class constructor with a valid
	 * SoloannotationType to set up the module. would be nice to have done this 
	 * as an argument to the AnnotationModule constructor, but too many things
	 * to initialise, so has to be a call further down the constructor. 
	 * @param annotationType
	 */
	public void addAnnotationType(DataAnnotationType annotationType) {
		AnnotationHandler annotationHandler = annotationProcess.getMarkDataBlock().getAnnotationHandler();
		if (annotationHandler == null) {
			annotationProcess.getMarkDataBlock().setAnnotationHandler(annotationHandler = new AnnotationHandler(annotationProcess.getMarkDataBlock()));
		}
		annotationHandler.addAnnotationType(annotationType);
		SQLLoggingAddon sqlAddon = annotationType.getSQLLoggingAddon();
		if (sqlAddon != null) {
			annotationProcess.getAsql().addAddOn(sqlAddon);
		}
	}

	/**
	 * Ask the user to confirm, then delete annotations (from memory and the database).
	 * Shows a modal Swing dialog so must be called on the Swing EDT.
	 * @param parent parent window for the confirmation, can be null.
	 * @param units annotations to delete.
	 * @return true if they were deleted.
	 */
	public boolean deleteAnnotations(Window parent, List<MarkDataUnit> units) {
		if (units == null || units.isEmpty()) {
			return false;
		}
		String msg;
		if (units.size() == 1) {
			msg = String.format("Delete annotation UID %d ?<br>%s", units.get(0).getUID(), units.get(0).getSummaryString());
		}
		else {
			msg = String.format("Delete these %d annotations ?", units.size());
		}
		// not WarnOnce - that skips the question (returning OK) when the parent is null.
		int ans = JOptionPane.showConfirmDialog(parent, "<html>" + msg + "</html>", "Delete annotation",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		if (ans != JOptionPane.OK_OPTION) {
			return false;
		}
		for (MarkDataUnit unit : units) {
			getAnnotationDataBlock().remove(unit, true);
		}
		annotationsChanged(units);
		return true;
	}

	/**
	 * Show the annotation dialog for an annotation and save any changes. Shows a modal
	 * Swing dialog so must be called on the Swing EDT.
	 * @param parent parent window, can be null.
	 * @param unit the annotation.
	 * @return true if the annotation was changed.
	 */
	public boolean editAnnotation(Window parent, MarkDataUnit unit) {
		if (MarkAnnotationDialog.showDialog(parent, this, unit, null)) {
			getAnnotationDataBlock().updatePamData(unit, System.currentTimeMillis());
			annotationsChanged(null);
			return true;
		}
		return false;
	}

	/**
	 * Called after annotations have been deleted or edited, e.g. so displays which
	 * don't repaint on data removal can be redrawn. Called on the Swing EDT.
	 * @param deleted annotations which were deleted, or null if they were edited.
	 */
	protected void annotationsChanged(List<MarkDataUnit> deleted) {
	}

	public MarkProcess getAnnotationProcess() {
		return annotationProcess;
	}

	/**
	 * Gets the Mark data block from the MarkProcess 
	 * @return
	 */
	public MarkDataBlock getAnnotationDataBlock() {
		return annotationProcess.getMarkDataBlock();
	}

}
