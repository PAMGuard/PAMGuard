package annotation.tasks;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import offlineProcessing.OLProcessDialog;
import offlineProcessing.OfflineTaskGroup;
import annotation.DataAnnotationType;
import annotation.handler.AnnotationHandler;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamguardMVC.PamDataBlock;

public class AnnotationManager {

	private static AnnotationManager singleinstance;
	
	private PamController pamController;

	private boolean isViewer;
	
	public static AnnotationManager getAnnotationManager() {
		if (singleinstance == null) {
			singleinstance = new AnnotationManager();
		}
		return singleinstance;
	}

	public AnnotationManager() {
		super();
		pamController = PamController.getInstance();
		isViewer = pamController.getRunMode() == PamController.RUN_PAMVIEW;
	}
	
	public JMenuItem getAutoMenu(Window parentFrame) {
		if (!isViewer) return null;
		int n = 0;
		JMenuItem menu = new JMenu("Annotations");
		ArrayList<PamDataBlock> dataBlocks = pamController.getDataBlocks();
		for (PamDataBlock dataBlock:dataBlocks) {
			// see if this datablock can be annotated. 
			boolean can = false;
			AnnotationHandler annotationHandler = dataBlock.getAnnotationHandler();
			if (annotationHandler == null) continue;
			List<DataAnnotationType<?>> anTypes = annotationHandler.getUsedAnnotationTypes();
			for (int i = 0; i < anTypes.size(); i++) {
				DataAnnotationType dat = anTypes.get(i);
				if (dat.canAutoAnnotate()) {
					can = true;
					break;
				}
			}
			if (can == false) continue;
			JMenuItem detItem = new JMenuItem(dataBlock.getDataName());
			detItem.addActionListener(new AutoAnnotateAction(parentFrame, dataBlock));
			detItem.setToolTipText("Automatically annotate data in " + dataBlock.getParentProcess().getPamControlledUnit().getUnitName());
			menu.add(detItem);
			n++;
		}
		
		if (n == 0) return null;
		return menu;
	}
	
	class AutoAnnotateAction implements ActionListener {

		private PamDataBlock dataBlock;
		private Window parentFrame;
		public AutoAnnotateAction(Window parentFrame, PamDataBlock dataBlock) {
			super();
			this.parentFrame = parentFrame;
			this.dataBlock = dataBlock;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			annotateDataBlock(parentFrame, dataBlock);
		}
		
	}

	public void annotateDataBlock(Window parentFrame, PamDataBlock dataBlock) {
		AnnotationHandler annotationHandler = dataBlock.getAnnotationHandler();
		int nT = 0;
		List<DataAnnotationType<?>> usedTypes = null;
		if (annotationHandler != null) {
			usedTypes = annotationHandler.getUsedAnnotationTypes();
			nT = usedTypes.size();
		}
		PamControlledUnit pamControlledUnit = dataBlock.getParentProcess().getPamControlledUnit();
		OfflineTaskGroup annotationTaskGroup = new OfflineTaskGroup(pamControlledUnit, dataBlock.getDataName()+" Auto Annotation");
		annotationTaskGroup.setPrimaryDataBlock(dataBlock);
		int n = 0;
		for (int i = 0; i < nT; i++) {
			DataAnnotationType dat = usedTypes.get(i);
			if (dat.canAutoAnnotate() == false) continue;
			AnnotationOfflineTask aot = new AnnotationOfflineTask(dataBlock, dat);
			annotationTaskGroup.addTask(aot);
			n++;
		}
		if (n == 0) return;
		OLProcessDialog olProcessDialog = new OLProcessDialog(parentFrame, annotationTaskGroup, "Annotate " + dataBlock.getDataName());
		
		olProcessDialog.setVisible(true);
	}
	
}
